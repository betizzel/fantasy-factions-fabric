package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class VillagerMinerWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "villager_miner_world_event";
   public static final String GROUP_ROLE = "miner";
   public static final String TAG_MINER_ANCHOR_X = "villagerpatrols_miner_anchor_x";
   public static final String TAG_MINER_ANCHOR_Y = "villagerpatrols_miner_anchor_y";
   public static final String TAG_MINER_ANCHOR_Z = "villagerpatrols_miner_anchor_z";
   public static final String TAG_MINER_TARGET_X = "villagerpatrols_miner_target_x";
   public static final String TAG_MINER_TARGET_Y = "villagerpatrols_miner_target_y";
   public static final String TAG_MINER_TARGET_Z = "villagerpatrols_miner_target_z";
   public static final String TAG_MINER_BLOCKS_BROKEN = "villagerpatrols_miner_blocks_broken";
   public static final String TAG_MINER_SWINGS_ON_TARGET = "villagerpatrols_miner_swings_on_target";
   public static final String TAG_MINER_NEXT_SWING_AT = "villagerpatrols_miner_next_swing_at";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 4;
   private static final int MAX_PLAYER_DISTANCE = 8;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 3;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;
   private static final int MAX_SPAWN_Y = 64;
   private static final int MIN_OPEN_SIDES = 0;
   private static final int TARGET_SEARCH_RADIUS = 6;
   private static final int TARGET_SEARCH_VERTICAL_RANGE = 3;
   private static final int SPAWN_SEARCH_HORIZONTAL_RADIUS = 6;
   private static final int SPAWN_SEARCH_VERTICAL_RANGE = 16;
   private static final int[][] TORCH_OFFSETS = new int[][]{{0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, -1}, {-1, -1}, {1, 1}, {-1, 1}};

   @Override
   String groupType() {
      return "villager_miner_world_event";
   }

   @Override
   int selectionWeight() {
      return 4;
   }

   @Override
   int minPlayerDistance() {
      return 4;
   }

   @Override
   int maxPlayerDistance() {
      return 8;
   }

   @Override
   protected int effectiveMinPlayerDistance() {
      return 4;
   }

   @Override
   protected int effectiveMaxPlayerDistance() {
      return 8;
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
      return 3;
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
      return "miner";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return true;
   }

   @Override
   protected boolean requiresSkyAccess() {
      return false;
   }

   @Override
   protected boolean isExtraSpawnPositionValid(ServerLevel level, BlockPos surface) {
      return isValidMinerSpawn(level, surface);
   }

   @Override
   protected BlockPos findSpawnPosition(ServerLevel level, BlockPos anchor, EntityType<? extends Mob> type, List<Vec3> occupied) {
      int maxSpawnY = Math.min(64, level.getSeaLevel() - 8);
      int minSpawnY = level.getMinBuildHeight() + 1;
      if (maxSpawnY < minSpawnY) {
         return null;
      }

      int preferredY = Math.max(minSpawnY, Math.min(maxSpawnY, anchor.getY()));

      for (int attempt = 0; attempt < 128; attempt++) {
         int offsetX = level.random.nextInt(13) - 6;
         int offsetZ = level.random.nextInt(13) - 6;
         int offsetY = level.random.nextInt(33) - 16;
         int y = Math.max(minSpawnY, Math.min(maxSpawnY, preferredY + offsetY));
         BlockPos candidate = new BlockPos(anchor.getX() + offsetX, y, anchor.getZ() + offsetZ);
         if (this.isSpawnPositionValid(level, candidate, type, occupied)) {
            return candidate.immutable();
         }
      }

      BlockPos bestPos = null;
      double bestDistance = Double.MAX_VALUE;

      for (int dx = -6; dx <= 6; dx++) {
         for (int dz = -6; dz <= 6; dz++) {
            for (int dy = -16; dy <= 16; dy++) {
               int y = Math.max(minSpawnY, Math.min(maxSpawnY, preferredY + dy));
               BlockPos candidate = new BlockPos(anchor.getX() + dx, y, anchor.getZ() + dz);
               if (this.isSpawnPositionValid(level, candidate, type, occupied)) {
                  double distance = candidate.distSqr(anchor);
                  if (bestPos == null || distance < bestDistance) {
                     bestPos = candidate.immutable();
                     bestDistance = distance;
                  }
               }
            }
         }
      }

      return bestPos;
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "miner"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      BlockPos anchor = placement.pos();
      BlockPos target = findNearbyMiningTarget(level, anchor);
      if (target != null) {
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_anchor_x", anchor.getX());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_anchor_y", anchor.getY());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_anchor_z", anchor.getZ());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_target_x", target.getX());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_target_y", target.getY());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_target_z", target.getZ());
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_blocks_broken", 0);
         PersistentEntityData.get(entity).putInt("villagerpatrols_miner_swings_on_target", 0);
         PersistentEntityData.get(entity).putLong("villagerpatrols_miner_next_swing_at", level.getGameTime() + 30L + level.random.nextInt(11));
         applyMinerCompatibility(entity);
         EventVillagerSkins.applyMinerSkin(entity);
         EventVillagerProfessions.apply(level, entity, VillagerProfession.MASON);
      }
   }

   @Override
   protected void afterSpawn(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement) {
      BlockPos torchPos = this.findTorchPos(level, placement.pos());
      if (torchPos != null) {
         level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
      }
   }

   public static void applyMinerCompatibility(Mob mob) {
      ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
      mob.setItemSlot(EquipmentSlot.MAINHAND, pickaxe.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      CompoundTag persistentMainHand = pickaxe.copy().save(new CompoundTag());
      PersistentEntityData.get(mob).put("PersistentMainHand", persistentMainHand);
   }

   public static boolean isValidMinerSpawn(ServerLevel level, BlockPos surface) {
      if (surface.getY() > Math.min(64, level.getSeaLevel() - 8)) {
         return false;
      } else if (level.canSeeSky(surface) || level.canSeeSky(surface.above())) {
         return false;
      } else {
         return findNearbyMiningTarget(level, surface) == null ? false : countOpenSides(level, surface) >= 0;
      }
   }

   public static BlockPos findNearbyMiningTarget(ServerLevel level, BlockPos origin) {
      BlockPos bestPos = null;
      double bestDistance = Double.MAX_VALUE;

      for (int dy = -3; dy <= 3; dy++) {
         for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  BlockPos candidate = origin.offset(dx, dy, dz);
                  if (level.isLoaded(candidate)) {
                     BlockState state = level.getBlockState(candidate);
                     if (isValidMiningTarget(state)) {
                        double distance = candidate.distSqr(origin);
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           bestPos = candidate.immutable();
                        }
                     }
                  }
               }
            }
         }
      }

      return bestPos;
   }

   public static boolean isValidMiningTarget(BlockState state) {
      return state.is(Blocks.STONE)
         || state.is(Blocks.DEEPSLATE)
         || state.is(Blocks.COBBLESTONE)
         || state.is(Blocks.ANDESITE)
         || state.is(Blocks.DIORITE)
         || state.is(Blocks.GRANITE)
         || state.is(Blocks.TUFF);
   }

   private static int countOpenSides(ServerLevel level, BlockPos surface) {
      int openSides = 0;

      for (BlockPos candidate : new BlockPos[]{surface.north(), surface.south(), surface.east(), surface.west()}) {
         if (level.isLoaded(candidate)
            && level.isLoaded(candidate.above())
            && !level.getBlockState(candidate).blocksMotion()
            && !level.getBlockState(candidate.above()).blocksMotion()) {
            openSides++;
         }
      }

      return openSides;
   }

   private BlockPos findTorchPos(ServerLevel level, BlockPos around) {
      for (int[] offset : TORCH_OFFSETS) {
         BlockPos candidate = around.offset(offset[0], 0, offset[1]);
         if (this.isTorchPositionValid(level, candidate)) {
            return candidate;
         }
      }

      return null;
   }

   private boolean isTorchPositionValid(ServerLevel level, BlockPos pos) {
      if (!level.isLoaded(pos)) {
         return false;
      }

      BlockState state = level.getBlockState(pos);
      return state.canBeReplaced() && level.getFluidState(pos).isEmpty() ? Blocks.TORCH.defaultBlockState().canSurvive(level, pos) : false;
   }
}
