package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShepherdMovingAnimalsWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final int MIN_TARGET_DISTANCE = 32;
   private static final int MAX_TARGET_DISTANCE = 64;
   private static final int MAX_TARGET_TIME = 200;
   private static final int STUCK_TIMEOUT = 60;
   private static final int BIG_TURN_AFTER_FAILED_ATTEMPTS = 8;
   private static final double ARRIVAL_DISTANCE_SQR = 9.0;
   private static final double MIN_PROGRESS_DISTANCE_SQR = 4.0;
   private static final float MAX_TURN_ANGLE_RADIANS = (float)Math.toRadians(45.0);
   private static final double SEARCH_RADIUS = 48.0;
   private static final double FORMATION_DISTANCE = 2.0;
   private static final double COMFORT_RADIUS_SQR = 4.0;
   private static final double FAR_DISTANCE_SQR = 100.0;
   private static final double FOLLOWER_WAIT_DISTANCE_SQR = 64.0;
   private static final double FOLLOW_SPEED = 0.55;
   private static final double CATCH_UP_SPEED = 0.75;
   private static final double SOFT_ADVANCE_DISTANCE = 0.75;
   private static final double LEADER_MOVING_DISTANCE_SQR = 0.01;
   private static final double HARD_CORRECTION_DISTANCE_SQR = 9.0;
   private static final int REPATH_INTERVAL_MIN_TICKS = 15;
   private static final int REPATH_INTERVAL_MAX_TICKS = 25;
   private static final Map<UUID, ShepherdMovingAnimalsWorldEventController.TravelState> LEADER_STATES = new HashMap<>();

   private ShepherdMovingAnimalsWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "shepherd_moving_animals_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         String role = PersistentEntityData.get(mob).getString("villagerpatrols_group_role");
         if ("shepherd_leader".equals(role)) {
            tickLeader(level, mob);
         } else {
            if ("sheep".equals(role)) {
               tickFollower(level, mob);
            }
         }
      }
   }

   private static void tickLeader(ServerLevel level, Mob mob) {
      ShepherdMovingAnimalsWorldEvent.applyShepherdCompatibility(mob);
      if (mob.getMainHandItem().getItem() != Items.WHEAT) {
         ShepherdMovingAnimalsWorldEvent.applyShepherdCompatibility(mob);
      }

      if (mob instanceof PathfinderMob leader && leader.getTarget() == null && !leader.isAggressive() && !leader.isPassenger() && !leader.isLeashed()) {
         if (hasDistantFollower(leader)) {
            leader.getNavigation().stop();
         } else {
            ShepherdMovingAnimalsWorldEventController.TravelState state = LEADER_STATES.computeIfAbsent(
               leader.getUUID(), unused -> new ShepherdMovingAnimalsWorldEventController.TravelState()
            );
            if (state.targetPos == null || shouldPickNewLeaderTarget(leader, state)) {
               state.targetPos = findNextLeaderTarget(level, leader, state);
               state.targetTime = 0;
               state.stuckTime = 0;
               state.lastProgressPos = leader.position();
            }

            if (state.targetPos != null) {
               state.targetTime++;
               Vec3 currentPos = leader.position();
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

               if (--state.nextPathTick <= 0 || leader.getNavigation().isDone()) {
                  state.nextPathTick = randomRepathInterval(leader.getRandom());
                  leader.getNavigation().moveTo(state.targetPos.x, state.targetPos.y, state.targetPos.z, 0.55);
               }
            }
         }
      }
   }

   private static void tickFollower(ServerLevel level, Mob mob) {
      if (mob instanceof PathfinderMob follower && !follower.isPassenger() && !follower.isLeashed()) {
         if (follower.getTarget() != null) {
            follower.setTarget(null);
         }

         PathfinderMob leader = findLeader(follower);
         if (leader != null) {
            Vec3 targetPos = resolveFormationTarget(follower, leader);
            if (targetPos != null) {
               if (follower.distanceToSqr(targetPos) <= 4.0 && follower.distanceToSqr(leader) <= 100.0 && !isLeaderTraveling(leader)) {
                  follower.getNavigation().stop();
               } else {
                  double speed = follower.distanceToSqr(leader) > 100.0 ? 0.75 : 0.55;
                  if (!isLeaderTraveling(leader) || !follower.getNavigation().isDone() && !(follower.distanceToSqr(targetPos) > 9.0)) {
                     int interval = randomRepathInterval(follower.getRandom());
                     if (follower.tickCount % interval == follower.getId() % interval) {
                        follower.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
                     }
                  } else {
                     follower.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
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
            && "shepherd_moving_animals_world_event".equals(data.getString("villagerpatrols_group_type"))
            && "shepherd_leader".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }

   private static boolean hasDistantFollower(PathfinderMob leader) {
      String groupId = PersistentEntityData.get(leader).getString("villagerpatrols_group_id");
      AABB searchBox = leader.getBoundingBox().inflate(48.0, 8.0, 48.0);

      for (PathfinderMob follower : leader.level()
         .getEntitiesOfClass(PathfinderMob.class, searchBox, candidate -> isMatchingFollower(candidate, groupId, leader))) {
         if (leader.distanceToSqr(follower) > 64.0) {
            return true;
         }
      }

      return false;
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
      int slot = findFollowerFormationSlot(mob, leader);
      int row = slot / 2 + 1;
      double sideSign = slot % 2 == 0 ? 1.0 : -1.0;
      return side.scale(2.0 * sideSign * row).add(forward.scale(-(1.0 + row * 1.5)));
   }

   private static int findFollowerFormationSlot(PathfinderMob mob, PathfinderMob leader) {
      String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
      AABB searchBox = mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> followers = new ArrayList<>(
         mob.level().getEntitiesOfClass(PathfinderMob.class, searchBox, candidate -> isMatchingFollower(candidate, groupId, leader))
      );
      followers.sort(Comparator.comparing(Entity::getUUID));

      for (int index = 0; index < followers.size(); index++) {
         if (followers.get(index).getUUID().equals(mob.getUUID())) {
            return index;
         }
      }

      return Mth.abs(mob.getUUID().hashCode()) % 4;
   }

   private static boolean isMatchingFollower(PathfinderMob candidate, String groupId, PathfinderMob leader) {
      if (candidate.isAlive() && candidate != leader) {
         CompoundTag data = PersistentEntityData.get(candidate);
         String role = data.getString("villagerpatrols_group_role");
         return groupId.equals(data.getString("villagerpatrols_group_id"))
            && "shepherd_moving_animals_world_event".equals(data.getString("villagerpatrols_group_type"))
            && "sheep".equals(role);
      } else {
         return false;
      }
   }

   private static boolean shouldPickNewLeaderTarget(PathfinderMob leader, ShepherdMovingAnimalsWorldEventController.TravelState state) {
      if (state.targetPos == null) {
         return true;
      } else {
         return leader.distanceToSqr(state.targetPos) <= 9.0 ? true : state.targetTime >= 200 || state.stuckTime >= 60;
      }
   }

   private static Vec3 findNextLeaderTarget(ServerLevel level, PathfinderMob leader, ShepherdMovingAnimalsWorldEventController.TravelState state) {
      RandomSource random = leader.getRandom();
      BlockPos origin = leader.blockPosition();
      ensureTravelAngle(leader, state, random);

      for (int attempt = 0; attempt < 12; attempt++) {
         float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
         float angle = state.travelAngle + (random.nextFloat() * 2.0F - 1.0F) * maxTurn;
         int distance = 32 + random.nextInt(33);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = origin.offset(offsetX, 0, offsetZ);
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

   private static void ensureTravelAngle(PathfinderMob leader, ShepherdMovingAnimalsWorldEventController.TravelState state, RandomSource random) {
      if (!state.hasTravelAngle) {
         Vec3 motion = leader.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            state.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            state.hasTravelAngle = true;
         } else {
            Vec3 view = leader.getViewVector(1.0F);
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

   private static boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private static boolean isLeaderTraveling(PathfinderMob leader) {
      Vec3 motion = leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      return horizontalMotion.lengthSqr() > 0.01 || !leader.getNavigation().isDone();
   }

   private static int randomRepathInterval(RandomSource random) {
      return 15 + random.nextInt(11);
   }

   private static final class TravelState {
      private Vec3 targetPos;
      private Vec3 lastProgressPos;
      private float travelAngle;
      private boolean hasTravelAngle;
      private int nextPathTick;
      private int targetTime;
      private int stuckTime;
   }
}
