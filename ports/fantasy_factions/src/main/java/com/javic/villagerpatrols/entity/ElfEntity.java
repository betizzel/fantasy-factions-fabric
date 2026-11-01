package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.entity.goal.ConditionalMeleeAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class ElfEntity extends AbstractCivilizedHumanoidEntity implements RangedAttackMob {
   private static final int SKINS = 12;
   private static final float BOW_CHANCE = 0.6F;
   private static final int BOW_ATTACK_INTERVAL = 14;

   public ElfEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 5;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }

   public static boolean checkElfSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new RangedBowAttackGoal(this, 1.0, 14, 15.0F));
      this.goalSelector.addGoal(3, new ConditionalMeleeAttackGoal(this, 0.95, false, () -> !this.isHoldingBow()));
      this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true, this::isDefensiveTarget));
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      if (this.getRandom().nextFloat() < 0.6F) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
      } else {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)ModItems.ELVEN_SWORD.get()));
      }

      this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      return groupData;
   }

   public void performRangedAttack(LivingEntity target, float distanceFactor) {
      ItemStack weapon = this.getMainHandItem().getItem() instanceof BowItem ? this.getMainHandItem() : this.getOffhandItem();
      ItemStack projectile = this.getProjectile(weapon);
      AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, distanceFactor);

      double d0 = target.getX() - this.getX();
      double d1 = target.getY(0.3333333333333333) - arrow.getY();
      double d2 = target.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      arrow.shoot(d0, d1 + d3 * 0.2, d2, 1.8F, 6.0F);
      this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(arrow);
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
      return projectileWeapon == Items.BOW;
   }

   private boolean isHoldingBow() {
      return this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BowItem;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      InteractionResult result = FantasyVillagerSupport.tryConvertElf(this, player, hand);
      return result.consumesAction() ? result : super.mobInteract(player, hand);
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Mob> type = (EntityType<? extends Mob>) this.getType();
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnReason, this.blockPosition(), this.getRandom());
   }

   @Override
   protected String getTextureFolder() {
      return "elf";
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
      return new ItemStack((ItemLike)ModItems.ELVEN_SWORD.get());
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return false;
   }
}
