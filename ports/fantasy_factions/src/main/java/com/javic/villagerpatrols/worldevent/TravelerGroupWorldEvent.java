package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class TravelerGroupWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "traveler_group_world_event";
   public static final String LEADER_ROLE = "traveler_leader";
   public static final String COMPANION_ROLE = "traveler_companion";
   private static final String DEFAULT_GROUP_ROLE = "traveler_leader";
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
      return "traveler_group_world_event";
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
}
