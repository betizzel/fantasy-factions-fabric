package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class EvilKnightOrcSkirmish extends AbstractSkirmish {
   private static final String GROUP_TYPE = "evil_knight_orc_skirmish";
   private static final int SELECTION_WEIGHT = 10;
   private static final int MIN_PLAYER_DISTANCE = 32;
   private static final int MAX_PLAYER_DISTANCE = 64;
   private static final int MIN_EVIL_KNIGHT_SIDE_SIZE = 12;
   private static final int MAX_EVIL_KNIGHT_SIDE_SIZE = 15;
   private static final int MIN_ORC_SIDE_SIZE = 8;
   private static final int MAX_ORC_SIDE_SIZE = 12;
   private static final float LEADER_CHANCE = 0.3F;
   private static final int MIN_SIDE_SEPARATION = 14;
   private static final int MAX_SIDE_SEPARATION = 24;
   private static final int SIDE_CLUSTER_RADIUS = 5;
   private static final int PROXIMITY_RADIUS = 192;
   private static final int VILLAGE_RADIUS_SECTIONS = 5;

   @Override
   String groupType() {
      return "evil_knight_orc_skirmish";
   }

   @Override
   int selectionWeight() {
      return 10;
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
      return 5;
   }

   @Override
   int proximityRadius() {
      return 192;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 5;
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseOrcSkirmishBiome(level, pos);
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildFriendlySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 12, 15);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();
      if (level.random.nextFloat() < 0.3F) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.EVIL_KNIGHT_LEADER.get(), "evil_knight_fighter"));
      }

      while (definitions.size() < totalMembers) {
         if (level.random.nextBoolean()) {
            definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.EVIL_KNIGHT_SHIELD.get(), "evil_knight_fighter"));
         } else {
            definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.EVIL_KNIGHT.get(), "evil_knight_fighter"));
         }
      }

      return definitions;
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildEnemySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 8, 12);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.ORC.get(), "orc_fighter"));
      }

      return definitions;
   }
}
