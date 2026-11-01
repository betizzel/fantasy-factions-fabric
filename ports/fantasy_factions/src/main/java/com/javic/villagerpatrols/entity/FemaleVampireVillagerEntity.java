package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

public class FemaleVampireVillagerEntity extends FemaleVillagerEntity {
   private static final String TAG_VAMPIRE_INITIALIZED = "villagerpatrols_vampire_initialized";

   public FemaleVampireVillagerEntity(EntityType<? extends Villager> type, Level level) {
      super(type, level);
      this.xpReward = 20;
      VampireEntitySupport.applyVillagerCompatibility(this, true, this.getSkinIndex(), false);
   }

   public static Builder createAttributes() {
      return Villager.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 5.0);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.55, false));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, Monster.class, true));
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_vampire_initialized")) {
         this.setSkinIndex(this.getRandom().nextInt(6) + 1);
         if (this.getCustomName() == null) {
            this.setCustomName(VampireEntitySupport.randomName(this, true));
         }

         data.putBoolean("villagerpatrols_vampire_initialized", true);
      }

      this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      VampireEntitySupport.applyVillagerCompatibility(this, true, this.getSkinIndex(), true);
      this.setCustomNameVisible(false);
      return groupData;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      VampireEntitySupport.applyVillagerCompatibility(this, true, this.getSkinIndex(), false);
      VampireEntitySupport.updateLowHealthStrength(this);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      VampireEntitySupport.applyVillagerCompatibility(this, true, this.getSkinIndex(), false);
   }

   public void aiStep() {
      if (!this.level().isClientSide && this.isAlive() && this.isSunBurnTick()) {
         VampireEntitySupport.igniteFromSunlight(this);
      }

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
   public ResourceLocation getSkinTexture() {
      return VampireEntitySupport.getTexture(true, this.getSkinIndex());
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("vampire", true);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("vampire", true);
   }

   @Override
   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("vampire", true);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      InteractionResult itemResult = itemStack.interactLivingEntity(player, this, hand);
      return itemResult.consumesAction() ? itemResult : ModEntitySounds.playInteract(this, "vampire", true);
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }
}
