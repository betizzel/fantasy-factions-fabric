package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.entity.NaturalTravelerEquipment;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class LoneIceTravelerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "lone_ice_traveler_world_event";
   public static final String GROUP_ROLE = "traveler";
   public static final String WOLF_ROLE = "wolf_companion";
   private static final int SELECTION_WEIGHT = 15;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 1;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 4;
   private static final float WOLF_CHANCE = 0.6F;

   @Override
   String groupType() {
      return "lone_ice_traveler_world_event";
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
      return 4;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return (EntityType<? extends Mob>)ModEntities.MALE_VILLAGER.get();
   }

   @Override
   protected String groupRole() {
      return "traveler";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseLoneIceTravelerWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
      return level.random.nextFloat() < 0.6F
         ? List.of(
            new AbstractWorldEvent.EntitySpawnDefinition(type, "traveler"), new AbstractWorldEvent.EntitySpawnDefinition(EntityType.WOLF, "wolf_companion")
         )
         : List.of(new AbstractWorldEvent.EntitySpawnDefinition(type, "traveler"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      if ("traveler".equals(placement.role())) {
         EventVillagerSkins.applyIceTravelerSkin(entity);
         EventVillagerProfessions.apply(level, entity, VillagerProfession.BUTCHER);
      }
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if ("traveler".equals(placement.role()) && (entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity)) {
         NaturalTravelerEquipment.tryEquipLoneTravelerWeapon(entity);
      }
   }
}
