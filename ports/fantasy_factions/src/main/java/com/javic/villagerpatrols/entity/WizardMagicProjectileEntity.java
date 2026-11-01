package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class WizardMagicProjectileEntity extends AbstractHurtingProjectile {
   public WizardMagicProjectileEntity(EntityType<? extends WizardMagicProjectileEntity> entityType, Level level) {
      super(entityType, level);
   }

   public WizardMagicProjectileEntity(Level level, LivingEntity owner, Vec3 movement) {
      super((EntityType)ModEntities.WIZARD_MAGIC_PROJECTILE.get(), owner, movement.x, movement.y, movement.z, level);
      this.xPower *= 1.6;
      this.yPower *= 1.6;
      this.zPower *= 1.6;
   }

   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      if (!this.level().isClientSide && this.getOwner() instanceof LivingEntity owner && result.getEntity() instanceof LivingEntity target) {
         if (owner instanceof Mob mobOwner && WizardEntitySupport.canSpellDamageTarget(mobOwner, target)) {
            target.hurt(this.level().damageSources().indirectMagic(this, owner), 5.0F);
         }
      }
   }

   protected void onHit(HitResult result) {
      super.onHit(result);
      if (result.getType() != Type.ENTITY || !this.ownedBy(((EntityHitResult)result).getEntity())) {
         if (!this.level().isClientSide && this.getOwner() instanceof LivingEntity owner && this.level() instanceof ServerLevel serverLevel) {
            WizardEntitySupport.spawnWizardBreathCloud(serverLevel, owner, result.getLocation());
            this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
            this.discard();
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   protected boolean canHitEntity(Entity target) {
      if (!super.canHitEntity(target)) {
         return false;
      } else {
         return this.getOwner() instanceof LivingEntity owner && target instanceof LivingEntity livingTarget
            ? !owner.isAlliedTo(target)
               && (!(livingTarget instanceof Player player && owner.getLastHurtByMob() != player) || owner instanceof Mob mob && mob.getTarget() == player)
               && !(livingTarget instanceof Monster monster && !WizardEntitySupport.isDefensiveTarget(monster))
            : true;
      }
   }

   protected ParticleOptions getTrailParticle() {
      return ParticleTypes.DRAGON_BREATH;
   }

   protected boolean shouldBurn() {
      return false;
   }
}
