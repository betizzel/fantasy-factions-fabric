package com.javic.villagerpatrols.village;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.AbstractKnightEntity;
import com.javic.villagerpatrols.entity.VillageGuardEntity;
import com.javic.villagerpatrols.entity.VillageGuardLeaderEntity;
import com.javic.villagerpatrols.spawn.SpawnSurfaceValidator;
import com.javic.villagerpatrols.ward.WardBannerManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VillageGuardManager {
   private static final String TAG_VILLAGE_GUARD = "villagerpatrols_village_guard";
   private static final String TAG_VILLAGE_GUARD_SPAWN_TIME = "villagerpatrols_village_guard_spawn_time";
   private static final String TAG_VILLAGE_GUARD_ROLE = "villagerpatrols_village_guard_role";
   private static final String GROUP_TYPE = "village_guard_group";
   private static final String LEADER_ROLE = "village_guard_leader";
   private static final String GUARD_ROLE = "village_guard";
   private static final int MAX_GUARDS = 4;
   private static final int MAX_LEADERS = 1;
   private static final int MAX_TOTAL_GUARDS = 5;
   private static final int PLAYER_SEARCH_ATTEMPTS = 8;
   private static final int VILLAGE_SEARCH_RADIUS = 48;
   private static final int VILLAGE_RADIUS_SECTIONS = 6;
   private static final int GUARD_COUNT_RADIUS = 64;
   private static final int MANAGED_GUARD_AREA_RADIUS = 96;
   private static final int SPAWN_ANCHOR_SEARCH_RADIUS = 48;
   private static final int SPAWN_RADIUS = 16;
   private static final int SPAWN_POSITION_ATTEMPTS = 64;
   private static final int BELL_SEARCH_RADIUS = 64;
   private static final int RESPAWN_COOLDOWN_MIN_TICKS = 48000;
   private static final int RESPAWN_COOLDOWN_MAX_TICKS = 72000;
   private final Map<VillageGuardManager.GuardBellKey, Long> nextRespawnByBell = new HashMap<>();

   public void tick(ServerLevel serverLevel) {
      if (((com.javic.villagerpatrols.persistence.ServerTimeAccess) serverLevel.getServer()).villagerpatrols$haveTime()) {
         if (serverLevel.dimension() == Level.OVERWORLD && serverLevel.getDifficulty() != Difficulty.PEACEFUL && (Boolean)Config.ENABLE_VILLAGE_GUARDS.get()) {
            List<ServerPlayer> players = serverLevel.getPlayers(playerx -> playerx.isAlive() && !playerx.isSpectator());
            if (!players.isEmpty()) {
               long gameTime = serverLevel.getGameTime();
               VillageGuardSpawnState spawnState = (VillageGuardSpawnState)serverLevel.getDataStorage()
                  .computeIfAbsent(VillageGuardSpawnState.loader(), VillageGuardSpawnState.supplier(), VillageGuardSpawnState.dataName());

               for (int attempt = 0; attempt < 8; attempt++) {
                  ServerPlayer player = players.get(serverLevel.random.nextInt(players.size()));
                  BlockPos bellPos = this.findVillageBell(serverLevel, player);
                  if (bellPos != null && !WardBannerManager.isBlocked(serverLevel, bellPos)) {
                     String groupId = VillageGuardHome.createGroupId(serverLevel.dimension(), bellPos);
                     VillageGuardManager.GuardBellKey bellKey = new VillageGuardManager.GuardBellKey(
                        serverLevel.dimension(), bellPos.getX(), bellPos.getY(), bellPos.getZ()
                     );
                     VillageGuardManager.ManagedGuardRoster managedRoster = this.collectManagedGuardRoster(serverLevel, bellPos, 96);
                     if (this.cleanupExcessManagedGuards(bellPos, groupId, managedRoster)) {
                        managedRoster = this.collectManagedGuardRoster(serverLevel, bellPos, 96);
                     }

                     VillageGuardManager.GuardRoster roster = this.collectGuardRoster(serverLevel, bellPos, groupId);
                     if (this.cleanupDuplicateGuards(roster)) {
                        roster = this.collectGuardRoster(serverLevel, bellPos, groupId);
                     }

                     long nextRespawn = Math.max(this.nextRespawnByBell.getOrDefault(bellKey, 0L), spawnState.getNextRespawn(groupId));
                     if (gameTime >= nextRespawn) {
                        VillageGuardManager.GuardCounts counts = managedRoster.counts();
                        int missingLeader = Math.max(0, 1 - counts.leaders());
                        int missingGuards = Math.max(0, 4 - counts.guards());
                        int remainingCapacity = 5 - counts.total();
                        if (remainingCapacity > 0 && (missingLeader != 0 || missingGuards != 0)) {
                           int spawned = 0;
                           if (missingLeader > 0
                              && remainingCapacity > 0
                              && this.spawnGuard(
                                 serverLevel,
                                 bellPos,
                                 groupId,
                                 (EntityType<? extends AbstractKnightEntity>)ModEntities.VILLAGE_GUARD_LEADER.get(),
                                 "village_guard_leader"
                              )) {
                              spawned++;
                              remainingCapacity--;
                           }

                           int guardsToSpawn = Math.min(missingGuards, remainingCapacity);

                           for (int i = 0; i < guardsToSpawn; i++) {
                              if (this.spawnGuard(
                                 serverLevel, bellPos, groupId, (EntityType<? extends AbstractKnightEntity>)ModEntities.VILLAGE_GUARD.get(), "village_guard"
                              )) {
                                 spawned++;
                              }
                           }

                           if (spawned > 0) {
                              long scheduledRespawn = gameTime + this.randomBetween(serverLevel, 48000, 72000);
                              this.nextRespawnByBell.put(bellKey, scheduledRespawn);
                              spawnState.setNextRespawn(groupId, scheduledRespawn);
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos findVillageBell(ServerLevel level, ServerPlayer player) {
      BlockPos origin = player.blockPosition();
      if (level.isCloseToVillage(origin, 6)) {
         BlockPos bell = VillageGuardHome.findNearestBell(level, origin, 64);
         if (bell != null) {
            return bell;
         }
      }

      for (int attempt = 0; attempt < 8; attempt++) {
         int offsetX = level.random.nextInt(97) - 48;
         int offsetZ = level.random.nextInt(97) - 48;
         BlockPos candidate = origin.offset(offsetX, 0, offsetZ);
         if (level.isCloseToVillage(candidate, 6)) {
            BlockPos bell = VillageGuardHome.findNearestBell(level, candidate, 64);
            if (bell != null) {
               return bell;
            }
         }
      }

      return null;
   }

   private VillageGuardManager.GuardRoster collectGuardRoster(ServerLevel level, BlockPos bellPos, String groupId) {
      AABB searchBox = AABB.ofSize(Vec3.atCenterOf(bellPos), 128.0, 96.0, 128.0);
      List<VillageGuardEntity> guards = new ArrayList<>();
      List<VillageGuardLeaderEntity> leaders = new ArrayList<>();

      for (AbstractKnightEntity entity : level.getEntitiesOfClass(
         AbstractKnightEntity.class, searchBox, entityx -> this.isCountableGuard(entityx, bellPos, groupId)
      )) {
         if (entity instanceof VillageGuardLeaderEntity leader) {
            leaders.add(leader);
         } else if (entity instanceof VillageGuardEntity guard) {
            guards.add(guard);
         }
      }

      return new VillageGuardManager.GuardRoster(guards, leaders);
   }

   private VillageGuardManager.ManagedGuardRoster collectManagedGuardRoster(ServerLevel level, BlockPos bellPos, int radius) {
      AABB searchBox = AABB.ofSize(Vec3.atCenterOf(bellPos), radius * 2.0, 96.0, radius * 2.0);
      List<VillageGuardEntity> guards = new ArrayList<>();
      List<VillageGuardLeaderEntity> leaders = new ArrayList<>();

      for (AbstractKnightEntity entity : level.getEntitiesOfClass(AbstractKnightEntity.class, searchBox, this::isManagedGuard)) {
         if (entity instanceof VillageGuardLeaderEntity leader) {
            leaders.add(leader);
         } else if (entity instanceof VillageGuardEntity guard) {
            guards.add(guard);
         }
      }

      return new VillageGuardManager.ManagedGuardRoster(guards, leaders);
   }

   private boolean isCountableGuard(AbstractKnightEntity entity, BlockPos bellPos, String groupId) {
      return entity.isAlive()
         && !entity.isRemoved()
         && (entity instanceof VillageGuardEntity || entity instanceof VillageGuardLeaderEntity)
         && this.matchesGuardBell(entity, bellPos, groupId);
   }

   private boolean cleanupDuplicateGuards(VillageGuardManager.GuardRoster roster) {
      boolean removed = false;
      removed |= this.removeExcessEntities(roster.leaders(), 1);
      return removed | this.removeExcessEntities(roster.guards(), 4);
   }

   private boolean cleanupExcessManagedGuards(BlockPos bellPos, String groupId, VillageGuardManager.ManagedGuardRoster roster) {
      boolean removed = false;
      removed |= this.removeExcessManagedEntities(roster.leaders(), 1, bellPos, groupId);
      return removed | this.removeExcessManagedEntities(roster.guards(), 4, bellPos, groupId);
   }

   private boolean removeExcessEntities(List<? extends AbstractKnightEntity> entities, int maxAllowed) {
      if (entities.size() <= maxAllowed) {
         return false;
      }

      List<AbstractKnightEntity> sorted = new ArrayList<>(entities);
      sorted.sort(Comparator.comparingLong(this::guardSpawnTime));
      boolean removed = false;

      for (int i = maxAllowed; i < sorted.size(); i++) {
         sorted.get(i).discard();
         removed = true;
      }

      return removed;
   }

   private boolean removeExcessManagedEntities(List<? extends AbstractKnightEntity> entities, int maxAllowed, BlockPos bellPos, String groupId) {
      if (entities.size() <= maxAllowed) {
         return false;
      }

      List<AbstractKnightEntity> sorted = new ArrayList<>(entities);
      sorted.sort(this.managedGuardRetentionComparator(bellPos, groupId));
      boolean removed = false;

      for (int i = maxAllowed; i < sorted.size(); i++) {
         sorted.get(i).discard();
         removed = true;
      }

      return removed;
   }

   private long guardSpawnTime(AbstractKnightEntity entity) {
      return PersistentEntityData.get(entity).getLong("villagerpatrols_village_guard_spawn_time");
   }

   private Comparator<AbstractKnightEntity> managedGuardRetentionComparator(BlockPos bellPos, String groupId) {
      return Comparator.<AbstractKnightEntity, Boolean>comparing(entity -> !groupId.equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_id")))
         .thenComparing(entity -> !this.matchesGuardBell(entity, bellPos, groupId))
         .thenComparingDouble(entity -> entity.blockPosition().distSqr(bellPos))
         .thenComparingLong(this::guardSpawnTime);
   }

   private boolean isManagedGuard(AbstractKnightEntity entity) {
      if (!entity.isAlive() || entity.isRemoved()) {
         return false;
      } else {
         return !(entity instanceof VillageGuardEntity) && !(entity instanceof VillageGuardLeaderEntity)
            ? false
            : PersistentEntityData.get(entity).getBoolean("villagerpatrols_village_guard")
               || PersistentEntityData.get(entity).contains("villagerpatrols_group_id")
               || VillageGuardHome.hasBellAnchor(entity)
               || VillageGuardHome.hasHome(entity);
      }
   }

   private boolean matchesGuardBell(AbstractKnightEntity entity, BlockPos bellPos, String groupId) {
      if (groupId.equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_id"))) {
         return true;
      }

      BlockPos entityBell = VillageGuardHome.getBellAnchor(entity);
      if (bellPos.equals(entityBell)) {
         return true;
      }

      BlockPos legacyHome = VillageGuardHome.getHome(entity);
      return bellPos.equals(legacyHome);
   }

   private boolean spawnGuard(ServerLevel level, BlockPos bellPos, String groupId, EntityType<? extends AbstractKnightEntity> type, String role) {
      Optional<BlockPos> spawnAnchor = this.findSpawnAnchorNearVillage(level, bellPos);
      if (spawnAnchor.isEmpty()) {
         return false;
      }

      BlockPos surface = this.findSpawnSurface(level, bellPos, spawnAnchor.get(), type, pos -> true);
      if (surface == null) {
         return false;
      }

      AbstractKnightEntity entity = (AbstractKnightEntity)type.create(level);
      if (entity == null) {
         return false;
      }

      entity.moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
      entity.finalizeSpawn(level, level.getCurrentDifficultyAt(surface), MobSpawnType.EVENT, null, null);
      entity.setPersistenceRequired();
      PersistentEntityData.get(entity).putBoolean("villagerpatrols_village_guard", true);
      PersistentEntityData.get(entity).putLong("villagerpatrols_village_guard_spawn_time", level.getGameTime());
      PersistentEntityData.get(entity).putString("villagerpatrols_village_guard_role", role);
      entity.setGroupData(groupId, "village_guard_group", role);
      entity.setSpawnedCompanions(true);
      VillageGuardHome.setBellAnchor(entity, bellPos);
      level.addFreshEntity(entity);
      return true;
   }

   private Optional<BlockPos> findSpawnAnchorNearVillage(ServerLevel level, BlockPos bellPos) {
      AABB villagerSearchBox = AABB.ofSize(Vec3.atCenterOf(bellPos), 96.0, 32.0, 96.0);
      List<Villager> villagers = level.getEntitiesOfClass(Villager.class, villagerSearchBox, villagerx -> villagerx.isAlive() && !villagerx.isRemoved());
      Villager closestVillager = null;
      double closestDistance = Double.MAX_VALUE;

      for (Villager villager : villagers) {
         double distance = villager.blockPosition().distSqr(bellPos);
         if (distance < closestDistance) {
            closestDistance = distance;
            closestVillager = villager;
         }
      }

      return closestVillager != null
         ? Optional.of(closestVillager.blockPosition())
         : level.getPoiManager().findClosest(poi -> poi.is(PoiTypes.HOME), bellPos, 48, Occupancy.IS_OCCUPIED);
   }

   private BlockPos findSpawnSurface(
      ServerLevel level, BlockPos bellPos, BlockPos spawnAnchor, EntityType<? extends AbstractKnightEntity> type, Predicate<BlockPos> extraFilter
   ) {
      for (int attempt = 0; attempt < 64; attempt++) {
         int range = this.prioritizedSpawnRange(attempt);
         int offsetX = level.random.nextInt(range * 2 + 1) - range;
         int offsetZ = level.random.nextInt(range * 2 + 1) - range;
         BlockPos base = spawnAnchor.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(base) && level.isCloseToVillage(base, 6) && extraFilter.test(base)) {
            int startY = spawnAnchor.getY() + 2;

            for (int offsetY = 0; offsetY <= 6; offsetY++) {
               BlockPos surface = new BlockPos(base.getX(), startY - offsetY, base.getZ());
               if (this.isSpawnPositionValid(level, bellPos, surface, type)) {
                  return surface;
               }
            }
         }
      }

      return null;
   }

   private int prioritizedSpawnRange(int attempt) {
      if (attempt < 16) {
         return 4;
      } else if (attempt < 32) {
         return 8;
      } else {
         return attempt < 48 ? 12 : 16;
      }
   }

   private boolean isSpawnPositionValid(ServerLevel level, BlockPos bellPos, BlockPos surface, EntityType<? extends AbstractKnightEntity> type) {
      if (!level.isLoaded(surface) || !level.isLoaded(surface.above()) || !level.isCloseToVillage(surface, 6)) {
         return false;
      }

      if (!level.getBlockState(surface.below()).blocksMotion()) {
         return false;
      }

      if (!SpawnSurfaceValidator.hasValidSpawnSupport(level, surface)) {
         return false;
      }

      if (level.getFluidState(surface).isEmpty() && level.getFluidState(surface.above()).isEmpty()) {
         AbstractKnightEntity entity = (AbstractKnightEntity)type.create(level);
         if (entity == null) {
            return false;
         }

         entity.moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
         return level.noCollision(entity);
      } else {
         return false;
      }
   }

   private int randomBetween(ServerLevel level, int minInclusive, int maxInclusive) {
      return minInclusive + level.random.nextInt(maxInclusive - minInclusive + 1);
   }

   private record GuardBellKey(ResourceKey<Level> dimension, int x, int y, int z) {
   }

   private record GuardCounts(int guards, int leaders) {
      private int total() {
         return this.guards + this.leaders;
      }
   }

   private record GuardRoster(List<VillageGuardEntity> guards, List<VillageGuardLeaderEntity> leaders) {
      private VillageGuardManager.GuardCounts counts() {
         return new VillageGuardManager.GuardCounts(this.guards.size(), this.leaders.size());
      }
   }

   private record ManagedGuardRoster(List<VillageGuardEntity> guards, List<VillageGuardLeaderEntity> leaders) {
      private VillageGuardManager.GuardCounts counts() {
         return new VillageGuardManager.GuardCounts(this.guards.size(), this.leaders.size());
      }
   }
}
