package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FishingVillagerWorldEvent extends AbstractWorldEvent {
   public static final String TAG_FISHING_ANCHOR_X = "villagerpatrols_fishing_anchor_x";
   public static final String TAG_FISHING_ANCHOR_Y = "villagerpatrols_fishing_anchor_y";
   public static final String TAG_FISHING_ANCHOR_Z = "villagerpatrols_fishing_anchor_z";
   public static final String GROUP_TYPE = "fishing_villager_world_event";
   public static final String GROUP_ROLE = "fisher";
   private static final int SELECTION_WEIGHT = 6;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 2;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;
   private static final int WATER_SEARCH_RADIUS = 7;
   private static final int WATER_VERTICAL_RANGE = 2;
   private static final int MIN_NEARBY_WATER_BLOCKS = 8;
   private static final int MAX_WATER_EDGE_DISTANCE = 3;

   @Override
   String groupType() {
      return "fishing_villager_world_event";
   }

   @Override
   int selectionWeight() {
      return 6;
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
      return 1;
   }

   @Override
   int clusterRadius() {
      return 2;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 1;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return SlimPatchCompat.resolveVillagerType(false);
   }

   @Override
   protected String groupRole() {
      return "fisher";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return true;
   }

   @Override
   protected boolean isExtraSpawnPositionValid(ServerLevel level, BlockPos surface) {
      return findNearbyWater(level, surface) != null;
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      EntityType<? extends Mob> type = SlimPatchCompat.resolveVillagerType(level.random.nextBoolean());
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(type, "fisher"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      BlockPos anchor = placement.pos();
      PersistentEntityData.get(entity).putInt("villagerpatrols_fishing_anchor_x", anchor.getX());
      PersistentEntityData.get(entity).putInt("villagerpatrols_fishing_anchor_y", anchor.getY());
      PersistentEntityData.get(entity).putInt("villagerpatrols_fishing_anchor_z", anchor.getZ());
      applyFishingVillagerCompatibility(entity);
      EventVillagerSkins.applyFishingVillagerSkin(entity);
      EventVillagerProfessions.apply(level, entity, VillagerProfession.FISHERMAN);
   }

   public static void applyFishingVillagerCompatibility(Mob mob) {
      ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
      mob.setItemSlot(EquipmentSlot.MAINHAND, fishingRod.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      CompoundTag persistentMainHand = fishingRod.copy().save(new CompoundTag());
      PersistentEntityData.get(mob).put("PersistentMainHand", persistentMainHand);
      SlimPatchCompat.trySetPersistentMainHand(mob, fishingRod);
   }

   public static BlockPos findNearbyWater(ServerLevel level, BlockPos origin) {
      BlockPos bestPos = null;
      double bestScore = Double.MAX_VALUE;
      int nearbyWaterBlocks = 0;

      for (int dy = -2; dy <= 2; dy++) {
         for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  BlockPos candidate = origin.offset(dx, dy, dz);
                  if (level.isLoaded(candidate) && Math.abs(candidate.getY() - origin.getY()) <= 2 && level.getFluidState(candidate).is(FluidTags.WATER)) {
                     nearbyWaterBlocks++;
                     int horizontalDistance = Math.max(Math.abs(candidate.getX() - origin.getX()), Math.abs(candidate.getZ() - origin.getZ()));
                     if (horizontalDistance <= 3) {
                        double distance = candidate.distSqr(origin);
                        if (level.getBlockState(candidate.above()).isAir() && distance < bestScore) {
                           bestScore = distance;
                           bestPos = candidate;
                        }
                     }
                  }
               }
            }
         }
      }

      return nearbyWaterBlocks < 8 ? null : bestPos;
   }
}
