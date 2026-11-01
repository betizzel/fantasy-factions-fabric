package com.javic.villagerpatrols.entity.goal;

import com.javic.villagerpatrols.village.VillageGuardHome;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class StayNearVillageGuardHomeGoal extends Goal {
   private static final int COMBAT_COOLDOWN_TICKS = 100;
   private static final int BELL_SEARCH_RADIUS = 48;
   private static final double HARD_RETURN_DISTANCE_SQR = 2304.0;
   private final PathfinderMob mob;
   private final double speedModifier;
   private final double startDistanceSqr;
   private final double stopDistanceSqr;
   private final int returnTargetRandomRadius;
   private BlockPos homePos;
   private BlockPos returnTarget;

   public StayNearVillageGuardHomeGoal(PathfinderMob mob, double speedModifier, double startDistance, double stopDistance, int returnTargetRandomRadius) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.startDistanceSqr = startDistance * startDistance;
      this.stopDistanceSqr = stopDistance * stopDistance;
      this.returnTargetRandomRadius = returnTargetRandomRadius;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      if (!this.canReturnHome()) {
         return false;
      }

      this.homePos = VillageGuardHome.resolveHome(this.mob, 48);
      return this.homePos != null && this.mob.blockPosition().distSqr(this.homePos) > this.startDistanceSqr;
   }

   public boolean canContinueToUse() {
      return this.canReturnHome() && this.homePos != null && this.mob.blockPosition().distSqr(this.homePos) > this.stopDistanceSqr;
   }

   public void start() {
      this.pickReturnTarget();
      this.moveToReturnTarget();
   }

   public void stop() {
      this.homePos = null;
      this.returnTarget = null;
      this.mob.getNavigation().stop();
   }

   public void tick() {
      if (this.homePos != null) {
         if (this.mob.tickCount % 20 == 0 || this.returnTarget == null || this.mob.blockPosition().distSqr(this.returnTarget) < 9.0) {
            this.pickReturnTarget();
            this.moveToReturnTarget();
         }
      }
   }

   private boolean canReturnHome() {
      if (!this.mob.level().isClientSide() && this.mob.isAlive() && VillageGuardHome.hasHome(this.mob)) {
         LivingEntity target = this.mob.getTarget();
         return target != null && target.isAlive()
            ? false
            : !this.mob.isAggressive()
               && !this.recentCombat(this.mob.getLastHurtByMob(), this.mob.getLastHurtByMobTimestamp())
               && !this.recentCombat(this.mob.getLastHurtMob(), this.mob.getLastHurtMobTimestamp());
      } else {
         return false;
      }
   }

   private boolean recentCombat(LivingEntity entity, int timestamp) {
      return entity != null && entity.isAlive() && this.mob.tickCount - timestamp < 100;
   }

   private void pickReturnTarget() {
      if (!(this.mob.level() instanceof ServerLevel serverLevel && this.homePos != null)) {
         this.returnTarget = null;
      } else if (this.mob.blockPosition().distSqr(this.homePos) > 2304.0) {
         BlockPos fallback = serverLevel.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, this.homePos);
         this.returnTarget = VillageGuardHome.isValidSurface(serverLevel, fallback) ? fallback : this.homePos;
      } else {
         for (int attempt = 0; attempt < 16; attempt++) {
            int offsetX = this.mob.getRandom().nextInt(this.returnTargetRandomRadius * 2 + 1) - this.returnTargetRandomRadius;
            int offsetZ = this.mob.getRandom().nextInt(this.returnTargetRandomRadius * 2 + 1) - this.returnTargetRandomRadius;
            BlockPos base = this.homePos.offset(offsetX, 0, offsetZ);
            if (serverLevel.isLoaded(base)) {
               BlockPos surface = serverLevel.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, base);
               if (VillageGuardHome.isValidSurface(serverLevel, surface)) {
                  this.returnTarget = surface;
                  return;
               }
            }
         }

         BlockPos fallback = serverLevel.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, this.homePos);
         this.returnTarget = VillageGuardHome.isValidSurface(serverLevel, fallback) ? fallback : null;
      }
   }

   private void moveToReturnTarget() {
      if (this.returnTarget != null) {
         this.mob.getNavigation().moveTo(this.returnTarget.getX() + 0.5, this.returnTarget.getY(), this.returnTarget.getZ() + 0.5, this.speedModifier);
      }
   }
}
