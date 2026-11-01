package com.javic.villagerpatrols.sound;

import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.network.ClientboundLutePlaybackMessage;
import com.javic.villagerpatrols.network.ModNetworking;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class LutePlaybackManager {
   private static final int[] TRACK_DURATIONS = new int[]{5080, 6720, 4280, 820, 860, 800, 560, 800, 1380, 3300};
   private final Map<ResourceKey<Level>, Map<UUID, LutePlaybackManager.PlaybackState>> statesByLevel = new HashMap<>();
   private int nextSessionId = 1;

   public void tick(ServerLevel level) {
      Map<UUID, PlaybackState> states = this.statesByLevel.computeIfAbsent(level.dimension(), key -> new HashMap<>());
      HashSet<UUID> activeEntities = new HashSet<>();
      for (Entity entity : level.getAllEntities()) {
         if (entity instanceof LivingEntity living && shouldPlay(living)) {
            UUID uuid = living.getUUID();
            activeEntities.add(uuid);
            PlaybackState state = states.get(uuid);
            if (state == null) {
               int trackIndex = chooseRandomTrack(level, -1);
               PlaybackState started = new PlaybackState(trackIndex, TRACK_DURATIONS[trackIndex], this.nextSessionId());
               states.put(uuid, started);
               sync(living, true, started);
            } else {
               state.ticksRemaining--;
               if (state.ticksRemaining <= 0) {
                  state.trackIndex = chooseRandomTrack(level, state.trackIndex);
                  state.ticksRemaining = TRACK_DURATIONS[state.trackIndex];
                  state.sessionId = this.nextSessionId();
                  sync(living, true, state);
               }
            }
         }
      }

      Iterator<Entry<UUID, PlaybackState>> iterator = states.entrySet().iterator();
      while (iterator.hasNext()) {
         Entry<UUID, PlaybackState> entry = iterator.next();
         if (!activeEntities.contains(entry.getKey())) {
            if (level.getEntity(entry.getKey()) instanceof LivingEntity living) {
               sync(living, false, entry.getValue());
            }
            iterator.remove();
         }
      }
      if (states.isEmpty()) {
         this.statesByLevel.remove(level.dimension());
      }
   }

   public void onStartTracking(Entity target, ServerPlayer player) {
      if (target instanceof LivingEntity living) {
         Map<UUID, LutePlaybackManager.PlaybackState> states = this.statesByLevel.get(living.level().dimension());
         if (states != null) {
            LutePlaybackManager.PlaybackState state = states.get(living.getUUID());
            if (state != null && shouldPlay(living)) {
               ModNetworking.sendToPlayer(player, new ClientboundLutePlaybackMessage(living.getId(), true, state.trackIndex, state.sessionId));
            }
         }
      }
   }

   private static boolean shouldPlay(LivingEntity entity) {
      return entity.isAlive() && !entity.isRemoved() && hasLuteInHands(entity);
   }

   private static boolean hasLuteInHands(LivingEntity entity) {
      return entity.getMainHandItem().is((Item)ModItems.LUTE.get()) || entity.getOffhandItem().is((Item)ModItems.LUTE.get());
   }

   private static int chooseRandomTrack(ServerLevel level, int previousTrackIndex) {
      if (TRACK_DURATIONS.length <= 1) {
         return 0;
      }

      if (previousTrackIndex >= 0 && previousTrackIndex < TRACK_DURATIONS.length) {
         int trackIndex = level.getRandom().nextInt(TRACK_DURATIONS.length - 1);
         if (trackIndex >= previousTrackIndex) {
            trackIndex++;
         }

         return trackIndex;
      } else {
         return level.getRandom().nextInt(TRACK_DURATIONS.length);
      }
   }

   private static void sync(LivingEntity entity, boolean active, LutePlaybackManager.PlaybackState state) {
      ModNetworking.sendToTrackingAndSelf(entity, new ClientboundLutePlaybackMessage(entity.getId(), active, state.trackIndex, state.sessionId));
   }

   private int nextSessionId() {
      int sessionId = this.nextSessionId++;
      if (this.nextSessionId == Integer.MAX_VALUE) {
         this.nextSessionId = 1;
      }

      return sessionId;
   }

   private static final class PlaybackState {
      private int trackIndex;
      private int ticksRemaining;
      private int sessionId;

      private PlaybackState(int trackIndex, int ticksRemaining, int sessionId) {
         this.trackIndex = trackIndex;
         this.ticksRemaining = ticksRemaining;
         this.sessionId = sessionId;
      }
   }
}
