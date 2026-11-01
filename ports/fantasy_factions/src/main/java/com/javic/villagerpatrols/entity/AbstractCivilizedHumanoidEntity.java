package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AbstractCivilizedHumanoidEntity extends AbstractKnightEntity {
   protected AbstractCivilizedHumanoidEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95, false));
      this.goalSelector.addGoal(4, new StayNearGroupGoal(this, 0.6, 24.0, 12.0));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true, this::isDefensiveTarget));
   }

   @Override
   protected void customServerAiStep() {
      CombatRetargeting.retargetFromPlayerIfNeeded(this, this::isDefensiveTarget);
      super.customServerAiStep();
   }

   protected boolean isDefensiveTarget(LivingEntity target) {
      return !(target instanceof Creeper) && !(target instanceof AbstractKnightEntity knight && knight.isFriendlyKnight());
   }

   @Override
   protected boolean isFriendlyKnight() {
      return true;
   }
}
