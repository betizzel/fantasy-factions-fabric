package com.javic.villagerpatrols.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CampingSeatEntity extends Entity {
   private static final double PASSENGER_Y_OFFSET = -0.6;
   private static final double DISMOUNT_Y_OFFSET = 0.8;

   public CampingSeatEntity(EntityType<? extends CampingSeatEntity> entityType, Level level) {
      super(entityType, level);
      this.blocksBuilding = false;
      this.noPhysics = true;
      this.setNoGravity(true);
      this.setInvulnerable(true);
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
   }

   public void tick() {
      this.noPhysics = true;
      this.setNoGravity(true);
      this.setDeltaMovement(Vec3.ZERO);
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.getFirstPassenger() instanceof Mob passenger && this.getPassengers().size() == 1) {
            if (!passenger.isAlive() || passenger.isRemoved() || passenger.getVehicle() != this) {
               if (passenger.getVehicle() == this) {
                  passenger.stopRiding();
               }

               this.discard();
            }
         } else {
            this.discard();
         }
      }
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      return new Vec3(this.getX(), this.getY() + 0.8, this.getZ());
   }

   public double getPassengersRidingOffset() {
      return -0.6;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   public boolean isInvisible() {
      return true;
   }
}
