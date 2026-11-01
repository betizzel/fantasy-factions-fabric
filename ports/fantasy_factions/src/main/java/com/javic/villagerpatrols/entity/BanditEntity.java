package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class BanditEntity extends AbstractKnightEntity {
   private static final int SKINS = 16;
   private static final float DAY_BANDIT_SPAWN_CHANCE = 0.25F;

   public BanditEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 5;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 22.0)
         .add(Attributes.ATTACK_DAMAGE, 1.5)
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }

   public static boolean checkBanditSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      if (level.getDifficulty() == Difficulty.PEACEFUL || !Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
         return false;
      } else {
         return level.getLevel().isDay() && level.canSeeSky(pos) ? random.nextFloat() < 0.25F : true;
      }
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95, false));
      this.goalSelector.addGoal(4, new StayNearGroupGoal(this, 0.6, 24.0, 12.0));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, MaleVillagerEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, FemaleVillagerEntity.class, true));
      this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, KnightEntity.class, true));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, KnightShieldEntity.class, true));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, KnightLeaderEntity.class, true));
      this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, VillageGuardEntity.class, true));
      this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, VillageGuardLeaderEntity.class, true));
      this.targetSelector.addGoal(11, new NearestAttackableTargetGoal(this, OrcEntity.class, true));
      this.targetSelector.addGoal(12, new NearestAttackableTargetGoal(this, ElfEntity.class, true));
      this.targetSelector.addGoal(13, new NearestAttackableTargetGoal(this, DwarfEntity.class, true));
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.randomMainHandItem()));
      this.setItemSlot(EquipmentSlot.OFFHAND, this.getRandom().nextFloat() < 0.35F ? new ItemStack(this.randomMainHandItem()) : ItemStack.EMPTY);
      return groupData;
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Mob> type = (EntityType<? extends Mob>) this.getType();
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnReason, this.blockPosition(), this.getRandom());
   }

   private Item randomMainHandItem() {
      float roll = this.getRandom().nextFloat();
      if (roll < 0.6F) {
         return (Item)ModItems.BANDIT_DAGGER.get();
      } else {
         roll = (roll - 0.6F) / 0.4F;
         if (roll < 0.08F) {
            return Items.DIAMOND_SWORD;
         } else if (roll < 0.3F) {
            return Items.IRON_SWORD;
         } else {
            return roll < 0.6F ? Items.GOLDEN_SWORD : Items.STONE_SWORD;
         }
      }
   }

   @Override
   protected boolean canRetargetFromPlayerTo(LivingEntity candidate) {
      return candidate instanceof MaleVillagerEntity
         || candidate instanceof FemaleVillagerEntity
         || candidate instanceof Villager
         || candidate instanceof KnightEntity
         || candidate instanceof KnightShieldEntity
         || candidate instanceof KnightLeaderEntity
         || candidate instanceof VillageGuardEntity
         || candidate instanceof VillageGuardLeaderEntity
         || candidate instanceof OrcEntity
         || candidate instanceof ElfEntity
         || candidate instanceof DwarfEntity;
   }

   @Override
   public boolean isAlliedTo(Entity entity) {
      return !(entity instanceof BanditEntity)
            && !(entity instanceof EvilWizardEntity)
            && !(entity instanceof EvilKnightEntity)
            && !(entity instanceof EvilKnightShieldEntity)
            && !(entity instanceof EvilKnightLeaderEntity)
         ? super.isAlliedTo(entity)
         : true;
   }

   @Override
   protected String getTextureFolder() {
      return "bandit";
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return false;
   }

   @Override
   protected int getMaleSkinCount() {
      return 16;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 16;
   }

   @Override
   protected Item getDefaultMainHandItem() {
      return (Item)ModItems.BANDIT_DAGGER.get();
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return false;
   }
}
