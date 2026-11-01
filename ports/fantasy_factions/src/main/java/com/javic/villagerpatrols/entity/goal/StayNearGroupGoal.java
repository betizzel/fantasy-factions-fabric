package com.javic.villagerpatrols.entity.goal;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StayNearGroupGoal extends Goal {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String LEADER_ROLE = "leader";
   private static final int COMBAT_COOLDOWN_TICKS = 100;
   private final PathfinderMob mob;
   private final double speedModifier;
   private final double searchRadius;
   private final double startDistanceSqr;
   private final double stopDistanceSqr;
   private int nextSearchTick;
   private int nextPathTick;
   private Vec3 targetPos;

   public StayNearGroupGoal(PathfinderMob mob, double speedModifier, double searchRadius, double startDistance) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.searchRadius = searchRadius;
      this.startDistanceSqr = startDistance * startDistance;
      double stopDistance = Math.max(4.0, startDistance - 2.0);
      this.stopDistanceSqr = stopDistance * stopDistance;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if (!this.canGroupMove()) {
         return false;
      }

      if (this.mob.tickCount < this.nextSearchTick) {
         return false;
      }

      this.nextSearchTick = this.mob.tickCount + 20 + this.mob.getRandom().nextInt(20);
      this.targetPos = this.findTargetPos();
      return this.targetPos != null;
   }

   public boolean canContinueToUse() {
      return this.canGroupMove() && this.targetPos != null ? this.mob.distanceToSqr(this.targetPos) > this.stopDistanceSqr : false;
   }

   public void start() {
      this.nextPathTick = 0;
      this.moveToTarget();
   }

   public void stop() {
      this.targetPos = null;
      this.mob.getNavigation().stop();
   }

   public void tick() {
      if (this.targetPos != null) {
         this.mob.getLookControl().setLookAt(this.targetPos.x, this.targetPos.y, this.targetPos.z);
         if (--this.nextPathTick <= 0) {
            this.nextPathTick = 10;
            Vec3 updatedPos = this.findTargetPos();
            if (updatedPos != null) {
               this.targetPos = updatedPos;
            }

            this.moveToTarget();
         }
      }
   }

   private boolean canGroupMove() {
      if (!this.mob.level().isClientSide() && this.mob.isAlive()) {
         if (!PersistentEntityData.get(this.mob).contains("villagerpatrols_group_id")) {
            return false;
         }

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

   private Vec3 findTargetPos() {
      String groupId = PersistentEntityData.get(this.mob).getString("villagerpatrols_group_id");
      List<PathfinderMob> allies = this.mob
         .level()
         .getEntitiesOfClass(
            PathfinderMob.class,
            AABB.ofSize(this.mob.position(), this.searchRadius * 2.0, this.searchRadius, this.searchRadius * 2.0),
            allyx -> this.isGroupedAlly(allyx, groupId)
         );
      if (allies.isEmpty()) {
         return null;
      }

      PathfinderMob leader = null;
      double closestLeaderDistance = Double.MAX_VALUE;
      PathfinderMob nearestAlly = null;
      double closestAllyDistance = Double.MAX_VALUE;
      Vec3 center = Vec3.ZERO;

      for (PathfinderMob ally : allies) {
         double distance = this.mob.distanceToSqr(ally);
         if (distance < closestAllyDistance) {
            closestAllyDistance = distance;
            nearestAlly = ally;
         }

         if ("leader".equals(PersistentEntityData.get(ally).getString("villagerpatrols_group_role")) && distance < closestLeaderDistance) {
            closestLeaderDistance = distance;
            leader = ally;
         }

         center = center.add(ally.getX(), ally.getY(), ally.getZ());
      }

      if (leader != null && closestLeaderDistance > this.startDistanceSqr) {
         return leader.position();
      } else {
         Vec3 centerPos = center.scale(1.0 / allies.size());
         if (this.mob.distanceToSqr(centerPos) > this.startDistanceSqr) {
            return centerPos;
         } else {
            return nearestAlly != null && closestAllyDistance > this.startDistanceSqr ? nearestAlly.position() : null;
         }
      }
   }

   private boolean isGroupedAlly(PathfinderMob ally, String groupId) {
      if (ally != this.mob && ally.isAlive()) {
         CompoundTag data = PersistentEntityData.get(ally);
         return data.contains("villagerpatrols_group_id") && groupId.equals(data.getString("villagerpatrols_group_id"));
      } else {
         return false;
      }
   }

   private void moveToTarget() {
      if (this.targetPos != null) {
         this.mob.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.speedModifier);
      }
   }
}
