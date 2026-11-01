package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class TravelerFamilyWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "traveler_family_world_event";
   private static final String DEFAULT_GROUP_ROLE = "traveler_leader";
   private static final int SELECTION_WEIGHT = 20;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 2;
   private static final int MAX_COUNT = 2;
   private static final int CLUSTER_RADIUS = 3;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;

   @Override
   String groupType() {
      return "traveler_family_world_event";
   }

   @Override
   int selectionWeight() {
      return 20;
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
      return 2;
   }

   @Override
   int maxCount() {
      return 2;
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
      return 1;
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
      EntityType<? extends Mob> maleType = SlimPatchCompat.resolveMaleVillagerType();
      EntityType<? extends Mob> femaleType = SlimPatchCompat.resolveFemaleVillagerType();
      boolean maleLeads = level.random.nextBoolean();
      return maleLeads
         ? List.of(
            new AbstractWorldEvent.EntitySpawnDefinition(maleType, "traveler_leader"),
            new AbstractWorldEvent.EntitySpawnDefinition(femaleType, "traveler_companion")
         )
         : List.of(
            new AbstractWorldEvent.EntitySpawnDefinition(femaleType, "traveler_leader"),
            new AbstractWorldEvent.EntitySpawnDefinition(maleType, "traveler_companion")
         );
   }
}
