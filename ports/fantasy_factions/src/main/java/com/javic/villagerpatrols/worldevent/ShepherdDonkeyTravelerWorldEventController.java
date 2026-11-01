package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShepherdDonkeyTravelerWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 48.0;
   private static final double DONKEY_WAIT_DISTANCE_SQR = 36.0;
   private static final int MIN_TARGET_DISTANCE = 24;
   private static final int MAX_TARGET_DISTANCE = 48;
   private static final int MAX_TARGET_TIME = 200;
   private static final int STUCK_TIMEOUT = 60;
   private static final int BIG_TURN_AFTER_FAILED_ATTEMPTS = 8;
   private static final double ARRIVAL_DISTANCE_SQR = 9.0;
   private static final double MIN_PROGRESS_DISTANCE_SQR = 4.0;
   private static final float MAX_TURN_ANGLE_RADIANS = (float)Math.toRadians(45.0);
   private static final double LEADER_SPEED = 0.4;
   private static final Map<UUID, ShepherdDonkeyTravelerWorldEventController.TravelState> LEADER_STATES = new HashMap<>();

   private ShepherdDonkeyTravelerWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "shepherd_donkey_traveler_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         if ("leader".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))) {
            tickLeader(level, mob);
         }
      }
   }

   private static void tickLeader(ServerLevel level, Mob mob) {
      ShepherdMovingAnimalsWorldEvent.applyShepherdCompatibility(mob);
      if (mob.getMainHandItem().getItem() != Items.WHEAT) {
         ShepherdMovingAnimalsWorldEvent.applyShepherdCompatibility(mob);
      }

      if (mob instanceof PathfinderMob leader && leader.getTarget() == null && !leader.isAggressive() && !leader.isPassenger() && !leader.isLeashed()) {
         Donkey donkey = findGroupDonkey(level, leader);
         if (donkey == null) {
            leader.getNavigation().stop();
            LEADER_STATES.remove(leader.getUUID());
         } else if (leader.distanceToSqr(donkey) > 36.0) {
            leader.getNavigation().stop();
         } else {
            ShepherdDonkeyTravelerWorldEventController.TravelState state = LEADER_STATES.computeIfAbsent(
               leader.getUUID(), unused -> new ShepherdDonkeyTravelerWorldEventController.TravelState()
            );
            if (state.targetPos == null || shouldPickNewTarget(leader, state)) {
               state.targetPos = findNextTarget(level, leader, state);
               state.targetTime = 0;
               state.stuckTime = 0;
               state.lastProgressPos = leader.position();
            }

            if (state.targetPos != null) {
               state.targetTime++;
               Vec3 currentPos = leader.position();
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

               if (--state.nextPathTick <= 0 || leader.getNavigation().isDone()) {
                  state.nextPathTick = 10;
                  leader.getNavigation().moveTo(state.targetPos.x, state.targetPos.y, state.targetPos.z, 0.4);
               }
            }
         }
      } else {
         LEADER_STATES.remove(mob.getUUID());
      }
   }

   public static boolean shouldCleanupBrokenEvent(ServerLevel level, Mob mob) {
      if (mob != null && !mob.isRemoved()) {
         String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
         if (groupId.isEmpty()) {
            return true;
         } else {
            AABB searchBox = mob.getBoundingBox().inflate(48.0, 8.0, 48.0);
            boolean hasLeader = !level.getEntitiesOfClass(
                  Mob.class,
                  searchBox,
                  candidate -> candidate.isAlive()
                     && candidate != mob
                     && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                     && "shepherd_donkey_traveler_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
                     && "leader".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
               )
               .isEmpty();
            boolean hasDonkey = !level.getEntitiesOfClass(
                  Donkey.class,
                  searchBox,
                  candidate -> candidate.isAlive()
                     && candidate != mob
                     && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                     && "shepherd_donkey_traveler_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
                     && "donkey".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
               )
               .isEmpty();
            if (!(mob instanceof Donkey donkey)) {
               return !hasDonkey;
            } else {
               Entity leashHolder = donkey.getLeashHolder();
               return !hasLeader || leashHolder == null || !leashHolder.isAlive() || leashHolder.level() != level;
            }
         }
      } else {
         return false;
      }
   }

   private static Donkey findGroupDonkey(ServerLevel level, PathfinderMob leader) {
      String groupId = PersistentEntityData.get(leader).getString("villagerpatrols_group_id");
      AABB searchBox = leader.getBoundingBox().inflate(48.0, 8.0, 48.0);
      return (Donkey)level.getEntitiesOfClass(Donkey.class, searchBox, candidate -> isMatchingDonkey(candidate, groupId, leader))
         .stream()
         .findFirst()
         .orElse(null);
   }

   private static boolean isMatchingDonkey(Donkey candidate, String groupId, PathfinderMob leader) {
      if (!candidate.isAlive()) {
         return false;
      }

      CompoundTag data = PersistentEntityData.get(candidate);
      return groupId.equals(data.getString("villagerpatrols_group_id"))
         && "shepherd_donkey_traveler_world_event".equals(data.getString("villagerpatrols_group_type"))
         && "donkey".equals(data.getString("villagerpatrols_group_role"))
         && candidate.getLeashHolder() == leader;
   }

   private static boolean shouldPickNewTarget(PathfinderMob leader, ShepherdDonkeyTravelerWorldEventController.TravelState state) {
      if (state.targetPos == null) {
         return true;
      } else {
         return leader.distanceToSqr(state.targetPos) <= 9.0 ? true : state.targetTime >= 200 || state.stuckTime >= 60;
      }
   }

   private static Vec3 findNextTarget(ServerLevel level, PathfinderMob leader, ShepherdDonkeyTravelerWorldEventController.TravelState state) {
      RandomSource random = leader.getRandom();
      BlockPos origin = leader.blockPosition();
      ensureTravelAngle(leader, state, random);

      for (int attempt = 0; attempt < 12; attempt++) {
         float maxTurn = attempt >= 8 ? (float) Math.PI : MAX_TURN_ANGLE_RADIANS;
         float angle = state.travelAngle + (random.nextFloat() * 2.0F - 1.0F) * maxTurn;
         int distance = 24 + random.nextInt(25);
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

   private static void ensureTravelAngle(PathfinderMob leader, ShepherdDonkeyTravelerWorldEventController.TravelState state, RandomSource random) {
      if (!state.hasTravelAngle) {
         Vec3 motion = leader.getDeltaMovement();
         Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
         if (horizontalMotion.lengthSqr() > 0.04) {
            state.travelAngle = (float)Math.atan2(horizontalMotion.z, horizontalMotion.x);
            state.hasTravelAngle = true;
         } else {
            Vec3 view = leader.getViewVector(1.0F);
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

   private static boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty()
         && level.getBlockState(pos.below()).blocksMotion();
   }

   private static final class TravelState {
      private Vec3 targetPos;
      private Vec3 lastProgressPos;
      private float travelAngle;
      private boolean hasTravelAngle;
      private int nextPathTick;
      private int targetTime;
      private int stuckTime;
   }
}
