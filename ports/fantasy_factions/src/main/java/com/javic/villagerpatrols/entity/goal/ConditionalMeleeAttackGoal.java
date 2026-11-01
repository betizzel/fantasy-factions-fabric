package com.javic.villagerpatrols.entity.goal;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class ConditionalMeleeAttackGoal extends MeleeAttackGoal {
   private final BooleanSupplier condition;

   public ConditionalMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, BooleanSupplier condition) {
      super(mob, speedModifier, followingTargetEvenIfNotSeen);
      this.condition = condition;
   }

   public boolean canUse() {
      return this.condition.getAsBoolean() && super.canUse();
   }

   public boolean canContinueToUse() {
      return this.condition.getAsBoolean() && super.canContinueToUse();
   }
}
