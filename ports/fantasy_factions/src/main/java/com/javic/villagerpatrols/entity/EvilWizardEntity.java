package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModItems;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EvilWizardEntity extends Witch implements GenderedTextureEntity {
   private static final int MALE_SKINS = 10;
   private static final int FEMALE_SKINS = 10;
   private static final String TAG_INITIALIZED = "villagerpatrols_initialized";
   private static final String TAG_GENDER = "villagerpatrols_gender";
   private static final String TAG_SKIN = "villagerpatrols_skin";
   private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(EvilWizardEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> DATA_GENDER = SynchedEntityData.defineId(EvilWizardEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> DATA_CASTING_FANGS = SynchedEntityData.defineId(EvilWizardEntity.class, EntityDataSerializers.BOOLEAN);

   public EvilWizardEntity(EntityType<? extends Witch> type, Level level) {
      super(type, level);
      this.xpReward = 10;
   }

   public static Builder createAttributes() {
      return Witch.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.FOLLOW_RANGE, 28.0);
   }

   public static boolean checkEvilWizardSpawnRules(
      EntityType<? extends Witch> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new EvilWizardEntity.FangAttackGoal());
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
      this.targetSelector.addGoal(14, new NearestAttackableTargetGoal(this, MaleWizardEntity.class, true));
      this.targetSelector.addGoal(15, new NearestAttackableTargetGoal(this, FemaleWizardEntity.class, true));
   }

   protected void customServerAiStep() {
      CombatRetargeting.retargetFromPlayerIfNeeded(this, this::canRetargetFromPlayerTo);
      MobEffectInstance current = this.getEffect(MobEffects.FIRE_RESISTANCE);
      if (current == null || current.getAmplifier() != 0 || current.getDuration() <= 20) {
         this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, false), this);
      }

      super.customServerAiStep();
   }

   public boolean isCastingFangs() {
      return (Boolean)this.entityData.get(DATA_CASTING_FANGS);
   }

   private void setCastingFangs(boolean castingFangs) {
      this.entityData.set(DATA_CASTING_FANGS, castingFangs);
   }

   private boolean canRetargetFromPlayerTo(LivingEntity candidate) {
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
         || candidate instanceof DwarfEntity
         || candidate instanceof MaleWizardEntity
         || candidate instanceof FemaleWizardEntity;
   }

   public boolean isAlliedTo(Entity entity) {
      return !(entity instanceof EvilWizardEntity)
            && !(entity instanceof BanditEntity)
            && !(entity instanceof EvilKnightEntity)
            && !(entity instanceof EvilKnightShieldEntity)
            && !(entity instanceof EvilKnightLeaderEntity)
         ? super.isAlliedTo(entity)
         : true;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SKIN, 1);
      this.entityData.define(DATA_GENDER, "male");
      this.entityData.define(DATA_CASTING_FANGS, false);
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_initialized")) {
         boolean female = this.getRandom().nextBoolean();
         this.setGender(female ? "female" : "male");
         this.setSkinIndex(this.getRandom().nextInt(female ? 10 : 10) + 1);
         data.putBoolean("villagerpatrols_initialized", true);
      } else {
         if (data.contains("villagerpatrols_gender")) {
            this.setGender(data.getString("villagerpatrols_gender"));
         }

         if (data.contains("villagerpatrols_skin")) {
            this.setSkinIndex(data.getInt("villagerpatrols_skin"));
         }
      }

      this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack((ItemLike)ModItems.WIZARD_STAFF.get()));
      this.setCustomName(null);
      this.setCustomNameVisible(false);
      return groupData;
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Mob> type = (EntityType<? extends Mob>) this.getType();
      return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, spawnReason, this.blockPosition(), this.getRandom());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("villagerpatrols_gender")) {
         this.setGender(tag.getString("villagerpatrols_gender"));
      }

      if (tag.contains("villagerpatrols_skin")) {
         this.setSkinIndex(tag.getInt("villagerpatrols_skin"));
      }

      if (tag.contains("villagerpatrols_initialized")) {
         PersistentEntityData.get(this).putBoolean("villagerpatrols_initialized", tag.getBoolean("villagerpatrols_initialized"));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("villagerpatrols_gender", this.getGender());
      tag.putInt("villagerpatrols_skin", this.getSkinIndex());
      tag.putBoolean("villagerpatrols_initialized", PersistentEntityData.get(this).getBoolean("villagerpatrols_initialized"));
   }

   public boolean shouldShowName() {
      return false;
   }

   public boolean isCustomNameVisible() {
      return false;
   }

   public Component getName() {
      return this.getType().getDescription();
   }

   @Override
   public boolean isFemale() {
      return "female".equalsIgnoreCase((String)this.entityData.get(DATA_GENDER));
   }

   public void setGender(String gender) {
      String normalized = "female".equalsIgnoreCase(gender) ? "female" : "male";
      this.entityData.set(DATA_GENDER, normalized);
      PersistentEntityData.get(this).putString("villagerpatrols_gender", normalized);
   }

   public String getGender() {
      return (String)this.entityData.get(DATA_GENDER);
   }

   public void setSkinIndex(int index) {
      int max = this.isFemale() ? 10 : 10;
      int resolved = Math.max(1, Math.min(index, max));
      this.entityData.set(DATA_SKIN, resolved);
      PersistentEntityData.get(this).putInt("villagerpatrols_skin", resolved);
   }

   public int getSkinIndex() {
      return (Integer)this.entityData.get(DATA_SKIN);
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return new ResourceLocation("villagerpatrols", "textures/entity/evil_wizard/" + this.getGender() + "/skin_" + String.format("%02d", this.getSkinIndex()) + ".png");
   }

   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("evil_wizard", this.isFemale());
   }

   public int getAmbientSoundInterval() {
      return ModEntitySounds.ambientInterval(this);
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("evil_wizard", this.isFemale());
   }

   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("evil_wizard", this.isFemale());
   }

   private final class FangAttackGoal extends Goal {
      private int attackWarmupDelay;
      private int nextAttackTickCount;

      private FangAttackGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity target = EvilWizardEntity.this.getTarget();
         return target != null
            && target.isAlive()
            && !EvilWizardEntity.this.isDrinkingPotion()
            && EvilWizardEntity.this.getMainHandItem().getItem() != Items.POTION
            && !EvilWizardEntity.this.isCastingFangs()
            && EvilWizardEntity.this.tickCount >= this.nextAttackTickCount;
      }

      public boolean canContinueToUse() {
         LivingEntity target = EvilWizardEntity.this.getTarget();
         return target != null && target.isAlive() && this.attackWarmupDelay > 0;
      }

      public void start() {
         this.attackWarmupDelay = 20;
         this.nextAttackTickCount = EvilWizardEntity.this.tickCount + 90;
         EvilWizardEntity.this.setCastingFangs(true);
      }

      public void stop() {
         EvilWizardEntity.this.setCastingFangs(false);
      }

      public void tick() {
         LivingEntity target = EvilWizardEntity.this.getTarget();
         if (target == null) {
            EvilWizardEntity.this.setCastingFangs(false);
         } else {
            EvilWizardEntity.this.getNavigation().stop();
            EvilWizardEntity.this.getLookControl().setLookAt(target, EvilWizardEntity.this.getMaxHeadYRot(), EvilWizardEntity.this.getMaxHeadXRot());
            this.attackWarmupDelay--;
            if (this.attackWarmupDelay == 0 && EvilWizardEntity.this.canAttack(target)) {
               this.performSpellCasting(target);
               EvilWizardEntity.this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.0F);
               EvilWizardEntity.this.setCastingFangs(false);
            }
         }
      }

      private void performSpellCasting(LivingEntity target) {
         double minY = Math.min(target.getY(), EvilWizardEntity.this.getY());
         double maxY = Math.max(target.getY(), EvilWizardEntity.this.getY()) + 1.0;
         float angle = (float)Mth.atan2(target.getZ() - EvilWizardEntity.this.getZ(), target.getX() - EvilWizardEntity.this.getX());
         if (EvilWizardEntity.this.distanceToSqr(target) < 9.0) {
            for (int i = 0; i < 5; i++) {
               float ringAngle = angle + i * (float) Math.PI * 0.4F;
               this.createSpellEntity(
                  EvilWizardEntity.this.getX() + Mth.cos(ringAngle) * 1.5, EvilWizardEntity.this.getZ() + Mth.sin(ringAngle) * 1.5, minY, maxY, ringAngle, 0
               );
            }

            for (int i = 0; i < 8; i++) {
               float ringAngle = angle + i * (float) Math.PI * 2.0F / 8.0F + (float) (Math.PI * 2.0 / 5.0);
               this.createSpellEntity(
                  EvilWizardEntity.this.getX() + Mth.cos(ringAngle) * 2.5, EvilWizardEntity.this.getZ() + Mth.sin(ringAngle) * 2.5, minY, maxY, ringAngle, 3
               );
            }
         } else {
            for (int i = 0; i < 16; i++) {
               double distance = 1.25 * (i + 1);
               this.createSpellEntity(
                  EvilWizardEntity.this.getX() + Mth.cos(angle) * distance, EvilWizardEntity.this.getZ() + Mth.sin(angle) * distance, minY, maxY, angle, i
               );
            }
         }
      }

      private void createSpellEntity(double x, double z, double minY, double maxY, float yRot, int warmupDelay) {
         BlockPos blockPos = BlockPos.containing(x, maxY, z);
         boolean placed = false;
         double collisionOffset = 0.0;

         do {
            BlockPos belowPos = blockPos.below();
            BlockState belowState = EvilWizardEntity.this.level().getBlockState(belowPos);
            if (belowState.isFaceSturdy(EvilWizardEntity.this.level(), belowPos, Direction.UP)) {
               if (!EvilWizardEntity.this.level().isEmptyBlock(blockPos)) {
                  BlockState state = EvilWizardEntity.this.level().getBlockState(blockPos);
                  VoxelShape shape = state.getCollisionShape(EvilWizardEntity.this.level(), blockPos);
                  if (!shape.isEmpty()) {
                     collisionOffset = shape.max(Axis.Y);
                  }
               }

               placed = true;
               break;
            }

            blockPos = blockPos.below();
         } while (blockPos.getY() >= Mth.floor(minY) - 1);

         if (placed && EvilWizardEntity.this.level() instanceof ServerLevel serverLevel) {
            double y = blockPos.getY() + collisionOffset;
            serverLevel.addFreshEntity(new EvokerFangs(serverLevel, x, y, z, yRot, warmupDelay, EvilWizardEntity.this));
            serverLevel.gameEvent(EvilWizardEntity.this, GameEvent.ENTITY_PLACE, BlockPos.containing(x, y, z));
         }
      }
   }
}
