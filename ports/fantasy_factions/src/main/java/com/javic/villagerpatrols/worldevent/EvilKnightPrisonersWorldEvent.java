package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.AbstractKnightEntity;
import com.javic.villagerpatrols.entity.EvilKnightLeaderEntity;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class EvilKnightPrisonersWorldEvent extends AbstractWorldEvent {
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   public static final String TAG_PRISONER_RESTRAINED = "villagerpatrols_prisoner_restrained";
   public static final String GROUP_TYPE = "evil_knight_prisoners_world_event";
   public static final String LEADER_ROLE = "evil_knight_prisoner_leader";
   public static final String GUARD_ROLE = "evil_knight_prisoner_guard";
   public static final String PRISONER_ROLE = "evil_knight_prisoner_prisoner";
   private static final int SELECTION_WEIGHT = 5;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 4;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 5;
   private static final float SECOND_PRISONER_CHANCE = 0.25F;

   @Override
   String groupType() {
      return "evil_knight_prisoners_world_event";
   }

   @Override
   int selectionWeight() {
      return 5;
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
      return 4;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 5;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return (EntityType<? extends Mob>)ModEntities.EVIL_KNIGHT_LEADER.get();
   }

   @Override
   protected String groupRole() {
      return "evil_knight_prisoner_leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof EvilKnightLeaderEntity leader) {
         String groupId = PersistentEntityData.get(leader).getString("villagerpatrols_group_id");
         leader.setGroupData(groupId, "evil_knight_prisoners_world_event", "evil_knight_prisoner_leader");
         leader.setSpawnedCompanions(true);
         List<Vec3> occupied = new ArrayList<>();
         occupied.add(Vec3.atBottomCenterOf(placement.pos()));
         this.spawnGuards(level, leader, groupId, occupied);
         this.spawnPrisoners(level, leader, groupId, occupied);
      }
   }

   private void spawnGuards(ServerLevel level, EvilKnightLeaderEntity leader, String groupId, List<Vec3> occupied) {
      int guardCount = 3 + level.random.nextInt(2);

      for (int index = 0; index < guardCount; index++) {
         AbstractKnightEntity guard = level.random.nextBoolean()
            ? (AbstractKnightEntity)((EntityType)ModEntities.EVIL_KNIGHT.get()).create(level)
            : (AbstractKnightEntity)((EntityType)ModEntities.EVIL_KNIGHT_SHIELD.get()).create(level);
         if (guard != null) {
            BlockPos spawnPos = this.findSpawnPos(level, leader.blockPosition(), occupied, 3, 6);
            if (spawnPos != null) {
               guard.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, leader.getYRot(), leader.getXRot());
               guard.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
               PersistentEntityData.get(guard).putBoolean("villagerpatrols_event_entity", true);
               PersistentEntityData.get(guard).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
               PersistentEntityData.get(guard).putString("villagerpatrols_group_id", groupId);
               PersistentEntityData.get(guard).putString("villagerpatrols_group_type", "evil_knight_prisoners_world_event");
               PersistentEntityData.get(guard).putString("villagerpatrols_group_role", "evil_knight_prisoner_guard");
               guard.setGroupData(groupId, "evil_knight_prisoners_world_event", "evil_knight_prisoner_guard");
               guard.setSpawnedCompanions(true);
               level.addFreshEntity(guard);
               occupied.add(Vec3.atBottomCenterOf(spawnPos));
            }
         }
      }
   }

   private void spawnPrisoners(ServerLevel level, EvilKnightLeaderEntity leader, String groupId, List<Vec3> occupied) {
      int prisonerCount = level.random.nextFloat() < 0.25F ? 2 : 1;

      for (int index = 0; index < prisonerCount; index++) {
         Mob prisoner = (Mob)SlimPatchCompat.resolveRandomVillagerType(level).create(level);
         if (prisoner != null) {
            BlockPos spawnPos = this.findSpawnPos(level, leader.blockPosition(), occupied, 1, 3);
            if (spawnPos != null) {
               prisoner.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, leader.getYRot(), leader.getXRot());
               prisoner.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
               PersistentEntityData.get(prisoner).putBoolean("villagerpatrols_event_entity", true);
               PersistentEntityData.get(prisoner).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
               PersistentEntityData.get(prisoner).putString("villagerpatrols_group_id", groupId);
               PersistentEntityData.get(prisoner).putString("villagerpatrols_group_type", "evil_knight_prisoners_world_event");
               PersistentEntityData.get(prisoner).putString("villagerpatrols_group_role", "evil_knight_prisoner_prisoner");
               PersistentEntityData.get(prisoner).putBoolean("villagerpatrols_prisoner_restrained", true);
               if (prisoner instanceof MaleVillagerEntity maleVillager) {
                  maleVillager.setGroupData(groupId, "evil_knight_prisoners_world_event", "evil_knight_prisoner_prisoner");
                  maleVillager.setPrisonerRestrained(true);
               } else if (prisoner instanceof FemaleVillagerEntity femaleVillager) {
                  femaleVillager.setGroupData(groupId, "evil_knight_prisoners_world_event", "evil_knight_prisoner_prisoner");
                  femaleVillager.setPrisonerRestrained(true);
               }

               level.addFreshEntity(prisoner);
               occupied.add(Vec3.atBottomCenterOf(spawnPos));
            }
         }
      }
   }

   private BlockPos findSpawnPos(ServerLevel level, BlockPos origin, List<Vec3> occupied, int minDistance, int maxDistance) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = level.random.nextDouble() * Math.PI * 2.0;
         int distance = minDistance + level.random.nextInt(maxDistance - minDistance + 1);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = origin.offset(offsetX, 0, offsetZ);
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
         if (this.isValidSpawnPos(level, surface, occupied)) {
            return surface;
         }
      }

      return null;
   }

   private boolean isValidSpawnPos(ServerLevel level, BlockPos pos, List<Vec3> occupied) {
      if (!level.isLoaded(pos) || !level.isLoaded(pos.above())) {
         return false;
      }

      if (!level.getBlockState(pos.below()).blocksMotion()) {
         return false;
      }

      if (level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()) {
         Vec3 spawnPos = Vec3.atBottomCenterOf(pos);

         for (Vec3 occupiedPos : occupied) {
            if (occupiedPos.distanceToSqr(spawnPos) < 2.25) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
