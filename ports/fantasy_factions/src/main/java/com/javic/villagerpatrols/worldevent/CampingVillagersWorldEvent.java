package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.CampingSeatEntity;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class CampingVillagersWorldEvent extends AbstractWorldEvent {
   private static final float PROFESSION_CHANCE = 0.5F;
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   public static final String TAG_CAMPFIRE_X = "villagerpatrols_campfire_x";
   public static final String TAG_CAMPFIRE_Y = "villagerpatrols_campfire_y";
   public static final String TAG_CAMPFIRE_Z = "villagerpatrols_campfire_z";
   public static final String TAG_CAMPING_WANDERER = "villagerpatrols_camping_wanderer";
   public static final String TAG_CAMPING_BARD = "villagerpatrols_camping_bard";
   public static final String GROUP_TYPE = "camping_villagers_world_event";
   public static final String CAMPER_ROLE = "camping_villager_camper";
   private static final int SELECTION_WEIGHT = 15;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 4;
   private static final int CLUSTER_RADIUS = 3;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 3;
   private static final float WANDERER_CHANCE = 0.5F;
   private static final float BARD_CHANCE = 0.25F;
   private static final float SITTING_CHANCE = 0.65F;
   private static final Map<String, BlockPos> CAMPFIRES = new HashMap<>();
   private static final Map<String, Set<BlockPos>> SEATS = new HashMap<>();
   private static final int[][] SEAT_OFFSETS = new int[][]{
      {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {2, 0}, {-2, 0}, {0, 2}, {0, -2}
   };

   @Override
   String groupType() {
      return "camping_villagers_world_event";
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
      return 3;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return (EntityType<? extends Mob>)ModEntities.MALE_VILLAGER.get();
   }

   @Override
   protected String groupRole() {
      return "camping_villager_camper";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 1, 4);
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();
      int bardIndex = totalMembers > 0 && level.random.nextFloat() < 0.25F ? level.random.nextInt(totalMembers) : -1;

      for (int index = 0; index < totalMembers; index++) {
         EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
         String role = index == bardIndex ? "bard" : "camping_villager_camper";
         definitions.add(new AbstractWorldEvent.EntitySpawnDefinition(type, role));
      }

      return definitions;
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      if ("bard".equals(placement.role())) {
         applyCampingBardCompatibility(entity);
         EventVillagerSkins.applyBardSkin(entity);
      } else {
         EventVillagerProfessions.applyRandom(level, entity, 0.5F, VillagerProfession.FARMER, VillagerProfession.CARTOGRAPHER);
      }
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      String groupId = PersistentEntityData.get(entity).getString("villagerpatrols_group_id");
      BlockPos campfirePos = CAMPFIRES.computeIfAbsent(groupId, unused -> this.findCampfirePos(level, placement.pos()));
      if (campfirePos != null && level.getBlockState(campfirePos).canBeReplaced()) {
         level.setBlock(campfirePos, Blocks.CAMPFIRE.defaultBlockState(), 3);
      }

      if (campfirePos != null) {
         PersistentEntityData.get(entity).putInt("villagerpatrols_campfire_x", campfirePos.getX());
         PersistentEntityData.get(entity).putInt("villagerpatrols_campfire_y", campfirePos.getY());
         PersistentEntityData.get(entity).putInt("villagerpatrols_campfire_z", campfirePos.getZ());
      }

      boolean isBard = "bard".equals(placement.role());
      PersistentEntityData.get(entity).putBoolean("villagerpatrols_camping_bard", isBard);
      PersistentEntityData.get(entity).putBoolean("villagerpatrols_camping_wanderer", level.random.nextFloat() < 0.5F);
      if (!isBard) {
         EventVillagerSkins.applyDefaultStandaloneVillagerSkin(entity);
      }

      if (entity instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setGroupData(groupId, "camping_villagers_world_event", "camping_villager_camper");
      } else if (entity instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setGroupData(groupId, "camping_villagers_world_event", "camping_villager_camper");
      }

      this.trySeatCamper(level, entity, groupId, campfirePos);
   }

   public static void applyCampingBardCompatibility(Mob mob) {
      BardWorldEvent.applyBardCompatibility(mob);
      PersistentEntityData.get(mob).putBoolean("villagerpatrols_camping_bard", true);
   }

   public static void forgetCampfire(String groupId) {
      CAMPFIRES.remove(groupId);
      SEATS.remove(groupId);
   }

   private BlockPos findCampfirePos(ServerLevel level, BlockPos around) {
      int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {2, 0}, {-2, 0}, {0, 2}, {0, -2}};

      for (int[] offset : offsets) {
         BlockPos candidate = around.offset(offset[0], 0, offset[1]);
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, candidate);
         if (this.isCampfirePositionValid(level, surface)) {
            return surface;
         }
      }

      return null;
   }

   private boolean isCampfirePositionValid(ServerLevel level, BlockPos pos) {
      if (!level.isLoaded(pos) || !level.isLoaded(pos.above())) {
         return false;
      } else if (!level.getBlockState(pos).canBeReplaced()) {
         return false;
      } else if (!level.getBlockState(pos.above()).canBeReplaced()) {
         return false;
      } else {
         return !level.getBlockState(pos.below()).blocksMotion() ? false : level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty();
      }
   }

   private void trySeatCamper(ServerLevel level, Mob entity, String groupId, BlockPos campfirePos) {
      if (campfirePos != null && !entity.isPassenger() && !(entity.getRandom().nextFloat() >= 0.65F)) {
         if (level.getFluidState(entity.blockPosition()).isEmpty() && level.getFluidState(entity.blockPosition().above()).isEmpty()) {
            BlockPos seatPos = this.findSeatPos(level, campfirePos, groupId);
            if (seatPos != null) {
               CampingSeatEntity seat = (CampingSeatEntity)((EntityType)ModEntities.CAMPING_SEAT.get()).create(level);
               if (seat != null) {
                  seat.moveTo(seatPos.getX() + 0.5, seatPos.getY(), seatPos.getZ() + 0.5, entity.getYRot(), 0.0F);
                  if (level.addFreshEntity(seat)) {
                     if (!entity.startRiding(seat, true)) {
                        seat.discard();
                     } else {
                        SEATS.computeIfAbsent(groupId, unused -> new HashSet<>()).add(seatPos.immutable());
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos findSeatPos(ServerLevel level, BlockPos campfirePos, String groupId) {
      Set<BlockPos> occupiedSeats = SEATS.computeIfAbsent(groupId, unused -> new HashSet<>());

      for (int[] offset : SEAT_OFFSETS) {
         BlockPos candidate = campfirePos.offset(offset[0], 0, offset[1]);
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, candidate);
         if (!occupiedSeats.contains(surface) && this.isSeatPositionValid(level, campfirePos, surface)) {
            return surface;
         }
      }

      return null;
   }

   private boolean isSeatPositionValid(ServerLevel level, BlockPos campfirePos, BlockPos pos) {
      if (level.isLoaded(pos) && level.isLoaded(pos.above())) {
         if (pos.equals(campfirePos) || pos.distSqr(campfirePos) > 9.0) {
            return false;
         }

         if (!level.getBlockState(pos.below()).blocksMotion()) {
            return false;
         }

         if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
         }

         if (!level.getBlockState(pos).blocksMotion() && !level.getBlockState(pos.above()).blocksMotion()) {
            CampingSeatEntity seat = (CampingSeatEntity)((EntityType)ModEntities.CAMPING_SEAT.get()).create(level);
            if (seat == null) {
               return false;
            }

            seat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            return level.noCollision(seat);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
