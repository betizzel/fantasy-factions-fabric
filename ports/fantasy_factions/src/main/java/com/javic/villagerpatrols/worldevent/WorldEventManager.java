package com.javic.villagerpatrols.worldevent;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WorldEventManager {
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final int PLAYER_SEARCH_ATTEMPTS = 8;
   private static final int VILLAGE_PRESENCE_RADIUS_SECTIONS = 3;
   private static final PlayerAreaPresenceTracker.PresenceProfile WORLD_EVENT_STATIONARY_PROFILE = new PlayerAreaPresenceTracker.PresenceProfile(
      1, 70, 48, 6000L
   );
   private static final PlayerAreaPresenceTracker.PresenceProfile WORLD_EVENT_VILLAGE_STATIONARY_PROFILE = new PlayerAreaPresenceTracker.PresenceProfile(
      2, 50, 36, 9600L
   );
   private static final int CLEANUP_CHECK_INTERVAL_TICKS = 200;
   private static final int CLEANUP_MINIMUM_AGE_TICKS = 2400;
   private static final double CLEANUP_SAFE_PLAYER_RADIUS = 128.0;
   private final Map<ResourceKey<Level>, Long> nextAttemptByLevel = new HashMap<>();
   private final Map<ResourceKey<Level>, Long> nextCleanupByLevel = new HashMap<>();
   private final PlayerAreaPresenceTracker areaPresenceTracker = new PlayerAreaPresenceTracker();
   private final List<AbstractWorldEvent> worldEvents = List.of(
      new HumanWitchWorldEvent(),
      new ZombieHordeWorldEvent(),
      new HealerWorldEvent(),
      new BardWorldEvent(),
      new FishingVillagerWorldEvent(),
      new TravelerGroupWorldEvent(),
      new ArmedTravelerGroupWorldEvent(),
      new TravelerFamilyWorldEvent(),
      new LoneDesertTravelerWorldEvent(),
      new LoneIceTravelerWorldEvent(),
      new WanderingTraderMeetingWorldEvent(),
      new VillagerMinerWorldEvent(),
      new ShepherdMovingAnimalsWorldEvent(),
      new CampingVillagersWorldEvent(),
      new EvilKnightPrisonersWorldEvent(),
      new MountedVillagerWorldEvent(),
      new MountedKnightWorldEvent(),
      new MountedEvilKnightWorldEvent(),
      new MountedIllagerWorldEvent(),
      new MountedCamelVillagerWorldEvent(),
      new DesertCamelTravelerWorldEvent(),
      new ShepherdDonkeyTravelerWorldEvent(),
      new MountedRavagerIllagerWorldEvent()
   );

   public void tick(ServerLevel serverLevel) {
      if (((com.javic.villagerpatrols.persistence.ServerTimeAccess) serverLevel.getServer()).villagerpatrols$haveTime()) {
         if (serverLevel.dimension() == Level.OVERWORLD) {
            ResourceKey<Level> dimension = serverLevel.dimension();
            long gameTime = serverLevel.getGameTime();
            this.tryCleanup(serverLevel, dimension, gameTime);
            if ((Boolean)Config.ENABLE_WORLD_EVENTS.get()) {
               if (!SessionSpawnDelayManager.isWorldEventAndSkirmishDelayActive(serverLevel.getServer())) {
                  long nextAttempt = this.nextAttemptByLevel.getOrDefault(dimension, 0L);
                  if (gameTime >= nextAttempt) {
                     long delay = this.trySpawnWorldEvent(serverLevel)
                        ? this.randomBetween(serverLevel, Config.worldEventSuccessCooldownMinTicks(), Config.worldEventSuccessCooldownMaxTicks())
                        : this.randomBetween(serverLevel, Config.worldEventAttemptDelayMinTicks(), Config.worldEventAttemptDelayMaxTicks());
                     this.nextAttemptByLevel.put(dimension, gameTime + delay);
                  }
               }
            }
         }
      }
   }

   private boolean trySpawnWorldEvent(ServerLevel level) {
      List<ServerPlayer> players = level.getPlayers(playerx -> playerx.isAlive() && !playerx.isSpectator());
      if (players.isEmpty()) {
         return false;
      }

      List<AbstractWorldEvent> availableWorldEvents = this.worldEvents
         .stream()
         .filter(worldEventx -> Config.worldEventWeight(worldEventx.groupType()) > 0)
         .filter(worldEventx -> this.canSpawnInCurrentDifficulty(level, worldEventx.groupType()))
         .toList();
      if (availableWorldEvents.isEmpty()) {
         return false;
      }

      for (int attempt = 0; attempt < 8; attempt++) {
         ServerPlayer player = players.get(level.random.nextInt(players.size()));
         if (!this.isBlockedByCamping(level, player, level.getGameTime())) {
            AbstractWorldEvent worldEvent = this.pickWorldEvent(level, availableWorldEvents);
            if (worldEvent.canStart(level)) {
               AbstractWorldEvent.WorldEventPlan plan = worldEvent.createPlan(level, player, center -> this.isValidCenter(level, center, worldEvent));
               if (plan != null) {
                  worldEvent.spawnPlan(level, plan);
                  return true;
               }
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

   private boolean isValidCenter(ServerLevel level, BlockPos center, AbstractWorldEvent worldEvent) {
      return level.isLoaded(center)
         && !WardBannerManager.isBlocked(level, center)
         && !level.isCloseToVillage(center, worldEvent.villageAvoidanceRadiusSections())
         && !this.hasNearbyWorldEvent(level, center, worldEvent.groupType(), worldEvent.proximityRadius());
   }

   private boolean isBlockedByCamping(ServerLevel level, ServerPlayer player, long gameTime) {
      return this.areaPresenceTracker
         .isBlocked(level, player, gameTime, this.isVillagePlayer(level, player) ? WORLD_EVENT_VILLAGE_STATIONARY_PROFILE : WORLD_EVENT_STATIONARY_PROFILE);
   }

   private boolean isVillagePlayer(ServerLevel level, ServerPlayer player) {
      return level.isCloseToVillage(player.blockPosition(), 3);
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
         } else if (!this.isWorldEventType(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))) {
            return false;
         } else if (entity instanceof Mob mob
            && "wandering_trader_meeting_world_event".equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
            && WanderingTraderMeetingWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else if (entity instanceof Mob mob
            && "camping_villagers_world_event".equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
            && CampingVillagersWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else if (entity instanceof Mob mob
            && "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
            && EvilKnightPrisonersWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else if (entity instanceof Mob mob && MountedVillagerWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else if (entity instanceof Mob mob
            && "desert_camel_traveler_world_event".equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
            && DesertCamelTravelerWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else if (entity instanceof Mob mob
            && "shepherd_donkey_traveler_world_event".equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
            && ShepherdDonkeyTravelerWorldEventController.shouldCleanupBrokenEvent(level, mob)) {
            return true;
         } else {
            long spawnTime = PersistentEntityData.get(entity).getLong("villagerpatrols_event_spawn_time");
            return gameTime - spawnTime < 2400L ? false : level.getNearestPlayer(entity, 128.0) == null;
         }
      } else {
         return false;
      }
   }

   private boolean hasNearbyWorldEvent(ServerLevel level, BlockPos center, String groupType, int radius) {
      AABB searchBox = AABB.ofSize(Vec3.atCenterOf(center), radius * 2.0, 96.0, radius * 2.0);
      return !level.getEntitiesOfClass(Entity.class, searchBox, entity -> this.isMatchingWorldEvent(entity, groupType)).isEmpty();
   }

   private boolean isMatchingWorldEvent(Entity entity, String groupType) {
      return entity != null && !entity.isRemoved() ? groupType.equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type")) : false;
   }

   private boolean canSpawnInCurrentDifficulty(ServerLevel level, String groupType) {
      return level.getDifficulty() != Difficulty.PEACEFUL || !"zombie_horde_world_event".equals(groupType) && !"human_witch_world_event".equals(groupType);
   }

   private boolean isWorldEventType(String groupType) {
      for (AbstractWorldEvent worldEvent : this.worldEvents) {
         if (worldEvent.groupType().equals(groupType)) {
            return true;
         }
      }

      return false;
   }

   private AbstractWorldEvent pickWorldEvent(ServerLevel level, List<AbstractWorldEvent> availableWorldEvents) {
      int totalWeight = 0;

      for (AbstractWorldEvent worldEvent : availableWorldEvents) {
         totalWeight += Config.worldEventWeight(worldEvent.groupType());
      }

      int selectedWeight = level.random.nextInt(totalWeight);
      int currentWeight = 0;

      for (AbstractWorldEvent worldEvent : availableWorldEvents) {
         currentWeight += Config.worldEventWeight(worldEvent.groupType());
         if (selectedWeight < currentWeight) {
            return worldEvent;
         }
      }

      return availableWorldEvents.get(0);
   }

   private int randomBetween(ServerLevel level, int minInclusive, int maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + level.random.nextInt(maxInclusive - minInclusive + 1);
   }
}
