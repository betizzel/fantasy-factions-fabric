package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;

public final class ModEntitySounds {
   private ModEntitySounds() {
   }

   public static SoundEvent ambient(String folder, boolean female) {
      return ModSounds.getAmbient(folder, female);
   }

   public static SoundEvent hurt(String folder, boolean female) {
      return ModSounds.getHurt(folder, female);
   }

   public static SoundEvent death(String folder, boolean female) {
      return ModSounds.getDeath(folder, female);
   }

   public static SoundEvent getInteract(String folder, boolean female) {
      return ModSounds.getInteract(folder, female);
   }

   public static SoundEvent positive(String folder, boolean female) {
      return ModSounds.getPositive(folder, female);
   }

   public static SoundEvent negative(String folder, boolean female) {
      return ModSounds.getNegative(folder, female);
   }

   public static InteractionResult playInteract(Mob mob, String folder, boolean female) {
      if (!mob.level().isClientSide) {
         mob.playSound(ModSounds.getInteract(folder, female), 1.0F, 0.95F + mob.getRandom().nextFloat() * 0.1F);
      }

      return InteractionResult.sidedSuccess(mob.level().isClientSide);
   }

   public static int ambientInterval(Mob mob) {
      return 800 + mob.getRandom().nextInt(401);
   }
}
