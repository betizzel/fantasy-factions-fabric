package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.ArmedTravelerEquipment;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ArmedTravelerGroupWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "armed_traveler_group_world_event";
   private static final double ADVENTURER_MAX_HEALTH = 30.0;
   private static final float PROFESSION_CHANCE = 0.5F;
   private static final int SELECTION_WEIGHT = 15;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 4;
   private static final int CLUSTER_RADIUS = 3;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "armed_traveler_group_world_event";
   }

   @Override
   int selectionWeight() {
      return 15;
   }

   @Override
   int minPlayerDistance() {
      return 24;
   }

   @Override
   int maxPlayerDistance() {
      return 56;
   }

   @Override
   int minCount() {
      return 1;
   }

   @Override
   int maxCount() {
      return 4;
   }

   @Override
   int clusterRadius() {
      return 3;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 0;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return (EntityType<? extends Mob>)ModEntities.MALE_VILLAGER.get();
   }

   @Override
   protected String groupRole() {
      return "traveler_leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 1, 4);
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();

      for (int index = 0; index < totalMembers; index++) {
         EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
         String role = index == 0 ? "traveler_leader" : "traveler_companion";
         definitions.add(new AbstractWorldEvent.EntitySpawnDefinition(type, role));
      }

      return definitions;
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      ArmedTravelerEquipment.equipRandomLoadout(entity);
      applyCombatStats(entity);
      applyArmorRoll(entity);
      EventVillagerSkins.applyArmedTravelerSkin(entity);
      EventVillagerProfessions.applyRandom(level, entity, 0.5F, VillagerProfession.LEATHERWORKER, VillagerProfession.FLETCHER, VillagerProfession.ARMORER);
   }

   private static void applyCombatStats(Mob entity) {
      if (entity.getAttribute(Attributes.MAX_HEALTH) != null) {
         entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0);
         entity.setHealth(entity.getMaxHealth());
      }
   }

   private static void applyArmorRoll(Mob entity) {
      entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      entity.setDropChance(EquipmentSlot.HEAD, 0.0F);
      float roll = entity.getRandom().nextFloat();
      if (roll < 0.5F) {
         clearArmor(entity);
      } else if (roll < 0.75F) {
         equipChestOnly(entity, Items.IRON_CHESTPLATE);
      } else if (roll < 0.9F) {
         equipFullArmor(entity, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
      } else if (roll < 0.97F) {
         equipChestOnly(entity, Items.DIAMOND_CHESTPLATE);
      } else {
         equipFullArmor(entity, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
      }
   }

   private static void clearArmor(Mob entity) {
      entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
      entity.setDropChance(EquipmentSlot.CHEST, 0.0F);
      entity.setDropChance(EquipmentSlot.LEGS, 0.0F);
      entity.setDropChance(EquipmentSlot.FEET, 0.0F);
   }

   private static void equipChestOnly(Mob entity, Item chestplate) {
      entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplate));
      entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
      entity.setDropChance(EquipmentSlot.CHEST, 0.0F);
      entity.setDropChance(EquipmentSlot.LEGS, 0.0F);
      entity.setDropChance(EquipmentSlot.FEET, 0.0F);
   }

   private static void equipFullArmor(Mob entity, Item chestplate, Item leggings, Item boots) {
      entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplate));
      entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(leggings));
      entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
      entity.setDropChance(EquipmentSlot.CHEST, 0.0F);
      entity.setDropChance(EquipmentSlot.LEGS, 0.0F);
      entity.setDropChance(EquipmentSlot.FEET, 0.0F);
   }
}
