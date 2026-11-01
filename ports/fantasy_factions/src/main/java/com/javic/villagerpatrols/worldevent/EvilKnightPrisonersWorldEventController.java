package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.AbstractKnightEntity;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.network.PrisonerPoseNetworking;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EvilKnightPrisonersWorldEventController {
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 64.0;
   private static final double GUARD_FORMATION_DISTANCE = 3.5;
   private static final double PRISONER_FORMATION_DISTANCE = 1.75;
   private static final double FOLLOW_SPEED = 0.55;
   private static final double RETURN_SPEED = 0.45;
   private static final double COMFORT_RADIUS_SQR = 4.0;
   private static final double HARD_CORRECTION_DISTANCE_SQR = 16.0;

   private EvilKnightPrisonersWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         if (shouldCleanupBrokenEvent(level, mob)) {
            if (!(mob instanceof AbstractKnightEntity)) {
               setRestrained(mob, false);
            }
         } else {
            String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
            if (mob instanceof AbstractKnightEntity knight) {
               tickKnight(level, knight, groupId);
            } else {
               tickPrisoner(level, mob, groupId);
            }
         }
      }
   }

   private static void tickKnight(ServerLevel level, AbstractKnightEntity knight, String groupId) {
      LivingEntity target = knight.getTarget();
      if (target instanceof Villager
         && groupId.equals(PersistentEntityData.get(target).getString("villagerpatrols_group_id"))
         && "evil_knight_prisoner_prisoner".equals(PersistentEntityData.get(target).getString("villagerpatrols_group_role"))) {
         knight.setTarget(null);
         target = null;
      }

      if (target == null || !target.isAlive()) {
         AbstractKnightEntity leader = findLeader(level, knight, groupId);
         if (leader != null && leader != knight) {
            Vec3 targetPos = resolveGuardTarget(level, knight, leader, groupId);
            if (targetPos != null) {
               if (knight.distanceToSqr(targetPos) > 4.0 || knight.getNavigation().isDone()) {
                  knight.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 0.55);
               }
            }
         }
      }
   }

   private static void tickPrisoner(ServerLevel level, Mob prisoner, String groupId) {
      if (prisoner.getTarget() != null) {
         prisoner.setTarget(null);
      }

      List<AbstractKnightEntity> guards = findAliveGuards(level, prisoner, groupId);
      boolean restrained = !guards.isEmpty();
      setRestrained(prisoner, restrained);
      if (!restrained) {
         releasePrisoner(prisoner);
      } else {
         AbstractKnightEntity leader = findLeader(level, prisoner, groupId);
         if (leader != null) {
            Vec3 targetPos = resolvePrisonerTarget(level, prisoner, leader, groupId);
            if (targetPos != null) {
               if (!(prisoner.distanceToSqr(targetPos) > 4.0) && !(prisoner.distanceToSqr(leader) > 16.0)) {
                  prisoner.getNavigation().stop();
               } else {
                  prisoner.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 0.45);
               }
            }
         }
      }
   }

   private static AbstractKnightEntity findLeader(ServerLevel level, Mob mob, String groupId) {
      List<AbstractKnightEntity> knights = findAliveGuards(level, mob, groupId);

      for (AbstractKnightEntity knight : knights) {
         if ("evil_knight_prisoner_leader".equals(PersistentEntityData.get(knight).getString("villagerpatrols_group_role"))) {
            return knight;
         }
      }

      return knights.isEmpty() ? null : knights.get(0);
   }

   private static List<AbstractKnightEntity> findAliveGuards(ServerLevel level, Mob mob, String groupId) {
      AABB searchBox = mob.getBoundingBox().inflate(64.0, 8.0, 64.0);
      return level.getEntitiesOfClass(
         AbstractKnightEntity.class,
         searchBox,
         candidate -> candidate.isAlive()
            && "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
            && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
            && (
               "evil_knight_prisoner_leader".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
                  || "evil_knight_prisoner_guard".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
            )
      );
   }

   private static Vec3 resolveGuardTarget(ServerLevel level, AbstractKnightEntity guard, AbstractKnightEntity leader, String groupId) {
      if (guard == leader) {
         return null;
      }

      Vec3 forward = resolveForward(leader);
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      int slot = findGuardSlot(level, guard, groupId);

      Vec3 offset = switch (slot) {
         case 0 -> side.scale(3.5);
         case 1 -> side.scale(-3.5);
         case 2 -> side.scale(3.5).add(forward.scale(-3.5));
         default -> side.scale(-3.5).add(forward.scale(-3.5));
      };
      return leader.position().add(offset);
   }

   private static Vec3 resolvePrisonerTarget(ServerLevel level, Mob prisoner, AbstractKnightEntity leader, String groupId) {
      Vec3 forward = resolveForward(leader);
      Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
      int slot = findPrisonerSlot(level, prisoner, groupId);
      Vec3 offset = slot == 0 ? forward.scale(-1.75) : forward.scale(-1.75).add(side.scale(1.1));
      return leader.position().add(offset);
   }

   private static int findGuardSlot(ServerLevel level, AbstractKnightEntity guard, String groupId) {
      List<AbstractKnightEntity> guards = new ArrayList<>(findAliveGuards(level, guard, groupId));
      guards.removeIf(candidate -> "evil_knight_prisoner_leader".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role")));
      guards.sort(Comparator.comparing(Entity::getUUID));

      for (int index = 0; index < guards.size(); index++) {
         if (guards.get(index).getUUID().equals(guard.getUUID())) {
            return index % 4;
         }
      }

      return Math.abs(guard.getUUID().hashCode()) % 4;
   }

   private static int findPrisonerSlot(ServerLevel level, Mob prisoner, String groupId) {
      AABB searchBox = prisoner.getBoundingBox().inflate(64.0, 8.0, 64.0);
      List<Mob> prisoners = new ArrayList<>(
         level.getEntitiesOfClass(
            Mob.class,
            searchBox,
            candidate -> candidate.isAlive()
               && "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
               && "evil_knight_prisoner_prisoner".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
               && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
         )
      );
      prisoners.sort(Comparator.comparing(Entity::getUUID));

      for (int index = 0; index < prisoners.size(); index++) {
         if (prisoners.get(index).getUUID().equals(prisoner.getUUID())) {
            return index;
         }
      }

      return 0;
   }

   private static Vec3 resolveForward(AbstractKnightEntity leader) {
      Vec3 motion = leader.getDeltaMovement();
      Vec3 horizontalMotion = new Vec3(motion.x, 0.0, motion.z);
      if (horizontalMotion.lengthSqr() > 0.04) {
         return horizontalMotion.normalize();
      }

      Vec3 view = leader.getViewVector(1.0F);
      Vec3 horizontalView = new Vec3(view.x, 0.0, view.z);
      return horizontalView.lengthSqr() > 1.0E-4 ? horizontalView.normalize() : new Vec3(0.0, 0.0, 1.0);
   }

   private static void setRestrained(Mob prisoner, boolean restrained) {
      boolean changed = PersistentEntityData.get(prisoner).getBoolean("villagerpatrols_prisoner_restrained") != restrained;
      PersistentEntityData.get(prisoner).putBoolean("villagerpatrols_prisoner_restrained", restrained);
      if (prisoner instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setPrisonerRestrained(restrained);
      } else if (prisoner instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setPrisonerRestrained(restrained);
      }

      if (changed) {
         PrisonerPoseNetworking.syncPrisonerState(prisoner, restrained);
      }
   }

   private static void releasePrisoner(Mob prisoner) {
      CompoundTag data = PersistentEntityData.get(prisoner);
      prisoner.getNavigation().stop();
      prisoner.setTarget(null);
      data.putBoolean("villagerpatrols_prisoner_restrained", false);
      data.remove("villagerpatrols_event_entity");
      data.remove("villagerpatrols_event_spawn_time");
      data.remove("villagerpatrols_group_id");
      data.remove("villagerpatrols_group_type");
      data.remove("villagerpatrols_group_role");
      if (prisoner instanceof Villager villager) {
         villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
         villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         if (villager.isSleeping()) {
            villager.stopSleeping();
         }
      }
   }

   public static boolean shouldCleanupBrokenEvent(ServerLevel level, Mob mob) {
      if (mob != null && !mob.isRemoved()) {
         String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
         if (groupId.isEmpty()) {
            return true;
         }

         List<AbstractKnightEntity> guards = findAliveGuards(level, mob, groupId);
         List<Mob> prisoners = findAlivePrisoners(level, mob, groupId);
         boolean hasLeader = guards.stream()
            .anyMatch(candidate -> "evil_knight_prisoner_leader".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role")));
         return !prisoners.isEmpty() && hasLeader ? !(mob instanceof AbstractKnightEntity) && guards.isEmpty() : true;
      } else {
         return false;
      }
   }

   private static List<Mob> findAlivePrisoners(ServerLevel level, Mob mob, String groupId) {
      AABB searchBox = mob.getBoundingBox().inflate(64.0, 8.0, 64.0);
      return level.getEntitiesOfClass(
         Mob.class,
         searchBox,
         candidate -> candidate.isAlive()
            && !candidate.isRemoved()
            && "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
            && "evil_knight_prisoner_prisoner".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
            && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
      );
   }
}
