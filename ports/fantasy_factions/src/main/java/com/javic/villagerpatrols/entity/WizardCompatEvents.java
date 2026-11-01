package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class WizardCompatEvents {
   private WizardCompatEvents() {}

   public static void onLevelTick(ServerLevel level) {
      for (Entity entity : level.getAllEntities()) {
         if (entity instanceof Mob mob) {
            if (WizardEntitySupport.isExternalWizardVillager(mob)) WizardEntitySupport.tickWizardVillager(mob);
            if (HealerEntitySupport.isExternalHealerVillager(mob)) HealerEntitySupport.tickHealerVillager(mob);
            if (VampireEntitySupport.isExternalVampireVillager(mob)) VampireEntitySupport.tickVampireVillager(mob);
         }
         if (entity instanceof AreaEffectCloud cloud && PersistentEntityData.get(cloud).getBoolean("villagerpatrols_wizard_breath_cloud")) {
            WizardEntitySupport.tickWizardBreathCloud(cloud);
         }
      }
   }

   public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
      if (amount > 0.0F && entity instanceof Mob mob && !mob.level().isClientSide) {
         if (WizardEntitySupport.isExternalWizardVillager(mob) && mob.isAlive()) WizardEntitySupport.playHurt(mob);
         if (VampireEntitySupport.isExternalVampireVillager(mob) && mob.isAlive()) {
            amount = VampireEntitySupport.applyFireWeakness(source, amount);
            VampireEntitySupport.playHurt(mob);
         }
      }
      return amount;
   }

   public static void onLivingDeath(LivingEntity entity) {
      if (entity instanceof Mob mob && !mob.level().isClientSide) {
         if (WizardEntitySupport.isExternalWizardVillager(mob)) WizardEntitySupport.playDeath(mob);
         if (VampireEntitySupport.isExternalVampireVillager(mob)) VampireEntitySupport.playDeath(mob);
      }
   }

   public static void onEntityInteract(Player player, Entity target, InteractionHand hand) {
      if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && target instanceof Mob mob) {
         if (WizardEntitySupport.isExternalWizardVillager(mob)) WizardEntitySupport.playInteract(mob);
         if (VampireEntitySupport.isExternalVampireVillager(mob)) VampireEntitySupport.playInteract(mob);
      }
   }

   public static boolean onPlayLevelSoundAtEntity(Entity entity, SoundEvent sound, SoundSource source) {
      if (!(entity instanceof Mob mob) || sound == null) return false;
      ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(sound);
      if (!isTargetSound(key)) return false;
      boolean vampire = isVampireCandidate(mob);
      if (!vampire && !isWizardCandidate(mob)) return false;
      replaceReaction(mob.level(), mob.getX(), mob.getY(), mob.getZ(), key, source, vampire);
      return true;
   }

   public static boolean onPlayLevelSoundAtPosition(Level level, double x, double y, double z, SoundEvent sound, SoundSource source) {
      if (sound == null) return false;
      ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(sound);
      if (!isTargetSound(key)) return false;
      AABB area = new AABB(x, y, z, x, y, z).inflate(1.0, 1.0, 1.0);
      boolean vampire = !level.getEntitiesOfClass(Mob.class, area, WizardCompatEvents::isVampireCandidate).isEmpty();
      if (!vampire && level.getEntitiesOfClass(Mob.class, area, WizardCompatEvents::isWizardCandidate).isEmpty()) return false;
      replaceReaction(level, x, y, z, key, source, vampire);
      return true;
   }

   private static void replaceReaction(Level level, double x, double y, double z, ResourceLocation key, SoundSource source, boolean vampire) {
      String path = key.getPath();
      if (!level.isClientSide && key.getNamespace().equals("slimpatch") && (path.contains("positive") || path.contains("negative"))) {
         boolean female = path.contains("female");
         String kind = vampire ? "vampire" : "wizard";
         SoundEvent replacement = path.contains("positive") ? ModEntitySounds.positive(kind, female) : ModEntitySounds.negative(kind, female);
         level.playSound(null, x, y, z, replacement, source, 1.0F, 1.0F);
      }
   }

   private static boolean isWizardCandidate(Mob mob) {
      return WizardEntitySupport.isExternalWizardVillager(mob) || mob.level().isClientSide && WizardEntitySupport.isVisualExternalWizardVillager(mob);
   }

   private static boolean isVampireCandidate(Mob mob) {
      return VampireEntitySupport.isExternalVampireVillager(mob) || mob.level().isClientSide && VampireEntitySupport.isVisualExternalVampireVillager(mob);
   }

   private static boolean isTargetSound(ResourceLocation key) {
      if (key == null) return false;
      String path = key.getPath();
      if (key.getNamespace().equals("minecraft")) {
         return path.equals("entity.villager.ambient") || path.equals("entity.villager.hurt") || path.equals("entity.villager.death") || path.equals("entity.villager.trade");
      }
      return key.getNamespace().equals("slimpatch") && (path.equals("male_click") || path.equals("female_click")
         || path.equals("male_hurt") || path.equals("female_hurt") || path.equals("male_death") || path.equals("female_death")
         || path.equals("villager.male.reaction.positive") || path.equals("villager.female.reaction.positive")
         || path.equals("villager.male.reaction.negative") || path.equals("villager.female.reaction.negative"));
   }
}
