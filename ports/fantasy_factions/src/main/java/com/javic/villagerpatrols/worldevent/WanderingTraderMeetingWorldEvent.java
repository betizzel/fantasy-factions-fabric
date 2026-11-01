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
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class WanderingTraderMeetingWorldEvent extends AbstractWorldEvent {
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   public static final String TAG_MEETING_TRADER_UUID = "villagerpatrols_meeting_trader_uuid";
   public static final String TAG_MEETING_ANCHOR_X = "villagerpatrols_meeting_anchor_x";
   public static final String TAG_MEETING_ANCHOR_Y = "villagerpatrols_meeting_anchor_y";
   public static final String TAG_MEETING_ANCHOR_Z = "villagerpatrols_meeting_anchor_z";
   public static final String GROUP_TYPE = "wandering_trader_meeting_world_event";
   public static final String TRADER_ROLE = "wandering_trader_meeting_trader";
   public static final String VILLAGER_ROLE = "wandering_trader_meeting_villager";
   private static final int SELECTION_WEIGHT = 5;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int MIN_VILLAGERS = 1;
   private static final int MAX_VILLAGERS = 3;
   private static final int CLUSTER_RADIUS = 2;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "wandering_trader_meeting_world_event";
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
      return 0;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return EntityType.WANDERING_TRADER;
   }

   @Override
   protected String groupRole() {
      return "wandering_trader_meeting_trader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonWorldEventBiome(level, pos);
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if (entity instanceof WanderingTrader trader) {
         String groupId = PersistentEntityData.get(trader).getString("villagerpatrols_group_id");
         BlockPos anchor = placement.pos();
         this.setAnchor(entity, anchor);
         int villagerCount = this.randomBetween(level, 1, 3);
         List<Vec3> occupied = new ArrayList<>();
         occupied.add(Vec3.atBottomCenterOf(anchor));

         for (int index = 0; index < villagerCount; index++) {
            Mob villager = this.createVillager(level);
            if (villager != null) {
               BlockPos villagerPos = this.findVillagerPos(level, anchor, occupied);
               if (villagerPos != null) {
                  villager.moveTo(villagerPos.getX() + 0.5, villagerPos.getY(), villagerPos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                  villager.finalizeSpawn(level, level.getCurrentDifficultyAt(villagerPos), MobSpawnType.EVENT, null, null);
                  this.applyVillagerData(level, villager, groupId, trader.getUUID(), villagerPos);
                  level.addFreshEntity(villager);
                  occupied.add(Vec3.atBottomCenterOf(villagerPos));
               }
            }
         }
      }
   }

   private Mob createVillager(ServerLevel level) {
      EntityType<? extends Mob> type = SlimPatchCompat.resolveRandomVillagerType(level);
      return (Mob)type.create(level);
   }

   private BlockPos findVillagerPos(ServerLevel level, BlockPos traderPos, List<Vec3> occupied) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = level.random.nextDouble() * Math.PI * 2.0;
         int distance = 2 + level.random.nextInt(3);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = traderPos.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (this.isValidMeetingPos(level, surface, occupied)) {
               return surface;
            }
         }
      }

      return null;
   }

   private boolean isValidMeetingPos(ServerLevel level, BlockPos pos, List<Vec3> occupied) {
      if (!level.isLoaded(pos) || !level.isLoaded(pos.above())) {
         return false;
      }

      if (!level.getBlockState(pos.below()).blocksMotion()) {
         return false;
      }

      if (level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()) {
         Vec3 spawnPos = Vec3.atBottomCenterOf(pos);

         for (Vec3 occupiedPos : occupied) {
            if (occupiedPos.distanceToSqr(spawnPos) < 2.25) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void applyVillagerData(ServerLevel level, Mob villager, String groupId, UUID traderUuid, BlockPos anchor) {
      PersistentEntityData.get(villager).putBoolean("villagerpatrols_event_entity", true);
      PersistentEntityData.get(villager).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
      PersistentEntityData.get(villager).putString("villagerpatrols_group_id", groupId);
      PersistentEntityData.get(villager).putString("villagerpatrols_group_type", "wandering_trader_meeting_world_event");
      PersistentEntityData.get(villager).putString("villagerpatrols_group_role", "wandering_trader_meeting_villager");
      PersistentEntityData.get(villager).putUUID("villagerpatrols_meeting_trader_uuid", traderUuid);
      this.setAnchor(villager, anchor);
      if (villager instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setGroupData(groupId, "wandering_trader_meeting_world_event", "wandering_trader_meeting_villager");
      } else if (villager instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setGroupData(groupId, "wandering_trader_meeting_world_event", "wandering_trader_meeting_villager");
      }
   }

   private void setAnchor(Mob mob, BlockPos anchor) {
      PersistentEntityData.get(mob).putInt("villagerpatrols_meeting_anchor_x", anchor.getX());
      PersistentEntityData.get(mob).putInt("villagerpatrols_meeting_anchor_y", anchor.getY());
      PersistentEntityData.get(mob).putInt("villagerpatrols_meeting_anchor_z", anchor.getZ());
   }
}
