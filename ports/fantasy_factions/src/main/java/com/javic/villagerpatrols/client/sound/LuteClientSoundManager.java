package com.javic.villagerpatrols.client.sound;

import com.javic.villagerpatrols.ModSounds;
import com.javic.villagerpatrols.network.ClientboundLutePlaybackMessage;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class LuteClientSoundManager {
    private static final Map<Integer, DesiredPlayback> DESIRED = new ConcurrentHashMap<>();
    private static final Map<Integer, LuteEntitySoundInstance> ACTIVE = new ConcurrentHashMap<>();
    private LuteClientSoundManager() {}

    public static void handleMessage(ClientboundLutePlaybackMessage message) {
        if (!message.active()) {
            remove(message.entityId());
            return;
        }
        DesiredPlayback current = DESIRED.get(message.entityId());
        if (current != null && current.sessionId == message.sessionId() && current.trackIndex == message.trackIndex()) {
            tryStart(message.entityId(), current);
        } else {
            stopActive(message.entityId());
            DesiredPlayback desired = new DesiredPlayback(message.trackIndex(), message.sessionId());
            DESIRED.put(message.entityId(), desired);
            tryStart(message.entityId(), desired);
        }
    }

    public static boolean canEntityKeepPlayingClient(LivingEntity entity) {
        return entity.isAlive() && !entity.isRemoved();
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.getSoundManager() == null) {
            clear();
            return;
        }
        Iterator<Map.Entry<Integer, LuteEntitySoundInstance>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            LuteEntitySoundInstance sound = iterator.next().getValue();
            if (sound.isStopped() || !minecraft.getSoundManager().isActive(sound)) iterator.remove();
        }
        for (var entry : DESIRED.entrySet()) {
            if (!ACTIVE.containsKey(entry.getKey())) tryStart(entry.getKey(), entry.getValue());
        }
    }

    public static void remove(int entityId) {
        DESIRED.remove(entityId);
        stopActive(entityId);
    }

    public static void clear() {
        for (Integer entityId : ACTIVE.keySet()) stopActive(entityId);
        ACTIVE.clear();
        DESIRED.clear();
    }

    private static void tryStart(int entityId, DesiredPlayback desired) {
        if (desired.started) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.getSoundManager() != null
            && minecraft.level.getEntity(entityId) instanceof LivingEntity living && canEntityKeepPlayingClient(living)) {
            LuteEntitySoundInstance sound = new LuteEntitySoundInstance(ModSounds.getLuteBard(desired.trackIndex), SoundSource.RECORDS, living);
            ACTIVE.put(entityId, sound);
            desired.started = true;
            minecraft.getSoundManager().play(sound);
        }
    }

    private static void stopActive(int entityId) {
        LuteEntitySoundInstance sound = ACTIVE.remove(entityId);
        if (sound != null) {
            sound.stopPlayback();
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }

    private static final class DesiredPlayback {
        private final int trackIndex;
        private final int sessionId;
        private boolean started;
        private DesiredPlayback(int trackIndex, int sessionId) {
            this.trackIndex = trackIndex;
            this.sessionId = sessionId;
        }
    }
}
