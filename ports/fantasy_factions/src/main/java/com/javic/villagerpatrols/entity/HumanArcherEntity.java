package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HumanArcherEntity extends HumanPillagerEntity implements RangedAttackMob {
   private static final String TAG_SLIMPATCH_REPLACED = "slimpatch_replaced";
   private static final ResourceLocation DEFAULT_LOOT_TABLE = new ResourceLocation("entities/pillager");

   public HumanArcherEntity(EntityType<? extends Pillager> type, Level level) {
      super(type, level);
      this.markSlimPatchReplacementHandled();
   }

   public static Builder createAttributes() {
      return HumanPillagerEntity.createAttributes();
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new HoldGroundAttackGoal(this, 10.0F));
      this.goalSelector.addGoal(3, new RangedBowAttackGoal(this, 1.0, 20, 15.0F));
      this.goalSelector.addGoal(4, new StayNearGroupGoal(this, 0.6, 24.0, 12.0));
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[]{Raider.class}).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, OrcEntity.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, KnightEntity.class, true));
      this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, KnightShieldEntity.class, true));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, KnightLeaderEntity.class, true));
      this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, VillageGuardEntity.class, true));
      this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, VillageGuardLeaderEntity.class, true));
      this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, ElfEntity.class, true));
      this.targetSelector.addGoal(11, new NearestAttackableTargetGoal(this, DwarfEntity.class, true));
   }

   protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
      this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      this.markSlimPatchReplacementHandled();
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
      this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
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
      arrow.shoot(d0, d1 + d3 * 0.2, d2, 1.6F, 6.0F);
      this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(arrow);
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
      return projectileWeapon == Items.BOW;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.markSlimPatchReplacementHandled();
   }

   @Override
   protected ResourceLocation getDefaultLootTable() {
      return DEFAULT_LOOT_TABLE;
   }

   private void markSlimPatchReplacementHandled() {
      PersistentEntityData.get(this).putBoolean("slimpatch_replaced", true);
   }
}
