package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.ward.WardBannerManager;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class EvilKnightEntity extends AbstractKnightEntity {
   public EvilKnightEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 8;
   }

   public static Builder createAttributes() {
      return createSwordKnightAttributes();
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      if (reason == MobSpawnType.NATURAL && !this.hasGroupId() && !this.hasSpawnedCompanions()) {
         this.spawnEvilKnightPatrol();
      }

      return groupData;
   }

   @Override
   protected String getTextureFolder() {
      return "evil_knight";
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
   protected boolean useBiasedGenderRoll() {
      return true;
   }

   @Override
   protected float getBiasedFemaleChance() {
      return 0.1F;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return this.createEvilKnightMainHandItemStack();
   }

   private void spawnEvilKnightPatrol() {
      ServerLevel serverLevel = this.getServerLevel();
      if (serverLevel != null) {
         if (WardBannerManager.isBlocked(serverLevel, this.blockPosition())) {
            this.setSpawnedCompanions(true);
         } else if (this.hasNearbyPatrolOfType("evil_knight_patrol", 48.0)) {
            this.setSpawnedCompanions(true);
         } else {
            UUID groupId = UUID.randomUUID();
            this.setGroupData(groupId, "evil_knight_patrol", "soldier");
            this.setSpawnedCompanions(true);
            int totalMembers = 4 + this.getRandom().nextInt(7);
            boolean includeLeader = this.getRandom().nextFloat() < 0.3F;
            int companionsToSpawn = totalMembers - 1;
            boolean leaderSpawned = false;

            for (int i = 0; i < companionsToSpawn; i++) {
               AbstractKnightEntity companion;
               String role;
               if (includeLeader && !leaderSpawned) {
                  companion = (AbstractKnightEntity)((EntityType)ModEntities.EVIL_KNIGHT_LEADER.get()).create(serverLevel);
                  role = "leader";
                  leaderSpawned = true;
               } else if (this.getRandom().nextBoolean()) {
                  companion = (AbstractKnightEntity)((EntityType)ModEntities.EVIL_KNIGHT_SHIELD.get()).create(serverLevel);
                  role = "shield";
               } else {
                  companion = (AbstractKnightEntity)((EntityType)ModEntities.EVIL_KNIGHT.get()).create(serverLevel);
                  role = "soldier";
               }

               if (companion != null) {
                  BlockPos spawnPos = this.findCompanionSpawnPos(companion);
                  if (spawnPos != null) {
                     companion.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, this.getYRot(), this.getXRot());
                     companion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
                     companion.setGroupData(groupId, "evil_knight_patrol", role);
                     companion.setSpawnedCompanions(true);
                     serverLevel.addFreshEntity(companion);
                  }
               }
            }
         }
      }
   }
}
