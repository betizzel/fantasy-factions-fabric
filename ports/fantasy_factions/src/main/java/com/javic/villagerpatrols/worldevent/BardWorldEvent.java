package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModItems;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class BardWorldEvent extends AbstractWorldEvent {
   public static final String GROUP_TYPE = "bard_world_event";
   public static final String GROUP_ROLE = "bard";
   public static final String TAG_BARD_ANCHOR_X = "villagerpatrols_bard_anchor_x";
   public static final String TAG_BARD_ANCHOR_Y = "villagerpatrols_bard_anchor_y";
   public static final String TAG_BARD_ANCHOR_Z = "villagerpatrols_bard_anchor_z";
   public static final String TAG_BARD_WALKING = "villagerpatrols_bard_walking";
   private static final int SELECTION_WEIGHT = 4;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 1;
   private static final int MAX_COUNT = 1;
   private static final int CLUSTER_RADIUS = 1;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 0;

   @Override
   String groupType() {
      return "bard_world_event";
   }

   @Override
   int selectionWeight() {
      return 4;
   }

   @Override
   int minPlayerDistance() {
      return 24;
   }

   @Override
   int maxPlayerDistance() {
      return 56;
   }

   @Override
   int minCount() {
      return 1;
   }

   @Override
   int maxCount() {
      return 1;
   }

   @Override
   int clusterRadius() {
      return 1;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 0;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return SlimPatchCompat.resolveMaleVillagerType();
   }

   @Override
   protected String groupRole() {
      return "bard";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return this.canUseBardWorldEventBiome(level, pos);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      return List.of(new AbstractWorldEvent.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "bard"));
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      BlockPos anchor = placement.pos();
      PersistentEntityData.get(entity).putInt("villagerpatrols_bard_anchor_x", anchor.getX());
      PersistentEntityData.get(entity).putInt("villagerpatrols_bard_anchor_y", anchor.getY());
      PersistentEntityData.get(entity).putInt("villagerpatrols_bard_anchor_z", anchor.getZ());
      PersistentEntityData.get(entity).putBoolean("villagerpatrols_bard_walking", level.random.nextBoolean());
      applyBardCompatibility(entity);
      EventVillagerSkins.applyBardSkin(entity);
   }

   public static void applyBardCompatibility(Mob mob) {
      ItemStack lute = new ItemStack((ItemLike)ModItems.LUTE.get());
      mob.setItemSlot(EquipmentSlot.MAINHAND, lute.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      CompoundTag persistentMainHand = lute.copy().save(new CompoundTag());
      PersistentEntityData.get(mob).put("PersistentMainHand", persistentMainHand);
      SlimPatchCompat.trySetPersistentMainHand(mob, lute);
   }

   public static boolean shouldMaintainBardLute(Mob mob) {
      return "bard_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
            && "bard".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))
         || "camping_villagers_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
            && PersistentEntityData.get(mob).getBoolean("villagerpatrols_camping_bard");
   }

   public static boolean hasStableBardLute(Mob mob) {
      return mob.getMainHandItem().is((Item)ModItems.LUTE.get()) && mob.getOffhandItem().isEmpty();
   }
}
