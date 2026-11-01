package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ShadowRiderMountSupport {
   private static final String TAG_SHADOW_RIDER_MOUNT = "villagerpatrols_shadow_rider_mount";
   private static final String TAG_SHADOW_RIDER_TARGET_STATE = "villagerpatrols_shadow_rider_target_state";
   private static final String TAG_SHADOW_RIDER_STATE_TICKS = "villagerpatrols_shadow_rider_state_ticks";
   private static final String TAG_SHADOW_RIDER_ATTACK_COOLDOWN = "villagerpatrols_shadow_rider_attack_cooldown";
   private static final String TAG_SHADOW_RIDER_GOAL_X = "villagerpatrols_shadow_rider_goal_x";
   private static final String TAG_SHADOW_RIDER_GOAL_Y = "villagerpatrols_shadow_rider_goal_y";
   private static final String TAG_SHADOW_RIDER_GOAL_Z = "villagerpatrols_shadow_rider_goal_z";
   private static final String TAG_SHADOW_RIDER_ORBIT_ANGLE = "villagerpatrols_shadow_rider_orbit_angle";
   private static final float SHADOW_RIDER_MOUNT_SCALE = 2.0F;
   private static final Vec3 SHADOW_RIDER_ATTACHMENT = new Vec3(0.0, 0.95, 0.0);
   private static final int STATE_APPROACH = 0;
   private static final int STATE_DIVE = 1;
   private static final int STATE_ESCAPE = 2;
   private static final int APPROACH_MIN_TICKS = 25;
   private static final int APPROACH_MAX_TICKS = 40;
   private static final int DIVE_TICKS = 22;
   private static final int ESCAPE_TICKS = 55;
   private static final int ATTACK_COOLDOWN_TICKS = 20;
   private static final double APPROACH_RADIUS = 10.0;
   private static final double APPROACH_HEIGHT = 7.0;
   private static final double APPROACH_SPEED = 0.45;
   private static final double DIVE_SPEED = 0.95;
   private static final double ESCAPE_SPEED = 0.7;
   private static final double ESCAPE_DISTANCE = 16.0;
   private static final double ESCAPE_HEIGHT = 10.0;
   private static final double DIVE_LEAD = 2.0;
   private static final double ATTACK_RANGE_BUFFER = 1.0;

   private ShadowRiderMountSupport() {
   }

   public static void markMount(Phantom phantom) {
      PersistentEntityData.get(phantom).putBoolean("villagerpatrols_shadow_rider_mount", true);
      phantom.setPhantomSize(5);
   }

   public static boolean isShadowRiderMount(@Nullable Entity entity) {
      return entity instanceof Phantom phantom && (PersistentEntityData.get(phantom).getBoolean("villagerpatrols_shadow_rider_mount") || phantom.getScale() >= 1.99F);
   }

   public static Vec3 shadowVehicleAttachmentPoint(Entity vehicle, Vec3 fallback) {
      return isShadowRiderMount(vehicle) ? SHADOW_RIDER_ATTACHMENT : fallback;
   }

   @Nullable
   public static Phantom spawnMountedShadowRider(ServerLevel level, double x, double y, double z, float yRot, float xRot, MobSpawnType spawnType) {
      return spawnMountedShadowRider(level, null, null, BlockPos.containing(x, y, z), spawnType, false, false);
   }

   @Nullable
   public static Phantom spawnMountedShadowRider(
      ServerLevel level,
      @Nullable ItemStack itemStack,
      @Nullable Player player,
      BlockPos pos,
      MobSpawnType spawnType,
      boolean shouldOffsetY,
      boolean shouldOffsetYMore
   ) {
      Phantom phantom = (Phantom)EntityType.PHANTOM.spawn(level, itemStack, player, pos, spawnType, shouldOffsetY, shouldOffsetYMore);
      if (phantom == null) {
         return null;
      } else {
         markMount(phantom);
         ShadowEntity shadow = (ShadowEntity)((EntityType)ModEntities.SHADOW.get())
            .spawn(level, null, player, pos, spawnType, shouldOffsetY, shouldOffsetYMore);
         if (shadow == null) {
            phantom.discard();
            return null;
         } else {
            shadow.moveTo(phantom.getX(), phantom.getY(), phantom.getZ(), phantom.getYRot(), shadow.getXRot());
            if (!shadow.startRiding(phantom, true)) {
               shadow.discard();
               phantom.discard();
               return null;
            } else {
               return phantom;
            }
         }
      }
   }

   public static void tickPhantomMount(Phantom phantom) {
      if (isShadowRiderMount(phantom)) {
         tickAttackCooldown(phantom);
         LivingEntity target = resolvePreferredTarget(phantom);
         if (target == null) {
            clearAttackState(phantom);
         } else {
            if (phantom.getTarget() != target) {
               phantom.setTarget(target);
            }

            int state = PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_target_state");
            if (!PersistentEntityData.get(phantom).contains("villagerpatrols_shadow_rider_state_ticks")) {
               beginApproach(phantom, target);
               state = 0;
            }

            switch (state) {
               case 1:
                  tickDive(phantom, target);
                  break;
               case 2:
                  tickEscape(phantom, target);
                  break;
               default:
                  tickApproach(phantom, target);
            }
         }
      }
   }

   public static void alertMountTarget(Entity entity, @Nullable LivingEntity attacker) {
      if (isValidPhantomTarget(attacker)) {
         if (entity instanceof Phantom phantom && isShadowRiderMount(phantom)) {
            phantom.setTarget(attacker);
            beginDive(phantom, attacker);
         } else {
            if (entity instanceof ShadowEntity shadow && shadow.getVehicle() instanceof Phantom phantom && isShadowRiderMount(phantom)) {
               phantom.setTarget(attacker);
               beginDive(phantom, attacker);
            }
         }
      }
   }

   public static void tickMountedShadow(ShadowEntity shadow) {
      if (shadow.getVehicle() instanceof Phantom phantom && isShadowRiderMount(phantom)) {
         float bodyYaw = phantom.yBodyRot;
         shadow.setYRot(bodyYaw);
         shadow.yRotO = bodyYaw;
         shadow.setYBodyRot(bodyYaw);
         shadow.yBodyRotO = bodyYaw;
      }
   }

   @Nullable
   private static LivingEntity resolvePreferredTarget(Phantom phantom) {
      LivingEntity currentTarget = phantom.getTarget();
      if (isValidPhantomTarget(currentTarget)) {
         return currentTarget;
      }

      if (phantom.getFirstPassenger() instanceof ShadowEntity shadow) {
         LivingEntity shadowTarget = shadow.getTarget();
         if (isValidPhantomTarget(shadowTarget)) {
            return shadowTarget;
         }

         LivingEntity shadowAttacker = shadow.getLastHurtByMob();
         if (isValidPhantomTarget(shadowAttacker)) {
            return shadowAttacker;
         }
      }

      LivingEntity phantomAttacker = phantom.getLastHurtByMob();
      return isValidPhantomTarget(phantomAttacker) ? phantomAttacker : null;
   }

   private static boolean isValidPhantomTarget(@Nullable LivingEntity target) {
      return target != null && target.isAlive() ? !(target instanceof Player player && (player.isCreative() || player.isSpectator())) : false;
   }

   private static void tickAttackCooldown(Phantom phantom) {
      int cooldown = PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_attack_cooldown");
      if (cooldown > 0) {
         PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_attack_cooldown", cooldown - 1);
      }
   }

   private static void tickApproach(Phantom phantom, LivingEntity target) {
      int ticks = PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_state_ticks") - 1;
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", ticks);
      float angle = PersistentEntityData.get(phantom).getFloat("villagerpatrols_shadow_rider_orbit_angle") + 8.0F;
      PersistentEntityData.get(phantom).putFloat("villagerpatrols_shadow_rider_orbit_angle", angle);
      double radians = Math.toRadians(angle);
      Vec3 goal = new Vec3(target.getX() + Math.cos(radians) * 10.0, target.getY() + 7.0, target.getZ() + Math.sin(radians) * 10.0);
      steerToward(phantom, goal, 0.45, 0.18);
      if (ticks <= 0) {
         beginDive(phantom, target);
      }
   }

   private static void tickDive(Phantom phantom, LivingEntity target) {
      int ticks = PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_state_ticks") - 1;
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", ticks);
      steerToward(phantom, readGoal(phantom), 0.95, 0.28);
      if (tryAttack(phantom, target) || ticks <= 0 || phantom.distanceToSqr(target) < 4.0) {
         beginEscape(phantom, target);
      }
   }

   private static void tickEscape(Phantom phantom, LivingEntity target) {
      int ticks = PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_state_ticks") - 1;
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", ticks);
      steerToward(phantom, readGoal(phantom), 0.7, 0.16);
      if (ticks <= 0) {
         beginApproach(phantom, target);
      }
   }

   private static void beginApproach(Phantom phantom, LivingEntity target) {
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_target_state", 0);
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", 25 + phantom.getRandom().nextInt(16));
      if (!PersistentEntityData.get(phantom).contains("villagerpatrols_shadow_rider_orbit_angle")) {
         Vec3 offset = phantom.position().subtract(target.position());
         float angle = (float)Math.toDegrees(Math.atan2(offset.z, offset.x));
         PersistentEntityData.get(phantom).putFloat("villagerpatrols_shadow_rider_orbit_angle", angle);
      }
   }

   private static void beginDive(Phantom phantom, LivingEntity target) {
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_target_state", 1);
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", 22);
      Vec3 lead = target.getDeltaMovement().scale(2.0);
      storeGoal(phantom, new Vec3(target.getX() + lead.x, target.getY() + Math.min(target.getBbHeight() * 0.55, 0.9), target.getZ() + lead.z));
   }

   private static void beginEscape(Phantom phantom, LivingEntity target) {
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_target_state", 2);
      PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_state_ticks", 55);
      Vec3 away = phantom.position().subtract(target.position());
      away = new Vec3(away.x, 0.0, away.z);
      if (away.lengthSqr() < 1.0E-4) {
         away = phantom.getViewVector(1.0F);
         away = new Vec3(away.x, 0.0, away.z);
      }

      if (away.lengthSqr() < 1.0E-4) {
         away = new Vec3(1.0, 0.0, 0.0);
      }

      storeGoal(phantom, phantom.position().add(away.normalize().scale(16.0)).add(0.0, 10.0, 0.0));
   }

   private static boolean tryAttack(Phantom phantom, LivingEntity target) {
      if (PersistentEntityData.get(phantom).getInt("villagerpatrols_shadow_rider_attack_cooldown") > 0) {
         return false;
      } else {
         double attackRange = phantom.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 1.0;
         if (phantom.distanceToSqr(target) > attackRange * attackRange) {
            return false;
         } else if (phantom.doHurtTarget(target)) {
            PersistentEntityData.get(phantom).putInt("villagerpatrols_shadow_rider_attack_cooldown", 20);
            return true;
         } else {
            return false;
         }
      }
   }

   private static void steerToward(Phantom phantom, Vec3 goal, double speed, double blend) {
      Vec3 delta = goal.subtract(phantom.position());
      if (!(delta.lengthSqr() < 1.0E-6)) {
         Vec3 desired = delta.normalize().scale(speed);
         Vec3 current = phantom.getDeltaMovement();
         Vec3 next = current.scale(1.0 - blend).add(desired.scale(blend));
         phantom.setDeltaMovement(next);
         phantom.hasImpulse = true;
         double horizontal = Math.sqrt(next.x * next.x + next.z * next.z);
         if (horizontal > 1.0E-5) {
            float yaw = (float)(Math.atan2(next.z, next.x) * 180.0 / Math.PI) - 90.0F;
            phantom.setYRot(yaw);
            phantom.yRotO = yaw;
            phantom.setYBodyRot(yaw);
            phantom.yBodyRotO = yaw;
         }

         float pitch = (float)(-(Math.atan2(next.y, Math.max(horizontal, 1.0E-5)) * 180.0 / Math.PI));
         phantom.setXRot(pitch);
         phantom.xRotO = pitch;
      }
   }

   private static void clearAttackState(Phantom phantom) {
      PersistentEntityData.get(phantom).remove("villagerpatrols_shadow_rider_target_state");
      PersistentEntityData.get(phantom).remove("villagerpatrols_shadow_rider_state_ticks");
      PersistentEntityData.get(phantom).remove("villagerpatrols_shadow_rider_goal_x");
      PersistentEntityData.get(phantom).remove("villagerpatrols_shadow_rider_goal_y");
      PersistentEntityData.get(phantom).remove("villagerpatrols_shadow_rider_goal_z");
      phantom.setTarget(null);
   }

   private static void storeGoal(Phantom phantom, Vec3 goal) {
      PersistentEntityData.get(phantom).putDouble("villagerpatrols_shadow_rider_goal_x", goal.x);
      PersistentEntityData.get(phantom).putDouble("villagerpatrols_shadow_rider_goal_y", goal.y);
      PersistentEntityData.get(phantom).putDouble("villagerpatrols_shadow_rider_goal_z", goal.z);
   }

   private static Vec3 readGoal(Phantom phantom) {
      return new Vec3(
         PersistentEntityData.get(phantom).getDouble("villagerpatrols_shadow_rider_goal_x"),
         PersistentEntityData.get(phantom).getDouble("villagerpatrols_shadow_rider_goal_y"),
         PersistentEntityData.get(phantom).getDouble("villagerpatrols_shadow_rider_goal_z")
      );
   }
}
