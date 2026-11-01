package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.entity.goal.StayNearVillageGuardHomeGoal;
import com.javic.villagerpatrols.village.VillageGuardHome;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class VillageGuardEntity extends AbstractKnightEntity {
   private static final int SKINS = 5;
   private static final int DAMAGE_COOLDOWN_TICKS = 200;
   private static final int HEAL_INTERVAL_TICKS = 100;
   private static final float HEAL_AMOUNT = 1.0F;

   public VillageGuardEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(3, new StayNearVillageGuardHomeGoal(this, 0.6, 32.0, 12.0, 12));
   }

   @Override
   protected String getTextureFolder() {
      return "village_guard";
   }

   protected float getSoundVolume() {
      return 1.2F;
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return true;
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return true;
   }

   @Override
   protected float getBiasedFemaleChance() {
      return 0.1F;
   }

   @Override
   protected int getMaleSkinCount() {
      return 5;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 5;
   }

   @Override
   protected Item getDefaultMainHandItem() {
      return Items.STONE_SWORD;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      if (!this.level().isClientSide() && this.isAlive() && !(this.getHealth() >= this.getMaxHealth())) {
         if (this.getTarget() == null || !this.getTarget().isAlive()) {
            if (!this.isAggressive() && this.tickCount - this.getLastHurtByMobTimestamp() >= 200 && this.tickCount - this.getLastHurtMobTimestamp() >= 200) {
               if (this.tickCount % 100 == 0) {
                  this.heal(1.0F);
               }
            }
         }
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      VillageGuardHome.readHome(this, tag);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      VillageGuardHome.writeHome(this, tag);
   }
}
