package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.javic.villagerpatrols.mixin.MobSunBurnAccessor;

public final class VampireEntitySupport {
   public static final String SOUND_FOLDER = "vampire";
   public static final int MALE_SKINS = 6;
   public static final int FEMALE_SKINS = 6;
   public static final double MAX_HEALTH = 60.0;
   public static final double ATTACK_DAMAGE = 5.0;
   public static final double VILLAGER_CHASE_SPEED = 0.55;
   private static final int ATTACK_INTERVAL = 20;
   private static final String TAG_SLIMPATCH_COMBAT_MODE = "SlimPatchCombatMode";
   private static final String TAG_SLIMPATCH_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String TAG_CUSTOM_SKIN_PATH = "CustomSkinPath";
   private static final String TAG_VAMPIRE_VILLAGER = "villagerpatrols_vampire_villager";
   private static final String TAG_VAMPIRE_FEMALE = "villagerpatrols_vampire_female";
   private static final String TAG_VAMPIRE_NEXT_AMBIENT = "villagerpatrols_vampire_next_ambient";
   private static final String TAG_VAMPIRE_LAST_ATTACK = "villagerpatrols_vampire_last_attack";
   private static final String[] MALE_NAMES = new String[]{
      "Adam",
      "Adrian",
      "Alexander",
      "Arthur",
      "Benjamin",
      "Caleb",
      "Daniel",
      "David",
      "Elias",
      "Ethan",
      "Felix",
      "Gabriel",
      "Henry",
      "Isaac",
      "Jacob",
      "James",
      "Julian",
      "Liam",
      "Lucas",
      "Noah",
      "Oliver",
      "Owen",
      "Samuel",
      "Thomas",
      "William"
   };
   private static final String[] FEMALE_NAMES = new String[]{
      "Abigail",
      "Alice",
      "Amelia",
      "Anna",
      "Audrey",
      "Ava",
      "Bella",
      "Caroline",
      "Charlotte",
      "Chloe",
      "Clara",
      "Daisy",
      "Eleanor",
      "Eliza",
      "Ella",
      "Emily",
      "Emma",
      "Evelyn",
      "Fiona",
      "Grace",
      "Hazel",
      "Isabella",
      "Julia",
      "Lily",
      "Lucy",
      "Mia",
      "Nora",
      "Olivia",
      "Sophia",
      "Violet"
   };

   private VampireEntitySupport() {
   }

   public static void igniteFromSunlight(Mob mob) {
      if (mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
         mob.setSecondsOnFire(8);
      }
   }

   public static float applyFireWeakness(DamageSource source, float amount) {
      return source.is(DamageTypeTags.IS_FIRE) ? amount * 2.0F : amount;
   }

   public static float healthBeforeDamage(Entity entity) {
      return entity instanceof LivingEntity livingEntity ? livingEntity.getHealth() : 0.0F;
   }

   public static float absorptionBeforeDamage(Entity entity) {
      return entity instanceof LivingEntity livingEntity ? livingEntity.getAbsorptionAmount() : 0.0F;
   }

   public static void applyLifesteal(Mob mob, Entity entity, boolean result, float previousHealth, float previousAbsorption) {
      if (result && !mob.level().isClientSide && entity instanceof LivingEntity livingEntity) {
         float dealt = Math.max(0.0F, previousHealth + previousAbsorption - livingEntity.getHealth() - livingEntity.getAbsorptionAmount());
         if (dealt > 0.0F) {
            mob.heal(dealt * 0.5F);
         }
      }
   }

   public static ResourceLocation getTexture(boolean female, int index) {
      String gender = female ? "female" : "male";
      ResourceLocation preferred = new ResourceLocation("villagerpatrols", "textures/entity/vampire/" + gender + "/skin_" + index + ".png");
      if (resourceExists(preferred)) {
         return preferred;
      } else {
         ResourceLocation fallback = new ResourceLocation("villagerpatrols", "textures/entity/vampire/" + gender + "/" + index + ".png");
         if (resourceExists(fallback)) {
            return fallback;
         } else {
            return female
               ? new ResourceLocation("villagerpatrols", "textures/entity/vampire/female/1.png")
               : new ResourceLocation("villagerpatrols", "textures/entity/vampire/male/skin_1.png");
         }
      }
   }

   public static Component randomName(Mob mob, boolean female) {
      String[] names = female ? FEMALE_NAMES : MALE_NAMES;
      return Component.literal(names[mob.getRandom().nextInt(names.length)]);
   }

