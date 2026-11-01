package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.entity.goal.StayNearGroupGoal;
import com.javic.villagerpatrols.spawn.SpawnSurfaceValidator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public abstract class AbstractKnightEntity extends Monster implements GenderedTextureEntity {
   private static final int DEFAULT_MALE_SKINS = 10;
   private static final int DEFAULT_FEMALE_SKINS = 10;
   private static final String TAG_INITIALIZED = "villagerpatrols_initialized";
   private static final String TAG_GENDER = "villagerpatrols_gender";
   private static final String TAG_SKIN = "villagerpatrols_skin";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_GROUP_SPAWNED_COMPANIONS = "villagerpatrols_group_spawned_companions";
   private static final String PRISONER_EVENT_GROUP_TYPE = "evil_knight_prisoners_world_event";
   private static final String PRISONER_ROLE = "evil_knight_prisoner_prisoner";
   private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(AbstractKnightEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> DATA_GENDER = SynchedEntityData.defineId(AbstractKnightEntity.class, EntityDataSerializers.STRING);

   protected AbstractKnightEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
   }

   public static Builder createLeaderAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 100.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.MOVEMENT_SPEED, 0.31)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   public static Builder createSwordKnightAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.ATTACK_DAMAGE, 1.5)
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SKIN, 1);
      this.entityData.define(DATA_GENDER, "male");
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
      int max = this.isFemale() ? this.getFemaleSkinCount() : this.getMaleSkinCount();
      int resolved = Math.max(1, Math.min(index, max));
      this.entityData.set(DATA_SKIN, resolved);
      PersistentEntityData.get(this).putInt("villagerpatrols_skin", resolved);
   }

   public int getSkinIndex() {
      return (Integer)this.entityData.get(DATA_SKIN);
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return new ResourceLocation("villagerpatrols", "textures/entity/" + this.getTextureFolder() + "/" + this.getGender() + "/skin_" + String.format("%02d", this.getSkinIndex()) + ".png");
   }

   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient(this.getSoundFolder(), this.isFemale());
   }

   public int getAmbientSoundInterval() {
      return ModEntitySounds.ambientInterval(this);
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt(this.getSoundFolder(), this.isFemale());
   }

   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death(this.getSoundFolder(), this.isFemale());
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      if (reason == MobSpawnType.SPAWN_EGG) {
         this.setPersistenceRequired();
      }

      CompoundTag data = PersistentEntityData.get(this);
      if (!data.getBoolean("villagerpatrols_initialized")) {
         boolean female = this.useBiasedGenderRoll() ? this.getRandom().nextFloat() < this.getBiasedFemaleChance() : this.getRandom().nextBoolean();
         this.setGender(female ? "female" : "male");
         this.setSkinIndex(this.getRandom().nextInt(female ? this.getFemaleSkinCount() : this.getMaleSkinCount()) + 1);
         this.equipDefaultItems();
         data.putBoolean("villagerpatrols_initialized", true);
      } else {
         if (data.contains("villagerpatrols_gender")) {
            this.setGender(data.getString("villagerpatrols_gender"));
         }

         if (data.contains("villagerpatrols_skin")) {
            this.setSkinIndex(data.getInt("villagerpatrols_skin"));
         }
      }

      this.clearLeaderBanner();
      this.setCustomName(null);
      this.setCustomNameVisible(false);
      return groupData;
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

      if (tag.contains("villagerpatrols_group_id")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_id", tag.getString("villagerpatrols_group_id"));
      }

      if (tag.contains("villagerpatrols_group_type")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_type", tag.getString("villagerpatrols_group_type"));
      }

      if (tag.contains("villagerpatrols_group_role")) {
         PersistentEntityData.get(this).putString("villagerpatrols_group_role", tag.getString("villagerpatrols_group_role"));
      }

      if (tag.contains("villagerpatrols_group_spawned_companions")) {
         PersistentEntityData.get(this).putBoolean("villagerpatrols_group_spawned_companions", tag.getBoolean("villagerpatrols_group_spawned_companions"));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("villagerpatrols_gender", this.getGender());
      tag.putInt("villagerpatrols_skin", this.getSkinIndex());
      tag.putBoolean("villagerpatrols_initialized", PersistentEntityData.get(this).getBoolean("villagerpatrols_initialized"));
      if (PersistentEntityData.get(this).contains("villagerpatrols_group_id")) {
         tag.putString("villagerpatrols_group_id", PersistentEntityData.get(this).getString("villagerpatrols_group_id"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_type")) {
         tag.putString("villagerpatrols_group_type", PersistentEntityData.get(this).getString("villagerpatrols_group_type"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_role")) {
         tag.putString("villagerpatrols_group_role", PersistentEntityData.get(this).getString("villagerpatrols_group_role"));
      }

      if (PersistentEntityData.get(this).contains("villagerpatrols_group_spawned_companions")) {
         tag.putBoolean("villagerpatrols_group_spawned_companions", PersistentEntityData.get(this).getBoolean("villagerpatrols_group_spawned_companions"));
      }
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95, false));
      this.goalSelector.addGoal(4, new StayNearGroupGoal(this, 0.6, 24.0, 12.0));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      if (this.isFriendlyKnight()) {
         this.targetSelector
            .addGoal(
               2,
               new NearestAttackableTargetGoal(
                  this,
                  Monster.class,
                  true,
                  target -> !(target instanceof Creeper) && !(target instanceof AbstractKnightEntity knight && knight.isFriendlyKnight())
               )
            );
      } else {
         this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, KnightEntity.class, true));
         this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, KnightShieldEntity.class, true));
         this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, KnightLeaderEntity.class, true));
         this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, HumanPillagerEntity.class, true));
         this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, HumanVindicatorEntity.class, true));
         this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, HumanEvokerEntity.class, true));
         this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, OrcEntity.class, true));
         this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, ElfEntity.class, true));
         this.targetSelector.addGoal(10, new NearestAttackableTargetGoal(this, DwarfEntity.class, true));
         this.targetSelector.addGoal(11, new NearestAttackableTargetGoal(this, Player.class, true));
         this.targetSelector.addGoal(12, new NearestAttackableTargetGoal(this, Villager.class, true));
      }
   }

   private void equipDefaultItems() {
      this.setItemSlot(EquipmentSlot.MAINHAND, this.createDefaultMainHandItemStack());
      if (this.usesShield()) {
         this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
      } else {
         this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      }
   }

   protected void customServerAiStep() {
      this.clearLeaderBanner();
      if (!(this instanceof OrcEntity) && !this.isFriendlyKnight()) {
         CombatRetargeting.retargetFromPlayerIfNeeded(this, this::canRetargetFromPlayerTo);
      }

      super.customServerAiStep();
   }

   protected boolean canRetargetFromPlayerTo(LivingEntity candidate) {
      if (candidate instanceof KnightEntity || candidate instanceof KnightShieldEntity || candidate instanceof KnightLeaderEntity) {
         return true;
      } else if (candidate instanceof HumanPillagerEntity) {
         return true;
      } else if (candidate instanceof HumanVindicatorEntity || candidate instanceof HumanEvokerEntity) {
         return true;
      } else if (candidate instanceof OrcEntity) {
         return true;
      } else if (candidate instanceof ElfEntity || candidate instanceof DwarfEntity) {
         return true;
      } else {
         return !(candidate instanceof MaleVillagerEntity) && !(candidate instanceof FemaleVillagerEntity) && !(candidate instanceof Villager)
            ? false
            : !this.isProtectedPrisonerCandidate(candidate);
      }
   }

   private boolean isProtectedPrisonerCandidate(LivingEntity candidate) {
      if (!(candidate instanceof MaleVillagerEntity) && !(candidate instanceof FemaleVillagerEntity)) {
         return false;
      } else if (!"evil_knight_prisoners_world_event".equals(PersistentEntityData.get(this).getString("villagerpatrols_group_type"))) {
         return false;
      } else if (!"evil_knight_prisoners_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))) {
         return false;
      } else {
         return !"evil_knight_prisoner_prisoner".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
            ? false
            : PersistentEntityData.get(this).getString("villagerpatrols_group_id").equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"));
      }
   }

   private void clearLeaderBanner() {
      ItemStack head = this.getItemBySlot(EquipmentSlot.HEAD);
      if (!head.isEmpty() && head.getItem() instanceof BannerItem) {
         this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
         this.setDropChance(EquipmentSlot.HEAD, 0.0F);
      }
   }

   public void setGroupData(UUID groupId, String groupType, String groupRole) {
      this.setGroupData(groupId.toString(), groupType, groupRole);
   }

   public void setGroupData(String groupId, String groupType, String groupRole) {
      CompoundTag data = PersistentEntityData.get(this);
      data.putString("villagerpatrols_group_id", groupId);
      data.putString("villagerpatrols_group_type", groupType);
      data.putString("villagerpatrols_group_role", groupRole);
   }

   protected boolean hasGroupId() {
      return PersistentEntityData.get(this).contains("villagerpatrols_group_id");
   }

   protected boolean hasSpawnedCompanions() {
      return PersistentEntityData.get(this).getBoolean("villagerpatrols_group_spawned_companions");
   }

   public void setSpawnedCompanions(boolean value) {
      PersistentEntityData.get(this).putBoolean("villagerpatrols_group_spawned_companions", value);
   }

   protected ServerLevel getServerLevel() {
      return this.level() instanceof ServerLevel serverLevel ? serverLevel : null;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.hasInteractSound()) {
         InteractionResult itemResult = player.getItemInHand(hand).interactLivingEntity(player, this, hand);
         return itemResult.consumesAction() ? itemResult : ModEntitySounds.playInteract(this, this.getSoundFolder(), this.isFemale());
      } else {
         return super.mobInteract(player, hand);
      }
   }

   protected boolean hasNearbyPatrolOfType(String groupType, double radius) {
      return !this.level()
         .getEntitiesOfClass(
            Entity.class,
            this.getBoundingBox().inflate(radius),
            entity -> entity != this
               && PersistentEntityData.get(entity).contains("villagerpatrols_group_type")
               && groupType.equals(PersistentEntityData.get(entity).getString("villagerpatrols_group_type"))
         )
         .isEmpty();
   }

   public boolean canBeSeenAsEnemy() {
      return super.canBeSeenAsEnemy();
   }

   public boolean isAlliedTo(Entity entity) {
      if (this instanceof OrcEntity || entity instanceof OrcEntity) {
         return this instanceof OrcEntity && entity instanceof OrcEntity || super.isAlliedTo(entity);
      } else if (entity instanceof AbstractKnightEntity otherKnight) {
         return this.isFriendlyKnight() == otherKnight.isFriendlyKnight() || super.isAlliedTo(entity);
      } else {
         return !this.isFriendlyKnight()
               || !(entity instanceof IronGolem)
                  && !(entity instanceof MaleVillagerEntity)
                  && !(entity instanceof FemaleVillagerEntity)
                  && !(entity instanceof Villager)
            ? super.isAlliedTo(entity)
            : true;
      }
   }

   protected boolean isSunSensitive() {
      return false;
   }

   protected BlockPos findCompanionSpawnPos(AbstractKnightEntity entity) {
      Level level = this.level();
      if (level instanceof ServerLevel serverLevel) {
         BlockPos origin = this.blockPosition();

         for (int attempt = 0; attempt < 12; attempt++) {
            int offsetX = this.getRandom().nextInt(9) - 4;
            int offsetZ = this.getRandom().nextInt(9) - 4;
            BlockPos candidate = origin.offset(offsetX, 0, offsetZ);
            BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            BlockPos below = surface.below();
            if (level.getBlockState(below).blocksMotion()
               && SpawnSurfaceValidator.hasValidSpawnSupport(serverLevel, surface)
               && level.getFluidState(surface).isEmpty()
               && level.getFluidState(surface.above()).isEmpty()) {
               entity.moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, this.getYRot(), this.getXRot());
               if (level.noCollision(entity)) {
                  return surface;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   protected abstract String getTextureFolder();

   protected String getSoundFolder() {
      return this.getTextureFolder();
   }

   protected boolean hasInteractSound() {
      return this.isFriendlyKnight();
   }

   protected abstract boolean usesShield();

   protected abstract boolean isFriendlyKnight();

   protected int getMaleSkinCount() {
      return 10;
   }

   protected int getFemaleSkinCount() {
      return 10;
   }

   protected ItemStack createDefaultMainHandItemStack() {
      return new ItemStack(this.getDefaultMainHandItem());
   }

   protected ItemStack createFriendlyKnightMainHandItemStack() {
      return this.getRandom().nextFloat() < 0.7F
         ? new ItemStack((ItemLike)ModItems.KNIGHT_SWORD.get())
         : new ItemStack((ItemLike)ModItems.KNIGHT_SHORT_SWORD.get());
   }

   protected ItemStack createEvilKnightMainHandItemStack() {
      return this.getRandom().nextFloat() < 0.7F
         ? new ItemStack((ItemLike)ModItems.EVIL_KNIGHT_SWORD.get())
         : new ItemStack((ItemLike)ModItems.EVIL_KNIGHT_SHORT_SWORD.get());
   }

   protected Item getDefaultMainHandItem() {
      return Items.IRON_SWORD;
   }

   protected boolean useBiasedGenderRoll() {
      return false;
   }

   protected float getBiasedFemaleChance() {
      return 0.5F;
   }
}
