package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.entity.NaturalTravelerEquipment;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class DesertCamelTravelerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "desert_camel_traveler_world_event";
   public static final String LEADER_ROLE = "leader";
   public static final String CAMEL_ROLE = "camel";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 2;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 4;
   private static final int CAMEL_POSITION_ATTEMPTS = 20;

   @Override
   String groupType() {
      return "desert_camel_traveler_world_event";
   }

   @Override
   int selectionWeight() {
      return 4;
   }

   @Override
   int minPlayerDistance() {
      return 24;
   }

   @Override
   int maxPlayerDistance() {
      return 56;
   }

   @Override
   int minCount() {
      return 1;
   }

   @Override
   int maxCount() {
      return 1;
   }

   @Override
   int clusterRadius() {
      return 2;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 4;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return SlimPatchCompat.resolveMaleVillagerType();
   }

   @Override
   protected String groupRole() {
      return "leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseLoneDesertTravelerWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "leader"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      EventVillagerSkins.applyDesertTravelerSkin(entity);
      EventVillagerProfessions.apply(level, entity, VillagerProfession.CLERIC);
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if ("leader".equals(placement.role())) {
         if (entity instanceof MaleVillagerEntity || entity instanceof FemaleVillagerEntity) {
            NaturalTravelerEquipment.tryEquipLoneTravelerWeapon(entity);
         }

         Camel camel = (Camel)EntityType.CAMEL.create(level);
         if (camel != null) {
            BlockPos camelPos = this.findCamelSpawnPosition(level, entity.blockPosition(), entity.position(), camel);
            if (camelPos != null) {
               camel.moveTo(camelPos.getX() + 0.5, camelPos.getY(), camelPos.getZ() + 0.5, entity.getYRot(), 0.0F);
               camel.finalizeSpawn(level, level.getCurrentDifficultyAt(camelPos), MobSpawnType.EVENT, null, null);
               PersistentEntityData.get(camel).putBoolean("villagerpatrols_event_entity", true);
               PersistentEntityData.get(camel).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
               PersistentEntityData.get(camel).putString("villagerpatrols_group_id", PersistentEntityData.get(entity).getString("villagerpatrols_group_id"));
               PersistentEntityData.get(camel).putString("villagerpatrols_group_type", "desert_camel_traveler_world_event");
               PersistentEntityData.get(camel).putString("villagerpatrols_group_role", "camel");
               camel.setPersistenceRequired();
               camel.standUpInstantly();
               if (camel.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                  camel.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.19);
               }

               level.addFreshEntity(camel);
               camel.setLeashedTo(entity, true);
            }
         }
      }
   }

   private BlockPos findCamelSpawnPosition(ServerLevel level, BlockPos leaderPos, Vec3 leaderCenter, Camel camel) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = level.random.nextDouble() * Math.PI * 2.0;
         int distance = 3 + level.random.nextInt(2);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = leaderPos.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (this.isValidCamelPosition(level, surface, leaderCenter, camel)) {
               return surface;
            }
         }
      }

      return null;
   }

   private boolean isValidCamelPosition(ServerLevel level, BlockPos pos, Vec3 leaderCenter, Camel camel) {
      if (!level.isLoaded(pos) || !level.isLoaded(pos.above())) {
         return false;
      }

      if (!level.canSeeSkyFromBelowWater(pos)) {
         return false;
      }

      if (!level.getBlockState(pos.below()).blocksMotion()) {
         return false;
      }

      if (level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()) {
         Vec3 camelPos = Vec3.atBottomCenterOf(pos);
         double distanceToLeader = camelPos.distanceToSqr(leaderCenter);
         if (!(distanceToLeader < 9.0) && !(distanceToLeader > 16.0)) {
            camel.moveTo(camelPos.x, pos.getY(), camelPos.z, 0.0F, 0.0F);
            return level.noCollision(camel);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
