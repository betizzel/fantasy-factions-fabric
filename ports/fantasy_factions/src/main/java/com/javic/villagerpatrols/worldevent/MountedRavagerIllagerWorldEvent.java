package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.HumanRavagerRiderEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Ravager;

public final class MountedRavagerIllagerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "mounted_ravager_illager_world_event";
   private static final String LEADER_ROLE = "leader";
   private static final String COMPANION_ROLE = "companion";
   private static final String RAVAGER_ROLE = "ravager";
   private static final int SELECTION_WEIGHT = 2;
   private static final int MIN_PLAYER_DISTANCE = 28;
   private static final int MAX_PLAYER_DISTANCE = 60;
   private static final int CLUSTER_RADIUS = 7;
   private static final int PROXIMITY_RADIUS = 176;
   private static final int VILLAGE_RADIUS_SECTIONS = 2;

   @Override
   String groupType() {
      return "mounted_ravager_illager_world_event";
   }

   @Override
   int selectionWeight() {
      return 2;
   }

   @Override
   int minPlayerDistance() {
      return 28;
   }

   @Override
   int maxPlayerDistance() {
      return 60;
   }

   @Override
   int minCount() {
      return 3;
   }

   @Override
   int maxCount() {
      return 3;
   }

   @Override
   int clusterRadius() {
      return 7;
   }

   @Override
   int proximityRadius() {
      return 176;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 2;
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return EntityType.RAVAGER;
   }

   @Override
   protected String groupRole() {
      return "ravager";
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(
         new AbstractWorldEvent.EntitySpawnDefinition(EntityType.RAVAGER, "ravager"),
         new AbstractWorldEvent.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.HUMAN_SWORDSMAN.get(), "companion"),
         new AbstractWorldEvent.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.HUMAN_SWORDSMAN.get(), "companion")
      );
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof Ravager ravager) {
         HumanRavagerRiderEntity rider = (HumanRavagerRiderEntity)((EntityType)ModEntities.HUMAN_RAVAGER_RIDER.get()).create(level);
         if (rider != null) {
            String groupId = PersistentEntityData.get(ravager).getString("villagerpatrols_group_id");
            rider.moveTo(ravager.getX(), ravager.getY(), ravager.getZ(), ravager.getYRot(), ravager.getXRot());
            rider.finalizeSpawn(level, level.getCurrentDifficultyAt(ravager.blockPosition()), MobSpawnType.EVENT, null, null);
            PersistentEntityData.get(rider).putBoolean("villagerpatrols_event_entity", true);
            PersistentEntityData.get(rider).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
            PersistentEntityData.get(rider).putString("villagerpatrols_group_id", groupId);
            PersistentEntityData.get(rider).putString("villagerpatrols_group_type", "mounted_ravager_illager_world_event");
            PersistentEntityData.get(rider).putString("villagerpatrols_group_role", "leader");
            rider.setGroupData(groupId, "mounted_ravager_illager_world_event", "leader");
            rider.setSpawnedCompanions(true);
            rider.setupExternalMount(ravager);
            if (level.addFreshEntity(rider)) {
               rider.startRiding(ravager, true);
            }
         }
      }
   }
}
