package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class OrcEntity extends AbstractKnightEntity {
   private static final int SKINS = 5;

   public OrcEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 7;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 25.0)
         .add(Attributes.ATTACK_DAMAGE, 2.5)
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }

   public static boolean checkOrcSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95, false));
      this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Villager.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, KnightEntity.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, KnightShieldEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, KnightLeaderEntity.class, true));
      this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, VillageGuardEntity.class, true));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, VillageGuardLeaderEntity.class, true));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, EvilKnightEntity.class, true));
      this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, EvilKnightShieldEntity.class, true));
      this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, EvilKnightLeaderEntity.class, true));
      this.targetSelector.addGoal(11, new NearestAttackableTargetGoal(this, HumanPillagerEntity.class, true));
      this.targetSelector.addGoal(12, new NearestAttackableTargetGoal(this, HumanVindicatorEntity.class, true));
      this.targetSelector.addGoal(13, new NearestAttackableTargetGoal(this, HumanEvokerEntity.class, true));
      this.targetSelector.addGoal(14, new NearestAttackableTargetGoal(this, HumanWitchEntity.class, true));
      this.targetSelector.addGoal(15, new NearestAttackableTargetGoal(this, ElfEntity.class, true));
      this.targetSelector.addGoal(16, new NearestAttackableTargetGoal(this, DwarfEntity.class, true));
      this.targetSelector.addGoal(17, new NearestAttackableTargetGoal(this, Zombie.class, true));
      this.targetSelector.addGoal(18, new NearestAttackableTargetGoal(this, Skeleton.class, true));
      this.targetSelector.addGoal(19, new NearestAttackableTargetGoal(this, Spider.class, true));
      this.targetSelector.addGoal(20, new NearestAttackableTargetGoal(this, Creeper.class, true));
      this.targetSelector.addGoal(21, new NearestAttackableTargetGoal(this, Player.class, true));
      this.targetSelector.addGoal(22, new NearestAttackableTargetGoal(this, Animal.class, true));
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      CombatRetargeting.retargetFromPlayerIfNeeded(this, this::canRetargetFromPlayerTo);
      ItemStack mainHand = this.getItemBySlot(EquipmentSlot.MAINHAND);
      Item weapon = this.resolveWeapon(mainHand);
      if (weapon == null) {
         weapon = this.randomMainHandItem();
      }

      if (mainHand.isEmpty() || !mainHand.is(weapon)) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
      }

      ItemStack offHand = this.getItemBySlot(EquipmentSlot.OFFHAND);
      if (offHand.isEmpty() || !offHand.is(weapon)) {
         this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(weapon));
      }
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      Item weapon = this.resolveWeapon(this.getItemBySlot(EquipmentSlot.MAINHAND));
      if (weapon == null) {
         weapon = this.randomMainHandItem();
      }

      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
      this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(weapon));
      return groupData;
   }

   @Override
   public boolean isAlliedTo(Entity entity) {
      return entity instanceof OrcEntity ? true : super.isAlliedTo(entity);
   }

   public boolean canAttackType(EntityType<?> type) {
      return type == ModEntities.ORC.get() ? false : super.canAttackType(type);
   }

   private boolean canRetargetFromPlayerTo(Entity candidate) {
      return candidate instanceof Villager
         || candidate instanceof KnightEntity
         || candidate instanceof KnightShieldEntity
         || candidate instanceof KnightLeaderEntity
         || candidate instanceof VillageGuardEntity
         || candidate instanceof VillageGuardLeaderEntity
         || candidate instanceof EvilKnightEntity
         || candidate instanceof EvilKnightShieldEntity
         || candidate instanceof EvilKnightLeaderEntity
         || candidate instanceof HumanPillagerEntity
         || candidate instanceof HumanVindicatorEntity
         || candidate instanceof HumanEvokerEntity
         || candidate instanceof HumanWitchEntity
         || candidate instanceof ElfEntity
         || candidate instanceof DwarfEntity
         || candidate instanceof Zombie
         || candidate instanceof Skeleton
         || candidate instanceof Spider
         || candidate instanceof Creeper
         || candidate instanceof Animal;
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Mob> type = (EntityType<? extends Mob>) this.getType();
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnReason, this.blockPosition(), this.getRandom());
   }

   @Override
   protected String getTextureFolder() {
      return "orc";
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
      return 5;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 5;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return new ItemStack((ItemLike)ModItems.WAR_AXE.get());
   }

   private Item randomMainHandItem() {
      return this.getRandom().nextFloat() < 0.7F ? (Item)ModItems.WAR_AXE.get() : (Item)ModItems.HEAVY_WAR_AXE.get();
   }

   private Item resolveWeapon(ItemStack stack) {
      Item item = stack.getItem();
      return item != ModItems.WAR_AXE.get() && item != ModItems.HEAVY_WAR_AXE.get() ? null : item;
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return true;
   }

   @Override
   protected float getBiasedFemaleChance() {
      return 0.3F;
   }
}
