package com.javic.villagerpatrols.entity.goal;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class TravelerGroupLeaderGoal extends Goal {
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
   private static final String TRAVELER_GROUP_TYPE = "traveler_group_world_event";
   private static final String ARMED_TRAVELER_GROUP_TYPE = "armed_traveler_group_world_event";
   private static final String FAMILY_GROUP_TYPE = "traveler_family_world_event";
   private static final String LEADER_ROLE = "leader";
   private final PathfinderMob mob;
   private final double speedModifier;
   private Vec3 targetPos;
   private Vec3 lastProgressPos;
   private float travelAngle;
   private boolean hasTravelAngle;
   private int nextPathTick;
   private int targetTime;
   private int stuckTime;

   public TravelerGroupLeaderGoal(PathfinderMob mob, double speedModifier) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if (!this.isEligibleLeader()) {
         return false;
      }

      if (this.targetPos == null || this.shouldPickNewTarget()) {
         this.targetPos = this.findNextTarget();
      }

      return this.targetPos != null;
   }

   public boolean canContinueToUse() {
      return this.isEligibleLeader() && this.targetPos != null;
   }

   public void start() {
      this.targetTime = 0;
      this.stuckTime = 0;
      this.lastProgressPos = this.mob.position();
      this.nextPathTick = 0;
      this.moveToTarget();
   }

   public void stop() {
      this.mob.getNavigation().stop();
      this.targetPos = null;
      this.lastProgressPos = null;
      this.hasTravelAngle = false;
      this.nextPathTick = 0;
      this.targetTime = 0;
      this.stuckTime = 0;
   }

   public void tick() {
      if (this.targetPos != null) {
         this.targetTime++;
         this.mob.getLookControl().setLookAt(this.targetPos.x, this.targetPos.y, this.targetPos.z);
         Vec3 currentPos = this.mob.position();
         if (this.lastProgressPos != null) {
            if (currentPos.distanceToSqr(this.lastProgressPos) >= 4.0) {
               this.lastProgressPos = currentPos;
               this.stuckTime = 0;
            } else {
               this.stuckTime++;
            }
         } else {
            this.lastProgressPos = currentPos;
         }

         if (this.shouldPickNewTarget()) {
            this.targetPos = this.findNextTarget();
            this.targetTime = 0;
            this.stuckTime = 0;
            this.lastProgressPos = this.mob.position();
         }

         if (this.targetPos != null && --this.nextPathTick <= 0) {
            this.nextPathTick = 10;
            this.moveToTarget();
         }
      }
   }

   private boolean isEligibleLeader() {
      if (this.mob.level().isClientSide() || !this.mob.isAlive() || !(this.mob.level() instanceof ServerLevel)) {
         return false;
      } else if (this.mob.isPassenger() || this.mob.isLeashed()) {
         return false;
      } else if (this.mob.getTarget() == null && !this.mob.isAggressive()) {
         CompoundTag data = PersistentEntityData.get(this.mob);
         return this.isSupportedGroupType(data.getString("villagerpatrols_group_type")) && "leader".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }

   private boolean isSupportedGroupType(String groupType) {
      return "traveler_group_world_event".equals(groupType)
         || "armed_traveler_group_world_event".equals(groupType)
         || "traveler_family_world_event".equals(groupType);
   }

   private boolean shouldPickNewTarget() {
      if (this.targetPos == null) {
         return true;
      } else {
         return this.mob.distanceToSqr(this.targetPos) <= 9.0 ? true : this.targetTime >= 200 || this.stuckTime >= 60;
      }
   }

   private Vec3 findNextTarget() {
      if (this.mob.level() instanceof ServerLevel level) {
         RandomSource var12 = this.mob.getRandom();
         BlockPos origin = this.mob.blockPosition();
         this.ensureTravelAngle(var12);

         for (int attempt = 0; attempt < 12; attempt++) {
            float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
            float angle = this.travelAngle + (var12.nextFloat() * 2.0F - 1.0F) * maxTurn;
            int distance = 32 + var12.nextInt(33);
            int offsetX = (int)Math.round(Math.cos(angle) * distance);
            int offsetZ = (int)Math.round(Math.sin(angle) * distance);
            BlockPos sample = origin.offset(offsetX, 0, offsetZ);
            if (level.isLoaded(sample)) {
               BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
               if (this.isValidSurface(level, surface)) {
                  this.travelAngle = angle;
                  this.hasTravelAngle = true;
                  return Vec3.atBottomCenterOf(surface);
               }
            }
         }

         this.travelAngle = var12.nextFloat() * (float) (Math.PI * 2);
         this.hasTravelAngle = true;
         return null;
      } else {
         return null;
      }
   }

   private void ensureTravelAngle(RandomSource random) {
      if (!this.hasTravelAngle) {
         Vec3 motion = this.mob.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            this.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            this.hasTravelAngle = true;
         } else {
            Vec3 view = this.mob.getViewVector(1.0F);
            Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
            if (horizontalView.lengthSqr() > 1.0E-4) {
               this.travelAngle = (float)Math.atan2(horizontalView.z, horizontalView.x);
               this.hasTravelAngle = true;
            } else {
               this.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
               this.hasTravelAngle = true;
            }
         }
      }
   }

   private boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private void moveToTarget() {
      if (this.targetPos != null) {
         this.mob.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.speedModifier);
      }
   }
}
