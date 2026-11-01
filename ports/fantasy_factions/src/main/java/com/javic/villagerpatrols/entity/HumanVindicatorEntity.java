package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HumanVindicatorEntity extends Vindicator {
   private static final int MALE_SKINS = 8;
   private static final int FEMALE_SKINS = 8;
   private static final String TAG_INITIALIZED = "villagerpatrols_initialized";
   private static final String TAG_GENDER = "villagerpatrols_gender";
   private static final String TAG_SKIN = "villagerpatrols_skin";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_GROUP_SPAWNED_COMPANIONS = "villagerpatrols_group_spawned_companions";
   private static final ResourceLocation DEFAULT_LOOT_TABLE = new ResourceLocation("entities/vindicator");
   private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(HumanVindicatorEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> DATA_GENDER = SynchedEntityData.defineId(HumanVindicatorEntity.class, EntityDataSerializers.STRING);

   public HumanVindicatorEntity(EntityType<? extends Vindicator> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Vindicator.createAttributes();
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(4, new StayNearGroupGoal(this, 0.6, 24.0, 12.0));
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
      this.entityData.define(DATA_GENDER, "male");
   }

   public boolean isFemale() {
      return "female".equalsIgnoreCase((String)this.entityData.get(DATA_GENDER));
   }

   public void setGender(String gender) {
      String normalized = "female".equalsIgnoreCase(gender) ? "female" : "male";
      this.entityData.set(DATA_GENDER, normalized);
      PersistentEntityData.get(this).putString("villagerpatrols_gender", normalized);
   }

   public String getGender() {
      return (String)this.entityData.get(DATA_GENDER);
   }

   public void setSkinIndex(int index) {
      int max = this.isFemale() ? 8 : 8;
      int resolved = Math.max(1, Math.min(index, max));
      this.entityData.set(DATA_SKIN, resolved);
      PersistentEntityData.get(this).putInt("villagerpatrols_skin", resolved);
   }

   public int getSkinIndex() {
      return (Integer)this.entityData.get(DATA_SKIN);
   }

   public ResourceLocation getSkinTexture() {
      return new ResourceLocation("villagerpatrols", "textures/entity/human_vindicator/" + this.getGender() + "/skin_" + String.format("%02d", this.getSkinIndex()) + ".png");
   }

   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("human_vindicator", this.isFemale());
   }

   public int getAmbientSoundInterval() {
      return ModEntitySounds.ambientInterval(this);
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("human_vindicator", this.isFemale());
   }

   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("human_vindicator", this.isFemale());
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_initialized")) {
         boolean female = this.getRandom().nextBoolean();
         this.setGender(female ? "female" : "male");
         this.setSkinIndex(this.getRandom().nextInt(female ? 8 : 8) + 1);
         data.putBoolean("villagerpatrols_initialized", true);
      } else {
         if (data.contains("villagerpatrols_gender")) {
            this.setGender(data.getString("villagerpatrols_gender"));
         }

         if (data.contains("villagerpatrols_skin")) {
            this.setSkinIndex(data.getInt("villagerpatrols_skin"));
         }
      }

      this.setCustomName(null);
      this.setCustomNameVisible(false);
      return groupData;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("villagerpatrols_gender")) {
         this.setGender(tag.getString("villagerpatrols_gender"));
      }

      if (tag.contains("villagerpatrols_skin")) {
         this.setSkinIndex(tag.getInt("villagerpatrols_skin"));
      }

      if (tag.contains("villagerpatrols_initialized")) {
         PersistentEntityData.get(this).putBoolean("villagerpatrols_initialized", tag.getBoolean("villagerpatrols_initialized"));
      }

      if (tag.contains("villagerpatrols_group_id")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_id", tag.getString("villagerpatrols_group_id"));
      }

      if (tag.contains("villagerpatrols_group_type")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_type", tag.getString("villagerpatrols_group_type"));
      }

      if (tag.contains("villagerpatrols_group_role")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_role", tag.getString("villagerpatrols_group_role"));
      }

      if (tag.contains("villagerpatrols_group_spawned_companions")) {
         PersistentEntityData.get(this).putBoolean("villagerpatrols_group_spawned_companions", tag.getBoolean("villagerpatrols_group_spawned_companions"));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("villagerpatrols_gender", this.getGender());
      tag.putInt("villagerpatrols_skin", this.getSkinIndex());
      tag.putBoolean("villagerpatrols_initialized", PersistentEntityData.get(this).getBoolean("villagerpatrols_initialized"));
      if (PersistentEntityData.get(this).contains("villagerpatrols_group_id")) {
         tag.putString("villagerpatrols_group_id", PersistentEntityData.get(this).getString("villagerpatrols_group_id"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_type")) {
         tag.putString("villagerpatrols_group_type", PersistentEntityData.get(this).getString("villagerpatrols_group_type"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_role")) {
         tag.putString("villagerpatrols_group_role", PersistentEntityData.get(this).getString("villagerpatrols_group_role"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_spawned_companions")) {
         tag.putBoolean("villagerpatrols_group_spawned_companions", PersistentEntityData.get(this).getBoolean("villagerpatrols_group_spawned_companions"));
      }
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

   public void setGroupData(UUID groupId, String groupType, String groupRole) {
      this.setGroupData(groupId.toString(), groupType, groupRole);
   }

   public void setGroupData(String groupId, String groupType, String groupRole) {
      CompoundTag data = PersistentEntityData.get(this);
      data.putString("villagerpatrols_group_id", groupId);
      data.putString("villagerpatrols_group_type", groupType);
      data.putString("villagerpatrols_group_role", groupRole);
   }

   public void setSpawnedCompanions(boolean value) {
      PersistentEntityData.get(this).putBoolean("villagerpatrols_group_spawned_companions", value);
   }

   protected ResourceLocation getDefaultLootTable() {
      return DEFAULT_LOOT_TABLE;
   }
}