   public static void applyAggressiveVillagerCombatData(Mob mob) {
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "AGGRESSIVE");
      SlimPatchCompat.trySetCombatMode(mob, "AGGRESSIVE");
   }

   public static void applyVillagerCompatibility(Mob mob, boolean female, int skinIndex, boolean healToFull) {
      ResourceLocation texture = getTexture(female, skinIndex);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_vampire_villager", true);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_vampire_female", female);
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "AGGRESSIVE");
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

      applyVampireAttributes(mob, healToFull);
   }

   public static void applyVampireAttributes(Mob mob, boolean healToFull) {
      SlimPatchCompat.trySetBaseMaxHealth(mob, 60.0);
      AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
      if (maxHealth != null) {
         maxHealth.setBaseValue(60.0);
      }

      AttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
      if (attackDamage != null) {
         attackDamage.setBaseValue(5.0);
      }

      if (healToFull) {
         mob.setHealth(mob.getMaxHealth());
      } else if (mob.getHealth() > mob.getMaxHealth()) {
         mob.setHealth(mob.getMaxHealth());
      }
   }

   public static boolean isVampireVillager(Mob mob) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_vampire_villager");
   }

   public static boolean isExternalVampireVillager(Mob mob) {
      return isVampireVillager(mob) && !(mob instanceof MaleVampireVillagerEntity) && !(mob instanceof FemaleVampireVillagerEntity);
   }

   public static boolean isVisualVampireVillager(Mob mob) {
      return isVampireVillager(mob) || hasVampireSkinPath(mob);
   }

   public static boolean isVisualExternalVampireVillager(Mob mob) {
      return isVisualVampireVillager(mob) && !(mob instanceof MaleVampireVillagerEntity) && !(mob instanceof FemaleVampireVillagerEntity);
   }

   public static boolean isFemale(Mob mob) {
      if (mob instanceof FemaleVillagerEntity) {
         return true;
      } else if (mob instanceof MaleVillagerEntity) {
         return false;
      } else {
         return PersistentEntityData.get(mob).contains("villagerpatrols_vampire_female")
            ? PersistentEntityData.get(mob).getBoolean("villagerpatrols_vampire_female")
            : "female".equals(PersistentEntityData.get(mob).getString("slimpatch_gender"));
      }
   }

   public static EntityType<? extends Mob> resolveFriendlyType(Level level, boolean female) {
      if (SlimPatchCompat.isLoaded()) {
         return SlimPatchCompat.resolveVillagerType(female);
      } else {
         return female ? (EntityType)ModEntities.FEMALE_VAMPIRE_VILLAGER.get() : (EntityType)ModEntities.MALE_VAMPIRE_VILLAGER.get();
      }
   }

   public static VampireEntitySupport.SpawnChoice chooseSpawn(Level level) {
      return level.getRandom().nextBoolean()
         ? new VampireEntitySupport.SpawnChoice((EntityType<? extends Mob>)ModEntities.VAMPIRE.get(), false, false)
         : chooseFriendlySpawn(level);
   }

   public static VampireEntitySupport.SpawnChoice chooseFriendlySpawn(Level level) {
      boolean female = level.getRandom().nextBoolean();
      return new VampireEntitySupport.SpawnChoice(resolveFriendlyType(level, female), true, female);
   }

   public static void initializeFriendlySpawn(Mob mob, boolean female, boolean healToFull) {
      int max = female ? 6 : 6;
      int skinIndex = mob.getRandom().nextInt(max) + 1;
      PersistentEntityData.get(mob).putInt("villagerpatrols_skin", skinIndex);
      applyVillagerCompatibility(mob, female, skinIndex, healToFull);
   }

   public static void prepareSpawnedEntity(Entity entity, VampireEntitySupport.SpawnChoice choice, boolean healToFull) {
      if (choice.friendly() && entity instanceof Mob mob) {
         initializeFriendlySpawn(mob, choice.female(), healToFull);
      }
   }

   public static void tickVampireVillager(Mob mob) {
      updateLowHealthStrength(mob);
      boolean female = isFemale(mob);
      int skinIndex = resolveSkinIndex(mob, female);
      applyVillagerCompatibility(mob, female, skinIndex, false);
      if (mob.isAlive() && shouldSunBurn(mob)) {
         igniteFromSunlight(mob);
      }

      if (mob.tickCount >= PersistentEntityData.get(mob).getLong("villagerpatrols_vampire_next_ambient")) {
         playAmbient(mob);
         PersistentEntityData.get(mob).putLong("villagerpatrols_vampire_next_ambient", mob.tickCount + ModEntitySounds.ambientInterval(mob));
      }

      LivingEntity target = mob.getTarget();
      if (target != null && (!target.isAlive() || target.isRemoved() || target instanceof Player && mob.getLastHurtByMob() != target)) {
         mob.setTarget(null);
         target = null;
      }

      if (target == null) {
         LivingEntity attacker = mob.getLastHurtByMob();
         if (attacker != null && attacker.isAlive() && !attacker.isRemoved()) {
            mob.setTarget(attacker);
            target = attacker;
         }
      }

      if (target == null && mob.tickCount % 20 == mob.getId() % 20) {
         target = mob.level()
            .getNearestEntity(
               Monster.class,
               TargetingConditions.forCombat().range(12.0),
               mob,
               mob.getX(),
               mob.getY(),
               mob.getZ(),
               mob.getBoundingBox().inflate(12.0, 8.0, 12.0)
            );
         if (target != null) {
            mob.setTarget(target);
         }
      }

      if (target != null) {
         mob.getNavigation().moveTo(target, 0.55);
         mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
         if (mob.tickCount - PersistentEntityData.get(mob).getInt("villagerpatrols_vampire_last_attack") >= 20) {
            double reach = mob.getBbWidth() * 2.0F * (mob.getBbWidth() * 2.0F) + target.getBbWidth();
            if (!(mob.distanceToSqr(target) > reach + 1.0)) {
               float previousHealth = healthBeforeDamage(target);
               float previousAbsorption = absorptionBeforeDamage(target);
               if (mob.doHurtTarget(target)) {
                  applyLifesteal(mob, target, true, previousHealth, previousAbsorption);
                  mob.swing(InteractionHand.MAIN_HAND);
                  PersistentEntityData.get(mob).putInt("villagerpatrols_vampire_last_attack", mob.tickCount);
               }
            }
         }
      }
   }

   public static void playAmbient(Mob mob) {
      playSound(mob, ModEntitySounds.ambient("vampire", isFemale(mob)));
   }

   public static void playHurt(Mob mob) {
      playSound(mob, ModEntitySounds.hurt("vampire", isFemale(mob)));
   }

   public static void playDeath(Mob mob) {
      playSound(mob, ModEntitySounds.death("vampire", isFemale(mob)));
   }

   public static void playInteract(Mob mob) {
      playSound(mob, ModEntitySounds.getInteract("vampire", isFemale(mob)));
   }

   private static void playSound(Mob mob, SoundEvent sound) {
      if (!mob.level().isClientSide) {
         mob.playSound(sound, 1.0F, 0.95F + mob.getRandom().nextFloat() * 0.1F);
      }
   }

   public static void updateLowHealthStrength(Mob mob) {
      if (!mob.level().isClientSide && mob.isAlive()) {
         double healthThreshold = mob.getMaxHealth() * 0.3;
         if (!(mob.getHealth() <= healthThreshold)) {
            MobEffectInstance current = mob.getEffect(MobEffects.DAMAGE_BOOST);
            if (current != null && current.getAmplifier() == 0 && current.getDuration() <= 40) {
               mob.removeEffect(MobEffects.DAMAGE_BOOST);
            }
         } else {
            MobEffectInstance current = mob.getEffect(MobEffects.DAMAGE_BOOST);
            if (current == null || current.getAmplifier() != 0 || current.getDuration() <= 20) {
               mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false, false), mob);
            }
         }
      }
   }

   private static int resolveSkinIndex(Mob mob, boolean female) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         return maleVillager.getSkinIndex();
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         return femaleVillager.getSkinIndex();
      } else {
         int max = female ? 6 : 6;
         int index = PersistentEntityData.get(mob).getInt("villagerpatrols_skin");
         if (index < 1 || index > max) {
            index = mob.getRandom().nextInt(max) + 1;
            PersistentEntityData.get(mob).putInt("villagerpatrols_skin", index);
         }

         return index;
      }
   }

   private static boolean hasVampireSkinPath(Mob mob) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         return maleVillager.getCustomSkinPath().contains("textures/entity/vampire/");
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         return femaleVillager.getCustomSkinPath().contains("textures/entity/vampire/");
      } else {
         String path = PersistentEntityData.get(mob).getString("CustomSkinPath");
         return path.contains("textures/entity/vampire/");
      }
   }

   private static boolean shouldSunBurn(Mob mob) {
      return ((MobSunBurnAccessor) mob).villagerpatrols$isSunBurnTick();
   }

   private static boolean resourceExists(ResourceLocation location) {
      return VampireEntitySupport.class.getClassLoader().getResource("assets/" + location.getNamespace() + "/" + location.getPath()) != null;
   }

   public record SpawnChoice(EntityType<? extends Mob> type, boolean friendly, boolean female) {
   }
}
