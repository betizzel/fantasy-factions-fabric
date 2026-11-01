package com.javic.villagerpatrols.block;

import com.javic.villagerpatrols.ModBlockEntities;
import com.javic.villagerpatrols.block.entity.WardBannerBlockEntity;
import com.javic.villagerpatrols.network.ClientboundOpenWardScreenMessage;
import com.javic.villagerpatrols.network.ModNetworking;
import com.javic.villagerpatrols.ward.WardBannerManager;
import com.javic.villagerpatrols.ward.WardBannerType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public final class WardBannerBlock extends BaseEntityBlock implements EntityBlock {
   public WardBannerBlock(Properties properties) {
      super(properties);
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      super.onPlace(state, level, pos, oldState, movedByPiston);
      if (level instanceof ServerLevel serverLevel && !state.is(oldState.getBlock())) {
         WardBannerManager.register(serverLevel, pos, WardBannerType.WARD_BANNER);
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (level instanceof ServerLevel serverLevel && !state.is(newState.getBlock())) {
         WardBannerManager.unregister(serverLevel, pos);
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new WardBannerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return level.isClientSide && blockEntityType == ModBlockEntities.WARD_BANNER.get()
         ? (tickerLevel, tickerPos, tickerState, blockEntity) -> WardBannerBlockEntity.clientTick(
            tickerLevel, tickerPos, tickerState, (WardBannerBlockEntity)blockEntity
         )
         : null;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
         if (level.getBlockEntity(pos) instanceof WardBannerBlockEntity wardBlockEntity) {
            if (level.isClientSide) {
               return InteractionResult.SUCCESS;
            }

            if (player instanceof ServerPlayer serverPlayer) {
               ModNetworking.sendToPlayer(serverPlayer, new ClientboundOpenWardScreenMessage(
                     pos, wardBlockEntity.isMuted(), wardBlockEntity.shouldShowCrystal(), wardBlockEntity.shouldShowParticles()));
            }

            return InteractionResult.CONSUME;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      double soundX = pos.getX() + 0.5;
      double soundY = pos.getY() + 0.8;
      double soundZ = pos.getZ() + 0.5;
      boolean muted = level.getBlockEntity(pos) instanceof WardBannerBlockEntity wardBlockEntity && wardBlockEntity.isMuted();
      boolean showParticles = !(level.getBlockEntity(pos) instanceof WardBannerBlockEntity wardBlockEntity && !wardBlockEntity.shouldShowParticles());
      if (!muted && random.nextInt(70) == 0) {
         level.playLocalSound(soundX, soundY, soundZ, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0F, 0.75F + random.nextFloat() * 0.2F, false);
      }

      if (!muted && random.nextInt(140) == 0) {
         level.playLocalSound(soundX, soundY, soundZ, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.0F, 0.85F + random.nextFloat() * 0.2F, false);
      }

      if (!muted && random.nextInt(220) == 0) {
         level.playLocalSound(soundX, soundY, soundZ, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 1.0F, 0.75F + random.nextFloat() * 0.15F, false);
      }

      if (showParticles && random.nextInt(3) == 0) {
         double x = pos.getX() + 0.35 + random.nextDouble() * 0.3;
         double y = pos.getY() + 0.95 + random.nextDouble() * 0.55;
         double z = pos.getZ() + 0.35 + random.nextDouble() * 0.3;
         double vx = (random.nextDouble() - 0.5) * 0.04;
         double vy = 0.02 + random.nextDouble() * 0.02;
         double vz = (random.nextDouble() - 0.5) * 0.04;
         level.addParticle(ParticleTypes.ENCHANT, x, y, z, vx, vy, vz);
      }

      if (showParticles && random.nextInt(12) == 0) {
         double x = pos.getX() + 0.4 + random.nextDouble() * 0.2;
         double y = pos.getY() + 1.15 + random.nextDouble() * 0.45;
         double z = pos.getZ() + 0.4 + random.nextDouble() * 0.2;
         level.addParticle(ParticleTypes.END_ROD, x, y, z, random.nextGaussian() * 0.003, 0.01 + random.nextDouble() * 0.01, random.nextGaussian() * 0.003);
      }
   }
}
