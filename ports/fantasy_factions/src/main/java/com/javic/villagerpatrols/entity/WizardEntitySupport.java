package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.network.WizardCastingNetworking;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WizardEntitySupport {
   public static final String SOUND_FOLDER = "wizard";
   public static final int MALE_SKINS = 5;
   public static final int FEMALE_SKINS = 5;
   public static final double MAX_HEALTH = 60.0;
   public static final double ATTACK_DAMAGE = 3.0;
   public static final double VILLAGER_CHASE_SPEED = 0.55;
   public static final float SPELL_CLOUD_RADIUS = 2.25F;
   public static final int SPELL_CLOUD_DURATION = 45;
   public static final float SPELL_DAMAGE = 4.0F;
   public static final float SPELL_DIRECT_DAMAGE = 5.0F;
   public static final int SPELL_COOLDOWN = 100;
   private static final String COMBAT_MODE_PASSIVE = "PASSIVE";
   private static final String COMBAT_MODE_DEFENSIVE = "DEFENSIVE";
   private static final String COMBAT_MODE_AGGRESSIVE = "AGGRESSIVE";
   private static final int ATTACK_INTERVAL = 20;
   private static final int SPELL_WARMUP = 20;
   private static final int SPELL_CANCEL_COOLDOWN = 20;
   private static final double SPELL_RANGE = 14.0;
   private static final int SPELL_CLOUD_HIT_INTERVAL = 10;
   private static final String TAG_CUSTOM_SKIN_PATH = "CustomSkinPath";
   private static final String TAG_PERSISTENT_MAIN_HAND = "PersistentMainHand";
   private static final String TAG_SLIMPATCH_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String TAG_SLIMPATCH_COMBAT_MODE = "SlimPatchCombatMode";
   private static final String TAG_WIZARD_VILLAGER = "villagerpatrols_wizard_villager";
   private static final String TAG_WIZARD_FEMALE = "villagerpatrols_wizard_female";
   private static final String TAG_WIZARD_NEXT_AMBIENT = "villagerpatrols_wizard_next_ambient";
   private static final String TAG_WIZARD_LAST_ATTACK = "villagerpatrols_wizard_last_attack";
   private static final String TAG_WIZARD_CASTING = "villagerpatrols_wizard_casting";
   private static final String TAG_WIZARD_CAST_END = "villagerpatrols_wizard_cast_end";
   private static final String TAG_WIZARD_NEXT_SPELL = "villagerpatrols_wizard_next_spell";
   private static final String TAG_WIZARD_CAST_TARGET = "villagerpatrols_wizard_cast_target";
   private static final String TAG_WIZARD_BREATH_CLOUD = "villagerpatrols_wizard_breath_cloud";
   private static final String TAG_WIZARD_BREATH_LAST_HIT = "villagerpatrols_wizard_breath_last_hit";

   private WizardEntitySupport() {
   }

   public static ResourceLocation getTexture(boolean female, int index) {
      return new ResourceLocation("villagerpatrols", "textures/entity/wizard/" + (female ? "female" : "male") + "/skin_" + index + ".png");
   }

   public static void applyWizardCompatibility(Mob mob, boolean female, int skinIndex, boolean healToFull) {
      ResourceLocation texture = getTexture(female, skinIndex);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_wizard_villager", true);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_wizard_female", female);
      PersistentEntityData.get(mob).putInt("villagerpatrols_skin", skinIndex);
      PersistentEntityData.get(mob).putString("CustomSkinPath", texture.toString());
      PersistentEntityData.get(mob).remove("SavedSkinInput");
      if (mob instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setCustomSkinPath(texture.toString());
         maleVillager.setSavedSkinInput("");
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setCustomSkinPath(texture.toString());
         femaleVillager.setSavedSkinInput("");
      } else {
         SlimPatchCompat.trySetSavedSkinInput(mob, "");
         SlimPatchCompat.trySetCustomSkinPath(mob, texture.toString());
      }

      ItemStack staff = new ItemStack((ItemLike)ModItems.WIZARD_STAFF.get());
      mob.setItemSlot(EquipmentSlot.MAINHAND, staff.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).put("PersistentMainHand", staff.copy().save(new CompoundTag()));
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      SlimPatchCompat.trySetPersistentMainHand(mob, staff);
      ensureDefaultCombatMode(mob);
      SlimPatchCompat.trySetBaseMaxHealth(mob, 60.0);
      AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
      if (maxHealth != null) {
         maxHealth.setBaseValue(60.0);
      }

      AttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
      if (attackDamage != null) {
         attackDamage.setBaseValue(3.0);
      }

      if (healToFull) {
         mob.setHealth(mob.getMaxHealth());
      } else if (mob.getHealth() > mob.getMaxHealth()) {
         mob.setHealth(mob.getMaxHealth());
      }
   }

   public static boolean isWizardVillager(Mob mob) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_villager");
   }

   public static boolean isExternalWizardVillager(Mob mob) {
      return isWizardVillager(mob) && !(mob instanceof MaleWizardEntity) && !(mob instanceof FemaleWizardEntity);
   }

   public static boolean isVisualWizardVillager(Mob mob) {
      return isWizardVillager(mob) || hasWizardSkinPath(mob);
   }

   public static boolean isVisualExternalWizardVillager(Mob mob) {
      return isVisualWizardVillager(mob) && !(mob instanceof MaleWizardEntity) && !(mob instanceof FemaleWizardEntity);
   }

   public static boolean isFemale(Mob mob) {
      if (mob instanceof FemaleVillagerEntity) {
         return true;
      } else if (mob instanceof MaleVillagerEntity) {
         return false;
      } else {
         return PersistentEntityData.get(mob).contains("villagerpatrols_wizard_female")
            ? PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_female")
            : "female".equals(PersistentEntityData.get(mob).getString("slimpatch_gender"));
      }
   }

   public static boolean isDefensiveTarget(LivingEntity target) {
      return !(target instanceof Creeper) && !(target instanceof AbstractKnightEntity knight && knight.isFriendlyKnight());
   }

   public static EntityType<? extends Mob> resolveFriendlyType(Level level, boolean female) {
      if (SlimPatchCompat.isLoaded()) {
         return SlimPatchCompat.resolveVillagerType(female);
      } else {
         return female ? (EntityType)ModEntities.FEMALE_WIZARD.get() : (EntityType)ModEntities.MALE_WIZARD.get();
      }
   }

   public static void initializeFriendlySpawn(Mob mob, boolean female, boolean healToFull) {
      int max = female ? 5 : 5;
      int skinIndex = mob.getRandom().nextInt(max) + 1;
      PersistentEntityData.get(mob).putInt("villagerpatrols_skin", skinIndex);
      applyWizardCompatibility(mob, female, skinIndex, healToFull);
   }

   public static void prepareSpawnedEntity(Entity entity, boolean female, boolean healToFull) {
      if (entity instanceof Mob mob) {
         initializeFriendlySpawn(mob, female, healToFull);
      }
   }

   public static void tickWizardVillager(Mob mob) {
      applySelfRegeneration(mob);
      boolean female = isFemale(mob);
      int skinIndex = resolveSkinIndex(mob, female);
      applyWizardCompatibility(mob, female, skinIndex, false);
      String combatMode = resolveCombatModeName(mob);
      if (isPassiveCombatMode(combatMode)) {
         mob.getNavigation().stop();
         mob.setTarget(null);
         stopCasting(mob);
      } else {
         if (mob.tickCount >= PersistentEntityData.get(mob).getLong("villagerpatrols_wizard_next_ambient")) {
            playAmbient(mob);
            PersistentEntityData.get(mob).putLong("villagerpatrols_wizard_next_ambient", mob.tickCount + ModEntitySounds.ambientInterval(mob));
         }

         LivingEntity target = mob.getTarget();
         if (target != null && (!target.isAlive() || target.isRemoved() || target instanceof Player player && mob.getLastHurtByMob() != player)) {
            mob.setTarget(null);
            target = null;
         }

         if (target != null && isDefensiveCombatMode(combatMode) && !isDefensiveThreat(mob, target)) {
            mob.setTarget(null);
            target = null;
         }

         if (target == null) {
            LivingEntity attacker = mob.getLastHurtByMob();
            if (attacker != null
               && attacker.isAlive()
               && !attacker.isRemoved()
               && (attacker instanceof Player || attacker instanceof Monster monster && isDefensiveTarget(monster))) {
               mob.setTarget(attacker);
               target = attacker;
            }
         }

         if (target == null && isAggressiveCombatMode(combatMode) && mob.tickCount % 20 == mob.getId() % 20) {
            target = mob.level()
               .getNearestEntity(
                  mob.level().getEntitiesOfClass(Monster.class, mob.getBoundingBox().inflate(12.0, 8.0, 12.0), WizardEntitySupport::isDefensiveTarget),
                  TargetingConditions.forCombat().range(12.0),
                  mob,
                  mob.getX(),
                  mob.getY(),
                  mob.getZ()
               );
            if (target != null) {
               mob.setTarget(target);
            }
         }

         if (target == null && !PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_casting")) {
            stopCasting(mob);
         } else {
            if (target != null) {
               mob.getNavigation().moveTo(target, 0.55);
               mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (!tickWizardSpellAttack(mob, target)) {
               if (target != null && mob.tickCount - PersistentEntityData.get(mob).getInt("villagerpatrols_wizard_last_attack") >= 20) {
                  double reach = mob.getBbWidth() * 2.0F * (mob.getBbWidth() * 2.0F) + target.getBbWidth();
                  if (!(mob.distanceToSqr(target) > reach + 1.0)) {
                     if (mob.doHurtTarget(target)) {
                        mob.swing(InteractionHand.MAIN_HAND);
                        PersistentEntityData.get(mob).putInt("villagerpatrols_wizard_last_attack", mob.tickCount);
                     }
                  }
               }
            }
         }
      }
   }

   public static void tickStandaloneWizard(Mob mob) {
      applySelfRegeneration(mob);
      LivingEntity target = mob.getTarget();
      if (target != null && (!target.isAlive() || target.isRemoved() || target instanceof Player player && mob.getLastHurtByMob() != player)) {
         mob.setTarget(null);
         target = null;
      }

      if (target == null && !PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_casting")) {
         stopCasting(mob);
      } else {
         if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
         }

         tickWizardSpellAttack(mob, target);
      }
   }

   public static void tickWizardBreathCloud(AreaEffectCloud cloud) {
      if (PersistentEntityData.get(cloud).getBoolean("villagerpatrols_wizard_breath_cloud") && cloud.tickCount >= cloud.getWaitTime()) {
         if (cloud.tickCount - PersistentEntityData.get(cloud).getInt("villagerpatrols_wizard_breath_last_hit") >= 10) {
            PersistentEntityData.get(cloud).putInt("villagerpatrols_wizard_breath_last_hit", cloud.tickCount);
            LivingEntity owner = cloud.getOwner();
            float radius = cloud.getRadius();
            if (!(radius <= 0.0F)) {
               for (LivingEntity living : cloud.level().getEntitiesOfClass(LivingEntity.class, cloud.getBoundingBox().inflate(radius, 1.0, radius))) {
                  if (canWizardBreathHit(owner, living)) {
                     double x = living.getX() - cloud.getX();
                     double z = living.getZ() - cloud.getZ();
                     if (!(x * x + z * z > radius * radius)) {
                        living.hurt(cloud.level().damageSources().indirectMagic(cloud, owner), 4.0F);
                     }
                  }
               }
            }
         }
      }
   }

   public static void applySelfRegeneration(Mob mob) {
      if (!mob.level().isClientSide && mob.isAlive()) {
         MobEffectInstance current = mob.getEffect(MobEffects.REGENERATION);
         if (current == null || current.getAmplifier() != 0 || current.getDuration() <= 20) {
            mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, false), mob);
         }
      }
   }

   public static void spawnWizardBreathCloud(ServerLevel level, LivingEntity owner, Vec3 pos) {
      AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
      cloud.setOwner(owner);
      cloud.setParticle(ParticleTypes.DRAGON_BREATH);
      cloud.setRadius(2.25F);
      cloud.setDuration(45);
      cloud.setWaitTime(0);
      cloud.setRadiusPerTick(-0.03888889F);
      PersistentEntityData.get(cloud).putBoolean("villagerpatrols_wizard_breath_cloud", true);
      level.addFreshEntity(cloud);
   }

   public static void playAmbient(Mob mob) {
      playSound(mob, ModEntitySounds.ambient("wizard", isFemale(mob)));
   }

   public static void playHurt(Mob mob) {
      playSound(mob, ModEntitySounds.hurt("wizard", isFemale(mob)));
   }

   public static void playDeath(Mob mob) {
      playSound(mob, ModEntitySounds.death("wizard", isFemale(mob)));
   }

   public static void playInteract(Mob mob) {
      playSound(mob, ModEntitySounds.getInteract("wizard", isFemale(mob)));
   }

   private static void playSound(Mob mob, SoundEvent sound) {
      if (!mob.level().isClientSide) {
         mob.playSound(sound, 1.0F, 0.95F + mob.getRandom().nextFloat() * 0.1F);
      }
   }

   private static boolean tickWizardSpellAttack(Mob mob, LivingEntity target) {
      if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_casting")) {
         LivingEntity castingTarget = resolveCastingTarget(mob, target);
         if (!canUseSpellTarget(mob, castingTarget)) {
            cancelCasting(mob);
            return true;
         }

         mob.getNavigation().stop();
         mob.getLookControl().setLookAt(castingTarget, 30.0F, 30.0F);
         if (mob.tickCount < PersistentEntityData.get(mob).getInt("villagerpatrols_wizard_cast_end")) {
            return true;
         }

         fireSpellProjectile(mob, castingTarget);
         stopCasting(mob);
         PersistentEntityData.get(mob).putInt("villagerpatrols_wizard_next_spell", mob.tickCount + 100);
         return true;
      } else {
         if (!canUseSpellTarget(mob, target)) {
            return false;
         }

         if (mob.hasLineOfSight(target)
            && !(mob.distanceToSqr(target) > 196.0)
            && mob.tickCount >= PersistentEntityData.get(mob).getInt("villagerpatrols_wizard_next_spell")) {
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            PersistentEntityData.get(mob).putBoolean("villagerpatrols_wizard_casting", true);
            PersistentEntityData.get(mob).putInt("villagerpatrols_wizard_cast_end", mob.tickCount + 20);
            PersistentEntityData.get(mob).putUUID("villagerpatrols_wizard_cast_target", target.getUUID());
            WizardCastingNetworking.setCastingActive(mob, true);
            if (!mob.isSilent()) {
               mob.level().levelEvent(null, 1017, mob.blockPosition(), 0);
               mob.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.9F, 0.95F + mob.getRandom().nextFloat() * 0.1F);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private static void fireSpellProjectile(Mob mob, LivingEntity target) {
      if (mob.level() instanceof ServerLevel level) {
         Vec3 var6 = mob.getEyePosition().add(mob.getLookAngle().scale(0.6));
         Vec3 targetPos = target.getEyePosition().subtract(var6).normalize();
         WizardMagicProjectileEntity projectile = new WizardMagicProjectileEntity(level, mob, targetPos);
         projectile.moveTo(var6.x, var6.y - 0.15, var6.z, mob.getYRot(), mob.getXRot());
         level.addFreshEntity(projectile);
      }
   }

   private static void stopCasting(Mob mob) {
      PersistentEntityData.get(mob).remove("villagerpatrols_wizard_cast_end");
      PersistentEntityData.get(mob).remove("villagerpatrols_wizard_cast_target");
      if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_wizard_casting")) {
         PersistentEntityData.get(mob).putBoolean("villagerpatrols_wizard_casting", false);
         WizardCastingNetworking.setCastingActive(mob, false);
      }
   }

   private static void cancelCasting(Mob mob) {
      stopCasting(mob);
      int nextSpell = Math.max(PersistentEntityData.get(mob).getInt("villagerpatrols_wizard_next_spell"), mob.tickCount + 20);
      PersistentEntityData.get(mob).putInt("villagerpatrols_wizard_next_spell", nextSpell);
   }

   private static boolean canUseSpellTarget(Mob mob, LivingEntity target) {
      if (target == null || !target.isAlive() || target.isRemoved() || target == mob || mob.isAlliedTo(target)) {
         return false;
      } else {
         return target instanceof Player player
            ? !isProtectedFollowPlayer(mob, player) && (mob.getLastHurtByMob() == player || mob.getTarget() == player)
            : target instanceof Monster monster && isDefensiveTarget(monster);
      }
   }

   private static boolean canWizardBreathHit(LivingEntity owner, LivingEntity target) {
      if (target == owner || !target.isAlive() || target.isRemoved()) {
         return false;
      } else {
         return owner instanceof Mob mobOwner ? canSpellDamageTarget(mobOwner, target) : target instanceof Monster monster && isDefensiveTarget(monster);
      }
   }

   public static boolean canSpellDamageTarget(Mob mob, LivingEntity target) {
      if (target == null || !target.isAlive() || target.isRemoved() || target == mob || mob.isAlliedTo(target)) {
         return false;
      } else {
         return target instanceof Player player ? !isProtectedFollowPlayer(mob, player) : target instanceof Monster monster && isDefensiveTarget(monster);
      }
   }

   private static LivingEntity resolveCastingTarget(Mob mob, LivingEntity target) {
      if (target != null && target.isAlive() && !target.isRemoved()) {
         PersistentEntityData.get(mob).putUUID("villagerpatrols_wizard_cast_target", target.getUUID());
         return target;
      } else if (PersistentEntityData.get(mob).hasUUID("villagerpatrols_wizard_cast_target") && mob.level() instanceof ServerLevel level) {
         return level.getEntity(PersistentEntityData.get(mob).getUUID("villagerpatrols_wizard_cast_target")) instanceof LivingEntity living ? living : null;
      } else {
         return null;
      }
   }

   private static boolean isProtectedFollowPlayer(Mob mob, Player player) {
      if (SlimPatchCompat.isLoaded() && isExternalWizardVillager(mob)) {
         String commandState = resolveSlimPatchCommandState(mob);
         if (!"FOLLOW".equals(commandState)) {
            return false;
         }

         UUID ownerUuid = resolveSlimPatchOwnerUuid(mob);
         if (ownerUuid != null && ownerUuid.equals(player.getUUID())) {
            return true;
         }

         UUID targetUuid = resolveSlimPatchCommandTargetUuid(mob);
         return targetUuid != null && targetUuid.equals(player.getUUID());
      } else {
         return false;
      }
   }

   private static String resolveSlimPatchCommandState(Mob mob) {
      return SlimPatchCompat.getCommandStateName(mob);
   }

   private static UUID resolveSlimPatchOwnerUuid(Mob mob) {
      return SlimPatchCompat.getCommandOwnerUuid(mob);
   }

   private static UUID resolveSlimPatchCommandTargetUuid(Mob mob) {
      return SlimPatchCompat.getCommandTargetUuid(mob);
   }

   private static int resolveSkinIndex(Mob mob, boolean female) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         return maleVillager.getSkinIndex();
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         return femaleVillager.getSkinIndex();
      } else {
         int max = female ? 5 : 5;
         int index = PersistentEntityData.get(mob).getInt("villagerpatrols_skin");
         if (index < 1 || index > max) {
            index = mob.getRandom().nextInt(max) + 1;
            PersistentEntityData.get(mob).putInt("villagerpatrols_skin", index);
         }

         return index;
      }
   }

   private static boolean hasWizardSkinPath(Mob mob) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         return maleVillager.getCustomSkinPath().contains("textures/entity/wizard/");
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         return femaleVillager.getCustomSkinPath().contains("textures/entity/wizard/");
      } else {
         String path = PersistentEntityData.get(mob).getString("CustomSkinPath");
         return path.contains("textures/entity/wizard/");
      }
   }

   private static void ensureDefaultCombatMode(Mob mob) {
      if (!PersistentEntityData.get(mob).contains("SlimPatchCombatMode")) {
         PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "AGGRESSIVE");
         SlimPatchCompat.trySetCombatMode(mob, "AGGRESSIVE");
      }
   }

   private static String resolveCombatModeName(Mob mob) {
      String persistentMode = PersistentEntityData.get(mob).getString("SlimPatchCombatMode");
      return persistentMode.isEmpty() ? "AGGRESSIVE" : persistentMode;
   }

   private static boolean isPassiveCombatMode(String combatMode) {
      return "PASSIVE".equals(combatMode);
   }

   private static boolean isDefensiveCombatMode(String combatMode) {
      return "DEFENSIVE".equals(combatMode);
   }

   private static boolean isAggressiveCombatMode(String combatMode) {
      return !isPassiveCombatMode(combatMode) && !isDefensiveCombatMode(combatMode);
   }

   private static boolean isDefensiveThreat(Mob mob, LivingEntity target) {
      if (target != null && target.isAlive() && !target.isRemoved()) {
         return mob.getLastHurtByMob() == target ? true : target instanceof Mob hostileMob && hostileMob.getTarget() == mob;
      } else {
         return false;
      }
   }
}
