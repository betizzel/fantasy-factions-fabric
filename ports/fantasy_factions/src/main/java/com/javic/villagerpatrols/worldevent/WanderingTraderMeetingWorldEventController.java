package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WanderingTraderMeetingWorldEventController {
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final double RETURN_DISTANCE_SQR = 9.0;
   private static final double RETURN_SPEED = 0.4;
   private static final double LOOK_RANGE = 12.0;

   private WanderingTraderMeetingWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "wandering_trader_meeting_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
         if (shouldCleanupBrokenEvent(level, mob)) {
            mob.discard();
         } else {
            if (mob.getTarget() != null) {
               mob.setTarget(null);
            }

            BlockPos anchor = getAnchor(mob);
            if (anchor != null) {
               if (mob.distanceToSqr(Vec3.atBottomCenterOf(anchor)) > 9.0) {
                  mob.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 0.4);
               } else {
                  mob.getNavigation().stop();
               }

               if (mob instanceof WanderingTrader trader) {
                  lookTrader(trader);
               } else {
                  lookVillager(level, mob);
               }
            }
         }
      }
   }

   private static void lookVillager(ServerLevel level, Mob mob) {
      if (PersistentEntityData.get(mob).hasUUID("villagerpatrols_meeting_trader_uuid")) {
         if (level.getEntity(PersistentEntityData.get(mob).getUUID("villagerpatrols_meeting_trader_uuid")) instanceof WanderingTrader wanderingTrader
            && wanderingTrader.isAlive()) {
            mob.getLookControl().setLookAt(wanderingTrader, 30.0F, 30.0F);
         }
      }
   }

   private static void lookTrader(WanderingTrader trader) {
      String groupId = PersistentEntityData.get(trader).getString("villagerpatrols_group_id");
      AABB searchBox = trader.getBoundingBox().inflate(12.0, 8.0, 12.0);
      List<Mob> villagers = trader.level()
         .getEntitiesOfClass(
            Mob.class,
            searchBox,
            candidate -> candidate.isAlive()
               && candidate != trader
               && "wandering_trader_meeting_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
               && "wandering_trader_meeting_villager".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
               && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
         );
      if (!villagers.isEmpty()) {
         Vec3 center = Vec3.ZERO;

         for (Mob villager : villagers) {
            center = center.add(villager.getX(), villager.getEyeY(), villager.getZ());
         }

         center = center.scale(1.0 / villagers.size());
         trader.getLookControl().setLookAt(center.x, center.y, center.z);
      }
   }

   private static BlockPos getAnchor(Mob mob) {
      return PersistentEntityData.get(mob).contains("villagerpatrols_meeting_anchor_x")
            && PersistentEntityData.get(mob).contains("villagerpatrols_meeting_anchor_y")
            && PersistentEntityData.get(mob).contains("villagerpatrols_meeting_anchor_z")
         ? new BlockPos(
            PersistentEntityData.get(mob).getInt("villagerpatrols_meeting_anchor_x"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_meeting_anchor_y"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_meeting_anchor_z")
         )
         : null;
   }

   public static boolean shouldCleanupBrokenEvent(ServerLevel level, Mob mob) {
      if (mob != null && !mob.isRemoved()) {
         BlockPos anchor = getAnchor(mob);
         if (anchor != null && level.isLoaded(anchor)) {
            String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
            if (groupId.isEmpty()) {
               return true;
            } else {
               AABB searchBox = mob.getBoundingBox().inflate(12.0, 8.0, 12.0);
               if (mob instanceof WanderingTrader) {
                  return level.getEntitiesOfClass(
                        Mob.class,
                        searchBox,
                        candidate -> candidate.isAlive()
                           && candidate != mob
                           && "wandering_trader_meeting_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
                           && "wandering_trader_meeting_villager".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
                           && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                     )
                     .isEmpty();
               } else {
                  return !PersistentEntityData.get(mob).hasUUID("villagerpatrols_meeting_trader_uuid")
                     ? true
                     : !(
                        level.getEntity(PersistentEntityData.get(mob).getUUID("villagerpatrols_meeting_trader_uuid")) instanceof WanderingTrader wanderingTrader
                           && wanderingTrader.isAlive()
                           && !wanderingTrader.isRemoved()
                     );
               }
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }
}
