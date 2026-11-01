package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.entity.goal.ConditionalMeleeAttackGoal;
import com.javic.villagerpatrols.entity.goal.ConditionalNearestAttackableTargetGoal;
import com.javic.villagerpatrols.entity.goal.TravelerGroupFollowLeaderGoal;
import com.javic.villagerpatrols.entity.goal.TravelerGroupLeaderGoal;
import com.javic.villagerpatrols.worldevent.BardWorldEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.fabricmc.loader.api.FabricLoader;

public class FemaleVillagerEntity extends Villager {
   private static final int SKINS = 50;
   private static final String TAG_INITIALIZED = "villagerpatrols_initialized";
   private static final String TAG_SKIN = "villagerpatrols_skin";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_PRISONER_RESTRAINED = "villagerpatrols_prisoner_restrained";
   private static final String TAG_CUSTOM_SKIN_PATH = "CustomSkinPath";
   private static final String TAG_SAVED_SKIN_INPUT = "SavedSkinInput";
   private static final String TAG_SLIMPATCH_GENDER = "slimpatch_gender";
   private static final String TAG_SLIMPATCH_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String[] NAMES = new String[]{
      "Abigail",
      "Alice",
      "Amelia",
      "Anna",
      "Audrey",
      "Ava",
      "Bella",
      "Caroline",
      "Charlotte",
      "Chloe",
      "Clara",
      "Daisy",
      "Eleanor",
      "Eliza",
      "Ella",
      "Emily",
      "Emma",
      "Evelyn",
      "Fiona",
      "Grace",
      "Hazel",
      "Isabella",
      "Julia",
      "Lily",
      "Lucy",
      "Mia",
      "Nora",
      "Olivia",
      "Sophia",
      "Violet"
   };
   private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(FemaleVillagerEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> DATA_CUSTOM_SKIN_PATH = SynchedEntityData.defineId(FemaleVillagerEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> DATA_SAVED_SKIN_INPUT = SynchedEntityData.defineId(FemaleVillagerEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> DATA_MINER_MINING = SynchedEntityData.defineId(FemaleVillagerEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_PRISONER_RESTRAINED = SynchedEntityData.defineId(
      FemaleVillagerEntity.class, EntityDataSerializers.BOOLEAN
   );

   public FemaleVillagerEntity(EntityType<? extends Villager> type, Level level) {
      super(type, level);
      this.applyCompatibilityData();
   }

   public static Builder createAttributes() {
      return Villager.createAttributes().add(Attributes.ATTACK_DAMAGE, 1.0);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(3, new TravelerGroupLeaderGoal(this, 0.4));
      this.goalSelector.addGoal(4, new TravelerGroupFollowLeaderGoal(this, 0.5));
      this.goalSelector.addGoal(1, new ConditionalMeleeAttackGoal(this, 0.5, false, this::isSkirmishFighter));
      this.targetSelector.addGoal(1, new ConditionalNearestAttackableTargetGoal(this, HumanPillagerEntity.class, true, this::isSkirmishFighter));
      this.targetSelector.addGoal(2, new ConditionalNearestAttackableTargetGoal(this, HumanVindicatorEntity.class, true, this::isSkirmishFighter));
      this.targetSelector.addGoal(3, new ConditionalNearestAttackableTargetGoal(this, HumanEvokerEntity.class, true, this::isSkirmishFighter));
      this.targetSelector.addGoal(4, new ConditionalNearestAttackableTargetGoal(this, OrcEntity.class, true, this::isSkirmishFighter));
      this.targetSelector.addGoal(5, new ConditionalNearestAttackableTargetGoal(this, BanditEntity.class, true, this::isSkirmishFighter));
      this.targetSelector.addGoal(6, new ConditionalNearestAttackableTargetGoal(this, Zombie.class, true, this::isZombieSkirmishFighter));
      this.targetSelector.addGoal(7, new ConditionalNearestAttackableTargetGoal(this, Spider.class, true, this::isSpiderSkirmishFighter));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SKIN, 1);
      this.entityData.define(DATA_CUSTOM_SKIN_PATH, "");
      this.entityData.define(DATA_SAVED_SKIN_INPUT, "");
      this.entityData.define(DATA_MINER_MINING, false);
      this.entityData.define(DATA_PRISONER_RESTRAINED, false);
   }

   public void setSkinIndex(int index) {
      int resolved = Math.max(1, Math.min(index, 50));
      this.entityData.set(DATA_SKIN, resolved);
      PersistentEntityData.get(this).putInt("villagerpatrols_skin", resolved);
   }

   public int getSkinIndex() {
      return (Integer)this.entityData.get(DATA_SKIN);
   }

   public String getCustomSkinPath() {
      return (String)this.entityData.get(DATA_CUSTOM_SKIN_PATH);
   }

   public void setCustomSkinPath(String path) {
      String resolved = path == null ? "" : path;
      this.entityData.set(DATA_CUSTOM_SKIN_PATH, resolved);
      if (resolved.isEmpty()) {
         PersistentEntityData.get(this).remove("CustomSkinPath");
      } else {
         PersistentEntityData.get(this).putString("CustomSkinPath", resolved);
      }
   }

   public String getSavedSkinInput() {
      return (String)this.entityData.get(DATA_SAVED_SKIN_INPUT);
   }

   public void setSavedSkinInput(String input) {
      String resolved = input == null ? "" : input;
      this.entityData.set(DATA_SAVED_SKIN_INPUT, resolved);
      if (resolved.isEmpty()) {
         PersistentEntityData.get(this).remove("SavedSkinInput");
      } else {
         PersistentEntityData.get(this).putString("SavedSkinInput", resolved);
      }
   }

   public void setMinerMining(boolean mining) {
      this.entityData.set(DATA_MINER_MINING, mining);
   }

   public boolean isMinerMining() {
      return (Boolean)this.entityData.get(DATA_MINER_MINING);
   }

   public ResourceLocation getSkinTexture() {
      ResourceLocation customSkin = ResourceLocation.tryParse(this.getCustomSkinPath());
      return customSkin != null && skinResourceExists(customSkin) ? customSkin : this.resolveDefaultSkinTexture();
   }

   private ResourceLocation resolveDefaultSkinTexture() {
      ResourceLocation indexedSkin = new ResourceLocation("villagerpatrols", "textures/entity/custom_villager/female/skin_" + this.getSkinIndex() + ".png");
      return skinResourceExists(indexedSkin)
         ? indexedSkin
         : new ResourceLocation("villagerpatrols", "textures/entity/custom_villager/female/skin_1.png");
   }

   private static boolean skinResourceExists(ResourceLocation location) {
      return FemaleVillagerEntity.class.getClassLoader().getResource("assets/" + location.getNamespace() + "/" + location.getPath()) != null;
   }

   protected SoundEvent getAmbientSound() {
      return ModEntitySounds.ambient("villager", true);
   }

   public int getAmbientSoundInterval() {
      return ModEntitySounds.ambientInterval(this);
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return ModEntitySounds.hurt("villager", true);
   }

   protected SoundEvent getDeathSound() {
      return ModEntitySounds.death("villager", true);
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag spawnTag
   ) {
      SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
      CompoundTag data = PersistentEntityData.get(this);
      this.applyCompatibilityData();
      if (!data.getBoolean("villagerpatrols_initialized")) {
         this.setSkinIndex(this.getRandom().nextInt(50) + 1);
         if (!isSlimPatchLoaded() && this.getCustomSkinPath().isEmpty()) {
            EventVillagerSkins.applyDefaultStandaloneVillagerSkin(this);
         }

         this.setCustomName(Component.literal(NAMES[this.getRandom().nextInt(NAMES.length)]));
         data.putBoolean("villagerpatrols_initialized", true);
         NaturalTravelerEquipment.tryEquipNaturalTravelerWeapon(this, reason);
      } else if (data.contains("villagerpatrols_skin")) {
         this.setSkinIndex(data.getInt("villagerpatrols_skin"));
      }

      this.setCustomNameVisible(false);
      return groupData;
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      NaturalTravelerEquipment.ensureNaturalTravelerWeapon(this);
      if (BardWorldEvent.shouldMaintainBardLute(this) && !BardWorldEvent.hasStableBardLute(this)) {
         BardWorldEvent.applyBardCompatibility(this);
      }
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.applyCompatibilityData();
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

      if (tag.contains("villagerpatrols_prisoner_restrained")) {
         this.setPrisonerRestrained(tag.getBoolean("villagerpatrols_prisoner_restrained"));
      }

      this.setCustomSkinPath(tag.contains("CustomSkinPath") ? tag.getString("CustomSkinPath") : "");
      this.setSavedSkinInput(tag.contains("SavedSkinInput") ? tag.getString("SavedSkinInput") : "");
      this.setCustomNameVisible(false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("slimpatch_gender", "female");
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

      tag.putBoolean("villagerpatrols_prisoner_restrained", this.isPrisonerRestrained());
      if (!this.getCustomSkinPath().isEmpty()) {
         tag.putString("CustomSkinPath", this.getCustomSkinPath());
      }

      if (!this.getSavedSkinInput().isEmpty()) {
         tag.putString("SavedSkinInput", this.getSavedSkinInput());
      }
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      InteractionResult itemResult = itemStack.interactLivingEntity(player, this, hand);
      if (itemResult.consumesAction()) {
         return itemResult;
      }

      if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
         ModEntitySounds.playInteract(this, "villager", true);
      }

      return super.mobInteract(player, hand);
   }

   public void setGroupData(String groupId, String groupType, String groupRole) {
      CompoundTag data = PersistentEntityData.get(this);
      data.putString("villagerpatrols_group_id", groupId);
      data.putString("villagerpatrols_group_type", groupType);
      data.putString("villagerpatrols_group_role", groupRole);
   }

   public void setPrisonerRestrained(boolean restrained) {
      this.entityData.set(DATA_PRISONER_RESTRAINED, restrained);
      PersistentEntityData.get(this).putBoolean("villagerpatrols_prisoner_restrained", restrained);
   }

   public boolean isPrisonerRestrained() {
      return (Boolean)this.entityData.get(DATA_PRISONER_RESTRAINED);
   }

   private boolean isSkirmishFighter() {
      return VillagerIllagerSkirmishEquipment.isSkirmishFighter(this);
   }

   private boolean isZombieSkirmishFighter() {
      return VillagerIllagerSkirmishEquipment.isSkirmishFighter(this, "villager_zombie_skirmish");
   }

   private boolean isSpiderSkirmishFighter() {
      return VillagerIllagerSkirmishEquipment.isSkirmishFighter(this, "villager_spider_skirmish");
   }

   private void applyCompatibilityData() {
      CompoundTag data = PersistentEntityData.get(this);
      data.putString("slimpatch_gender", "female");
      data.remove("slimpatch_forced");
      data.putBoolean("slimpatch_skip_natural_family_spawn", true);
   }

   private static boolean isSlimPatchLoaded() {
      return FabricLoader.getInstance().isModLoaded("slimpatch");
   }
}
