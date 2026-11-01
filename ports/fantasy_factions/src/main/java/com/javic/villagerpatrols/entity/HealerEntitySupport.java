package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public final class HealerEntitySupport {
   public static final int MALE_SKINS = 12;
   public static final int FEMALE_SKINS = 12;
   public static final double MAX_HEALTH = 36.0;
   private static final String COMBAT_MODE_PASSIVE = "PASSIVE";
   private static final String COMBAT_MODE_DEFENSIVE = "DEFENSIVE";
   private static final String COMBAT_MODE_AGGRESSIVE = "AGGRESSIVE";
   private static final String TAG_HEALER_VILLAGER = "villagerpatrols_healer_villager";
   private static final String TAG_HEALER_FEMALE = "villagerpatrols_healer_female";
   private static final String TAG_CUSTOM_SKIN_PATH = "CustomSkinPath";
   private static final String TAG_PERSISTENT_OFF_HAND = "PersistentOffHand";
   private static final String TAG_SLIMPATCH_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String TAG_SLIMPATCH_COMBAT_MODE = "SlimPatchCombatMode";
   private static final String TAG_NEXT_CHIME_TICK = "villagerpatrols_healer_next_chime_tick";
   private static final String TAG_NEXT_SPLASH_HEAL_TICK = "villagerpatrols_healer_next_splash_heal_tick";
   private static final int PARTICLE_INTERVAL = 6;
   private static final int PARTICLE_COUNT = 2;
   private static final int REGEN_INTERVAL = 40;
   private static final double REGEN_RADIUS = 8.0;
   private static final int REGEN_DURATION = 100;
   private static final int REGEN_AMPLIFIER = 0;
   private static final double SPLASH_HEAL_RADIUS = 10.0;
   private static final float SPLASH_HEAL_THRESHOLD = 6.0F;
   private static final int SPLASH_HEAL_COOLDOWN = 1800;
   private static final int CHIME_MIN_INTERVAL = 150;
   private static final int CHIME_MAX_INTERVAL = 300;

   private HealerEntitySupport() {
   }

   public static ResourceLocation getTexture(boolean female, int index) {
      return new ResourceLocation("villagerpatrols", "textures/entity/healer/" + (female ? "female" : "male") + "/skin_" + index + ".png");
   }

   public static EntityType<? extends Mob> resolveFriendlyType(Level level, boolean female) {
      if (SlimPatchCompat.isLoaded()) {
         return SlimPatchCompat.resolveVillagerType(female);
      } else {
         return female ? (EntityType)ModEntities.FEMALE_HEALER.get() : (EntityType)ModEntities.MALE_HEALER.get();
      }
   }

   public static void initializeFriendlySpawn(Mob mob, boolean female, boolean healToFull) {
      int max = female ? 12 : 12;
      int skinIndex = mob.getRandom().nextInt(max) + 1;
      PersistentEntityData.get(mob).putInt("villagerpatrols_skin", skinIndex);
      applyHealerCompatibility(mob, female, skinIndex, healToFull);
   }

   public static void prepareSpawnedEntity(Entity entity, boolean female, boolean healToFull) {
      if (entity instanceof Mob mob) {
         initializeFriendlySpawn(mob, female, healToFull);
      }
   }

   public static void tickHealerVillager(Mob mob) {
      boolean female = isFemale(mob);
      int skinIndex = resolveSkinIndex(mob, female);
      applyHealerCompatibility(mob, female, skinIndex, false);
      String combatMode = resolveCombatModeName(mob);
      if (isPassiveCombatMode(combatMode) || isDefensiveCombatMode(combatMode) && !isDefensiveThreat(mob, mob.getTarget())) {
         mob.setTarget(null);
      }

      if (mob.level() instanceof ServerLevel level) {
         spawnAmbientParticles(level, mob);
         playAmbientChime(level, mob);
         applyRegenerationAura(level, mob);
         tryThrowHealingPotion(level, mob);
      }
   }

   public static boolean isHealerVillager(Mob mob) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_healer_villager");
   }

   public static boolean isExternalHealerVillager(Mob mob) {
      return isHealerVillager(mob) && !(mob instanceof MaleHealerEntity) && !(mob instanceof FemaleHealerEntity);
   }

   public static boolean isFemale(Mob mob) {
      if (mob instanceof FemaleHealerEntity || mob instanceof FemaleVillagerEntity) {
         return true;
      } else if (!(mob instanceof MaleHealerEntity) && !(mob instanceof MaleVillagerEntity)) {
         return PersistentEntityData.get(mob).contains("villagerpatrols_healer_female")
            ? PersistentEntityData.get(mob).getBoolean("villagerpatrols_healer_female")
            : "female".equals(PersistentEntityData.get(mob).getString("slimpatch_gender"));
      } else {
         return false;
      }
   }

   public static void applyHealerCompatibility(Mob mob, boolean female, int skinIndex, boolean healToFull) {
      ResourceLocation texture = getTexture(female, skinIndex);
      ItemStack healingPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_healer_villager", true);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_healer_female", female);
      PersistentEntityData.get(mob).putInt("villagerpatrols_skin", skinIndex);
      PersistentEntityData.get(mob).putString("CustomSkinPath", texture.toString());
      PersistentEntityData.get(mob).remove("SavedSkinInput");
      PersistentEntityData.get(mob).put("PersistentOffHand", healingPotion.copy().save(new CompoundTag()));
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
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

      mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      if (!ItemStack.matches(mob.getOffhandItem(), healingPotion)) {
         mob.setItemSlot(EquipmentSlot.OFFHAND, healingPotion.copy());
      }

      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      SlimPatchCompat.trySetPersistentMainHand(mob, ItemStack.EMPTY);
      ensureDefaultCombatMode(mob);
      SlimPatchCompat.trySetBaseMaxHealth(mob, 36.0);
      AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
      if (maxHealth != null) {
         maxHealth.setBaseValue(36.0);
      }

      if (healToFull) {
         mob.setHealth(mob.getMaxHealth());
      } else if (mob.getHealth() > mob.getMaxHealth()) {
         mob.setHealth(mob.getMaxHealth());
      }

      if (mob.level() instanceof ServerLevel serverLevel
         && mob instanceof Villager villager
         && (villager.getVillagerData().getProfession() != VillagerProfession.CLERIC || villager.getVillagerXp() == 0)) {
         villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.CLERIC).setLevel(1));
         villager.setVillagerXp(1);
         villager.getOffers();
         villager.refreshBrain(serverLevel);
      }
   }

   private static int resolveSkinIndex(Mob mob, boolean female) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         return maleVillager.getSkinIndex();
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         return femaleVillager.getSkinIndex();
      } else {
         int max = female ? 12 : 12;
         int index = PersistentEntityData.get(mob).getInt("villagerpatrols_skin");
         if (index < 1 || index > max) {
            index = mob.getRandom().nextInt(max) + 1;
            PersistentEntityData.get(mob).putInt("villagerpatrols_skin", index);
         }

         return index;
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

   private static boolean isDefensiveThreat(Mob mob, Entity target) {
      return target instanceof Mob hostileMob && target.isAlive() && !target.isRemoved()
         ? mob.getLastHurtByMob() == target || hostileMob.getTarget() == mob
         : mob.getLastHurtByMob() == target;
   }

   private static void spawnAmbientParticles(ServerLevel level, Mob mob) {
      if (mob.tickCount % 6 == mob.getId() % 6) {
         level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + 1.0, mob.getZ(), 2, 0.25, 0.35, 0.25, 0.005);
      }
   }

   private static void playAmbientChime(ServerLevel level, Mob mob) {
      long gameTime = level.getGameTime();
      long nextChimeTick = PersistentEntityData.get(mob).getLong("villagerpatrols_healer_next_chime_tick");
      if (nextChimeTick == 0L) {
         PersistentEntityData.get(mob).putLong("villagerpatrols_healer_next_chime_tick", gameTime + randomBetween(mob, 150, 300));
      } else if (gameTime >= nextChimeTick) {
         level.playSound(
            null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.2F, 0.9F + mob.getRandom().nextFloat() * 0.2F
         );
         PersistentEntityData.get(mob).putLong("villagerpatrols_healer_next_chime_tick", gameTime + randomBetween(mob, 150, 300));
      }
   }

   private static void applyRegenerationAura(ServerLevel level, Mob mob) {
      if (mob.tickCount % 40 == mob.getId() % 40) {
         for (Player player : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(8.0, 4.0, 8.0), HealerEntitySupport::isValidSupportTarget)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true), mob);
         }
      }
   }

   private static void tryThrowHealingPotion(ServerLevel level, Mob mob) {
      long gameTime = level.getGameTime();
      if (gameTime >= PersistentEntityData.get(mob).getLong("villagerpatrols_healer_next_splash_heal_tick")) {
         Player target = findHealingTarget(level, mob);
         if (target != null) {
            HealerEntitySupport.Vec3Like delta = new HealerEntitySupport.Vec3Like(
               target.getX() - mob.getX(), target.getEyeY() - 1.1 - mob.getY(), target.getZ() - mob.getZ()
            );
            double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            ThrownPotion thrownPotion = new ThrownPotion(level, mob);
            thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.HEALING));
            thrownPotion.setXRot(thrownPotion.getXRot() - -20.0F);
            thrownPotion.shoot(delta.x, delta.y + horizontalDistance * 0.2, delta.z, 0.75F, 8.0F);
            level.addFreshEntity(thrownPotion);
            level.playSound(
               null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.WITCH_THROW, SoundSource.NEUTRAL, 0.6F, 1.0F + mob.getRandom().nextFloat() * 0.1F
            );
            PersistentEntityData.get(mob).putLong("villagerpatrols_healer_next_splash_heal_tick", gameTime + 1800L);
         }
      }
   }

   private static Player findHealingTarget(ServerLevel level, Mob mob) {
      Player bestTarget = null;
      double bestDistance = Double.MAX_VALUE;
      float bestHealth = Float.MAX_VALUE;

      for (Player player : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(10.0, 4.0, 10.0), HealerEntitySupport::isValidSupportTarget)) {
         if (!(player.getHealth() > 6.0F)) {
            float health = player.getHealth();
            double distance = mob.distanceToSqr(player);
            if (bestTarget == null || health < bestHealth || health == bestHealth && distance < bestDistance) {
               bestTarget = player;
               bestHealth = health;
               bestDistance = distance;
            }
         }
      }

      return bestTarget;
   }

   private static boolean isValidSupportTarget(Player player) {
      return player.isAlive() && !player.isSpectator() && !player.isCreative();
   }

   private static int randomBetween(Mob mob, int minInclusive, int maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + mob.getRandom().nextInt(maxInclusive - minInclusive + 1);
   }

   private record Vec3Like(double x, double y, double z) {
   }
}
