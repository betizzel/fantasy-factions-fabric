package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;

public class ShadowRiderSpawnEntity extends ShadowEntity {
   private static final float OVERWORLD_SPAWN_CHANCE = 0.35F;
   private static final float OVERWORLD_DAY_SPAWN_CHANCE = 0.02F;
   private static final float NETHER_SPAWN_CHANCE = 0.15F;
   private static final float END_SPAWN_CHANCE = 0.08F;

   public ShadowRiderSpawnEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
   }

   public static boolean checkShadowRiderSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      if (!Config.isNaturalSpawnEnabled((EntityType<?>)ModEntities.SHADOW_RIDER_SPAWN.get())
         || level.getDifficulty() == Difficulty.PEACEFUL
         || !Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
         return false;
      } else if (level.getLevel().dimension() == Level.NETHER) {
         return Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.15F;
      } else if (level.getLevel().dimension() == Level.END) {
         return Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.08F;
      } else {
         boolean deepDark = level.getBiome(pos).is(Biomes.DEEP_DARK);
         if (deepDark) {
            return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.35F;
         } else {
            return level.getLevel().isDay() && level.canSeeSky(pos)
               ? Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.02F
               : Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.35F;
         }
      }
   }
}
