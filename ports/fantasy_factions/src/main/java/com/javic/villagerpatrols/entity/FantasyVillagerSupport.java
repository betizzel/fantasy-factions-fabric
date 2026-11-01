package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import java.lang.reflect.Method;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FantasyVillagerSupport {
   private static final String TAG_CUSTOM_SKIN_PATH = "CustomSkinPath";
   private static final String TAG_SAVED_SKIN_INPUT = "SavedSkinInput";
   private static final String TAG_PERSISTENT_MAIN_HAND = "PersistentMainHand";
   private static final String TAG_SLIMPATCH_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String TAG_SLIMPATCH_COMBAT_MODE = "SlimPatchCombatMode";
   private static final String COMBAT_MODE_PASSIVE = "PASSIVE";
   private static final String TAG_ELF_VILLAGER = "villagerpatrols_elf_villager";
   private static final String TAG_ELF_FEMALE = "villagerpatrols_elf_female";
   private static final String TAG_DWARF_VILLAGER = "villagerpatrols_dwarf_villager";
   private static final String TAG_DWARF_FEMALE = "villagerpatrols_dwarf_female";
   private static final int ELF_VISUAL_HEIGHT = 106;
   private static final int ELF_VISUAL_WIDTH = 94;
   private static final int DWARF_VISUAL_HEIGHT = 65;
   private static final int DWARF_VISUAL_WIDTH = 95;
   private static final String[] ELF_MALE_NAMES = new String[]{
      "Aelar",
      "Faelar",
      "Thalion",
      "Eryndor",
      "Caelith",
      "Aerendil",
      "Lorien",
      "Sylvar",
      "Vaelis",
      "Elion",
      "Theron",
      "Caladrel",
      "Arannis",
      "Fenrel",
      "Ilyon",
      "Saelar",
      "Myrion",
      "Galenor",
      "Rhovan",
      "Talies",
      "Althir",
      "Serelis",
      "Nymeril",
      "Daerion"
   };
   private static final String[] ELF_FEMALE_NAMES = new String[]{
      "Aelira",
      "Sylwen",
      "Lythia",
      "Vaelora",
      "Elaria",
      "Faewyn",
      "Thalira",
      "Ilyrana",
      "Sereniel",
      "Caelyra",
      "Myrielle",
      "Althaea",
      "Naivara",
      "Elowen",
      "Lirael",
      "Saelira",
      "Virelya",
      "Calithra",
      "Aeris",
      "Nymera",
      "Loriel",
      "Aralyn",
      "Selith",
      "Theryn"
   };
   private static final String[] DWARF_MALE_NAMES = new String[]{
      "Brokk",
      "Dain",
      "Thrain",
      "Bofrik",
      "Kardin",
      "Thorin",
      "Durik",
      "Barend",
      "Orsik",
      "Rurik",
      "Balgrim",
      "Harbek",
      "Gundar",
      "Kargan",
      "Morgran",
      "Dorrim",
      "Baelin",
      "Thordek",
      "Kazrik",
      "Bromar",
      "Fargrim",
      "Hadrik",
      "Vondal",
      "Drengar"
   };
   private static final String[] DWARF_FEMALE_NAMES = new String[]{
      "Brynja",
      "Dagna",
      "Hlin",
      "Sigrid",
      "Torbera",
      "Helja",
      "Katra",
      "Vistra",
      "Audhild",
      "Bardryn",
      "Elda",
      "Frida",
      "Gunnloda",
      "Ingra",
      "Riswynn",
      "Sannl",
      "Torgga",
      "Yurgunn",
      "Dorrika",
      "Baldris",
      "Karna",
      "Hvedra",
      "Mardra",
      "Oriffa"
   };

   private FantasyVillagerSupport() {
   }

   public static InteractionResult tryConvertElf(ElfEntity elf, Player player, InteractionHand hand) {
      return tryConvert(elf, player, hand, FantasyVillagerSupport.Race.ELF, Items.EMERALD);
   }

   public static InteractionResult tryConvertDwarf(DwarfEntity dwarf, Player player, InteractionHand hand) {
      return tryConvert(dwarf, player, hand, FantasyVillagerSupport.Race.DWARF, Items.IRON_INGOT);
   }

   private static InteractionResult tryConvert(
      AbstractKnightEntity entity, Player player, InteractionHand hand, FantasyVillagerSupport.Race race, Item triggerItem
   ) {
      if (hand == InteractionHand.MAIN_HAND && SlimPatchCompat.isLoaded() && !entity.isRemoved() && entity.isAlive() && entity.getTarget() == null) {
         ItemStack heldItem = player.getItemInHand(hand);
         if (!heldItem.is(triggerItem)) {
            return InteractionResult.PASS;
         } else if (entity.level().isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (entity.level() instanceof ServerLevel serverLevel) {
            return convertToFantasyVillager(serverLevel, entity, player, heldItem, race) ? InteractionResult.CONSUME : InteractionResult.PASS;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static boolean convertToFantasyVillager(
      ServerLevel level, AbstractKnightEntity original, Player player, ItemStack heldItem, FantasyVillagerSupport.Race race
   ) {
      boolean female = original.isFemale();
      int skinIndex = original.getSkinIndex();
      EntityType<? extends Mob> type = SlimPatchCompat.resolveVillagerType(female);
      Mob replacement = (Mob)type.create(level);
      if (replacement == null) {
         return false;
      }

      float maxHealth = original.getMaxHealth();
      float healthRatio = maxHealth > 0.0F ? original.getHealth() / maxHealth : 1.0F;
      replacement.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
      replacement.setYHeadRot(original.getYHeadRot());
      replacement.setYBodyRot(original.yBodyRot);
      replacement.setDeltaMovement(original.getDeltaMovement());
      replacement.finalizeSpawn(level, level.getCurrentDifficultyAt(replacement.blockPosition()), MobSpawnType.CONVERSION, null, null);
      applyFantasyCompatibility(replacement, original, race, female, skinIndex, healthRatio);
      SlimPatchCompat.assignRandomPersonalityIfAvailable(replacement);
      if (original.hasCustomName()) {
         replacement.setCustomName(original.getCustomName());
         replacement.setCustomNameVisible(original.isCustomNameVisible());
      } else {
         replacement.setCustomName(randomFantasyName(original, race, female));
         replacement.setCustomNameVisible(false);
      }

      if (!level.addFreshEntity(replacement)) {
         return false;
      }

      spawnConversionParticles(level, replacement);
      if (!player.getAbilities().instabuild) {
         heldItem.shrink(1);
      }

      original.discard();
      return true;
   }

   private static void applyFantasyCompatibility(
      Mob replacement, AbstractKnightEntity original, FantasyVillagerSupport.Race race, boolean female, int skinIndex, float healthRatio
   ) {
      PersistentEntityData.get(replacement).putBoolean(race.villagerTag(), true);
      PersistentEntityData.get(replacement).putBoolean(race.femaleTag(), female);
      PersistentEntityData.get(replacement).putInt("villagerpatrols_skin", skinIndex);
      PersistentEntityData.get(replacement).putBoolean("slimpatch_skip_natural_family_spawn", true);
      trySetVisualScale(replacement, race.visualHeight(), race.visualWidth());
      String skinPath = original.getSkinTexture().toString();
      PersistentEntityData.get(replacement).putString("CustomSkinPath", skinPath);
      PersistentEntityData.get(replacement).remove("SavedSkinInput");
      trySetSkinData(replacement, skinPath);
      ItemStack mainHand = original.getMainHandItem().copy();
      ItemStack offhand = original.getOffhandItem().copy();
      replacement.setItemSlot(EquipmentSlot.MAINHAND, mainHand.copy());
      replacement.setItemSlot(EquipmentSlot.OFFHAND, offhand.copy());
      replacement.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      replacement.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(replacement).put("PersistentMainHand", mainHand.copy().save(new CompoundTag()));
      SlimPatchCompat.trySetPersistentMainHand(replacement, mainHand);
      ensureDefaultCombatMode(replacement);
      copyAttributeBase(original, replacement, Attributes.MAX_HEALTH, true);
      copyAttributeBase(original, replacement, Attributes.ATTACK_DAMAGE, false);
      copyAttributeBase(original, replacement, Attributes.MOVEMENT_SPEED, false);
      copyAttributeBase(original, replacement, Attributes.FOLLOW_RANGE, false);
      if (replacement.getAttribute(Attributes.MAX_HEALTH) != null) {
         float resolvedHealthRatio = Math.max(0.0F, Math.min(1.0F, healthRatio));
         replacement.setHealth(Math.max(1.0F, replacement.getMaxHealth() * resolvedHealthRatio));
      }
   }

   private static void copyAttributeBase(AbstractKnightEntity original, Mob replacement, Attribute attribute, boolean updateSlimPatchBaseHealth) {
      AttributeInstance source = original.getAttribute(attribute);
      AttributeInstance target = replacement.getAttribute(attribute);
      if (source != null && target != null) {
         double baseValue = source.getBaseValue();
         target.setBaseValue(baseValue);
         if (updateSlimPatchBaseHealth && attribute == Attributes.MAX_HEALTH) {
            trySetBaseMaxHealth(replacement, baseValue);
         }
      }
   }

   private static Component randomFantasyName(Mob mob, FantasyVillagerSupport.Race race, boolean female) {
      String[] names = race == FantasyVillagerSupport.Race.ELF
         ? (female ? ELF_FEMALE_NAMES : ELF_MALE_NAMES)
         : (female ? DWARF_FEMALE_NAMES : DWARF_MALE_NAMES);
      return Component.literal(names[mob.getRandom().nextInt(names.length)]);
   }

   public static boolean isElfVillager(Entity entity) {
      return entity instanceof Mob mob && PersistentEntityData.get(mob).getBoolean("villagerpatrols_elf_villager");
   }

   public static boolean isDwarfVillager(Entity entity) {
      return entity instanceof Mob mob && PersistentEntityData.get(mob).getBoolean("villagerpatrols_dwarf_villager");
   }

   public static boolean isFantasyVillager(Entity entity) {
      return isElfVillager(entity) || isDwarfVillager(entity);
   }

   public static boolean isFemaleFantasyVillager(Entity entity) {
      if (entity instanceof Mob mob) {
         if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_elf_villager")) {
            return PersistentEntityData.get(mob).getBoolean("villagerpatrols_elf_female");
         } else {
            return PersistentEntityData.get(mob).getBoolean("villagerpatrols_dwarf_villager")
               ? PersistentEntityData.get(mob).getBoolean("villagerpatrols_dwarf_female")
               : false;
         }
      } else {
         return false;
      }
   }

   public static boolean isVisualElfVillager(Mob mob) {
      return hasCustomSkinSegment(mob, "textures/entity/elf/") || PersistentEntityData.get(mob).getBoolean("villagerpatrols_elf_villager");
   }

   public static boolean isVisualDwarfVillager(Mob mob) {
      return hasCustomSkinSegment(mob, "textures/entity/dwarf/") || PersistentEntityData.get(mob).getBoolean("villagerpatrols_dwarf_villager");
   }

   public static boolean isVisualFantasyVillager(Mob mob) {
      return isVisualElfVillager(mob) || isVisualDwarfVillager(mob);
   }

   private static void ensureDefaultCombatMode(Mob mob) {
      if (!PersistentEntityData.get(mob).contains("SlimPatchCombatMode")) {
         PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
         SlimPatchCompat.trySetCombatMode(mob, "PASSIVE");
      }
   }

   private static void trySetSkinData(Mob mob, String path) {
      try {
         Method setSavedSkinInputMethod = mob.getClass().getMethod("setSavedSkinInput", String.class);
         Method setCustomSkinPathMethod = mob.getClass().getMethod("setCustomSkinPath", String.class);
         setSavedSkinInputMethod.invoke(mob, "");
         setCustomSkinPathMethod.invoke(mob, path);
      } catch (ReflectiveOperationException var4) {
      }
   }

   private static void trySetBaseMaxHealth(Mob mob, double baseMaxHealth) {
      try {
         Method setBaseMaxHealthMethod = mob.getClass().getMethod("setBaseMaxHealth", double.class);
         setBaseMaxHealthMethod.invoke(mob, baseMaxHealth);
      } catch (ReflectiveOperationException var4) {
      }
   }

   private static void trySetVisualScale(Mob mob, int height, int width) {
      try {
         Method setVisualHeightMethod = mob.getClass().getMethod("setVisualHeight", int.class);
         Method setVisualWidthMethod = mob.getClass().getMethod("setVisualWidth", int.class);
         setVisualHeightMethod.invoke(mob, height);
         setVisualWidthMethod.invoke(mob, width);
      } catch (ReflectiveOperationException var5) {
      }
   }

   private static boolean hasCustomSkinSegment(Mob mob, String segment) {
      try {
         Method getCustomSkinPathMethod = mob.getClass().getMethod("getCustomSkinPath");
         String path = (String)getCustomSkinPathMethod.invoke(mob);
         return path != null && path.contains(segment);
      } catch (ReflectiveOperationException ignored) {
         return false;
      }
   }

   private static void spawnConversionParticles(ServerLevel level, Mob replacement) {
      level.sendParticles(
         ParticleTypes.HAPPY_VILLAGER, replacement.getX(), replacement.getY() + replacement.getBbHeight() * 0.6, replacement.getZ(), 12, 0.35, 0.4, 0.35, 0.02
      );
   }

   private enum Race {
      ELF("villagerpatrols_elf_villager", "villagerpatrols_elf_female", 106, 94),
      DWARF("villagerpatrols_dwarf_villager", "villagerpatrols_dwarf_female", 65, 95);

      private final String villagerTag;
      private final String femaleTag;
      private final int visualHeight;
      private final int visualWidth;

      Race(String villagerTag, String femaleTag, int visualHeight, int visualWidth) {
         this.villagerTag = villagerTag;
         this.femaleTag = femaleTag;
         this.visualHeight = visualHeight;
         this.visualWidth = visualWidth;
      }

      private String villagerTag() {
         return this.villagerTag;
      }

      private String femaleTag() {
         return this.femaleTag;
      }

      private int visualHeight() {
         return this.visualHeight;
      }

      private int visualWidth() {
         return this.visualWidth;
      }
   }
}
