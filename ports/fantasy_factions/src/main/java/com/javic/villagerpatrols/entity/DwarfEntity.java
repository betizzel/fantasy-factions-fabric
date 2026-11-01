package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class DwarfEntity extends AbstractCivilizedHumanoidEntity {
   private static final int SKINS = 12;

   public DwarfEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 5;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 35.0)
         .add(Attributes.ATTACK_DAMAGE, 2.0)
         .add(Attributes.MOVEMENT_SPEED, 0.24)
         .add(Attributes.FOLLOW_RANGE, 22.0);
   }

   public static boolean checkDwarfSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.randomMainHandItem()));
      this.setItemSlot(EquipmentSlot.OFFHAND, this.getRandom().nextFloat() < 0.55F ? new ItemStack(this.randomOffHandItem()) : ItemStack.EMPTY);
      return groupData;
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Mob> type = (EntityType<? extends Mob>) this.getType();
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnReason, this.blockPosition(), this.getRandom());
   }

   private Item randomMainHandItem() {
      float roll = this.getRandom().nextFloat();
      if (roll < 0.2F) {
         return (Item)ModItems.DWARF_AXE.get();
      } else if (roll < 0.45F) {
         return Items.STONE_PICKAXE;
      } else {
         return roll < 0.65F ? Items.IRON_PICKAXE : (Item)ModItems.DWARF_AXE.get();
      }
   }

   private Item randomOffHandItem() {
      return this.getRandom().nextBoolean() ? Items.STONE_PICKAXE : Items.IRON_PICKAXE;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      InteractionResult result = FantasyVillagerSupport.tryConvertDwarf(this, player, hand);
      return result.consumesAction() ? result : super.mobInteract(player, hand);
   }

   @Override
   protected String getTextureFolder() {
      return "dwarf";
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected int getMaleSkinCount() {
      return 12;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 12;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return new ItemStack((ItemLike)ModItems.DWARF_AXE.get());
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return false;
   }
}
