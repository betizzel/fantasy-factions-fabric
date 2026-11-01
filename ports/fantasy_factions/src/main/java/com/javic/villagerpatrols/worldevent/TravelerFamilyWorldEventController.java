package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TravelerFamilyWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 48.0;
   private static final double FORMATION_DISTANCE = 1.2;
   private static final double COMFORT_RADIUS_SQR = 0.81;
   private static final double FAR_DISTANCE_SQR = 100.0;
   private static final double FOLLOW_SPEED = 0.45;
   private static final double CATCH_UP_SPEED = 0.55;
   private static final double SOFT_ADVANCE_DISTANCE = 0.75;
   private static final double LEADER_MOVING_DISTANCE_SQR = 0.01;

   private TravelerFamilyWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level && "traveler_family_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         SlimPatchCompat.tryInitializeTravelerFamily(level, mob);
         if (SlimPatchCompat.shouldUseTravelerFamilyFollowController(mob)) {
            if (mob instanceof PathfinderMob pathfinderMob && mob.getTarget() == null && !mob.isAggressive()) {
               PathfinderMob leader = findLeader(pathfinderMob);
               if (leader != null) {
                  Vec3 targetPos = resolveFormationTarget(pathfinderMob, leader);
                  if (targetPos != null) {
                     if (pathfinderMob.distanceToSqr(targetPos) <= 0.81 && pathfinderMob.distanceToSqr(leader) <= 100.0 && !isLeaderTraveling(leader)) {
                        pathfinderMob.getNavigation().stop();
                     } else {
                        double speed = pathfinderMob.distanceToSqr(leader) > 100.0 ? 0.55 : 0.45;
                        pathfinderMob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
                     }
                  }
               }
            }
         }
      }
   }

   private static PathfinderMob findLeader(PathfinderMob mob) {
      String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
      AABB searchBox = mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> leaders = mob.level().getEntitiesOfClass(PathfinderMob.class, searchBox, candidatex -> isMatchingLeader(candidatex, groupId, mob));
      PathfinderMob nearestLeader = null;
      double nearestDistance = Double.MAX_VALUE;

      for (PathfinderMob candidate : leaders) {
         double distance = mob.distanceToSqr(candidate);
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestLeader = candidate;
         }
      }

      return nearestLeader;
   }

   private static boolean isMatchingLeader(PathfinderMob candidate, String groupId, PathfinderMob mob) {
      if (candidate != mob && candidate.isAlive()) {
         CompoundTag data = PersistentEntityData.get(candidate);
         return groupId.equals(data.getString("villagerpatrols_group_id"))
            && "traveler_family_world_event".equals(data.getString("villagerpatrols_group_type"))
            && "traveler_leader".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }

   private static Vec3 resolveFormationTarget(PathfinderMob mob, PathfinderMob leader) {
      Vec3 desiredTarget = leader.position().add(resolveFormationOffset(mob, leader));
      if (isLeaderTraveling(leader)) {
         desiredTarget = desiredTarget.add(resolveLeaderForward(leader).scale(0.75));
      }

      return desiredTarget;
   }

   private static Vec3 resolveFormationOffset(PathfinderMob mob, PathfinderMob leader) {
      Vec3 forward = resolveLeaderForward(leader);
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      List<PathfinderMob> companions = SlimPatchCompat.findTravelerFamilyCompanions(mob);
      int index = Math.max(0, companions.indexOf(mob));
      int row = index / 2 + 1;
      double sideSign = index % 2 == 0 ? 1.0 : -1.0;
      return side.scale(1.2 * sideSign * row);
   }

   private static Vec3 resolveLeaderForward(PathfinderMob leader) {
      Vec3 motion = leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      if (horizontalMotion.lengthSqr() > 0.04) {
         return horizontalMotion.normalize();
      }

      Vec3 view = leader.getViewVector(1.0F);
      Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
      return horizontalView.lengthSqr() > 1.0E-4 ? horizontalView.normalize() : new Vec3(0.0, 0.0, 1.0);
   }

   private static boolean isLeaderTraveling(PathfinderMob leader) {
      Vec3 motion = leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      return horizontalMotion.lengthSqr() > 0.01 || !leader.getNavigation().isDone();
   }
}
