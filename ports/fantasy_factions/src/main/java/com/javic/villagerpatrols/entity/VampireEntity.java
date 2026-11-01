package com.javic.villagerpatrols.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class VampireEntity extends AbstractKnightEntity {
   public VampireEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 20;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 50.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.MOVEMENT_SPEED, 0.31)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(15, new NearestAttackableTargetGoal(this, Monster.class, true));
   }

   public void aiStep() {
      if (!this.level().isClientSide && this.isAlive() && this.isSunBurnTick()) {
         VampireEntitySupport.igniteFromSunlight(this);
      }

      VampireEntitySupport.updateLowHealthStrength(this);
      super.aiStep();
   }

   public boolean hurt(DamageSource source, float amount) {
      return super.hurt(source, VampireEntitySupport.applyFireWeakness(source, amount));
   }

   public boolean doHurtTarget(Entity entity) {
      float previousHealth = VampireEntitySupport.healthBeforeDamage(entity);
      float previousAbsorption = VampireEntitySupport.absorptionBeforeDamage(entity);
      boolean result = super.doHurtTarget(entity);
      VampireEntitySupport.applyLifesteal(this, entity, result, previousHealth, previousAbsorption);
      return result;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("vampire", this.isFemale());
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("vampire", this.isFemale());
   }

   @Override
   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("vampire", this.isFemale());
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      InteractionResult itemResult = player.getItemInHand(hand).interactLivingEntity(player, this, hand);
      return itemResult.consumesAction() ? itemResult : ModEntitySounds.playInteract(this, "vampire", this.isFemale());
   }

   @Override
   public boolean isAlliedTo(Entity entity) {
      return entity instanceof VampireEntity ? true : super.isAlliedTo(entity);
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return VampireEntitySupport.getTexture(this.isFemale(), this.getSkinIndex());
   }

   @Override
   protected String getTextureFolder() {
      return "vampire";
   }

   @Override
   protected String getSoundFolder() {
      return "vampire";
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return false;
   }

   @Override
   protected int getMaleSkinCount() {
      return 6;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 6;
   }

   @Override
   protected Item getDefaultMainHandItem() {
      return Items.AIR;
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return false;
   }

   @Override
   protected boolean canRetargetFromPlayerTo(LivingEntity candidate) {
      return candidate instanceof Monster ? true : super.canRetargetFromPlayerTo(candidate);
   }
}
