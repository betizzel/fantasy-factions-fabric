package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.lang.reflect.Method;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class EventVillagerSkins {
   private static final EventVillagerSkins.SkinPool DEFAULT_POOL = new EventVillagerSkins.SkinPool("textures/entity/custom_villager", null, 50);
   private static final EventVillagerSkins.SkinPool FISHER_POOL = new EventVillagerSkins.SkinPool("textures/entity/custom_villager", "fisher", 6);
   private static final EventVillagerSkins.SkinPool SHEPHERD_POOL = new EventVillagerSkins.SkinPool("textures/entity/custom_villager", "shepherd", 6);
   private static final EventVillagerSkins.SkinPool DESERT_TRAVELER_POOL = new EventVillagerSkins.SkinPool(
      "textures/entity/custom_villager", "desert_traveler", 6
   );
   private static final EventVillagerSkins.SkinPool ICE_TRAVELER_POOL = new EventVillagerSkins.SkinPool("textures/entity/custom_villager", "ice_traveler", 6);
   private static final EventVillagerSkins.SkinPool MINER_POOL = new EventVillagerSkins.SkinPool("textures/entity/custom_villager", "miner", 6);
   private static final EventVillagerSkins.SkinPool SKIRMISH_VILLAGER_POOL = new EventVillagerSkins.SkinPool(
      "textures/entity/custom_villager", "skirmish_villager", 21
   );
   private static final EventVillagerSkins.SkinPool BARD_POOL = new EventVillagerSkins.SkinPool("textures/entity", "bard", 6);
   private static final EventVillagerSkins.SkinPool ARMED_TRAVELER_POOL = new EventVillagerSkins.SkinPool("textures/entity", "armed_traveler", 15);

   private EventVillagerSkins() {
   }

   public static void applyDefaultStandaloneVillagerSkin(Mob mob) {
      applySkinPool(mob, DEFAULT_POOL);
   }

   public static void applyFishingVillagerSkin(Mob mob) {
      applySkinPool(mob, FISHER_POOL);
   }

   public static void applyShepherdSkin(Mob mob) {
      applySkinPool(mob, SHEPHERD_POOL);
   }

   public static void applyDesertTravelerSkin(Mob mob) {
      applySkinPool(mob, DESERT_TRAVELER_POOL);
   }

   public static void applyIceTravelerSkin(Mob mob) {
      applySkinPool(mob, ICE_TRAVELER_POOL);
   }

   public static void applyMinerSkin(Mob mob) {
      applySkinPool(mob, MINER_POOL);
   }

   public static void applyBardSkin(Mob mob) {
      applySkinPool(mob, BARD_POOL);
   }

   public static void applyArmedTravelerSkin(Mob mob) {
      applySkinPool(mob, ARMED_TRAVELER_POOL);
   }

   public static void applyBanditSkirmishSkin(Mob mob) {
      applySkinPool(mob, SKIRMISH_VILLAGER_POOL);
   }

   public static void applyIllagerSkirmishSkin(Mob mob) {
      applySkinPool(mob, SKIRMISH_VILLAGER_POOL);
   }

   public static void applyOrcSkirmishSkin(Mob mob) {
      applySkinPool(mob, SKIRMISH_VILLAGER_POOL);
   }

   public static boolean canApplyTo(Mob mob) {
      return resolveGender(mob) != null;
   }

   private static void applySkinPool(Mob mob, EventVillagerSkins.SkinPool skinPool) {
      if (mob != null && skinPool.maxIndex() > 0) {
         String gender = resolveGender(mob);
         if (gender != null) {
            int[] validIndices = resolveValidIndices(skinPool, gender);
            if (validIndices.length != 0) {
               int skinIndex = validIndices[mob.getRandom().nextInt(validIndices.length)];
               ResourceLocation skinPath = skinPool.path(gender, skinIndex);
               applySkin(mob, skinIndex, skinPath.toString());
            }
         }
      }
   }

   private static void applySkin(Mob mob, int skinIndex, String skinPath) {
      try {
         Method setSkinIndexMethod = mob.getClass().getMethod("setSkinIndex", int.class);
         Method setSavedSkinInputMethod = mob.getClass().getMethod("setSavedSkinInput", String.class);
         Method setCustomSkinPathMethod = mob.getClass().getMethod("setCustomSkinPath", String.class);
         setSkinIndexMethod.invoke(mob, skinIndex);
         setSavedSkinInputMethod.invoke(mob, "");
         setCustomSkinPathMethod.invoke(mob, skinPath);
      } catch (ReflectiveOperationException var6) {
      }
   }

   private static int[] resolveValidIndices(EventVillagerSkins.SkinPool pool, String gender) {
      int[] valid = new int[pool.maxIndex()];
      int count = 0;

      for (int index = 1; index <= pool.maxIndex(); index++) {
         if (resourceExists(pool.path(gender, index))) {
            valid[count++] = index;
         }
      }

      int[] result = new int[count];
      System.arraycopy(valid, 0, result, 0, count);
      return result;
   }

   private static String resolveGender(Mob mob) {
      if (mob instanceof MaleVillagerEntity) {
         return "male";
      }

      if (mob instanceof FemaleVillagerEntity) {
         return "female";
      }

      String gender = PersistentEntityData.get(mob).getString("slimpatch_gender");
      return !"male".equals(gender) && !"female".equals(gender) ? null : gender;
   }

   private static boolean resourceExists(ResourceLocation location) {
      return EventVillagerSkins.class.getClassLoader().getResource("assets/" + location.getNamespace() + "/" + location.getPath()) != null;
   }

   private record SkinPool(String basePath, String folder, int maxIndex) {
      private ResourceLocation path(String gender, int index) {
         String path = this.folder == null
            ? this.basePath + "/" + gender + "/skin_" + index + ".png"
            : this.basePath + "/" + this.folder + "/" + gender + "/skin_" + index + ".png";
         return new ResourceLocation("villagerpatrols", path);
      }
   }
}
