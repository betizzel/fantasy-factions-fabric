package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.HumanHorsemanEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MountedIllagerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "mounted_illager_world_event";
   public static final String RIDER_LEADER_ROLE = "mounted_illager_leader";
   public static final String RIDER_COMPANION_ROLE = "mounted_illager_companion";
   public static final String HORSE_LEADER_ROLE = "mounted_illager_horse_leader";
   public static final String HORSE_COMPANION_ROLE = "mounted_illager_horse_companion";
   private static final String DEFAULT_GROUP_ROLE = "mounted_illager_horse_leader";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 2;
   private static final int MAX_COUNT = 4;
   private static final int CLUSTER_RADIUS = 4;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 2;

   @Override
   String groupType() {
      return "mounted_illager_world_event";
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
      return 2;
   }

   @Override
   int maxCount() {
      return 4;
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
      return EntityType.HORSE;
   }

   @Override
   protected String groupRole() {
      return "mounted_illager_horse_leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 2, 4);
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();

      for (int index = 0; index < totalMembers; index++) {
         definitions.add(
            new AbstractWorldEvent.EntitySpawnDefinition(EntityType.HORSE, index == 0 ? "mounted_illager_horse_leader" : "mounted_illager_horse_companion")
         );
      }

      return definitions;
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof Horse horse) {
         HumanHorsemanEntity rider = (HumanHorsemanEntity)((EntityType)ModEntities.HUMAN_HORSEMAN.get()).create(level);
         if (rider != null) {
            String riderRole = "mounted_illager_horse_leader".equals(placement.role()) ? "mounted_illager_leader" : "mounted_illager_companion";
            String groupId = PersistentEntityData.get(horse).getString("villagerpatrols_group_id");
            horse.setTamed(true);
            horse.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.SADDLE));
            rider.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            rider.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), MobSpawnType.EVENT, null, null);
            PersistentEntityData.get(rider).putBoolean("villagerpatrols_event_entity", true);
            PersistentEntityData.get(rider).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
            PersistentEntityData.get(rider).putString("villagerpatrols_group_id", groupId);
            PersistentEntityData.get(rider).putString("villagerpatrols_group_type", "mounted_illager_world_event");
            PersistentEntityData.get(rider).putString("villagerpatrols_group_role", riderRole);
            rider.setupExternalMount(horse);
            PersistentEntityData.get(horse).putUUID("villagerpatrols_mounted_rider_uuid", rider.getUUID());
            PersistentEntityData.get(rider).putUUID("villagerpatrols_mounted_horse_uuid", horse.getUUID());
            if (level.addFreshEntity(rider)) {
               rider.startRiding(horse, true);
            }
         }
      }
   }
}
