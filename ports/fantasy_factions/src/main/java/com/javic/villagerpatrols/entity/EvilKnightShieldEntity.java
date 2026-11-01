package com.javic.villagerpatrols.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EvilKnightShieldEntity extends AbstractKnightEntity {
   public EvilKnightShieldEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 9;
   }

   public static Builder createAttributes() {
      return createSwordKnightAttributes();
   }

   @Override
   protected String getTextureFolder() {
      return "evil_knight";
   }

   @Override
   protected String getSoundFolder() {
      return "evil_knight_shield";
   }

   @Override
   protected boolean usesShield() {
      return true;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return false;
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
      return this.createEvilKnightMainHandItemStack();
   }
}
