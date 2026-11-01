package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class HumanWitchWorldEvent extends AbstractWorldEvent {
   private static final String GROUP_TYPE = "human_witch_world_event";
   private static final String GROUP_ROLE = "witch";
   private static final int SELECTION_WEIGHT = 8;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 2;
   private static final int CLUSTER_RADIUS = 4;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 2;

   @Override
   String groupType() {
      return "human_witch_world_event";
   }

   @Override
   int selectionWeight() {
      return 8;
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
      return 2;
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
      return 2;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return (EntityType<? extends Mob>)ModEntities.HUMAN_WITCH.get();
   }

   @Override
   protected String groupRole() {
      return "witch";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseWitchWorldEventBiome(level, pos);
   }
}
