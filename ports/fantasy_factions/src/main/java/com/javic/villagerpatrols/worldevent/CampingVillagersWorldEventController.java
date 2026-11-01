package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.CampingSeatEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CampingVillagersWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final double RETURN_DISTANCE_SQR = 25.0;
   private static final double WANDER_RADIUS = 3.0;
   private static final double ABSOLUTE_RADIUS = 5.0;
   private static final double SPEED = 0.4;
   private static final Map<UUID, CampingVillagersWorldEventController.CamperState> STATES = new HashMap<>();

   private CampingVillagersWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level && "camping_villagers_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_camping_bard") && !BardWorldEvent.hasStableBardLute(mob)) {
            CampingVillagersWorldEvent.applyCampingBardCompatibility(mob);
         }

         if (mob.getVehicle() instanceof CampingSeatEntity) {
            if (mob.getNavigation().isInProgress()) {
               mob.getNavigation().stop();
            }
         } else {
            if (mob.getTarget() != null) {
               mob.setTarget(null);
            }

            BlockPos campfirePos = getCampfirePos(mob);
            if (campfirePos != null) {
               Vec3 campfireCenter = Vec3.atBottomCenterOf(campfirePos);
               if (mob.distanceToSqr(campfireCenter) > 25.0) {
                  moveNearCampfire(level, mob, campfirePos, 1.5);
               } else if (PersistentEntityData.get(mob).getBoolean("villagerpatrols_camping_wanderer")) {
                  CampingVillagersWorldEventController.CamperState state = STATES.computeIfAbsent(
                     mob.getUUID(), unused -> new CampingVillagersWorldEventController.CamperState()
                  );
                  if (mob.tickCount >= state.nextMoveTick) {
                     if (mob.getNavigation().isDone()) {
                        moveNearCampfire(level, mob, campfirePos, 3.0);
                        state.nextMoveTick = mob.tickCount + 60 + mob.getRandom().nextInt(61);
                     }
                  }
               }
            }
         }
      }
   }

   public static void tryCleanupCampfire(ServerLevel level, Mob mob) {
      if ("camping_villagers_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         BlockPos campfirePos = getCampfirePos(mob);
         if (campfirePos != null && level.getBlockState(campfirePos).is(Blocks.CAMPFIRE)) {
            String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
            AABB searchBox = AABB.ofSize(Vec3.atCenterOf(campfirePos), 32.0, 16.0, 32.0);
            List<Mob> remainingCampers = level.getEntitiesOfClass(
               Mob.class,
               searchBox,
               candidate -> candidate != mob
                  && candidate.isAlive()
                  && !candidate.isRemoved()
                  && "camping_villagers_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
                  && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
            );
            if (remainingCampers.isEmpty()) {
               level.removeBlock(campfirePos, false);
               CampingVillagersWorldEvent.forgetCampfire(groupId);
            }
         }
      }
   }

   public static boolean shouldCleanupBrokenEvent(ServerLevel level, Mob mob) {
      if (mob != null && !mob.isRemoved()) {
         BlockPos campfirePos = getCampfirePos(mob);
         return campfirePos != null && level.isLoaded(campfirePos) ? false : false;
      } else {
         return false;
      }
   }

   private static void moveNearCampfire(ServerLevel level, Mob mob, BlockPos campfirePos, double radius) {
      for (int attempt = 0; attempt < 8; attempt++) {
         double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
         double distance = mob.getRandom().nextDouble() * radius;
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = campfirePos.offset(offsetX, 0, offsetZ);
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
         if (isValidCampArea(level, campfirePos, surface)) {
            mob.getNavigation().moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, 0.4);
            return;
         }
      }

      mob.getNavigation().moveTo(campfirePos.getX() + 0.5, campfirePos.getY(), campfirePos.getZ() + 0.5, 0.4);
   }

   private static boolean isValidCampArea(ServerLevel level, BlockPos campfirePos, BlockPos targetPos) {
      if (level.isLoaded(targetPos) && level.isLoaded(targetPos.above())) {
         if (targetPos.distSqr(campfirePos) > 25.0) {
            return false;
         } else {
            return !level.getBlockState(targetPos.below()).blocksMotion()
               ? false
               : level.getFluidState(targetPos).isEmpty() && level.getFluidState(targetPos.above()).isEmpty();
         }
      } else {
         return false;
      }
   }

   private static BlockPos getCampfirePos(Mob mob) {
      return PersistentEntityData.get(mob).contains("villagerpatrols_campfire_x")
            && PersistentEntityData.get(mob).contains("villagerpatrols_campfire_y")
            && PersistentEntityData.get(mob).contains("villagerpatrols_campfire_z")
         ? new BlockPos(
            PersistentEntityData.get(mob).getInt("villagerpatrols_campfire_x"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_campfire_y"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_campfire_z")
         )
         : null;
   }

   private static final class CamperState {
      private int nextMoveTick;
   }
}
