package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class KnightLeaderEntity extends AbstractKnightEntity {
   public KnightLeaderEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 15;
   }

   public static Builder createAttributes() {
      return createLeaderAttributes();
   }

   @Override
   protected String getTextureFolder() {
      return "knight_leader";
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return true;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return new ItemStack((ItemLike)ModItems.KNIGHT_LEADER_SWORD.get());
   }
}
