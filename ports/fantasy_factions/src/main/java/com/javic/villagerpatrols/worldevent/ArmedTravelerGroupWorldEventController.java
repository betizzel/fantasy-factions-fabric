package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.ArmedTravelerEquipment;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ArmedTravelerGroupWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 48.0;
   private static final double FORMATION_DISTANCE = 2.5;
   private static final double COMFORT_RADIUS_SQR = 4.0;
   private static final double FAR_DISTANCE_SQR = 100.0;
   private static final double FOLLOW_SPEED = 0.45;
   private static final double CATCH_UP_SPEED = 0.55;
   private static final double SOFT_ADVANCE_DISTANCE = 0.75;
   private static final double LEADER_MOVING_DISTANCE_SQR = 0.01;
   private static final double HARD_CORRECTION_DISTANCE_SQR = 9.0;
   private static final int MIN_TARGET_DISTANCE = 32;
   private static final int MAX_TARGET_DISTANCE = 64;
   private static final int MAX_TARGET_TIME = 200;
   private static final int STUCK_TIMEOUT = 60;
   private static final int BIG_TURN_AFTER_FAILED_ATTEMPTS = 8;
   private static final double ARRIVAL_DISTANCE_SQR = 9.0;
   private static final double MIN_PROGRESS_DISTANCE_SQR = 4.0;
   private static final float MAX_TURN_ANGLE_RADIANS = (float)Math.toRadians(45.0);
   private static final Map<UUID, ArmedTravelerGroupWorldEventController.LeaderState> LEADERS = new HashMap<>();
   private static final Map<UUID, Integer> FOLLOWERS = new HashMap<>();

   private ArmedTravelerGroupWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "armed_traveler_group_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         ArmedTravelerEquipment.ensureEquipment(mob);
         if (!(mob instanceof MaleVillagerEntity) && !(mob instanceof FemaleVillagerEntity)) {
            if (mob instanceof PathfinderMob pathfinderMob && mob.getTarget() == null && !mob.isAggressive()) {
               String role = PersistentEntityData.get(mob).getString("villagerpatrols_group_role");
               if ("traveler_leader".equals(role)) {
                  tickLeader(level, pathfinderMob);
               } else if ("traveler_companion".equals(role)) {
                  tickFollower(pathfinderMob);
               }
            }
         }
      }
   }

   private static void tickLeader(ServerLevel level, PathfinderMob mob) {
      if (!mob.isPassenger() && !mob.isLeashed()) {
         ArmedTravelerGroupWorldEventController.LeaderState state = LEADERS.computeIfAbsent(
            mob.getUUID(), unused -> new ArmedTravelerGroupWorldEventController.LeaderState()
         );
         if (state.targetPos == null || shouldPickNewTarget(mob, state.targetPos, state.targetTime, state.stuckTime)) {
            state.targetPos = findNextTarget(level, mob, state);
            state.targetTime = 0;
            state.stuckTime = 0;
            state.lastProgressPos = mob.position();
         }

         if (state.targetPos != null) {
            state.targetTime++;
            mob.getLookControl().setLookAt(state.targetPos.x, state.targetPos.y, state.targetPos.z);
            Vec3 currentPos = mob.position();
            if (state.lastProgressPos != null) {
               if (currentPos.distanceToSqr(state.lastProgressPos) >= 4.0) {
                  state.lastProgressPos = currentPos;
                  state.stuckTime = 0;
               } else {
                  state.stuckTime++;
               }
            } else {
               state.lastProgressPos = currentPos;
            }

            if (state.targetPos != null && --state.nextPathTick <= 0) {
               state.nextPathTick = 10;
               mob.getNavigation().moveTo(state.targetPos.x, state.targetPos.y, state.targetPos.z, 0.4);
            }
         }
      } else {
         LEADERS.remove(mob.getUUID());
         mob.getNavigation().stop();
      }
   }

   private static void tickFollower(PathfinderMob mob) {
      if (!mob.isPassenger() && !mob.isLeashed()) {
         PathfinderMob leader = findLeader(mob);
         if (leader != null) {
            Vec3 targetPos = resolveFormationTarget(mob, leader);
            if (targetPos != null) {
               if (mob.distanceToSqr(targetPos) <= 4.0 && mob.distanceToSqr(leader) <= 100.0 && !isLeaderTraveling(leader)) {
                  mob.getNavigation().stop();
               } else {
                  int nextPathTick = FOLLOWERS.getOrDefault(mob.getUUID(), 0) - 1;
                  if (nextPathTick <= 0 || mob.getNavigation().isDone() || mob.distanceToSqr(targetPos) > 9.0) {
                     double speed = mob.distanceToSqr(leader) > 100.0 ? 0.55 : 0.45;
                     mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
                     nextPathTick = 15 + mob.getRandom().nextInt(11);
                  }

                  FOLLOWERS.put(mob.getUUID(), nextPathTick);
               }
            }
         }
      } else {
         FOLLOWERS.remove(mob.getUUID());
         mob.getNavigation().stop();
      }
   }

   private static boolean shouldPickNewTarget(PathfinderMob mob, Vec3 targetPos, int targetTime, int stuckTime) {
      if (targetPos == null) {
         return true;
      } else {
         return mob.distanceToSqr(targetPos) <= 9.0 ? true : targetTime >= 200 || stuckTime >= 60;
      }
   }

   private static Vec3 findNextTarget(ServerLevel level, PathfinderMob mob, ArmedTravelerGroupWorldEventController.LeaderState state) {
      RandomSource random = mob.getRandom();
      ensureTravelAngle(mob, state, random);

      for (int attempt = 0; attempt < 12; attempt++) {
         float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
         float angle = state.travelAngle + (random.nextFloat() * 2.0F - 1.0F) * maxTurn;
         int distance = 32 + random.nextInt(33);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = mob.blockPosition().offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (isValidSurface(level, surface)) {
               state.travelAngle = angle;
               state.hasTravelAngle = true;
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
      state.hasTravelAngle = true;
      return null;
   }

   private static void ensureTravelAngle(PathfinderMob mob, ArmedTravelerGroupWorldEventController.LeaderState state, RandomSource random) {
      if (!state.hasTravelAngle) {
         Vec3 motion = mob.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            state.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            state.hasTravelAngle = true;
         } else {
            Vec3 view = mob.getViewVector(1.0F);
            Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
            if (horizontalView.lengthSqr() > 1.0E-4) {
               state.travelAngle = (float)Math.atan2(horizontalView.z, horizontalView.x);
               state.hasTravelAngle = true;
            } else {
               state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
               state.hasTravelAngle = true;
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
            && "armed_traveler_group_world_event".equals(data.getString("villagerpatrols_group_type"))
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

      return mob.level() instanceof ServerLevel level ? findNavigableTarget(level, leader, desiredTarget) : desiredTarget;
   }

   private static Vec3 resolveFormationOffset(PathfinderMob mob, PathfinderMob leader) {
      Vec3 forward = resolveLeaderForward(leader);
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      int slot = findCompanionFormationSlot(mob);

      return switch (slot) {
         case 0 -> side.scale(2.5);
         case 1 -> side.scale(-2.5);
         case 2 -> side.scale(2.5).add(forward.scale(-2.125));
         default -> side.scale(-2.5).add(forward.scale(-2.125));
      };
   }

   private static int findCompanionFormationSlot(PathfinderMob mob) {
      String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
      AABB searchBox = mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> companions = new ArrayList<>(
         mob.level().getEntitiesOfClass(PathfinderMob.class, searchBox, candidate -> isMatchingCompanion(candidate, groupId))
      );
      companions.sort(Comparator.comparing(Entity::getUUID));

      for (int index = 0; index < companions.size(); index++) {
         if (companions.get(index).getUUID().equals(mob.getUUID())) {
            return index % 4;
         }
      }

      return Mth.abs(mob.getUUID().hashCode()) % 4;
   }

   private static boolean isMatchingCompanion(PathfinderMob candidate, String groupId) {
      if (!candidate.isAlive()) {
         return false;
      }

      CompoundTag data = PersistentEntityData.get(candidate);
      return groupId.equals(data.getString("villagerpatrols_group_id"))
         && "armed_traveler_group_world_event".equals(data.getString("villagerpatrols_group_type"))
         && "traveler_companion".equals(data.getString("villagerpatrols_group_role"));
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

   private static Vec3 findNavigableTarget(ServerLevel level, PathfinderMob leader, Vec3 desiredTarget) {
      int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
      int baseX = Mth.floor(desiredTarget.x);
      int baseZ = Mth.floor(desiredTarget.z);

      for (int[] offset : offsets) {
         BlockPos sample = leader.blockPosition().offset(baseX - leader.getBlockX() + offset[0], 0, baseZ - leader.getBlockZ() + offset[1]);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (isValidSurface(level, surface)) {
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      return desiredTarget;
   }

   private static boolean isLeaderTraveling(PathfinderMob leader) {
      Vec3 motion = leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      return horizontalMotion.lengthSqr() > 0.01 || !leader.getNavigation().isDone();
   }

   private static boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private static final class LeaderState {
      private Vec3 targetPos;
      private Vec3 lastProgressPos;
      private float travelAngle;
      private boolean hasTravelAngle;
      private int nextPathTick;
      private int targetTime;
      private int stuckTime;
   }
}
