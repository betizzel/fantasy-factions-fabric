package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LoneIceTravelerWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double SEARCH_RADIUS = 48.0;
   private static final double COMFORT_RADIUS_SQR = 9.0;
   private static final double FAR_DISTANCE_SQR = 196.0;
   private static final double FOLLOW_SPEED = 0.7;
   private static final double CATCH_UP_SPEED = 1.0;

   private LoneIceTravelerWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel && "lone_ice_traveler_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         if (mob instanceof Wolf wolf && "wolf_companion".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))) {
            wolf.setOrderedToSit(false);
            PathfinderMob leader = findLeader(wolf);
            if (leader == null) {
               wolf.discard();
            } else {
               if (wolf.getTarget() == leader) {
                  wolf.setTarget(null);
               }

               Vec3 targetPos = leader.position();
               if (wolf.distanceToSqr(targetPos) <= 9.0) {
                  if (wolf.getNavigation().isInProgress()) {
                     wolf.getNavigation().stop();
                  }
               } else {
                  double speed = wolf.distanceToSqr(leader) > 196.0 ? 1.0 : 0.7;
                  wolf.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
               }
            }
         }
      }
   }

   private static PathfinderMob findLeader(Wolf wolf) {
      String groupId = PersistentEntityData.get(wolf).getString("villagerpatrols_group_id");
      AABB searchBox = wolf.getBoundingBox().inflate(48.0, 8.0, 48.0);
      List<PathfinderMob> leaders = wolf.level().getEntitiesOfClass(PathfinderMob.class, searchBox, candidatex -> isMatchingLeader(candidatex, groupId, wolf));
      PathfinderMob nearestLeader = null;
      double nearestDistance = Double.MAX_VALUE;

      for (PathfinderMob candidate : leaders) {
         double distance = wolf.distanceToSqr(candidate);
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestLeader = candidate;
         }
      }

      return nearestLeader;
   }

   private static boolean isMatchingLeader(PathfinderMob candidate, String groupId, Wolf wolf) {
      if (candidate != wolf && candidate.isAlive()) {
         if (candidate.getType() == EntityType.WOLF) {
            return false;
         }

         CompoundTag data = PersistentEntityData.get(candidate);
         return groupId.equals(data.getString("villagerpatrols_group_id"))
            && "lone_ice_traveler_world_event".equals(data.getString("villagerpatrols_group_type"))
            && "traveler".equals(data.getString("villagerpatrols_group_role"));
      } else {
         return false;
      }
   }
}
