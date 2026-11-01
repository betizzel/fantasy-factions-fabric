package com.javic.villagerpatrols.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

public class LuteEntitySoundInstance extends AbstractTickableSoundInstance {
    private final LivingEntity entity;

    public LuteEntitySoundInstance(SoundEvent soundEvent, SoundSource source, LivingEntity entity) {
        super(soundEvent, source, RandomSource.create());
        this.entity = entity;
        volume = 1.0F;
        pitch = 1.0F;
        looping = false;
        delay = 0;
        relative = false;
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }

    @Override
    public boolean canPlaySound() { return LuteClientSoundManager.canEntityKeepPlayingClient(entity); }

    @Override
    public void tick() {
        if (!LuteClientSoundManager.canEntityKeepPlayingClient(entity)) stop();
        else {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        }
    }

    public void stopPlayback() { stop(); }
}
