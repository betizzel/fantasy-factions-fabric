package com.javic.villagerpatrols.ward;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.FantasyVillagerSupport;
import com.javic.villagerpatrols.entity.FemaleHealerEntity;
import com.javic.villagerpatrols.entity.FemaleVampireVillagerEntity;
import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import com.javic.villagerpatrols.entity.HealerEntitySupport;
import com.javic.villagerpatrols.entity.MaleHealerEntity;
import com.javic.villagerpatrols.entity.MaleVampireVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import com.javic.villagerpatrols.entity.VampireEntitySupport;
import com.javic.villagerpatrols.entity.VillageGuardEntity;
import com.javic.villagerpatrols.entity.VillageGuardLeaderEntity;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class WardMobRemovalService {
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";

   private WardMobRemovalService() {
   }

   public static int removeMobs(ServerLevel level, BlockPos center, int radius) {
      AABB area = new AABB(center).inflate(radius, radius, radius);
      Set<Entity> toRemove = new LinkedHashSet<>();

      for (Mob mob : level.getEntitiesOfClass(Mob.class, area, WardMobRemovalService::shouldRemoveRootMob)) {
         collectEntityAndAssociates(mob, toRemove);
      }

      int removed = 0;

      for (Entity entity : toRemove) {
         if (!entity.isRemoved()) {
            entity.discard();
            removed++;
         }
      }

      return removed;
   }

   private static boolean shouldRemoveRootMob(Mob mob) {
      return isDirectVillagerPatrolsMob(mob) && !isProtected(mob);
   }

   private static void collectEntityAndAssociates(Entity entity, Set<Entity> toRemove) {
      if (entity != null && !entity.isRemoved() && toRemove.add(entity)) {
         for (Entity passenger : entity.getPassengers()) {
            if (shouldRemoveAssociated(passenger, entity)) {
               collectEntityAndAssociates(passenger, toRemove);
            }
         }

         Entity vehicle = entity.getVehicle();
         if (shouldRemoveAssociated(vehicle, entity)) {
            collectEntityAndAssociates(vehicle, toRemove);
         }
      }
   }

   private static boolean shouldRemoveAssociated(Entity candidate, Entity source) {
      if (candidate == null || candidate.isRemoved() || isProtected(candidate)) {
         return false;
      } else {
         return hasVillagerPatrolsTags(candidate)
            ? true
            : (candidate instanceof AbstractHorse || candidate instanceof Camel || candidate instanceof Ravager) && hasVillagerPatrolsTags(source);
      }
   }

   private static boolean isProtected(Entity entity) {
      if (entity instanceof Player || entity.hasCustomName()) {
         return true;
      } else if (!(entity instanceof Mob mob)) {
         return false;
      } else if (mob instanceof VillageGuardEntity || mob instanceof VillageGuardLeaderEntity || mob.isLeashed()) {
         return true;
      } else if (SlimPatchCompat.isFollowingOrStaying(mob)
         || FantasyVillagerSupport.isFantasyVillager(mob)
         || WizardEntitySupport.isExternalWizardVillager(mob)
         || HealerEntitySupport.isExternalHealerVillager(mob)
         || VampireEntitySupport.isExternalVampireVillager(mob)) {
         return true;
      } else {
         return !(mob instanceof MaleVillagerEntity) && !(mob instanceof Villager)
            ? false
            : !(mob instanceof MaleWizardEntity)
               && !(mob instanceof FemaleWizardEntity)
               && !(mob instanceof MaleHealerEntity)
               && !(mob instanceof FemaleHealerEntity)
               && !(mob instanceof MaleVampireVillagerEntity)
               && !(mob instanceof FemaleVampireVillagerEntity);
      }
   }

   private static boolean isDirectVillagerPatrolsMob(Entity entity) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return key != null && "villagerpatrols".equals(key.getNamespace()) && !(entity instanceof Villager) || isRemovableVillagerPatrolsVillager(entity);
   }

   private static boolean isRemovableVillagerPatrolsVillager(Entity entity) {
      return entity instanceof MaleWizardEntity
         || entity instanceof FemaleWizardEntity
         || entity instanceof MaleHealerEntity
         || entity instanceof FemaleHealerEntity
         || entity instanceof MaleVampireVillagerEntity
         || entity instanceof FemaleVampireVillagerEntity;
   }

   private static boolean hasVillagerPatrolsTags(Entity entity) {
      return PersistentEntityData.get(entity).getBoolean("villagerpatrols_event_entity")
         || PersistentEntityData.get(entity).contains("villagerpatrols_group_id")
         || PersistentEntityData.get(entity).contains("villagerpatrols_group_type");
   }
}
