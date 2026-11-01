package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class VillagerMinerWorldEventController {
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final int TARGET_RADIUS = 4;
   private static final int MAX_BROKEN_BLOCKS = 9;
   private static final int SWINGS_TO_BREAK = 8;
   private static final long MIN_SWING_INTERVAL = 30L;
   private static final long MAX_SWING_INTERVAL = 40L;
   private static final double RETURN_DISTANCE_SQR = 9.0;
   private static final double BREAK_DISTANCE_SQR = 6.25;
   private static final double MOVE_SPEED = 0.35;

   private VillagerMinerWorldEventController() {
   }

   public static void tickMob(Mob mob) {
      if (mob.level() instanceof ServerLevel level
         && "villager_miner_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
         && "miner".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))) {
         VillagerMinerWorldEvent.applyMinerCompatibility(mob);
         setMiningActive(mob, false);
         BlockPos anchor = getAnchor(mob);
         if (anchor == null || !level.isLoaded(anchor)) {
            clearMiningProgress(level, mob);
         } else if (PersistentEntityData.get(mob).getInt("villagerpatrols_miner_blocks_broken") >= 9) {
            clearMiningProgress(level, mob);
            keepNearAnchor(mob, anchor);
            mob.getNavigation().stop();
         } else {
            BlockPos target = resolveTarget(level, mob, anchor);
            if (target == null) {
               clearMiningProgress(level, mob);
               keepNearAnchor(mob, anchor);
            } else {
               if (mob.getTarget() != null) {
                  mob.setTarget(null);
               }

               Vec3 lookPos = Vec3.atCenterOf(target);
               mob.getLookControl().setLookAt(lookPos.x, lookPos.y, lookPos.z, 30.0F, 30.0F);
               if (mob.distanceToSqr(Vec3.atBottomCenterOf(anchor)) > 9.0) {
                  clearMiningProgress(level, mob, target);
                  mob.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 0.35);
               } else if (mob.distanceToSqr(lookPos) > 6.25) {
                  clearMiningProgress(level, mob, target);
                  Vec3 standPos = Vec3.atBottomCenterOf(anchor);
                  mob.getNavigation().moveTo(standPos.x, standPos.y, standPos.z, 0.35);
               } else {
                  mob.getNavigation().stop();
                  setMiningActive(mob, true);
                  trySwing(level, mob, target);
               }
            }
         }
      }
   }

   private static void setMiningActive(Mob mob, boolean mining) {
      if (mob instanceof MaleVillagerEntity maleVillager) {
         maleVillager.setMinerMining(mining);
      } else if (mob instanceof FemaleVillagerEntity femaleVillager) {
         femaleVillager.setMinerMining(mining);
      }

      PersistentEntityData.get(mob).putBoolean("villagerpatrols_miner_mining_active", mining);
   }

   private static void trySwing(ServerLevel level, Mob mob, BlockPos target) {
      BlockState state = level.getBlockState(target);
      if (!VillagerMinerWorldEvent.isValidMiningTarget(state)) {
         clearMiningProgress(level, mob, target);
         setMiningActive(mob, false);
         clearTarget(mob);
      } else {
         long gameTime = level.getGameTime();
         long nextSwingAt = PersistentEntityData.get(mob).getLong("villagerpatrols_miner_next_swing_at");
         if (gameTime >= nextSwingAt) {
            if (mob.getMainHandItem().getItem() != Items.IRON_PICKAXE) {
               VillagerMinerWorldEvent.applyMinerCompatibility(mob);
            }

            mob.swing(InteractionHand.MAIN_HAND);
            level.playSound(
               null,
               target,
               state.getSoundType().getHitSound(),
               SoundSource.BLOCKS,
               0.25F,
               state.getSoundType().getPitch() * 0.5F
            );
            int swings = PersistentEntityData.get(mob).getInt("villagerpatrols_miner_swings_on_target") + 1;
            PersistentEntityData.get(mob).putInt("villagerpatrols_miner_swings_on_target", swings);
            PersistentEntityData.get(mob).putLong("villagerpatrols_miner_next_swing_at", gameTime + 30L + mob.getRandom().nextInt(11));
            level.destroyBlockProgress(mob.getId(), target, Math.min(9, swings * 10 / 8));
            if (swings >= 8 && isValidTarget(level, target)) {
               clearMiningProgress(level, mob, target);
               setMiningActive(mob, false);
               if (level.destroyBlock(target, true, mob)) {
                  PersistentEntityData.get(mob)
                     .putInt("villagerpatrols_miner_blocks_broken", PersistentEntityData.get(mob).getInt("villagerpatrols_miner_blocks_broken") + 1);
               }

               clearTarget(mob);
            }
         }
      }
   }

   private static BlockPos resolveTarget(ServerLevel level, Mob mob, BlockPos anchor) {
      BlockPos savedTarget = getTarget(mob);
      if (savedTarget != null && isValidTarget(level, savedTarget) && savedTarget.distSqr(anchor) <= 25.0) {
         return savedTarget;
      }

      BlockPos replacement = VillagerMinerWorldEvent.findNearbyMiningTarget(level, anchor);
      if (replacement != null && !(replacement.distSqr(anchor) > 25.0)) {
         if (savedTarget != null && !savedTarget.equals(replacement)) {
            clearMiningProgress(level, mob, savedTarget);
         }

         PersistentEntityData.get(mob).putInt("villagerpatrols_miner_target_x", replacement.getX());
         PersistentEntityData.get(mob).putInt("villagerpatrols_miner_target_y", replacement.getY());
         PersistentEntityData.get(mob).putInt("villagerpatrols_miner_target_z", replacement.getZ());
         PersistentEntityData.get(mob).putInt("villagerpatrols_miner_swings_on_target", 0);
         return replacement;
      } else {
         clearMiningProgress(level, mob);
         clearTarget(mob);
         return null;
      }
   }

   private static boolean isValidTarget(ServerLevel level, BlockPos target) {
      return level.isLoaded(target) && VillagerMinerWorldEvent.isValidMiningTarget(level.getBlockState(target));
   }

   private static void keepNearAnchor(Mob mob, BlockPos anchor) {
      Vec3 anchorCenter = Vec3.atBottomCenterOf(anchor);
      if (mob.distanceToSqr(anchorCenter) > 9.0) {
         mob.getNavigation().moveTo(anchorCenter.x, anchorCenter.y, anchorCenter.z, 0.35);
      } else {
         mob.getNavigation().stop();
      }
   }

   private static BlockPos getAnchor(Mob mob) {
      return PersistentEntityData.get(mob).contains("villagerpatrols_miner_anchor_x")
            && PersistentEntityData.get(mob).contains("villagerpatrols_miner_anchor_y")
            && PersistentEntityData.get(mob).contains("villagerpatrols_miner_anchor_z")
         ? new BlockPos(
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_anchor_x"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_anchor_y"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_anchor_z")
         )
         : null;
   }

   private static BlockPos getTarget(Mob mob) {
      return PersistentEntityData.get(mob).contains("villagerpatrols_miner_target_x")
            && PersistentEntityData.get(mob).contains("villagerpatrols_miner_target_y")
            && PersistentEntityData.get(mob).contains("villagerpatrols_miner_target_z")
         ? new BlockPos(
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_target_x"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_target_y"),
            PersistentEntityData.get(mob).getInt("villagerpatrols_miner_target_z")
         )
         : null;
   }

   private static void clearMiningProgress(ServerLevel level, Mob mob) {
      BlockPos target = getTarget(mob);
      if (target != null) {
         clearMiningProgress(level, mob, target);
      } else {
         PersistentEntityData.get(mob).putInt("villagerpatrols_miner_swings_on_target", 0);
      }
   }

   private static void clearMiningProgress(ServerLevel level, Mob mob, BlockPos target) {
      level.destroyBlockProgress(mob.getId(), target, -1);
      PersistentEntityData.get(mob).putInt("villagerpatrols_miner_swings_on_target", 0);
   }

   private static void clearTarget(Mob mob) {
      PersistentEntityData.get(mob).remove("villagerpatrols_miner_target_x");
      PersistentEntityData.get(mob).remove("villagerpatrols_miner_target_y");
      PersistentEntityData.get(mob).remove("villagerpatrols_miner_target_z");
   }
}
