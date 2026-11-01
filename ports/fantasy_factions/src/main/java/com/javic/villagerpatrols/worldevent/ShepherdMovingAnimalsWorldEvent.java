package com.javic.villagerpatrols.worldevent;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.EventVillagerSkins;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

public final class ShepherdMovingAnimalsWorldEvent extends AbstractWorldEvent {
   private static final TagKey<Biome> SHEPHERD_MOVING_ANIMALS_WORLD_EVENTS = TagKey.create(
      Registries.BIOME, new ResourceLocation("villagerpatrols", "shepherd_moving_animals_world_events")
   );
   public static final String GROUP_TYPE = "shepherd_moving_animals_world_event";
   public static final String SHEPHERD_LEADER_ROLE = "shepherd_leader";
   public static final String SHEEP_ROLE = "sheep";
   private static final int SELECTION_WEIGHT = 7;
   private static final int MIN_PLAYER_DISTANCE = 24;
   private static final int MAX_PLAYER_DISTANCE = 56;
   private static final int MIN_COUNT = 3;
   private static final int MAX_COUNT = 5;
   private static final int CLUSTER_RADIUS = 4;
   private static final int PROXIMITY_RADIUS = 160;
   private static final int VILLAGE_RADIUS_SECTIONS = 1;

   @Override
   String groupType() {
      return "shepherd_moving_animals_world_event";
   }

   @Override
   int selectionWeight() {
      return 7;
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
      return 3;
   }

   @Override
   int maxCount() {
      return 5;
   }

   @Override
   int clusterRadius() {
      return 4;
   }

   @Override
   int proximityRadius() {
      return 160;
   }

   @Override
   int villageAvoidanceRadiusSections() {
      return 1;
   }

   @Override
   protected EntityType<? extends Mob> entityType() {
      return SlimPatchCompat.resolveMaleVillagerType();
   }

   @Override
   protected String groupRole() {
      return "shepherd_leader";
   }

   @Override
   protected boolean canUseBiome(ServerLevel level, BlockPos pos) {
      return level.getBiome(pos).is(SHEPHERD_MOVING_ANIMALS_WORLD_EVENTS);
   }

   @Override
   protected List<AbstractWorldEvent.EntitySpawnDefinition> buildDefinitions(ServerLevel level) {
      List<AbstractWorldEvent.EntitySpawnDefinition> definitions = new ArrayList<>();
      definitions.add(new AbstractWorldEvent.EntitySpawnDefinition(SlimPatchCompat.resolveRandomVillagerType(level), "shepherd_leader"));
      int sheepCount = 2 + level.random.nextInt(3);

      for (int index = 0; index < sheepCount; index++) {
         definitions.add(new AbstractWorldEvent.EntitySpawnDefinition(EntityType.SHEEP, "sheep"));
      }

      return definitions;
   }

   @Override
   protected void prepareSpawnedEntity(ServerLevel level, Mob entity, AbstractWorldEvent.SpawnPlacement placement, String groupId) {
      if ("shepherd_leader".equals(placement.role())) {
         applyShepherdCompatibility(entity);
         EventVillagerSkins.applyShepherdSkin(entity);
         EventVillagerProfessions.apply(level, entity, VillagerProfession.SHEPHERD);
      }
   }

   public static void applyShepherdCompatibility(Mob mob) {
      ItemStack wheat = new ItemStack(Items.WHEAT);
      mob.setItemSlot(EquipmentSlot.MAINHAND, wheat.copy());
      mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
      PersistentEntityData.get(mob).putString("SlimPatchCombatMode", "PASSIVE");
      PersistentEntityData.get(mob).putBoolean("slimpatch_skip_natural_family_spawn", true);
      CompoundTag persistentMainHand = wheat.copy().save(new CompoundTag());
      PersistentEntityData.get(mob).put("PersistentMainHand", persistentMainHand);
   }
}
