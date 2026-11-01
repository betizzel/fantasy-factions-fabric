package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class FemaleHealerEntity extends FemaleVillagerEntity {
   private static final String TAG_HEALER_INITIALIZED = "villagerpatrols_healer_initialized";

   public FemaleHealerEntity(EntityType<? extends Villager> type, Level level) {
      super(type, level);
      HealerEntitySupport.applyHealerCompatibility(this, true, this.getSkinIndex(), false);
   }

   public static Builder createAttributes() {
      return FemaleVillagerEntity.createAttributes().add(Attributes.MAX_HEALTH, 36.0);
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_healer_initialized")) {
         this.setSkinIndex(this.getRandom().nextInt(12) + 1);
         data.putBoolean("villagerpatrols_healer_initialized", true);
      }

      HealerEntitySupport.applyHealerCompatibility(this, true, this.getSkinIndex(), true);
      this.setCustomNameVisible(false);
      return groupData;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      HealerEntitySupport.tickHealerVillager(this);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      HealerEntitySupport.applyHealerCompatibility(this, true, this.getSkinIndex(), false);
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return HealerEntitySupport.getTexture(true, this.getSkinIndex());
   }
}
