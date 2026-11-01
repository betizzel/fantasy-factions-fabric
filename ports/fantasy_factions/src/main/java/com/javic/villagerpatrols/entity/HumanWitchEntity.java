package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HumanWitchEntity extends Witch {
   private static final int SKINS = 10;
   private static final String TAG_INITIALIZED = "villagerpatrols_initialized";
   private static final String TAG_SKIN = "villagerpatrols_skin";
   private static final ResourceLocation DEFAULT_LOOT_TABLE = new ResourceLocation("entities/witch");
   private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(HumanWitchEntity.class, EntityDataSerializers.INT);

   public HumanWitchEntity(EntityType<? extends Witch> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Witch.createAttributes();
   }

   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, OrcEntity.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, KnightEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, KnightShieldEntity.class, true));
      this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, KnightLeaderEntity.class, true));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, VillageGuardEntity.class, true));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, VillageGuardLeaderEntity.class, true));
      this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, ElfEntity.class, true));
      this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, DwarfEntity.class, true));
   }

   protected void customServerAiStep() {
      CombatRetargeting.retargetFromPlayerIfNeeded(
         this,
         candidate -> candidate instanceof OrcEntity
            || candidate instanceof KnightEntity
            || candidate instanceof KnightShieldEntity
            || candidate instanceof KnightLeaderEntity
            || candidate instanceof VillageGuardEntity
            || candidate instanceof VillageGuardLeaderEntity
            || candidate instanceof ElfEntity
            || candidate instanceof DwarfEntity
      );
      super.customServerAiStep();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SKIN, 1);
   }

   public void setSkinIndex(int index) {
      int resolved = Math.max(1, Math.min(index, 10));
      this.entityData.set(DATA_SKIN, resolved);
      PersistentEntityData.get(this).putInt("villagerpatrols_skin", resolved);
   }

   public int getSkinIndex() {
      return (Integer)this.entityData.get(DATA_SKIN);
   }

   public ResourceLocation getSkinTexture() {
      return new ResourceLocation("villagerpatrols", "textures/entity/human_witch/skin_" + String.format("%02d", this.getSkinIndex()) + ".png");
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_initialized")) {
         this.setSkinIndex(this.getRandom().nextInt(10) + 1);
         data.putBoolean("villagerpatrols_initialized", true);
      } else if (data.contains("villagerpatrols_skin")) {
         this.setSkinIndex(data.getInt("villagerpatrols_skin"));
      }

      this.setCustomName(null);
      this.setCustomNameVisible(false);
      return groupData;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("villagerpatrols_skin")) {
         this.setSkinIndex(tag.getInt("villagerpatrols_skin"));
      }

      if (tag.contains("villagerpatrols_initialized")) {
         PersistentEntityData.get(this).putBoolean("villagerpatrols_initialized", tag.getBoolean("villagerpatrols_initialized"));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("villagerpatrols_skin", this.getSkinIndex());
      tag.putBoolean("villagerpatrols_initialized", PersistentEntityData.get(this).getBoolean("villagerpatrols_initialized"));
   }

   public boolean shouldShowName() {
      return false;
   }

   public boolean isCustomNameVisible() {
      return false;
   }

   public Component getName() {
      return this.getType().getDescription();
   }

   protected ResourceLocation getDefaultLootTable() {
      return DEFAULT_LOOT_TABLE;
   }
}
