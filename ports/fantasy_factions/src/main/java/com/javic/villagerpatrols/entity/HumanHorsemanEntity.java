package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HumanHorsemanEntity extends HumanPillagerEntity {
   private static final String TAG_SLIMPATCH_REPLACED = "slimpatch_replaced";
   private static final String TAG_HORSEMAN_MOUNT_CREATED = "villagerpatrols_horseman_mount_created";
   private static final String TAG_HORSE_UUID = "villagerpatrols_horseman_horse_uuid";
   private static final String TAG_RIDER_UUID = "villagerpatrols_horseman_rider_uuid";
   private static final ResourceLocation DEFAULT_LOOT_TABLE = new ResourceLocation("entities/vindicator");

   public HumanHorsemanEntity(EntityType<? extends Pillager> type, Level level) {
      super(type, level);
      this.markSlimPatchReplacementHandled();
   }

   public static Builder createAttributes() {
      return HumanSwordsmanEntity.createAttributes();
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new HoldGroundAttackGoal(this, 10.0F));
      this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
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
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextBoolean() ? Items.IRON_SWORD : Items.STONE_SWORD));
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
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.getRandom().nextBoolean() ? Items.IRON_SWORD : Items.STONE_SWORD));
      this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
      return groupData;
   }

   @Override
   protected void customServerAiStep() {
      super.customServerAiStep();
      this.ensureMountedHorse();
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
      return false;
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

   public void setupExternalMount(Horse horse) {
      PersistentEntityData.get(this).putBoolean("villagerpatrols_horseman_mount_created", true);
      PersistentEntityData.get(this).putUUID("villagerpatrols_horseman_horse_uuid", horse.getUUID());
      PersistentEntityData.get(horse).putUUID("villagerpatrols_horseman_rider_uuid", this.getUUID());
   }

   private void ensureMountedHorse() {
      if (this.level() instanceof ServerLevel serverLevel) {
         if (this.isPassenger()) {
            PersistentEntityData.get(this).putBoolean("villagerpatrols_horseman_mount_created", true);
         } else if (PersistentEntityData.get(this).hasUUID("villagerpatrols_horseman_horse_uuid")) {
            if (serverLevel.getEntity(PersistentEntityData.get(this).getUUID("villagerpatrols_horseman_horse_uuid")) instanceof Horse horse && horse.isAlive()) {
               this.startRiding(horse, true);
            }
         } else if (!PersistentEntityData.get(this).getBoolean("villagerpatrols_horseman_mount_created")) {
            Horse horse = (Horse)EntityType.HORSE.create(serverLevel);
            if (horse != null) {
               horse.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
               horse.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
               horse.setTamed(true);
               horse.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.SADDLE));
               horse.setPersistenceRequired();
               PersistentEntityData.get(horse).putUUID("villagerpatrols_horseman_rider_uuid", this.getUUID());
               if (serverLevel.addFreshEntity(horse)) {
                  PersistentEntityData.get(this).putBoolean("villagerpatrols_horseman_mount_created", true);
                  PersistentEntityData.get(this).putUUID("villagerpatrols_horseman_horse_uuid", horse.getUUID());
                  this.startRiding(horse, true);
               }
            }
         }
      }
   }

   private void markSlimPatchReplacementHandled() {
      PersistentEntityData.get(this).putBoolean("slimpatch_replaced", true);
   }
}
