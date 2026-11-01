package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.Config;
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

abstract class AbstractWorldEvent {
   private static final TagKey<Biome> BARD_WORLD_EVENTS = biomeTag("bard_world_events");
   private static final TagKey<Biome> DESERT_WORLD_EVENTS = biomeTag("desert_world_events");
   private static final TagKey<Biome> LONE_DESERT_TRAVELER_WORLD_EVENTS = biomeTag("lone_desert_traveler_world_events");
   private static final TagKey<Biome> LONE_ICE_TRAVELER_WORLD_EVENTS = biomeTag("lone_ice_traveler_world_events");
   private static final TagKey<Biome> WITCH_WORLD_EVENTS = biomeTag("witch_world_events");
   private static final TagKey<Biome> NO_COMMON_WORLD_EVENTS = biomeTag("no_common_world_events");
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

   abstract int minCount();

   abstract int maxCount();

   abstract int clusterRadius();

   abstract int proximityRadius();

   abstract int villageAvoidanceRadiusSections();

   protected abstract EntityType<? extends Mob> entityType();

   protected abstract String groupRole();

   protected boolean canStart(ServerLevel level) {
      return true;
   }

   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return true;
   }

   protected boolean isExtraSpawnPositionValid(ServerLevel level, BlockPos surface) {
      return true;
   }

   protected boolean requiresSkyAccess() {
      return true;
   }

   protected boolean enforceSurfaceHeightSafety() {
      return this.requiresSkyAccess();
   }

   protected int effectiveMinPlayerDistance() {
      return Config.worldEventMinPlayerDistance();
   }

   protected int effectiveMaxPlayerDistance() {
      return Config.worldEventMaxPlayerDistance();
   }

   protected BlockPos findSpawnPosition(ServerLevel level, BlockPos anchor, EntityType<? extends Mob> type, List<Vec3> occupied) {
      for (int attempt = 0; attempt < 64; attempt++) {
         int offsetX = level.random.nextInt(this.clusterRadius() * 2 + 1) - this.clusterRadius();
         int offsetZ = level.random.nextInt(this.clusterRadius() * 2 + 1) - this.clusterRadius();
         BlockPos base = anchor.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(base)) {
            BlockPos surface = this.resolveSpawnPosition(level, base);
            if (this.isSpawnPositionValid(level, surface, type, occupied)) {
               return surface;
            }
         }
      }

      return null;
   }

   protected BlockPos resolveSpawnPosition(ServerLevel level, BlockPos base) {
      return level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, base);
   }

   protected final boolean canUseBardWorldEventBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(BARD_WORLD_EVENTS);
   }

   protected final boolean canUseDesertWorldEventBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(DESERT_WORLD_EVENTS);
   }

   protected final boolean canUseLoneDesertTravelerWorldEventBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(LONE_DESERT_TRAVELER_WORLD_EVENTS);
   }

   protected final boolean canUseLoneIceTravelerWorldEventBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(LONE_ICE_TRAVELER_WORLD_EVENTS);
   }

   protected final boolean canUseWitchWorldEventBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(WITCH_WORLD_EVENTS);
   }

   protected final boolean canUseCommonWorldEventBiome(ServerLevel level, BlockPos pos) {
      return !level.getBiome(pos).is(NO_COMMON_WORLD_EVENTS);
   }

   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
   }

   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
   }

   final AbstractWorldEvent.WorldEventPlan createPlan(ServerLevel level, ServerPlayer player, Predicate<BlockPos> centerValidator) {
      float angle = level.random.nextFloat() * (float) (Math.PI * 2);
      int radius = this.randomBetween(level, this.effectiveMinPlayerDistance(), this.effectiveMaxPlayerDistance());
      int centerX = Mth.floor(player.getX() + Math.cos(angle) * radius);
      int centerZ = Mth.floor(player.getZ() + Math.sin(angle) * radius);
      BlockPos center = new BlockPos(centerX, player.blockPosition().getY(), centerZ);
      if (!centerValidator.test(center)) {
         return null;
      }

      List<AbstractWorldEvent.SpawnPlacement> placements = this.findPlacements(level, center, this.buildDefinitions(level));
      return placements == null ? null : new AbstractWorldEvent.WorldEventPlan(placements, UUID.randomUUID().toString());
   }

   final void spawnPlan(ServerLevel level, AbstractWorldEvent.WorldEventPlan plan) {
      for (AbstractWorldEvent.SpawnPlacement placement : plan.placements()) {
         this.spawnEntity(level, placement, plan.groupId());
      }
   }

   protected final int randomBetween(ServerLevel level, int minInclusive, int maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + level.random.nextInt(maxInclusive - minInclusive + 1);
   }

   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      int totalMembers = this.randomBetween(level, this.minCount(), this.maxCount());
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractWorldEvent.EntitySpawnDefinition(this.entityType(), this.groupRole()));
      }

      return definitions;
   }

   private List<AbstractWorldEvent.SpawnPlacement> findPlacements(
      ServerLevel level, BlockPos anchor, List<AbstractWorldEvent.EntitySpawnDefinition> definitions
   ) {
      List<AbstractWorldEvent.SpawnPlacement> placements = new ArrayList<>();
      List<Vec3> occupied = new ArrayList<>();

      for (AbstractWorldEvent.EntitySpawnDefinition definition : definitions) {
         AbstractWorldEvent.SpawnPlacement placement = this.findPlacement(level, anchor, definition, occupied);
         if (placement == null) {
            return null;
         }

         placements.add(placement);
         occupied.add(Vec3.atBottomCenterOf(placement.pos()));
      }

      return placements;
   }

   private AbstractWorldEvent.SpawnPlacement findPlacement(
      ServerLevel level, BlockPos anchor, AbstractWorldEvent.EntitySpawnDefinition definition, List<Vec3> occupied
   ) {
      BlockPos spawnPos = this.findSpawnPosition(level, anchor, definition.type(), occupied);
      return spawnPos == null ? null : new AbstractWorldEvent.SpawnPlacement(definition.type(), definition.role(), spawnPos);
   }

   protected final boolean isSpawnPositionValid(ServerLevel level, BlockPos surface, EntityType<? extends Mob> type, List<Vec3> occupied) {
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

      if (this.requiresSkyAccess() && !level.canSeeSkyFromBelowWater(surface)) {
         return false;
      }

      if (!level.getBlockState(surface.below()).blocksMotion()) {
         return false;
      }

      if (!SpawnSurfaceValidator.hasValidSpawnSupport(level, surface)) {
         return false;
      }

      if (level.getFluidState(surface).isEmpty() && level.getFluidState(surface.above()).isEmpty()) {
         if (this.enforceSurfaceHeightSafety() && !SpawnSurfaceValidator.isSurfaceHeightSafe(level, surface)) {
            return false;
         }

         if (!this.isExtraSpawnPositionValid(level, surface)) {
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

   private void spawnEntity(ServerLevel level, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      Mob entity = (Mob)placement.type().create(level);
      if (entity != null) {
         BlockPos pos = placement.pos();
         entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
         PersistentEntityData.get(entity).putBoolean("villagerpatrols_event_entity", true);
         PersistentEntityData.get(entity).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
         PersistentEntityData.get(entity).putString("villagerpatrols_group_id", groupId);
         PersistentEntityData.get(entity).putString("villagerpatrols_group_type", this.groupType());
         PersistentEntityData.get(entity).putString("villagerpatrols_group_role", placement.role());
         this.prepareSpawnedEntity(level, entity, placement, groupId);
         level.addFreshEntity(entity);
         this.afterSpawn(level, entity, placement);
      }
   }

   private static TagKey<Biome> biomeTag(String path) {
      return TagKey.create(Registries.BIOME, new ResourceLocation("villagerpatrols", path));
   }

   protected record EntitySpawnDefinition(EntityType<? extends Mob> type, String role) {
   }

   protected record SpawnPlacement(EntityType<? extends Mob> type, String role, BlockPos pos) {
   }

   record WorldEventPlan(List<AbstractWorldEvent.SpawnPlacement> placements, String groupId) {
   }
}
