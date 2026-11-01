package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class MaleWizardEntity extends MaleVillagerEntity {
   private static final String TAG_WIZARD_INITIALIZED = "villagerpatrols_wizard_initialized";

   public MaleWizardEntity(EntityType<? extends Villager> type, Level level) {
      super(type, level);
      this.xpReward = 10;
      WizardEntitySupport.applyWizardCompatibility(this, false, this.getSkinIndex(), false);
   }

   public static Builder createAttributes() {
      return Villager.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   public static boolean checkWizardSpawnRules(
      EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.55, false));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Monster.class, true, WizardEntitySupport::isDefensiveTarget));
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_wizard_initialized")) {
         this.setSkinIndex(this.getRandom().nextInt(5) + 1);
         data.putBoolean("villagerpatrols_wizard_initialized", true);
      }

      this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
      WizardEntitySupport.applyWizardCompatibility(this, false, this.getSkinIndex(), true);
      this.setCustomNameVisible(false);
      return groupData;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      WizardEntitySupport.applyWizardCompatibility(this, false, this.getSkinIndex(), false);
      WizardEntitySupport.tickStandaloneWizard(this);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      WizardEntitySupport.applyWizardCompatibility(this, false, this.getSkinIndex(), false);
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return WizardEntitySupport.getTexture(false, this.getSkinIndex());
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("wizard", false);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("wizard", false);
   }

   @Override
   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("wizard", false);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      InteractionResult itemResult = itemStack.interactLivingEntity(player, this, hand);
      if (itemResult.consumesAction()) {
         return itemResult;
      }

      WizardEntitySupport.playInteract(this);
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }
}
