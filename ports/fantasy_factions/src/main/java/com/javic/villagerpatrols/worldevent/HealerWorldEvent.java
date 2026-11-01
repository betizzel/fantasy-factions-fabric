package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.HealerEntitySupport;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class HealerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "healer_world_event";
   public static final String MALE_ROLE = "healer_male";
   public static final String FEMALE_ROLE = "healer_female";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 1;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "healer_world_event";
   }

   @Override
   int selectionWeight() {
      return 4;
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
      return 1;
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
      return (EntityType<? extends Mob>)ModEntities.MALE_HEALER.get();
   }

   @Override
   protected String groupRole() {
      return "healer_male";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseBardWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      boolean female = level.random.nextBoolean();
      return List.of(
         new AbstractWorldEvent.EntitySpawnDefinition(HealerEntitySupport.resolveFriendlyType(level, female), female ? "healer_female" : "healer_male")
      );
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      HealerEntitySupport.initializeFriendlySpawn(entity, "healer_female".equals(placement.role()), true);
   }
}
