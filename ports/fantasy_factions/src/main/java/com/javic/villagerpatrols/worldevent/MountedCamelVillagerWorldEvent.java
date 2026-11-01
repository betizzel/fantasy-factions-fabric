package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;

public final class MountedCamelVillagerWorldEvent extends AbstractWorldEvent {
   public static final String TAG_MOUNTED_SECOND_RIDER_UUID = "villagerpatrols_mounted_second_rider_uuid";
   public static final String GROUP_TYPE = "mounted_camel_villager_world_event";
   public static final String CAMEL_ROLE = "mounted_camel_villager_camel";
   public static final String DRIVER_ROLE = "mounted_camel_villager_driver";
   public static final String PASSENGER_ROLE = "mounted_camel_villager_passenger";
   private static final int SELECTION_WEIGHT = 10;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 2;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 5;
   private static final float DOUBLE_RIDER_CHANCE = 0.2F;

   @Override
   String groupType() {
      return "mounted_camel_villager_world_event";
   }

   @Override
   int selectionWeight() {
      return 10;
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
      return 5;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return EntityType.CAMEL;
   }

   @Override
   protected String groupRole() {
      return "mounted_camel_villager_camel";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseDesertWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(EntityType.CAMEL, "mounted_camel_villager_camel"));
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof Camel camel) {
         String groupId = PersistentEntityData.get(camel).getString("villagerpatrols_group_id");
         camel.setTamed(true);
         if (camel.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            camel.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.19);
         }

         camel.standUpInstantly();
         Mob driver = this.createRider(level);
         if (driver != null) {
            driver.moveTo(camel.getX(), camel.getY(), camel.getZ(), camel.getYRot(), camel.getXRot());
            driver.finalizeSpawn(level, level.getCurrentDifficultyAt(camel.blockPosition()), MobSpawnType.EVENT, null, null);
            this.applyRiderData(level, driver, groupId, "mounted_camel_villager_driver", camel.getUUID());
            PersistentEntityData.get(camel).putUUID("villagerpatrols_mounted_rider_uuid", driver.getUUID());
            if (level.addFreshEntity(driver)) {
               driver.startRiding(camel, true);
               if (!(level.random.nextFloat() >= 0.2F)) {
                  Mob passenger = this.createRider(level);
                  if (passenger != null) {
                     passenger.moveTo(camel.getX(), camel.getY(), camel.getZ(), camel.getYRot(), camel.getXRot());
                     passenger.finalizeSpawn(level, level.getCurrentDifficultyAt(camel.blockPosition()), MobSpawnType.EVENT, null, null);
                     this.applyRiderData(level, passenger, groupId, "mounted_camel_villager_passenger", camel.getUUID());
                     PersistentEntityData.get(camel).putUUID("villagerpatrols_mounted_second_rider_uuid", passenger.getUUID());
                     if (level.addFreshEntity(passenger)) {
                        passenger.startRiding(camel, true);
                     }
                  }
               }
            }
         }
      }
   }

   private Mob createRider(ServerLevel level) {
      EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
      return (Mob)type.create(level);
   }

   private void applyRiderData(ServerLevel level, Mob rider, String groupId, String riderRole, UUID camelUuid) {
      PersistentEntityData.get(rider).putBoolean("villagerpatrols_event_entity", true);
      PersistentEntityData.get(rider).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
      PersistentEntityData.get(rider).putString("villagerpatrols_group_id", groupId);
      PersistentEntityData.get(rider).putString("villagerpatrols_group_type", "mounted_camel_villager_world_event");
      PersistentEntityData.get(rider).putString("villagerpatrols_group_role", riderRole);
      PersistentEntityData.get(rider).putUUID("villagerpatrols_mounted_horse_uuid", camelUuid);
      EventVillagerSkins.applyDesertTravelerSkin(rider);
      if (rider instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setGroupData(groupId, "mounted_camel_villager_world_event", riderRole);
      } else if (rider instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setGroupData(groupId, "mounted_camel_villager_world_event", riderRole);
      }
   }
}
