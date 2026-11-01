package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

public final class ArmedTravelerEquipment {
   private static final float STONE_SWORD_CHANCE = 0.7F;
   private static final float SHIELD_CHANCE = 0.25F;
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_WEAPON = "villagerpatrols_armed_traveler_weapon";
   private static final String TAG_SHIELD = "villagerpatrols_armed_traveler_shield";
   private static final String TAG_PERSISTENT_MAIN_HAND = "PersistentMainHand";

   private ArmedTravelerEquipment() {
   }

   public static boolean isSupported(Mob mob) {
      return PersistentEntityData.get(mob).getBoolean("villagerpatrols_event_entity")
         && "armed_traveler_group_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"));
   }

   public static void equipRandomLoadout(Mob mob) {
      ItemStack weapon = mob.getRandom().nextFloat() < 0.7F ? new ItemStack(Items.STONE_SWORD) : new ItemStack(Items.IRON_SWORD);
      equipLoadout(mob, weapon, mob.getRandom().nextFloat() < 0.25F);
   }

   public static void ensureEquipment(Mob mob) {
      if (isSupported(mob)) {
         if (hasValidWeapon(mob.getMainHandItem())) {
            rememberWeapon(mob, mob.getMainHandItem());
         } else {
            ItemStack storedWeapon = getStoredWeapon(mob);
            if (!storedWeapon.isEmpty()) {
               mob.setItemSlot(EquipmentSlot.MAINHAND, storedWeapon.copy());
               SlimPatchCompat.trySetPersistentMainHand(mob, storedWeapon);
            }
         }

         if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_armed_traveler_shield")) {
            if (!mob.getOffhandItem().is(Items.SHIELD)) {
               mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
         } else if (!mob.getOffhandItem().isEmpty()) {
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
         }

         mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
         mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
         PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
         PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      }
   }

   private static void equipLoadout(Mob mob, ItemStack weapon, boolean shield) {
      mob.setItemSlot(EquipmentSlot.MAINHAND, weapon.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, shield ? new ItemStack(Items.SHIELD) : ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      rememberWeapon(mob, weapon);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_armed_traveler_shield", shield);
      SlimPatchCompat.trySetPersistentMainHand(mob, weapon);
   }

   private static void rememberWeapon(Mob mob, ItemStack weapon) {
      if (!hasValidWeapon(weapon)) {
         PersistentEntityData.get(mob).remove("villagerpatrols_armed_traveler_weapon");
         PersistentEntityData.get(mob).remove("PersistentMainHand");
      } else {
         ResourceLocation key = BuiltInRegistries.ITEM.getKey(weapon.getItem());
         PersistentEntityData.get(mob).putString("villagerpatrols_armed_traveler_weapon", key.toString());
         CompoundTag persistentMainHand = weapon.copy().save(new CompoundTag());
         PersistentEntityData.get(mob).put("PersistentMainHand", persistentMainHand);
      }
   }

   private static ItemStack getStoredWeapon(Mob mob) {
      String storedWeapon = PersistentEntityData.get(mob).getString("villagerpatrols_armed_traveler_weapon");
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

   private static boolean hasValidWeapon(ItemStack stack) {
      return stack.getItem() instanceof SwordItem;
   }
}
