package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.DwarfEntity;
import com.javic.villagerpatrols.entity.ElfEntity;
import com.javic.villagerpatrols.entity.HumanHorsemanEntity;
import com.javic.villagerpatrols.entity.KnightEntity;
import com.javic.villagerpatrols.entity.KnightLeaderEntity;
import com.javic.villagerpatrols.entity.KnightShieldEntity;
import com.javic.villagerpatrols.entity.VillageGuardEntity;
import com.javic.villagerpatrols.entity.VillageGuardLeaderEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MountedVillagerWorldEventController {
   private static final double CAMEL_EVENT_BASE_SPEED = 0.19;
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 64.0;
   private static final double EVIL_KNIGHT_PLAYER_TARGET_RADIUS = 24.0;
   private static final int MIN_TARGET_DISTANCE = 32;
   private static final int MAX_TARGET_DISTANCE = 64;
   private static final int MAX_TARGET_TIME = 200;
   private static final int STUCK_TIMEOUT = 60;
   private static final int BIG_TURN_AFTER_FAILED_ATTEMPTS = 8;
   private static final float MAX_TURN_ANGLE_RADIANS = (float)Math.toRadians(45.0);
   private static final double ARRIVAL_DISTANCE_SQR = 16.0;
   private static final double MIN_PROGRESS_DISTANCE_SQR = 4.0;
   private static final double FORMATION_DISTANCE = 4.0;
   private static final double COMFORT_RADIUS_SQR = 6.25;
   private static final double FAR_DISTANCE_SQR = 144.0;
   private static final double HARD_CORRECTION_DISTANCE_SQR = 16.0;
   private static final double SOFT_ADVANCE_DISTANCE = 1.0;
   private static final double LEADER_MOVING_DISTANCE_SQR = 0.01;
   private static final double VILLAGER_FLEE_THREAT_RADIUS = 18.0;
   private static final double VILLAGER_FLEE_RADIUS_Y = 8.0;
   private static final double VILLAGER_FLEE_DISTANCE = 28.0;
   private static final int VILLAGER_FLEE_DURATION_TICKS = 100;
   private static final int VILLAGER_FLEE_RECENT_DAMAGE_TICKS = 100;
   private static final int REPATH_INTERVAL_MIN_TICKS = 15;
   private static final int REPATH_INTERVAL_MAX_TICKS = 25;
   private static final Map<UUID, MountedVillagerWorldEventController.TravelState> STATES = new HashMap<>();
   private static final Map<String, MountedVillagerWorldEventController.MountedConfig> CONFIGS = Map.of(
      "mounted_villager_world_event",
      new MountedVillagerWorldEventController.MountedConfig(
         "mounted_villager_world_event", "mounted_villager_horse_leader", "mounted_villager_horse_companion", true, false, false, 0.7, 0.75, 0.8, 0.8, 1.2
      ),
      "mounted_knight_world_event",
      new MountedVillagerWorldEventController.MountedConfig(
         "mounted_knight_world_event", "mounted_knight_horse_leader", "mounted_knight_horse_companion", false, false, false, 0.8, 0.86, 0.94, 1.3, 1.2
      ),
      "mounted_evil_knight_world_event",
      new MountedVillagerWorldEventController.MountedConfig(
         "mounted_evil_knight_world_event",
         "mounted_evil_knight_horse_leader",
         "mounted_evil_knight_horse_companion",
         false,
         true,
         false,
         0.8,
         0.86,
         0.94,
         1.3,
         1.2
      ),
      "mounted_illager_world_event",
      new MountedVillagerWorldEventController.MountedConfig(
         "mounted_illager_world_event", "mounted_illager_horse_leader", "mounted_illager_horse_companion", false, false, false, 0.8, 0.86, 0.94, 1.3, 1.2
      ),
      "mounted_camel_villager_world_event",
      new MountedVillagerWorldEventController.MountedConfig(
         "mounted_camel_villager_world_event", "mounted_camel_villager_camel", null, true, false, true, 1.05, 0.72, 0.75, 0.8, 1.1
      )
   );

   private MountedVillagerWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level) {
         MountedVillagerWorldEventController.MountedConfig config = CONFIGS.get(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"));
         if (config != null) {
            if (!shouldCleanupBrokenEvent(level, mob)) {
               if (mob instanceof Horse horse) {
                  tickHorse(level, horse, config);
               } else if (mob instanceof Camel camel) {
                  tickCamel(level, camel, config);
               } else {
                  tickRider(level, mob, config);
               }
            }
         }
      }
   }

   private static void tickRider(ServerLevel level, Mob rider, MountedVillagerWorldEventController.MountedConfig config) {
      if (config.clearRiderTarget() && rider.getTarget() != null) {
         rider.setTarget(null);
      }

      if (config.forcePlayerTarget()) {
         ensureEvilKnightPlayerTarget(level, rider);
      }

      if (!(rider instanceof HumanHorsemanEntity horseman && horseman.getTarget() != null)) {
         if (!rider.isPassenger()) {
            if (PersistentEntityData.get(rider).hasUUID("villagerpatrols_mounted_horse_uuid")) {
               Entity entity = level.getEntity(PersistentEntityData.get(rider).getUUID("villagerpatrols_mounted_horse_uuid"));
               if (entity instanceof Horse horse && horse.isAlive()) {
                  rider.startRiding(horse, true);
               } else if (entity instanceof Camel camel && camel.isAlive()) {
                  rider.startRiding(camel, true);
               }
            }
         }
      }
   }

   private static void tickCamel(ServerLevel level, Camel camel, MountedVillagerWorldEventController.MountedConfig config) {
      ensureMountedCamelRiders(level, camel);
      ensureCamelMobility(camel);
      if (camel.isCamelSitting() || camel.isInPoseTransition()) {
         camel.standUpInstantly();
      }

      MountedVillagerWorldEventController.TravelState state = STATES.computeIfAbsent(
         camel.getUUID(), uuid -> new MountedVillagerWorldEventController.TravelState()
      );
      if (!advanceVillagerFleeMount(level, camel, getMountedCamelRider(camel), state, config)) {
         if (state.targetPos == null || shouldPickNewCamelTarget(camel, state)) {
            state.targetPos = findNextCamelTarget(level, camel, state);
            state.targetTime = 0;
            state.stuckTime = 0;
            state.lastProgressPos = camel.position();
         }

         if (state.targetPos != null) {
            state.targetTime++;
            Vec3 currentPos = camel.position();
            if (state.lastProgressPos != null) {
               if (currentPos.distanceToSqr(state.lastProgressPos) >= 4.0) {
                  state.lastProgressPos = currentPos;
                  state.stuckTime = 0;
               } else {
                  state.stuckTime++;
               }
            } else {
               state.lastProgressPos = currentPos;
            }

            if (--state.nextPathTick <= 0 || camel.getNavigation().isDone()) {
               state.nextPathTick = randomRepathInterval(camel.getRandom());
               moveCamel(camel, state, config);
            }
         }
      }
   }

   private static void tickHorse(ServerLevel level, Horse horse, MountedVillagerWorldEventController.MountedConfig config) {
      ensureMountedRider(level, horse);
      MountedVillagerWorldEventController.TravelState state = STATES.computeIfAbsent(
         horse.getUUID(), uuid -> new MountedVillagerWorldEventController.TravelState()
      );
      if (!config.clearRiderTarget() || !advanceVillagerFleeMount(level, horse, getMountedRider(horse), state, config)) {
         if (!advanceCombatHorse(horse, config)) {
            String role = PersistentEntityData.get(horse).getString("villagerpatrols_group_role");
            if (config.horseLeaderRole().equals(role)) {
               tickLeaderHorse(level, horse, config, state);
            } else if (config.horseCompanionRole() != null && config.horseCompanionRole().equals(role)) {
               tickCompanionHorse(level, horse, config, state);
            }
         }
      }
   }

   private static boolean advanceCombatHorse(Horse horse, MountedVillagerWorldEventController.MountedConfig config) {
      Mob rider = getMountedRider(horse);
      if (rider != null && !config.clearRiderTarget()) {
         LivingEntity target = rider.getTarget();
         if (target != null && target.isAlive()) {
            if (horse.tickCount % 10 == horse.getId() % 10 || horse.getNavigation().isDone()) {
               horse.getNavigation().moveTo(target, config.combatSpeed());
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void ensureEvilKnightPlayerTarget(ServerLevel level, Mob rider) {
      LivingEntity currentTarget = rider.getTarget();
      if (currentTarget == null || !currentTarget.isAlive()) {
         AABB searchBox = rider.getBoundingBox().inflate(24.0, 8.0, 24.0);
         ServerPlayer nearestPlayer = null;
         double nearestDistance = Double.MAX_VALUE;

         for (ServerPlayer player : level.getEntitiesOfClass(
            ServerPlayer.class, searchBox, candidate -> candidate.isAlive() && !candidate.isSpectator() && rider.canAttack(candidate)
         )) {
            double distance = rider.distanceToSqr(player);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearestPlayer = player;
            }
         }

         if (nearestPlayer != null) {
            rider.setTarget(nearestPlayer);
         }
      }
   }

   private static Mob getMountedRider(Horse horse) {
      return horse.getFirstPassenger() instanceof Mob mob ? mob : null;
   }

   private static Mob getMountedCamelRider(Camel camel) {
      return camel.getFirstPassenger() instanceof Mob mob ? mob : null;
   }

   private static void ensureMountedCamelRiders(ServerLevel level, Camel camel) {
      ensureCamelRider(level, camel, "villagerpatrols_mounted_rider_uuid");
      ensureCamelRider(level, camel, "villagerpatrols_mounted_second_rider_uuid");
   }

   private static void ensureCamelRider(ServerLevel level, Camel camel, String tagName) {
      if (PersistentEntityData.get(camel).hasUUID(tagName)) {
         UUID riderUuid = PersistentEntityData.get(camel).getUUID(tagName);

         for (Entity passenger : camel.getPassengers()) {
            if (passenger.getUUID().equals(riderUuid)) {
               return;
            }
         }

         if (level.getEntity(riderUuid) instanceof Mob rider && rider.isAlive()) {
            rider.startRiding(camel, true);
         }
      }
   }

   private static void ensureCamelMobility(Camel camel) {
      if (camel.getAttribute(Attributes.MOVEMENT_SPEED) != null && camel.getAttributeValue(Attributes.MOVEMENT_SPEED) < 0.19) {
         camel.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.19);
      }
   }

   private static void ensureMountedRider(ServerLevel level, Horse horse) {
      if (horse.getFirstPassenger() == null) {
         if (PersistentEntityData.get(horse).hasUUID("villagerpatrols_mounted_rider_uuid")) {
            if (level.getEntity(PersistentEntityData.get(horse).getUUID("villagerpatrols_mounted_rider_uuid")) instanceof Mob rider && rider.isAlive()) {
               rider.startRiding(horse, true);
            }
         }
      }
   }

   private static void tickLeaderHorse(
      ServerLevel level, Horse horse, MountedVillagerWorldEventController.MountedConfig config, MountedVillagerWorldEventController.TravelState state
   ) {
      if (state.targetPos == null || shouldPickNewLeaderTarget(horse, state)) {
         state.targetPos = findNextLeaderTarget(level, horse, state);
         state.targetTime = 0;
         state.stuckTime = 0;
         state.lastProgressPos = horse.position();
      }

      if (state.targetPos != null) {
         state.targetTime++;
         Vec3 currentPos = horse.position();
         if (state.lastProgressPos != null) {
            if (currentPos.distanceToSqr(state.lastProgressPos) >= 4.0) {
               state.lastProgressPos = currentPos;
               state.stuckTime = 0;
            } else {
               state.stuckTime++;
            }
         } else {
            state.lastProgressPos = currentPos;
         }

         if (--state.nextPathTick <= 0 || horse.getNavigation().isDone()) {
            state.nextPathTick = randomRepathInterval(horse.getRandom());
            moveLeaderHorse(horse, state, config);
         }
      }
   }

   private static void tickCompanionHorse(
      ServerLevel level, Horse horse, MountedVillagerWorldEventController.MountedConfig config, MountedVillagerWorldEventController.TravelState state
   ) {
      Horse leader = findLeaderHorse(level, horse, config);
      if (leader != null) {
         state.targetPos = resolveCompanionTarget(level, horse, leader);
         if (state.targetPos != null) {
            if (isLeaderTraveling(leader) && (horse.getNavigation().isDone() || horse.distanceToSqr(state.targetPos) > 16.0)) {
               moveCompanionHorse(horse, leader, state.targetPos, config);
            }

            if (--state.nextPathTick <= 0) {
               state.nextPathTick = randomRepathInterval(horse.getRandom());
               state.targetPos = resolveCompanionTarget(level, horse, leader);
               if (state.targetPos != null) {
                  moveCompanionHorse(horse, leader, state.targetPos, config);
               }
            }
         }
      }
   }

   private static boolean shouldPickNewLeaderTarget(Horse horse, MountedVillagerWorldEventController.TravelState state) {
      if (state.targetPos == null) {
         return true;
      } else {
         return horse.distanceToSqr(state.targetPos) <= 16.0 ? true : state.targetTime >= 200 || state.stuckTime >= 60;
      }
   }

   private static Vec3 findNextLeaderTarget(ServerLevel level, Horse horse, MountedVillagerWorldEventController.TravelState state) {
      RandomSource random = horse.getRandom();
      BlockPos origin = horse.blockPosition();
      ensureTravelAngle(horse, state, random);

      for (int attempt = 0; attempt < 12; attempt++) {
         float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
         float angle = state.travelAngle + (random.nextFloat() * 2.0F - 1.0F) * maxTurn;
         int distance = 32 + random.nextInt(33);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = origin.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (isValidSurface(level, surface)) {
               state.travelAngle = angle;
               state.hasTravelAngle = true;
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
      state.hasTravelAngle = true;
      return null;
   }

   private static boolean shouldPickNewCamelTarget(Camel camel, MountedVillagerWorldEventController.TravelState state) {
      if (state.targetPos == null) {
         return true;
      } else {
         return camel.distanceToSqr(state.targetPos) <= 16.0 ? true : state.targetTime >= 200 || state.stuckTime >= 60;
      }
   }

   private static Vec3 findNextCamelTarget(ServerLevel level, Camel camel, MountedVillagerWorldEventController.TravelState state) {
      RandomSource random = camel.getRandom();
      BlockPos origin = camel.blockPosition();
      ensureTravelAngle(camel, state, random);

      for (int attempt = 0; attempt < 12; attempt++) {
         float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
         float angle = state.travelAngle + (random.nextFloat() * 2.0F - 1.0F) * maxTurn;
         int distance = 32 + random.nextInt(33);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = origin.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (isValidSurface(level, surface)) {
               state.travelAngle = angle;
               state.hasTravelAngle = true;
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
      state.hasTravelAngle = true;
      return null;
   }

   private static void ensureTravelAngle(Horse horse, MountedVillagerWorldEventController.TravelState state, RandomSource random) {
      if (!state.hasTravelAngle) {
         Vec3 motion = horse.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            state.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            state.hasTravelAngle = true;
         } else {
            Vec3 view = horse.getViewVector(1.0F);
            Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
            if (horizontalView.lengthSqr() > 1.0E-4) {
               state.travelAngle = (float)Math.atan2(horizontalView.z, horizontalView.x);
               state.hasTravelAngle = true;
            } else {
               state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
               state.hasTravelAngle = true;
            }
         }
      }
   }

   private static void ensureTravelAngle(Camel camel, MountedVillagerWorldEventController.TravelState state, RandomSource random) {
      if (!state.hasTravelAngle) {
         Vec3 motion = camel.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            state.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            state.hasTravelAngle = true;
         } else {
            Vec3 view = camel.getViewVector(1.0F);
            Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
            if (horizontalView.lengthSqr() > 1.0E-4) {
               state.travelAngle = (float)Math.atan2(horizontalView.z, horizontalView.x);
               state.hasTravelAngle = true;
            } else {
               state.travelAngle = random.nextFloat() * (float) (Math.PI * 2);
               state.hasTravelAngle = true;
            }
         }
      }
   }

   private static void moveLeaderHorse(
      Horse horse, MountedVillagerWorldEventController.TravelState state, MountedVillagerWorldEventController.MountedConfig config
   ) {
      if (state.targetPos != null) {
         horse.getNavigation().moveTo(state.targetPos.x, state.targetPos.y, state.targetPos.z, config.leaderSpeed());
      }
   }

   private static void moveCamel(Camel camel, MountedVillagerWorldEventController.TravelState state, MountedVillagerWorldEventController.MountedConfig config) {
      if (state.targetPos != null) {
         camel.getNavigation().moveTo(state.targetPos.x, state.targetPos.y, state.targetPos.z, config.leaderSpeed());
      }
   }

   private static Horse findLeaderHorse(ServerLevel level, Horse horse, MountedVillagerWorldEventController.MountedConfig config) {
      String groupId = PersistentEntityData.get(horse).getString("villagerpatrols_group_id");
      AABB searchBox = horse.getBoundingBox().inflate(64.0, 8.0, 64.0);
      List<Horse> horses = level.getEntitiesOfClass(
         Horse.class,
         searchBox,
         candidate -> candidate.isAlive()
            && candidate != horse
            && config.groupType().equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
            && config.horseLeaderRole().equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
            && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
      );
      return horses.isEmpty() ? null : horses.get(0);
   }

   private static Vec3 resolveCompanionTarget(ServerLevel level, Horse horse, Horse leader) {
      int companionIndex = findCompanionIndex(level, horse, leader);
      int row = companionIndex / 2 + 1;
      double sideSign = companionIndex % 2 == 0 ? 1.0 : -1.0;
      Vec3 forward = resolveForward(leader);
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      Vec3 desiredTarget = leader.position().add(side.scale(4.0 * sideSign * row)).add(forward.scale(-(2.0 + row * 2.0)));
      if (isLeaderTraveling(leader)) {
         desiredTarget = desiredTarget.add(forward.scale(1.0));
      }

      return findNavigableTarget(level, leader, desiredTarget);
   }

   private static int findCompanionIndex(ServerLevel level, Horse horse, Horse leader) {
      String groupId = PersistentEntityData.get(horse).getString("villagerpatrols_group_id");
      List<Horse> companions = level.getEntitiesOfClass(
         Horse.class,
         leader.getBoundingBox().inflate(64.0, 8.0, 64.0),
         candidate -> candidate.isAlive() && !candidate.equals(leader) && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
      );
      companions.sort((left, right) -> left.getUUID().compareTo(right.getUUID()));
      return Math.max(0, companions.indexOf(horse));
   }

   private static void moveCompanionHorse(Horse horse, Horse leader, Vec3 targetPos, MountedVillagerWorldEventController.MountedConfig config) {
      if (horse.distanceToSqr(targetPos) <= 6.25 && horse.distanceToSqr(leader) <= 144.0 && !isLeaderTraveling(leader)) {
         horse.getNavigation().stop();
      } else {
         double speed = horse.distanceToSqr(leader) > 144.0 ? config.catchUpSpeed() : config.followSpeed();
         horse.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
      }
   }

   private static Vec3 resolveForward(Horse horse) {
      Vec3 motion = horse.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      if (horizontalMotion.lengthSqr() > 0.04) {
         return horizontalMotion.normalize();
      }

      Vec3 view = horse.getViewVector(1.0F);
      Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
      return horizontalView.lengthSqr() > 1.0E-4 ? horizontalView.normalize() : new Vec3(0.0, 0.0, 1.0);
   }

   private static boolean isLeaderTraveling(Horse horse) {
      Vec3 motion = horse.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      return horizontalMotion.lengthSqr() > 0.01 || !horse.getNavigation().isDone();
   }

   private static Vec3 findNavigableTarget(ServerLevel level, Horse leader, Vec3 desiredTarget) {
      int[][] offsets = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
      int baseX = Mth.floor(desiredTarget.x);
      int baseZ = Mth.floor(desiredTarget.z);

      for (int[] offset : offsets) {
         BlockPos sample = leader.blockPosition().offset(baseX - leader.getBlockX() + offset[0], 0, baseZ - leader.getBlockZ() + offset[1]);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (isValidSurface(level, surface)) {
               return Vec3.atBottomCenterOf(surface);
            }
         }
      }

      return desiredTarget;
   }

   private static boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private static boolean advanceVillagerFleeMount(
      ServerLevel level, Mob mount, Mob rider, MountedVillagerWorldEventController.TravelState state, MountedVillagerWorldEventController.MountedConfig config
   ) {
      if (rider == null) {
         return false;
      }

      if (shouldTriggerVillagerFlee(mount, rider)) {
         LivingEntity threat = findNearestVillagerThreat(level, mount);
         if (threat != null) {
            Vec3 fleeTarget = findFleeTarget(level, mount, threat);
            if (fleeTarget != null) {
               state.fleeTargetPos = fleeTarget;
               state.fleeTicks = 100;
            }
         }
      }

      if (state.fleeTicks > 0 && state.fleeTargetPos != null) {
         state.fleeTicks--;
         if (--state.nextPathTick <= 0 || mount.getNavigation().isDone()) {
            state.nextPathTick = randomRepathInterval(mount.getRandom());
            mount.getNavigation().moveTo(state.fleeTargetPos.x, state.fleeTargetPos.y, state.fleeTargetPos.z, config.fleeSpeed());
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean shouldTriggerVillagerFlee(Mob mount, Mob rider) {
      return mount.getLastHurtByMob() != null && mount.tickCount - mount.getLastHurtByMobTimestamp() < 100
         ? true
         : rider.getLastHurtByMob() != null && rider.tickCount - rider.getLastHurtByMobTimestamp() < 100;
   }

   private static LivingEntity findNearestVillagerThreat(ServerLevel level, Mob mount) {
      AABB searchBox = mount.getBoundingBox().inflate(18.0, 8.0, 18.0);
      LivingEntity nearestThreat = null;
      double nearestDistance = Double.MAX_VALUE;

      for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchBox, MountedVillagerWorldEventController::isVillagerThreat)) {
         double distance = mount.distanceToSqr(candidate);
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestThreat = candidate;
         }
      }

      return nearestThreat;
   }

   private static boolean isVillagerThreat(LivingEntity candidate) {
      if (!(candidate instanceof Monster)) {
         return false;
      } else {
         return !(candidate instanceof KnightEntity)
               && !(candidate instanceof KnightShieldEntity)
               && !(candidate instanceof KnightLeaderEntity)
               && !(candidate instanceof VillageGuardEntity)
               && !(candidate instanceof VillageGuardLeaderEntity)
            ? !(candidate instanceof ElfEntity) && !(candidate instanceof DwarfEntity)
            : false;
      }
   }

   private static Vec3 findFleeTarget(ServerLevel level, Mob mount, LivingEntity threat) {
      Vec3 away = mount.position().subtract(threat.position());
      if (away.lengthSqr() < 1.0E-4) {
         away = new Vec3(1.0, 0.0, 0.0);
      }

      away = away.normalize().scale(28.0);
      BlockPos sample = BlockPos.containing(mount.getX() + away.x, mount.getY(), mount.getZ() + away.z);
      if (!level.isLoaded(sample)) {
         return null;
      }

      BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
      return isValidSurface(level, surface) ? Vec3.atBottomCenterOf(surface) : null;
   }

   private static int randomRepathInterval(RandomSource random) {
      return 15 + random.nextInt(11);
   }

   public static boolean shouldCleanupBrokenEvent(ServerLevel level, Mob mob) {
      if (mob != null && !mob.isRemoved()) {
         MountedVillagerWorldEventController.MountedConfig config = CONFIGS.get(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"));
         if (config == null) {
            return false;
         }

         String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
         if (groupId.isEmpty()) {
            return true;
         }

         AABB searchBox = mob.getBoundingBox().inflate(64.0, 8.0, 64.0);
         boolean hasMount = level.getEntitiesOfClass(
                  Mob.class,
                  searchBox,
                  candidate -> candidate.isAlive()
                     && !candidate.isRemoved()
                     && candidate != mob
                     && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                     && isMatchingMountedGroup(candidate, config)
                     && (candidate instanceof Horse || candidate instanceof Camel)
               )
               .size()
            > 0;
         boolean hasRider = level.getEntitiesOfClass(
                  Mob.class,
                  searchBox,
                  candidate -> candidate.isAlive()
                     && !candidate.isRemoved()
                     && candidate != mob
                     && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                     && isMatchingMountedGroup(candidate, config)
                     && !(candidate instanceof Horse)
                     && !(candidate instanceof Camel)
               )
               .size()
            > 0;
         return !(mob instanceof Horse) && !(mob instanceof Camel) ? !hasMount : !hasRider;
      } else {
         return false;
      }
   }

   private static boolean isMatchingMountedGroup(Mob mob, MountedVillagerWorldEventController.MountedConfig config) {
      return config.groupType().equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"));
   }

   private record MountedConfig(
      String groupType,
      String horseLeaderRole,
      String horseCompanionRole,
      boolean clearRiderTarget,
      boolean forcePlayerTarget,
      boolean camelMount,
      double leaderSpeed,
      double followSpeed,
      double catchUpSpeed,
      double combatSpeed,
      double fleeSpeed
   ) {
   }

   private static final class TravelState {
      private Vec3 targetPos;
      private Vec3 fleeTargetPos;
      private Vec3 lastProgressPos;
      private float travelAngle;
      private boolean hasTravelAngle;
      private int nextPathTick;
      private int targetTime;
      private int stuckTime;
      private int fleeTicks;
   }
}
