package com.javic.villagerpatrols.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WizardStaffItem extends SwordItem {
   private static final int FIRE_TICKS = 40;
   private static final int COOLDOWN_TICKS = 60;

   public WizardStaffItem(Tier tier, Properties properties) {
      super(tier, 3, -2.4F, properties);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!target.level().isClientSide) {
         target.setRemainingFireTicks(40);
      }

      return super.hurtEnemy(stack, target, attacker);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide) {
         Vec3 look = player.getLookAngle().normalize();
         SmallFireball fireball = new SmallFireball(level, player, look.x, look.y, look.z);
         Vec3 pos = player.getEyePosition().add(look.scale(0.5));
         fireball.setPos(pos.x, pos.y, pos.z);
         level.addFreshEntity(fireball);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
         player.getCooldowns().addCooldown(this, 60);
         stack.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

      return InteractionResultHolder.consume(stack);
   }
}
