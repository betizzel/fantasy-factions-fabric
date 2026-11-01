package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class BardWorldEventController {
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_NEXT_MOVE_TICK = "villagerpatrols_bard_next_move_tick";
   private static final double RETURN_DISTANCE_SQR = 16.0;
   private static final double WALK_RADIUS = 4.0;
   private static final double MIN_WALK_DISTANCE_SQR = 2.25;
   private static final double STATIONARY_SPEED = 0.35;
   private static final double WALK_SPEED = 0.22;
   private static final int MIN_WAIT_TICKS = 60;
   private static final int MOVE_DELAY_VARIATION_TICKS = 61;

   private BardWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "bard_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
         && "bard".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))) {
         ensureLute(mob);
         BlockPos anchor = getAnchor(mob);
         if (anchor != null && level.isLoaded(anchor)) {
            if (mob.getTarget() != null) {
               mob.setTarget(null);
            }

            Vec3 anchorCenter = Vec3.atBottomCenterOf(anchor);
            if (mob.distanceToSqr(anchorCenter) > 16.0) {
               mob.getNavigation().moveTo(anchorCenter.x, anchorCenter.y, anchorCenter.z, 0.35);
            } else if (!PersistentEntityData.get(mob).getBoolean("villagerpatrols_bard_walking")) {
               mob.getNavigation().stop();
            } else if (mob.tickCount < PersistentEntityData.get(mob).getInt("villagerpatrols_bard_next_move_tick")) {
               if (mob.getNavigation().isDone()) {
                  mob.getNavigation().stop();
               }
            } else if (mob.getNavigation().isDone()) {
               moveNearAnchor(level, mob, anchor);
               PersistentEntityData.get(mob).putInt("villagerpatrols_bard_next_move_tick", mob.tickCount + 60 + mob.getRandom().nextInt(61));
            }
         } else {
            mob.discard();
         }
      }
   }

   private static void ensureLute(Mob mob) {
      if (!BardWorldEvent.hasStableBardLute(mob)) {
         BardWorldEvent.applyBardCompatibility(mob);
      }
   }

   private static void moveNearAnchor(ServerLevel level, Mob mob, BlockPos anchor) {
      for (int attempt = 0; attempt < 8; attempt++) {
         double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
         double distance = 1.5 + mob.getRandom().nextDouble() * 2.5;
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = anchor.offset(offsetX, 0, offsetZ);
         BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
         if (isValidWalkTarget(level, anchor, surface) && !(mob.distanceToSqr(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5) < 2.25)) {
            mob.getNavigation().moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, 0.22);
            return;
         }
      }

      mob.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 0.22);
   }

   private static boolean isValidWalkTarget(ServerLevel level, BlockPos anchor, BlockPos targetPos) {
      if (level.isLoaded(targetPos) && level.isLoaded(targetPos.above())) {
         if (targetPos.distSqr(anchor) > 25.0) {
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

   private static BlockPos getAnchor(Mob mob) {
      return PersistentEntityData.get(mob).contains("villagerpatrols_bard_anchor_x")
            && PersistentEntityData.get(mob).contains("villagerpatrols_bard_anchor_y")
            && PersistentEntityData.get(mob).contains("villagerpatrols_bard_anchor_z")
         ? new BlockPos(
            PersistentEntityData.get(mob).getInt("villagerpatrols_bard_anchor_x"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_bard_anchor_y"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_bard_anchor_z")
         )
         : null;
   }
}
