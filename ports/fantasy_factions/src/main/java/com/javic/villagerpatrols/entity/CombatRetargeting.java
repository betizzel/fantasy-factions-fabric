package com.javic.villagerpatrols.entity;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class CombatRetargeting {
   private static final int RETARGET_INTERVAL_TICKS = 20;
   private static final double RETARGET_RANGE = 20.0;
   private static final double RETARGET_RANGE_Y = 8.0;
   private static final double PLAYER_DISTANCE_ADVANTAGE_SQR = 16.0;

   private CombatRetargeting() {
   }

   public static void retargetFromPlayerIfNeeded(Mob mob, Predicate<LivingEntity> candidatePredicate) {
      if (!mob.level().isClientSide() && mob.isAlive() && mob.tickCount % 20 == mob.getId() % 20) {
         LivingEntity currentTarget = mob.getTarget();
         if (currentTarget instanceof Player playerTarget && playerTarget.isAlive()) {
            LivingEntity nearestCandidate = null;
            double nearestDistance = Double.MAX_VALUE;

            for (LivingEntity candidate : mob.level()
               .getEntitiesOfClass(
                  LivingEntity.class,
                  mob.getBoundingBox().inflate(20.0, 8.0, 20.0),
                  entity -> entity != mob
                     && entity != currentTarget
                     && entity.isAlive()
                     && !entity.isRemoved()
                     && mob.canAttack(entity)
                     && !mob.isAlliedTo(entity)
                     && mob.hasLineOfSight(entity)
                     && candidatePredicate.test(entity)
               )) {
               double distance = mob.distanceToSqr(candidate);
               if (distance < nearestDistance) {
                  nearestDistance = distance;
                  nearestCandidate = candidate;
               }
            }

            if (nearestCandidate != null) {
               double playerDistance = mob.distanceToSqr(playerTarget);
               if (nearestDistance + 16.0 < playerDistance) {
                  mob.setTarget(nearestCandidate);
               }
            }
         }
      }
   }
}
