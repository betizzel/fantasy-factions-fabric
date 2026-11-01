package com.javic.villagerpatrols.entity.goal;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TravelerGroupFollowLeaderGoal extends Goal {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 48.0;
   private static final double FORMATION_DISTANCE = 2.5;
   private static final double FAMILY_FORMATION_DISTANCE = 1.2;
   private static final double COMFORT_RADIUS_SQR = 4.0;
   private static final double FAMILY_COMFORT_RADIUS_SQR = 0.81;
   private static final double START_FOLLOW_DISTANCE_SQR = 16.0;
   private static final double FAR_DISTANCE_SQR = 100.0;
   private static final double FOLLOW_SPEED = 0.45;
   private static final double CATCH_UP_SPEED = 0.55;
   private static final double SOFT_ADVANCE_DISTANCE = 0.75;
   private static final double LEADER_MOVING_DISTANCE_SQR = 0.01;
   private static final double HARD_CORRECTION_DISTANCE_SQR = 9.0;
   private static final String TRAVELER_GROUP_TYPE = "traveler_group_world_event";
   private static final String ARMED_TRAVELER_GROUP_TYPE = "armed_traveler_group_world_event";
   private static final String FAMILY_GROUP_TYPE = "traveler_family_world_event";
   private static final int REPATH_INTERVAL_MIN_TICKS = 15;
   private static final int REPATH_INTERVAL_MAX_TICKS = 25;
   private static final String COMPANION_ROLE = "companion";
   private static final String LEADER_ROLE = "leader";
   private final PathfinderMob mob;
   private final double speedModifier;
   private PathfinderMob leader;
   private Vec3 targetPos;
   private int nextPathTick;

   public TravelerGroupFollowLeaderGoal(PathfinderMob mob, double speedModifier) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      if (!this.isEligibleCompanion()) {
         return false;
      }

      this.leader = this.findLeader();
      this.targetPos = this.resolveFormationTarget();
      return this.targetPos != null && this.shouldStartFollowing(this.targetPos);
   }

   public boolean canContinueToUse() {
      if (this.isEligibleCompanion() && this.leader != null && this.leader.isAlive()) {
         this.targetPos = this.resolveFormationTarget();
         return this.isEligibleCompanion()
            && this.targetPos != null
            && (this.mob.distanceToSqr(this.targetPos) > this.getComfortRadiusSqr() || this.mob.distanceToSqr(this.leader) > 100.0 || this.isLeaderTraveling());
      } else {
         return false;
      }
   }

   public void start() {
      this.nextPathTick = 0;
      this.moveToFormation();
   }

   public void stop() {
      this.leader = null;
      this.targetPos = null;
      this.mob.getNavigation().stop();
   }

   public void tick() {
      if (this.leader != null && this.targetPos != null) {
         if (this.isLeaderTraveling() && (this.mob.getNavigation().isDone() || this.mob.distanceToSqr(this.targetPos) > 9.0)) {
            this.leader = this.findLeader();
            if (this.leader != null) {
               this.targetPos = this.resolveFormationTarget();
               this.moveToFormation();
            }
         }

         if (--this.nextPathTick <= 0) {
            this.nextPathTick = this.randomRepathInterval();
            this.leader = this.findLeader();
            if (this.leader != null) {
               this.targetPos = this.resolveFormationTarget();
               this.moveToFormation();
            }
         }
      }
   }

   private boolean isEligibleCompanion() {
      if (this.mob.level().isClientSide() || !this.mob.isAlive()) {
         return false;
      } else if (this.mob.isPassenger() || this.mob.isLeashed()) {
         return false;
      } else if (this.mob.getTarget() == null && !this.mob.isAggressive()) {
         CompoundTag data = PersistentEntityData.get(this.mob);
         return data.contains("villagerpatrols_group_id")
            && this.isSupportedGroupType(data.getString("villagerpatrols_group_type"))
            && "companion".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }

   private PathfinderMob findLeader() {
      String groupId = PersistentEntityData.get(this.mob).getString("villagerpatrols_group_id");
      AABB searchBox = this.mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> leaders = this.mob
         .level()
         .getEntitiesOfClass(PathfinderMob.class, searchBox, candidatex -> this.isMatchingLeader(candidatex, groupId));
      PathfinderMob nearestLeader = null;
      double nearestDistance = Double.MAX_VALUE;

      for (PathfinderMob candidate : leaders) {
         double distance = this.mob.distanceToSqr(candidate);
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestLeader = candidate;
         }
      }

      return nearestLeader;
   }

   private boolean isMatchingLeader(PathfinderMob candidate, String groupId) {
      if (candidate != this.mob && candidate.isAlive()) {
         CompoundTag data = PersistentEntityData.get(candidate);
         return groupId.equals(data.getString("villagerpatrols_group_id"))
            && this.isSupportedGroupType(data.getString("villagerpatrols_group_type"))
            && "leader".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }

   private boolean isSupportedGroupType(String groupType) {
      return "traveler_group_world_event".equals(groupType)
         || "armed_traveler_group_world_event".equals(groupType)
         || "traveler_family_world_event".equals(groupType);
   }

   private boolean shouldStartFollowing(Vec3 formationTarget) {
      return this.mob.distanceToSqr(formationTarget) > 16.0 || this.mob.distanceToSqr(this.leader) > 100.0 || this.isLeaderTraveling();
   }

   private Vec3 resolveFormationTarget() {
      if (this.leader == null) {
         return null;
      }

      Vec3 desiredTarget = this.leader.position().add(this.resolveFormationOffset());
      if (this.isLeaderTraveling()) {
         desiredTarget = desiredTarget.add(this.resolveLeaderForward().scale(0.75));
      }

      return this.mob.level() instanceof ServerLevel level ? this.findNavigableTarget(level, desiredTarget) : desiredTarget;
   }

   private Vec3 resolveFormationOffset() {
      Vec3 forward = this.resolveLeaderForward();
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      String groupType = PersistentEntityData.get(this.mob).getString("villagerpatrols_group_type");
      if ("traveler_family_world_event".equals(groupType)) {
         double sideSign = (this.mob.getUUID().compareTo(this.leader.getUUID()) & 1) == 0 ? 1.0 : -1.0;
         return side.scale(1.2 * sideSign);
      }

      int slot = this.findCompanionFormationSlot();

      return switch (slot) {
         case 0 -> side.scale(2.5);
         case 1 -> side.scale(-2.5);
         case 2 -> side.scale(2.5).add(forward.scale(-2.125));
         default -> side.scale(-2.5).add(forward.scale(-2.125));
      };
   }

   private int findCompanionFormationSlot() {
      String groupId = PersistentEntityData.get(this.mob).getString("villagerpatrols_group_id");
      AABB searchBox = this.mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> companions = new ArrayList<>(
         this.mob.level().getEntitiesOfClass(PathfinderMob.class, searchBox, candidate -> this.isMatchingCompanion(candidate, groupId))
      );
      companions.sort(Comparator.comparing(Entity::getUUID));

      for (int index = 0; index < companions.size(); index++) {
         if (companions.get(index).getUUID().equals(this.mob.getUUID())) {
            return index % 4;
         }
      }

      return Mth.abs(this.mob.getUUID().hashCode()) % 4;
   }

   private boolean isMatchingCompanion(PathfinderMob candidate, String groupId) {
      if (!candidate.isAlive()) {
         return false;
      }

      CompoundTag data = PersistentEntityData.get(candidate);
      return groupId.equals(data.getString("villagerpatrols_group_id"))
         && this.isSupportedGroupType(data.getString("villagerpatrols_group_type"))
         && "companion".equals(data.getString("villagerpatrols_group_role"));
   }

   private Vec3 resolveLeaderForward() {
      Vec3 motion = this.leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      if (horizontalMotion.lengthSqr() > 0.04) {
         return horizontalMotion.normalize();
      }

      Vec3 view = this.leader.getViewVector(1.0F);
      Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
      return horizontalView.lengthSqr() > 1.0E-4 ? horizontalView.normalize() : new Vec3(0.0, 0.0, 1.0);
   }

   private Vec3 findNavigableTarget(ServerLevel level, Vec3 desiredTarget) {
      int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
      int baseX = Mth.floor(desiredTarget.x);
      int baseZ = Mth.floor(desiredTarget.z);

      for (int[] offset : offsets) {
         BlockPos sample = this.leader.blockPosition().offset(baseX - this.leader.getBlockX() + offset[0], 0, baseZ - this.leader.getBlockZ() + offset[1]);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (this.isValidSurface(level, surface)) {
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      return desiredTarget;
   }

   private boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private int randomRepathInterval() {
      return 15 + this.mob.getRandom().nextInt(11);
   }

   private double getComfortRadiusSqr() {
      return "traveler_family_world_event".equals(PersistentEntityData.get(this.mob).getString("villagerpatrols_group_type")) ? 0.81 : 4.0;
   }

   private boolean isLeaderTraveling() {
      if (this.leader == null) {
         return false;
      }

      Vec3 motion = this.leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      return horizontalMotion.lengthSqr() > 0.01 || !this.leader.getNavigation().isDone();
   }

   private void moveToFormation() {
      if (this.leader != null && this.targetPos != null) {
         if (this.mob.distanceToSqr(this.targetPos) <= this.getComfortRadiusSqr() && this.mob.distanceToSqr(this.leader) <= 100.0 && !this.isLeaderTraveling()) {
            this.mob.getNavigation().stop();
         } else {
            double speed = this.mob.distanceToSqr(this.leader) > 100.0 ? Math.max(this.speedModifier, 0.55) : Math.min(this.speedModifier, 0.45);
            this.mob.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, speed);
         }
      }
   }
}
