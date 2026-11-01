package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.spawn.PlayerAreaPresenceTracker;
import com.javic.villagerpatrols.spawn.SessionSpawnDelayManager;
import com.javic.villagerpatrols.ward.WardBannerManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SkirmishManager {
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final int PLAYER_SEARCH_ATTEMPTS = 8;
   private static final PlayerAreaPresenceTracker.PresenceProfile SKIRMISH_STATIONARY_PROFILE = new PlayerAreaPresenceTracker.PresenceProfile(3, 45, 60, 6000L);
   private static final int CLEANUP_CHECK_INTERVAL_TICKS = 200;
   private static final int CLEANUP_MINIMUM_AGE_TICKS = 2400;
   private static final double CLEANUP_SAFE_PLAYER_RADIUS = 128.0;
   private final Map<ResourceKey<Level>, Long> nextAttemptByLevel = new HashMap<>();
   private final Map<ResourceKey<Level>, Long> nextCleanupByLevel = new HashMap<>();
   private final PlayerAreaPresenceTracker areaPresenceTracker = new PlayerAreaPresenceTracker();
   private final List<AbstractSkirmish> skirmishes = List.of(
      new KnightIllagerSkirmish(),
      new KnightEvilKnightSkirmish(),
      new KnightZombieSkirmish(),
      new KnightOrcSkirmish(),
      new ElfOrcSkirmish(),
      new DwarfOrcSkirmish(),
      new EvilKnightOrcSkirmish(),
      new KnightBanditSkirmish(),
      new EvilKnightElfSkirmish(),
      new EvilKnightDwarfSkirmish(),
      new VillagerIllagerSkirmish(),
      new VillagerBanditSkirmish(),
      new VillagerOrcSkirmish(),
      new VillagerZombieSkirmish(),
      new KnightZombieSkirmish(),
      new VillagerSpiderSkirmish(),
      new WizardEvilWizardSkirmish()
   );

   public void tick(ServerLevel serverLevel) {
      if (((com.javic.villagerpatrols.persistence.ServerTimeAccess) serverLevel.getServer()).villagerpatrols$haveTime()) {
         if (serverLevel.dimension() == Level.OVERWORLD) {
            ResourceKey<Level> dimension = serverLevel.dimension();
            long gameTime = serverLevel.getGameTime();
            this.tryCleanup(serverLevel, dimension, gameTime);
            if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
               if ((Boolean)Config.ENABLE_SKIRMISHES.get()) {
                  if (!SessionSpawnDelayManager.isWorldEventAndSkirmishDelayActive(serverLevel.getServer())) {
                     long nextAttempt = this.nextAttemptByLevel.getOrDefault(dimension, 0L);
                     if (gameTime >= nextAttempt) {
                        long delay = this.trySpawnSkirmish(serverLevel)
                           ? this.randomBetween(serverLevel, Config.skirmishSuccessCooldownMinTicks(), Config.skirmishSuccessCooldownMaxTicks())
                           : this.randomBetween(serverLevel, Config.skirmishAttemptDelayMinTicks(), Config.skirmishAttemptDelayMaxTicks());
                        this.nextAttemptByLevel.put(dimension, gameTime + delay);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean trySpawnSkirmish(ServerLevel level) {
      List<ServerPlayer> players = level.getPlayers(playerx -> playerx.isAlive() && !playerx.isSpectator());
      if (players.isEmpty()) {
         return false;
      }

      List<AbstractSkirmish> availableSkirmishes = this.skirmishes
         .stream()
         .filter(skirmishx -> Config.skirmishWeight(skirmishx.groupType()) > 0)
         .filter(skirmishx -> skirmishx.canStart(level))
         .toList();
      if (availableSkirmishes.isEmpty()) {
         return false;
      }

      for (int attempt = 0; attempt < 8; attempt++) {
         ServerPlayer player = players.get(level.random.nextInt(players.size()));
         if (!this.areaPresenceTracker.isBlocked(level, player, level.getGameTime(), SKIRMISH_STATIONARY_PROFILE)) {
            AbstractSkirmish skirmish = this.pickSkirmish(level, availableSkirmishes);
            AbstractSkirmish.SkirmishPlan plan = skirmish.createPlan(level, player, center -> this.isValidCenter(level, center, skirmish));
            if (plan != null && skirmish.spawnPlan(level, plan)) {
               return true;
            }
         }
      }

      return false;
   }

   public void unload(ServerLevel serverLevel) {
      ResourceKey<Level> dimension = serverLevel.dimension();
      this.nextAttemptByLevel.remove(dimension);
      this.nextCleanupByLevel.remove(dimension);
      this.areaPresenceTracker.clearLevel(dimension);
   }

   private boolean isValidCenter(ServerLevel level, BlockPos center, AbstractSkirmish skirmish) {
      return level.isLoaded(center)
         && !WardBannerManager.isBlocked(level, center)
         && !level.isCloseToVillage(center, skirmish.villageAvoidanceRadiusSections())
         && !this.hasNearbySkirmish(level, center, skirmish.proximityRadius());
   }

   private void tryCleanup(ServerLevel level, ResourceKey<Level> dimension, long gameTime) {
      long nextCleanup = this.nextCleanupByLevel.getOrDefault(dimension, 0L);
      if (gameTime >= nextCleanup) {
         this.nextCleanupByLevel.put(dimension, gameTime + 200L);
         List<Entity> entities = new ArrayList<>();

         for (Entity entity : level.getAllEntities()) {
            entities.add(entity);
         }

         for (Entity entity : entities) {
            if (this.isCleanupCandidate(level, entity, gameTime)) {
               entity.discard();
            }
         }
      }
   }

   private boolean isCleanupCandidate(ServerLevel level, Entity entity, long gameTime) {
      if (entity != null && !entity.isRemoved()) {
         if (!PersistentEntityData.get(entity).getBoolean("villagerpatrols_event_entity")) {
            return false;
         }

         if (!this.isSkirmishType(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))) {
            return false;
         }

         long spawnTime = PersistentEntityData.get(entity).getLong("villagerpatrols_event_spawn_time");
         return gameTime - spawnTime < 2400L ? false : level.getNearestPlayer(entity, 128.0) == null;
      } else {
         return false;
      }
   }

   private boolean hasNearbySkirmish(ServerLevel level, BlockPos center, int radius) {
      AABB searchBox = AABB.ofSize(Vec3.atCenterOf(center), radius * 2.0, 96.0, radius * 2.0);
      return !level.getEntitiesOfClass(Entity.class, searchBox, this::isSkirmishEntity).isEmpty();
   }

   private boolean isSkirmishEntity(Entity entity) {
      return entity != null && !entity.isRemoved() ? this.isSkirmishType(PersistentEntityData.get(entity).getString("villagerpatrols_group_type")) : false;
   }

   private boolean isSkirmishType(String groupType) {
      for (AbstractSkirmish skirmish : this.skirmishes) {
         if (skirmish.groupType().equals(groupType)) {
            return true;
         }
      }

      return false;
   }

   private AbstractSkirmish pickSkirmish(ServerLevel level, List<AbstractSkirmish> availableSkirmishes) {
      int totalWeight = 0;

      for (AbstractSkirmish skirmish : availableSkirmishes) {
         totalWeight += Config.skirmishWeight(skirmish.groupType());
      }

      int selectedWeight = level.random.nextInt(totalWeight);
      int currentWeight = 0;

      for (AbstractSkirmish skirmish : availableSkirmishes) {
         currentWeight += Config.skirmishWeight(skirmish.groupType());
         if (selectedWeight < currentWeight) {
            return skirmish;
         }
      }

      return availableSkirmishes.get(0);
   }

   private int randomBetween(ServerLevel level, int minInclusive, int maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + level.random.nextInt(maxInclusive - minInclusive + 1);
   }
}
