package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.entity.AbstractKnightEntity;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.HumanEvokerEntity;
import com.javic.villagerpatrols.entity.HumanPillagerEntity;
import com.javic.villagerpatrols.entity.HumanVindicatorEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.spawn.SpawnSurfaceValidator;
import com.javic.villagerpatrols.ward.WardBannerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

abstract class AbstractSkirmish {
   private static final TagKey<Biome> NO_COMMON_SKIRMISHES = biomeTag("no_common_skirmishes");
   private static final TagKey<Biome> NO_ORC_SKIRMISHES = biomeTag("no_orc_skirmishes");
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final int POSITION_ATTEMPTS = 64;

   abstract String groupType();

   abstract int selectionWeight();

   abstract int minPlayerDistance();

   abstract int maxPlayerDistance();

   abstract int minSideSeparation();

   abstract int maxSideSeparation();

   abstract int sideClusterRadius();

   abstract int proximityRadius();

   abstract int villageAvoidanceRadiusSections();

   protected abstract List<AbstractSkirmish.EntitySpawnDefinition> buildFriendlySide(ServerLevel var1);

   protected abstract List<AbstractSkirmish.EntitySpawnDefinition> buildEnemySide(ServerLevel var1);

   protected boolean canStart(ServerLevel level) {
      return true;
   }

   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return true;
   }

   protected final boolean canUseCommonSkirmishBiome(ServerLevel level, BlockPos pos) {
      return !level.getBiome(pos).is(NO_COMMON_SKIRMISHES);
   }

   protected final boolean canUseOrcSkirmishBiome(ServerLevel level, BlockPos pos) {
      return !level.getBiome(pos).is(NO_ORC_SKIRMISHES);
   }

   protected void afterSpawn(ServerLevel level, Mob entity, AbstractSkirmish.SpawnPlacement placement) {
   }

   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractSkirmish.SpawnPlacement placement, String groupId) {
   }

   final AbstractSkirmish.SkirmishPlan createPlan(ServerLevel level, ServerPlayer player, Predicate<BlockPos> centerValidator) {
      if (!this.canStart(level)) {
         return null;
      }

      float angle = level.random.nextFloat() * (float) (Math.PI * 2);
      int radius = this.randomBetween(level, Config.skirmishMinPlayerDistance(), Config.skirmishMaxPlayerDistance());
      int centerX = Mth.floor(player.getX() + Math.cos(angle) * radius);
      int centerZ = Mth.floor(player.getZ() + Math.sin(angle) * radius);
      BlockPos center = new BlockPos(centerX, player.blockPosition().getY(), centerZ);
      if (!centerValidator.test(center)) {
         return null;
      }

      float battleAngle = level.random.nextFloat() * (float) (Math.PI * 2);
      int separation = this.randomBetween(level, this.minSideSeparation(), this.maxSideSeparation());
      int offsetX = this.resolveOffset((float)Math.cos(battleAngle) * separation * 0.5F);
      int offsetZ = this.resolveOffset((float)Math.sin(battleAngle) * separation * 0.5F);
      BlockPos friendlyAnchor = center.offset(offsetX, 0, offsetZ);
      BlockPos enemyAnchor = center.offset(-offsetX, 0, -offsetZ);
      List<AbstractSkirmish.SpawnPlacement> friendlyPlacements = this.findPlacements(level, friendlyAnchor, this.buildFriendlySide(level));
      if (friendlyPlacements == null) {
         return null;
      }

      List<AbstractSkirmish.SpawnPlacement> enemyPlacements = this.findPlacements(level, enemyAnchor, this.buildEnemySide(level));
      return enemyPlacements == null ? null : new AbstractSkirmish.SkirmishPlan(friendlyPlacements, enemyPlacements, UUID.randomUUID().toString());
   }

   final boolean spawnPlan(ServerLevel level, AbstractSkirmish.SkirmishPlan plan) {
      String friendlyGroupId = plan.skirmishId() + ":friendly";
      String enemyGroupId = plan.skirmishId() + ":enemy";
      List<Mob> spawnedEntities = new ArrayList<>();

      for (AbstractSkirmish.SpawnPlacement placement : plan.friendlyPlacements()) {
         Mob entity = this.spawnEntity(level, placement, friendlyGroupId);
         if (entity == null) {
            this.cleanupSpawnedEntities(spawnedEntities);
            return false;
         }

         spawnedEntities.add(entity);
      }

      for (AbstractSkirmish.SpawnPlacement placement : plan.enemyPlacements()) {
         Mob entity = this.spawnEntity(level, placement, enemyGroupId);
         if (entity == null) {
            this.cleanupSpawnedEntities(spawnedEntities);
            return false;
         }

         spawnedEntities.add(entity);
      }

      return true;
   }

   protected final int randomBetween(ServerLevel level, int minInclusive, int maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + level.random.nextInt(maxInclusive - minInclusive + 1);
   }

   private List<AbstractSkirmish.SpawnPlacement> findPlacements(ServerLevel level, BlockPos anchor, List<AbstractSkirmish.EntitySpawnDefinition> definitions) {
      List<AbstractSkirmish.SpawnPlacement> placements = new ArrayList<>();
      List<Vec3> occupied = new ArrayList<>();

      for (AbstractSkirmish.EntitySpawnDefinition definition : definitions) {
         AbstractSkirmish.SpawnPlacement placement = this.findPlacement(level, anchor, definition, occupied);
         if (placement == null) {
            return null;
         }

         placements.add(placement);
         occupied.add(Vec3.atBottomCenterOf(placement.pos()));
      }

      return placements;
   }

   private AbstractSkirmish.SpawnPlacement findPlacement(
      ServerLevel level, BlockPos anchor, AbstractSkirmish.EntitySpawnDefinition definition, List<Vec3> occupied
   ) {
      for (int attempt = 0; attempt < 64; attempt++) {
         int offsetX = level.random.nextInt(this.sideClusterRadius() * 2 + 1) - this.sideClusterRadius();
         int offsetZ = level.random.nextInt(this.sideClusterRadius() * 2 + 1) - this.sideClusterRadius();
         BlockPos base = anchor.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(base)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, base);
            if (this.isSpawnPositionValid(level, surface, definition.type(), occupied)) {
               return new AbstractSkirmish.SpawnPlacement(definition.type(), definition.role(), surface);
            }
         }
      }

      return null;
   }

   private boolean isSpawnPositionValid(ServerLevel level, BlockPos surface, EntityType<? extends Mob> type, List<Vec3> occupied) {
      int chunkX = SectionPos.blockToSectionCoord(surface.getX());
      int chunkZ = SectionPos.blockToSectionCoord(surface.getZ());
      if (!level.hasChunk(chunkX, chunkZ)) {
         return false;
      }

      if (!level.isLoaded(surface) || !level.isLoaded(surface.above())) {
         return false;
      }

      if (WardBannerManager.isBlocked(level, surface)) {
         return false;
      }

      if (!this.canUseBiome(level, surface)) {
         return false;
      }

      if (!level.canSeeSkyFromBelowWater(surface)) {
         return false;
      }

      if (!level.getBlockState(surface.below()).blocksMotion()) {
         return false;
      }

      if (!SpawnSurfaceValidator.hasValidSpawnSupport(level, surface)) {
         return false;
      }

      if (level.getFluidState(surface).isEmpty() && level.getFluidState(surface.above()).isEmpty()) {
         if (!SpawnSurfaceValidator.isSurfaceHeightSafe(level, surface)) {
            return false;
         }

         Vec3 spawnPos = Vec3.atBottomCenterOf(surface);

         for (Vec3 occupiedPos : occupied) {
            if (occupiedPos.distanceToSqr(spawnPos) < 2.25) {
               return false;
            }
         }

         Mob entity = (Mob)type.create(level);
         if (entity == null) {
            return false;
         }

         entity.moveTo(spawnPos.x, surface.getY(), spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
         return level.noCollision(entity);
      } else {
         return false;
      }
   }

   private Mob spawnEntity(ServerLevel level, AbstractSkirmish.SpawnPlacement placement, String groupId) {
      Mob entity = (Mob)placement.type().create(level);
      if (entity == null) {
         return null;
      }

      BlockPos pos = placement.pos();
      entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
      entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
      this.applyGroupData(entity, groupId, placement.role());
      this.markEventEntity(level, entity);
      this.markSpawnedCompanions(entity);
      this.prepareSpawnedEntity(level, entity, placement, groupId);
      if (!level.addFreshEntity(entity)) {
         return null;
      }

      this.afterSpawn(level, entity, placement);
      return entity;
   }

   private void markEventEntity(ServerLevel level, Mob entity) {
      PersistentEntityData.get(entity).putBoolean("villagerpatrols_event_entity", true);
      PersistentEntityData.get(entity).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
   }

   private void applyGroupData(Mob entity, String groupId, String groupRole) {
      PersistentEntityData.get(entity).putString("villagerpatrols_group_id", groupId);
      PersistentEntityData.get(entity).putString("villagerpatrols_group_type", this.groupType());
      PersistentEntityData.get(entity).putString("villagerpatrols_group_role", groupRole);
      if (entity instanceof AbstractKnightEntity knight) {
         knight.setGroupData(groupId, this.groupType(), groupRole);
      } else if (entity instanceof HumanPillagerEntity humanPillager) {
         humanPillager.setGroupData(groupId, this.groupType(), groupRole);
      } else if (entity instanceof HumanVindicatorEntity humanVindicator) {
         humanVindicator.setGroupData(groupId, this.groupType(), groupRole);
      } else if (entity instanceof HumanEvokerEntity humanEvoker) {
         humanEvoker.setGroupData(groupId, this.groupType(), groupRole);
      } else if (entity instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setGroupData(groupId, this.groupType(), groupRole);
      } else if (entity instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setGroupData(groupId, this.groupType(), groupRole);
      }
   }

   private void markSpawnedCompanions(Mob entity) {
      if (entity instanceof AbstractKnightEntity knight) {
         knight.setSpawnedCompanions(true);
      } else if (entity instanceof HumanPillagerEntity humanPillager) {
         humanPillager.setSpawnedCompanions(true);
      } else if (entity instanceof HumanVindicatorEntity humanVindicator) {
         humanVindicator.setSpawnedCompanions(true);
      } else if (entity instanceof HumanEvokerEntity humanEvoker) {
         humanEvoker.setSpawnedCompanions(true);
      }
   }

   private void cleanupSpawnedEntities(List<Mob> spawnedEntities) {
      for (Mob entity : spawnedEntities) {
         if (entity != null && !entity.isRemoved()) {
            entity.discard();
         }
      }
   }

   private int resolveOffset(float value) {
      int rounded = Math.round(value);
      if (rounded != 0) {
         return rounded;
      } else {
         return value >= 0.0F ? 1 : -1;
      }
   }

   private static TagKey<Biome> biomeTag(String path) {
      return TagKey.create(Registries.BIOME, new ResourceLocation("villagerpatrols", path));
   }

   protected record EntitySpawnDefinition(EntityType<? extends Mob> type, String role) {
   }

   record SkirmishPlan(List<AbstractSkirmish.SpawnPlacement> friendlyPlacements, List<AbstractSkirmish.SpawnPlacement> enemyPlacements, String skirmishId) {
   }

   protected record SpawnPlacement(EntityType<? extends Mob> type, String role, BlockPos pos) {
   }
}
