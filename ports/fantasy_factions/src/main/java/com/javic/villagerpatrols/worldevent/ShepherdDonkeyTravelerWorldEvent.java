package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public final class ShepherdDonkeyTravelerWorldEvent extends AbstractWorldEvent {
   private static final TagKey<Biome> SHEPHERD_MOVING_ANIMALS_WORLD_EVENTS = TagKey.create(
      Registries.BIOME, new ResourceLocation("villagerpatrols", "shepherd_moving_animals_world_events")
   );
   public static final String GROUP_TYPE = "shepherd_donkey_traveler_world_event";
   public static final String LEADER_ROLE = "leader";
   public static final String DONKEY_ROLE = "donkey";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 2;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;
   private static final int DONKEY_POSITION_ATTEMPTS = 20;

   @Override
   String groupType() {
      return "shepherd_donkey_traveler_world_event";
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
      return 1;
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
      return level.getBiome(pos).is(SHEPHERD_MOVING_ANIMALS_WORLD_EVENTS);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "leader"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      ShepherdMovingAnimalsWorldEvent.applyShepherdCompatibility(entity);
      EventVillagerSkins.applyShepherdSkin(entity);
      EventVillagerProfessions.apply(level, entity, VillagerProfession.SHEPHERD);
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      if ("leader".equals(placement.role())) {
         Donkey donkey = (Donkey)EntityType.DONKEY.create(level);
         if (donkey != null) {
            BlockPos donkeyPos = this.findDonkeySpawnPosition(level, entity.blockPosition(), entity.position(), donkey);
            if (donkeyPos != null) {
               donkey.moveTo(donkeyPos.getX() + 0.5, donkeyPos.getY(), donkeyPos.getZ() + 0.5, entity.getYRot(), 0.0F);
               donkey.finalizeSpawn(level, level.getCurrentDifficultyAt(donkeyPos), MobSpawnType.EVENT, null, null);
               PersistentEntityData.get(donkey).putBoolean("villagerpatrols_event_entity", true);
               PersistentEntityData.get(donkey).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
               PersistentEntityData.get(donkey).putString("villagerpatrols_group_id", PersistentEntityData.get(entity).getString("villagerpatrols_group_id"));
               PersistentEntityData.get(donkey).putString("villagerpatrols_group_type", "shepherd_donkey_traveler_world_event");
               PersistentEntityData.get(donkey).putString("villagerpatrols_group_role", "donkey");
               donkey.setPersistenceRequired();
               if (donkey.getAttribute(Attributes.MOVEMENT_SPEED) != null && donkey.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0.2) {
                  donkey.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2);
               }

               level.addFreshEntity(donkey);
               donkey.setLeashedTo(entity, true);
            }
         }
      }
   }

   private BlockPos findDonkeySpawnPosition(ServerLevel level, BlockPos leaderPos, Vec3 leaderCenter, Donkey donkey) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = level.random.nextDouble() * Math.PI * 2.0;
         int distance = 2 + level.random.nextInt(2);
         int offsetX = (int)Math.round(Math.cos(angle) * distance);
         int offsetZ = (int)Math.round(Math.sin(angle) * distance);
         BlockPos sample = leaderPos.offset(offsetX, 0, offsetZ);
         if (level.isLoaded(sample)) {
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (this.isValidDonkeyPosition(level, surface, leaderCenter, donkey)) {
               return surface;
            }
         }
      }

      return null;
   }

   private boolean isValidDonkeyPosition(ServerLevel level, BlockPos pos, Vec3 leaderCenter, Donkey donkey) {
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
         Vec3 donkeyPos = Vec3.atBottomCenterOf(pos);
         if (!(donkeyPos.distanceToSqr(leaderCenter) < 4.0) && !(donkeyPos.distanceToSqr(leaderCenter) > 9.0)) {
            donkey.moveTo(donkeyPos.x, pos.getY(), donkeyPos.z, 0.0F, 0.0F);
            return level.noCollision(donkey);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
