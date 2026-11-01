package com.javic.villagerpatrols.worldevent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class ZombieHordeWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "zombie_horde_world_event";
   private static final String GROUP_ROLE = "zombie";
   private static final int SELECTION_WEIGHT = 5;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 6;
   private static final int MAX_COUNT = 10;
   private static final int CLUSTER_RADIUS = 6;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;

   @Override
   String groupType() {
      return "zombie_horde_world_event";
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
      return 6;
   }

   @Override
   int maxCount() {
      return 10;
   }

   @Override
   int clusterRadius() {
      return 6;
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
      return EntityType.ZOMBIE;
   }

   @Override
   protected String groupRole() {
      return "zombie";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected boolean canStart(ServerLevel level) {
      return level.isNight();
   }
}
