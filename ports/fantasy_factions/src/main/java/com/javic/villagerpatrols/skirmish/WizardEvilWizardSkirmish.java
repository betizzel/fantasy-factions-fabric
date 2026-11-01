package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class WizardEvilWizardSkirmish extends AbstractSkirmish {
   private static final String GROUP_TYPE = "wizard_evil_wizard_skirmish";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 32;
   private static final int MAX_PLAYER_DISTANCE = 64;
   private static final int MIN_SIDE_SIZE = 5;
   private static final int MAX_SIDE_SIZE = 7;
   private static final int MIN_SIDE_SEPARATION = 14;
   private static final int MAX_SIDE_SEPARATION = 24;
   private static final int SIDE_CLUSTER_RADIUS = 6;
   private static final int PROXIMITY_RADIUS = 192;
   private static final int VILLAGE_RADIUS_SECTIONS = 4;
   private int pendingSideSize;

   @Override
   String groupType() {
      return "wizard_evil_wizard_skirmish";
   }

   @Override
   int selectionWeight() {
      return 4;
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
      return 6;
   }

   @Override
   int proximityRadius() {
      return 192;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 4;
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonSkirmishBiome(level, pos);
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildFriendlySide(ServerLevel level) {
      this.pendingSideSize = this.randomBetween(level, 5, 7);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < this.pendingSideSize) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition(this.selectWizardType(level), "wizard_fighter"));
      }

      return definitions;
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildEnemySide(ServerLevel level) {
      int totalMembers = this.pendingSideSize > 0 ? this.pendingSideSize : this.randomBetween(level, 5, 7);
      this.pendingSideSize = 0;
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.EVIL_WIZARD.get(), "evil_wizard_fighter"));
      }

      return definitions;
   }

   private EntityType<? extends Mob> selectWizardType(ServerLevel level) {
      return level.random.nextBoolean() ? (EntityType)ModEntities.MALE_WIZARD.get() : (EntityType)ModEntities.FEMALE_WIZARD.get();
   }
}
