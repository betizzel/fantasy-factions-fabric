package com.javic.villagerpatrols.entity.goal;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class ConditionalNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private final BooleanSupplier condition;

   public ConditionalNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, BooleanSupplier condition) {
      super(mob, targetType, mustSee);
      this.condition = condition;
   }

   public boolean canUse() {
      return this.condition.getAsBoolean() && super.canUse();
   }

   public boolean canContinueToUse() {
      return this.condition.getAsBoolean() && super.canContinueToUse();
   }
}
