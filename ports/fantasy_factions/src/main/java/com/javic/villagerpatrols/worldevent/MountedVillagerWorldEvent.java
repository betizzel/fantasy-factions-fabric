package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;

public final class MountedVillagerWorldEvent extends AbstractWorldEvent {
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   public static final String TAG_MOUNTED_HORSE_UUID = "villagerpatrols_mounted_horse_uuid";
   public static final String TAG_MOUNTED_RIDER_UUID = "villagerpatrols_mounted_rider_uuid";
   public static final String GROUP_TYPE = "mounted_villager_world_event";
   public static final String RIDER_LEADER_ROLE = "mounted_villager_leader";
   public static final String RIDER_COMPANION_ROLE = "mounted_villager_companion";
   public static final String HORSE_LEADER_ROLE = "mounted_villager_horse_leader";
   public static final String HORSE_COMPANION_ROLE = "mounted_villager_horse_companion";
   private static final String DEFAULT_GROUP_ROLE = "mounted_villager_horse_leader";
   private static final int SELECTION_WEIGHT = 12;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 2;
   private static final int CLUSTER_RADIUS = 4;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "mounted_villager_world_event";
   }

   @Override
   int selectionWeight() {
      return 12;
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
      return 0;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return EntityType.HORSE;
   }

   @Override
   protected String groupRole() {
      return "mounted_villager_horse_leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 1, 2);
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();

      for (int index = 0; index < totalMembers; index++) {
         definitions.add(
            new AbstractWorldEvent.EntitySpawnDefinition(EntityType.HORSE, index == 0 ? "mounted_villager_horse_leader" : "mounted_villager_horse_companion")
         );
      }

      return definitions;
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof Horse horse) {
         Mob rider = this.createRider(level);
         if (rider != null) {
            String riderRole = "mounted_villager_horse_leader".equals(placement.role()) ? "mounted_villager_leader" : "mounted_villager_companion";
            String groupId = PersistentEntityData.get(horse).getString("villagerpatrols_group_id");
            horse.setTamed(true);
            rider.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            rider.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), MobSpawnType.EVENT, null, null);
            this.applyRiderData(level, rider, groupId, riderRole, horse.getUUID());
            PersistentEntityData.get(horse).putUUID("villagerpatrols_mounted_rider_uuid", rider.getUUID());
            if (level.addFreshEntity(rider)) {
               rider.startRiding(horse, true);
            }
         }
      }
   }

   private Mob createRider(ServerLevel level) {
      EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
      return (Mob)type.create(level);
   }

   private void applyRiderData(ServerLevel level, Mob rider, String groupId, String riderRole, UUID horseUuid) {
      PersistentEntityData.get(rider).putBoolean("villagerpatrols_event_entity", true);
      PersistentEntityData.get(rider).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
      PersistentEntityData.get(rider).putString("villagerpatrols_group_id", groupId);
      PersistentEntityData.get(rider).putString("villagerpatrols_group_type", "mounted_villager_world_event");
      PersistentEntityData.get(rider).putString("villagerpatrols_group_role", riderRole);
      PersistentEntityData.get(rider).putUUID("villagerpatrols_mounted_horse_uuid", horseUuid);
      if (rider instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setGroupData(groupId, "mounted_villager_world_event", riderRole);
      } else if (rider instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setGroupData(groupId, "mounted_villager_world_event", riderRole);
      }
   }
}
