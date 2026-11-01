package com.javic.villagerpatrols.block.entity;

import com.javic.villagerpatrols.ModBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class WardBannerBlockEntity extends BlockEntity {
   private static final String TAG_MUTED = "Muted";
   private static final String TAG_SHOW_CRYSTAL = "ShowCrystal";
   private static final String TAG_SHOW_PARTICLES = "ShowParticles";
   private static final String TAG_NEXT_REMOVE_MOBS_TICK = "NextRemoveMobsTick";
   private int ambientSoundCooldown;
   private boolean muted;
   private boolean showCrystal = true;
   private boolean showParticles = true;
   private long nextRemoveMobsTick;

   public WardBannerBlockEntity(BlockPos pos, BlockState blockState) {
      super((BlockEntityType)ModBlockEntities.WARD_BANNER.get(), pos, blockState);
   }

   public boolean isMuted() {
      return this.muted;
   }

   public void setMuted(boolean muted) {
      if (this.muted != muted) {
         this.muted = muted;
         this.sync();
      }
   }

   public boolean shouldShowCrystal() {
      return this.showCrystal;
   }

   public void setShowCrystal(boolean showCrystal) {
      if (this.showCrystal != showCrystal) {
         this.showCrystal = showCrystal;
         this.sync();
      }
   }

   public boolean shouldShowParticles() {
      return this.showParticles;
   }

   public void setShowParticles(boolean showParticles) {
      if (this.showParticles != showParticles) {
         this.showParticles = showParticles;
         this.sync();
      }
   }

   public long getNextRemoveMobsTick() {
      return this.nextRemoveMobsTick;
   }

   public void setNextRemoveMobsTick(long nextRemoveMobsTick) {
      if (this.nextRemoveMobsTick != nextRemoveMobsTick) {
         this.nextRemoveMobsTick = nextRemoveMobsTick;
         this.sync();
      }
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.muted = tag.getBoolean("Muted");
      this.showCrystal = !tag.contains("ShowCrystal") || tag.getBoolean("ShowCrystal");
      this.showParticles = !tag.contains("ShowParticles") || tag.getBoolean("ShowParticles");
      this.nextRemoveMobsTick = tag.getLong("NextRemoveMobsTick");
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putBoolean("Muted", this.muted);
      tag.putBoolean("ShowCrystal", this.showCrystal);
      tag.putBoolean("ShowParticles", this.showParticles);
      tag.putLong("NextRemoveMobsTick", this.nextRemoveMobsTick);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      this.saveAdditional(tag);
      return tag;
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, WardBannerBlockEntity blockEntity) {
      if (level.isClientSide && !blockEntity.isMuted()) {
         if (blockEntity.ambientSoundCooldown > 0) {
            blockEntity.ambientSoundCooldown--;
         } else {
            RandomSource random = level.getRandom();
            level.playLocalSound(
               pos.getX() + 0.5,
               pos.getY() + 0.8,
               pos.getZ() + 0.5,
               SoundEvents.CONDUIT_AMBIENT,
               SoundSource.BLOCKS,
               1.8F,
               0.65F + random.nextFloat() * 0.2F,
               false
            );
            blockEntity.ambientSoundCooldown = 80 + random.nextInt(20);
         }
      }
   }

   private void sync() {
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }
}
