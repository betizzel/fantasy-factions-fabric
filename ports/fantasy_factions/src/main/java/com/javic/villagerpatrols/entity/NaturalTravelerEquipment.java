package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

public final class NaturalTravelerEquipment {
   private static final float WEAPON_CHANCE = 0.5F;
   private static final float STONE_WEAPON_CHANCE = 0.3F;
   private static final String TAG_NATURAL_TRAVELER_ARMED = "villagerpatrols_natural_traveler_armed";
   private static final String TAG_NATURAL_TRAVELER_WEAPON = "villagerpatrols_natural_traveler_weapon";

   private NaturalTravelerEquipment() {
   }

   public static void tryEquipNaturalTravelerWeapon(Mob mob, MobSpawnType reason) {
      if (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) {
         tryEquipLoneTravelerWeapon(mob);
      }
   }

   public static void tryEquipLoneTravelerWeapon(Mob mob) {
      if (!(mob.getRandom().nextFloat() >= 0.5F)) {
         boolean stone = mob.getRandom().nextFloat() < 0.3F;
         boolean axe = mob.getRandom().nextBoolean();
         ItemStack weapon;
         if (stone) {
            weapon = axe ? new ItemStack(Items.STONE_AXE) : new ItemStack(Items.STONE_SWORD);
         } else {
            weapon = axe ? new ItemStack(Items.WOODEN_AXE) : new ItemStack(Items.WOODEN_SWORD);
         }

         equipWeapon(mob, weapon);
      }
   }

   public static void ensureNaturalTravelerWeapon(Mob mob) {
      if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_natural_traveler_armed")) {
         if (hasValidWeapon(mob.getMainHandItem())) {
            rememberWeapon(mob, mob.getMainHandItem());
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
         } else {
            ItemStack weapon = getStoredWeapon(mob);
            if (!weapon.isEmpty()) {
               equipWeapon(mob, weapon);
            }
         }
      }
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
         PersistentEntityData.get(mob).remove("villagerpatrols_natural_traveler_armed");
         PersistentEntityData.get(mob).remove("villagerpatrols_natural_traveler_weapon");
      } else {
         ResourceLocation key = BuiltInRegistries.ITEM.getKey(weapon.getItem());
         PersistentEntityData.get(mob).putBoolean("villagerpatrols_natural_traveler_armed", true);
         PersistentEntityData.get(mob).putString("villagerpatrols_natural_traveler_weapon", key.toString());
      }
   }

   private static ItemStack getStoredWeapon(Mob mob) {
      String storedWeapon = PersistentEntityData.get(mob).getString("villagerpatrols_natural_traveler_weapon");
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
      return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
   }
}
