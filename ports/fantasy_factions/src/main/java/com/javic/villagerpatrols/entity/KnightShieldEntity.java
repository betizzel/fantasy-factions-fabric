package com.javic.villagerpatrols.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KnightShieldEntity extends AbstractKnightEntity {
   public KnightShieldEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 9;
   }

   public static Builder createAttributes() {
      return createSwordKnightAttributes();
   }

   @Override
   protected String getTextureFolder() {
      return "knight";
   }

   @Override
   protected String getSoundFolder() {
      return "knight_shield";
   }

   @Override
   protected boolean usesShield() {
      return true;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return true;
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return true;
   }

   @Override
   protected float getBiasedFemaleChance() {
      return 0.1F;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return this.createFriendlyKnightMainHandItemStack();
   }
}
