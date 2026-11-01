package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

public final class VillagerIllagerSkirmishEquipment {
   public static final String GROUP_TYPE = "villager_illager_skirmish";
   public static final String ZOMBIE_GROUP_TYPE = "villager_zombie_skirmish";
   public static final String SPIDER_GROUP_TYPE = "villager_spider_skirmish";
   public static final String ORC_GROUP_TYPE = "villager_orc_skirmish";
   public static final String BANDIT_GROUP_TYPE = "villager_bandit_skirmish";
   private static final String GROUP_ROLE = "villager_fighter";
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_WEAPON = "villagerpatrols_skirmish_weapon";
   private static final String TAG_PERSISTENT_MAIN_HAND = "PersistentMainHand";
   private static final String TAG_SLIMPATCH_COMBAT_MODE = "SlimPatchCombatMode";
   private static final String TAG_MANUAL_MAINHAND_OVERRIDE = "villagerpatrols_manual_mainhand_override";

   private VillagerIllagerSkirmishEquipment() {
   }

   public static boolean isSkirmishFighter(Mob mob) {
      return isSupportedSkirmishFighter(mob) && hasValidWeapon(mob.getMainHandItem());
   }

   public static boolean isSupportedSkirmishFighter(Mob mob) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_event_entity")
         && isSupportedGroupType(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
         && "villager_fighter".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"));
   }

   public static boolean isSkirmishFighter(Mob mob, String groupType) {
      return isSupportedSkirmishFighter(mob, groupType) && hasValidWeapon(mob.getMainHandItem());
   }

   public static boolean isSupportedSkirmishFighter(Mob mob, String groupType) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_event_entity")
         && groupType.equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
         && "villager_fighter".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"));
   }

   public static void ensureSkirmishWeapon(Mob mob) {
      if (isSupportedSkirmishFighter(mob)) {
         PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "AGGRESSIVE");
         SlimPatchCompat.trySetCombatMode(mob, "AGGRESSIVE");
         if (!PersistentEntityData.get(mob).getBoolean("villagerpatrols_manual_mainhand_override")) {
            if (hasValidWeapon(mob.getMainHandItem())) {
               rememberWeapon(mob, mob.getMainHandItem());
               mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
               mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            } else {
               ItemStack weapon = getStoredWeapon(mob);
               if (weapon.isEmpty()) {
                  equipRandomWeapon(mob);
               } else {
                  equipWeapon(mob, weapon);
               }
            }
         }
      }
   }

   public static void equipRandomWeapon(Mob mob) {
      float roll = mob.getRandom().nextFloat();
      ItemStack weapon;
      if (roll < 0.15F) {
         weapon = new ItemStack(Items.STONE_AXE);
      } else if (roll < 0.3F) {
         weapon = new ItemStack(Items.STONE_SWORD);
      } else if (roll < 0.65F) {
         weapon = new ItemStack(Items.WOODEN_AXE);
      } else {
         weapon = new ItemStack(Items.WOODEN_SWORD);
      }

      equipWeapon(mob, weapon);
   }

   private static boolean hasValidWeapon(ItemStack stack) {
      return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
   }

   private static boolean isSupportedGroupType(String groupType) {
      return "villager_illager_skirmish".equals(groupType)
         || "villager_zombie_skirmish".equals(groupType)
         || "villager_spider_skirmish".equals(groupType)
         || "villager_orc_skirmish".equals(groupType)
         || "villager_bandit_skirmish".equals(groupType);
   }

   private static void equipWeapon(Mob mob, ItemStack weapon) {
      mob.setItemSlot(EquipmentSlot.MAINHAND, weapon.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      rememberWeapon(mob, weapon);
   }

   private static void rememberWeapon(Mob mob, ItemStack weapon) {
      if (!hasValidWeapon(weapon)) {
         PersistentEntityData.get(mob).remove("villagerpatrols_skirmish_weapon");
         PersistentEntityData.get(mob).remove("PersistentMainHand");
      } else {
         ResourceLocation key = BuiltInRegistries.ITEM.getKey(weapon.getItem());
         PersistentEntityData.get(mob).putString("villagerpatrols_skirmish_weapon", key.toString());
         PersistentEntityData.get(mob).put("PersistentMainHand", weapon.copy().save(new CompoundTag()));
         SlimPatchCompat.trySetPersistentMainHand(mob, weapon);
      }
   }

   private static ItemStack getStoredWeapon(Mob mob) {
      String storedWeapon = PersistentEntityData.get(mob).getString("villagerpatrols_skirmish_weapon");
      if (storedWeapon.isEmpty()) {
         return ItemStack.EMPTY;
      }

      ResourceLocation key = ResourceLocation.tryParse(storedWeapon);
      if (key == null) {
         return ItemStack.EMPTY;
      }

      Item item = (Item)BuiltInRegistries.ITEM.get(key);
      if (item == Items.AIR) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = new ItemStack(item);
      return hasValidWeapon(stack) ? stack : ItemStack.EMPTY;
   }
}
