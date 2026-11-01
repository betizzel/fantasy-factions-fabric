package com.javic.villagerpatrols.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class SpawnSurfaceValidator {
   private static final int MAX_WORLD_EVENT_SPAWN_Y = 150;
   private static final int RELATIVE_HEIGHT_LIMIT = 6;
   private static final int RELATIVE_HEIGHT_SAMPLE_DISTANCE = 8;
   private static final int RELATIVE_HEIGHT_REQUIRED_LOWER_SAMPLES = 5;
   private static final int[][] RELATIVE_HEIGHT_SAMPLE_OFFSETS = new int[][]{{8, 0}, {-8, 0}, {0, 8}, {0, -8}, {8, 8}, {8, -8}, {-8, 8}, {-8, -8}};

   private SpawnSurfaceValidator() {
   }

   public static boolean hasValidSpawnSupport(LevelReader level, BlockPos surface) {
      return !isBlockedSupportSurface(level, surface);
   }

   public static boolean isBlockedSupportSurface(LevelReader level, BlockPos surface) {
      BlockPos supportPos = surface.below();
      ChunkAccess chunk = getChunkIfAvailable(level, supportPos);
      return chunk == null || isSlabSupport(chunk.getBlockState(supportPos));
   }

   public static boolean isSlabSupport(BlockState state) {
      return state.is(BlockTags.SLABS) || state.hasProperty(BlockStateProperties.SLAB_TYPE) || state.getBlock() instanceof SlabBlock;
   }

   public static boolean isSurfaceHeightSafe(LevelReader level, BlockPos surface) {
      return surface.getY() <= 150 && isRelativeHeightSafe(level, surface);
   }

   private static boolean isRelativeHeightSafe(LevelReader level, BlockPos surface) {
      int lowerSamples = 0;
      int sampled = 0;

      for (int[] offset : RELATIVE_HEIGHT_SAMPLE_OFFSETS) {
         BlockPos sampleBase = surface.offset(offset[0], 0, offset[1]);
         ChunkAccess chunk = getChunkIfAvailable(level, sampleBase);
         if (chunk != null) {
            BlockPos nearbySurface = new BlockPos(
               sampleBase.getX(), chunk.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, sampleBase.getX(), sampleBase.getZ()), sampleBase.getZ()
            );
            sampled++;
            if (surface.getY() - nearbySurface.getY() >= 6) {
               lowerSamples++;
            }
         }
      }

      return sampled < 5 || lowerSamples < 5;
   }

   private static ChunkAccess getChunkIfAvailable(LevelReader level, BlockPos pos) {
      int chunkX = pos.getX() >> 4;
      int chunkZ = pos.getZ() >> 4;
      return !level.hasChunk(chunkX, chunkZ) ? null : level.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
   }
}
