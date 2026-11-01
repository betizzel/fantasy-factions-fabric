package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import com.javic.villagerpatrols.entity.VillagerIllagerSkirmishEquipment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class VillagerBanditSkirmish extends AbstractSkirmish {
   private static final String GROUP_TYPE = "villager_bandit_skirmish";
   private static final int SELECTION_WEIGHT = 6;
   private static final int MIN_PLAYER_DISTANCE = 32;
   private static final int MAX_PLAYER_DISTANCE = 64;
   private static final int MIN_VILLAGER_SIDE_SIZE = 10;
   private static final int MAX_VILLAGER_SIDE_SIZE = 15;
   private static final int MIN_BANDIT_SIDE_SIZE = 8;
   private static final int MAX_BANDIT_SIDE_SIZE = 12;
   private static final int MIN_SIDE_SEPARATION = 14;
   private static final int MAX_SIDE_SEPARATION = 24;
   private static final int SIDE_CLUSTER_RADIUS = 10;
   private static final int PROXIMITY_RADIUS = 192;
   private static final int VILLAGE_RADIUS_SECTIONS = 2;

   @Override
   String groupType() {
      return "villager_bandit_skirmish";
   }

   @Override
   int selectionWeight() {
      return 6;
   }

   @Override
   int minPlayerDistance() {
      return 32;
   }

   @Override
   int maxPlayerDistance() {
      return 64;
   }

   @Override
   int minSideSeparation() {
      return 14;
   }

   @Override
   int maxSideSeparation() {
      return 24;
   }

   @Override
   int sideClusterRadius() {
      return 10;
   }

   @Override
   int proximityRadius() {
      return 192;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 2;
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonSkirmishBiome(level, pos);
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildFriendlySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 10, 15);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "villager_fighter"));
      }

      return definitions;
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildEnemySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 8, 12);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.BANDIT.get(), "bandit_fighter"));
      }

      return definitions;
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractSkirmish.SpawnPlacement placement, String groupId) {
      if (EventVillagerSkins.canApplyTo(entity)) {
         VillagerIllagerSkirmishEquipment.equipRandomWeapon(entity);
         EventVillagerSkins.applyBanditSkirmishSkin(entity);
      }
   }
}
