package com.javic.villagerpatrols.skirmish;

import com.javic.villagerpatrols.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class KnightZombieSkirmish extends AbstractSkirmish {
   private static final String GROUP_TYPE = "knight_zombie_skirmish";
   private static final int SELECTION_WEIGHT = 12;
   private static final int MIN_PLAYER_DISTANCE = 32;
   private static final int MAX_PLAYER_DISTANCE = 64;
   private static final int MIN_KNIGHT_SIDE_SIZE = 5;
   private static final int MAX_KNIGHT_SIDE_SIZE = 8;
   private static final int MIN_ZOMBIE_SIDE_SIZE = 12;
   private static final int MAX_ZOMBIE_SIDE_SIZE = 18;
   private static final float LEADER_CHANCE = 0.35F;
   private static final int MIN_SIDE_SEPARATION = 14;
   private static final int MAX_SIDE_SEPARATION = 24;
   private static final int SIDE_CLUSTER_RADIUS = 10;
   private static final int PROXIMITY_RADIUS = 192;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "knight_zombie_skirmish";
   }

   @Override
   int selectionWeight() {
      return 12;
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
      return 0;
   }

   @Override
   protected boolean canStart(ServerLevel level) {
      return level.isNight();
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseCommonSkirmishBiome(level, pos);
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildFriendlySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 5, 8);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();
      if (level.random.nextFloat() < 0.35F) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.KNIGHT_LEADER.get(), "leader"));
      }

      while (definitions.size() < totalMembers) {
         if (level.random.nextBoolean()) {
            definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.KNIGHT_SHIELD.get(), "shield"));
         } else {
            definitions.add(new AbstractSkirmish.EntitySpawnDefinition((EntityType<? extends Mob>)ModEntities.KNIGHT.get(), "soldier"));
         }
      }

      return definitions;
   }

   @Override
   protected List<AbstractSkirmish.EntitySpawnDefinition> buildEnemySide(ServerLevel level) {
      int totalMembers = this.randomBetween(level, 12, 18);
      List<AbstractSkirmish.EntitySpawnDefinition> definitions = new ArrayList<>();

      while (definitions.size() < totalMembers) {
         definitions.add(new AbstractSkirmish.EntitySpawnDefinition(EntityType.ZOMBIE, "zombie"));
      }

      return definitions;
   }
}
