package com.javic.villagerpatrols.compat;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.loader.api.FabricLoader;

public final class SlimPatchCompat {
   private static final String MOD_ID = "slimpatch";
   private static final String MALE_VILLAGER_ID = "slimpatch:male_villager";
   private static final String FEMALE_VILLAGER_ID = "slimpatch:female_villager";
   private static final String TAG_SLIMPATCH_COMMAND = "SlimPatchCommand";
   private static final String TAG_SLIMPATCH_COMMAND_TARGET = "SlimPatchCommandTarget";
   private static final String TAG_SLIMPATCH_COMMAND_OWNER_UUID = "SlimPatchCommandOwnerUuid";
   private static final String TAG_EVENT_ENTITY = "villagerpatrols_event_entity";
   private static final String TAG_EVENT_SPAWN_TIME = "villagerpatrols_event_spawn_time";
   private static final String TAG_GROUP_ID = "villagerpatrols_group_id";
   private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
   private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";
   private static final String TAG_SKIP_NATURAL_FAMILY_SPAWN = "slimpatch_skip_natural_family_spawn";
   private static final String TAG_FAMILY_REGISTERED = "villagerpatrols_traveler_family_registered";
   private static final String TAG_FAMILY_CHILDREN_ROLLED = "villagerpatrols_traveler_family_children_rolled";
   private static final String TAG_FAMILY_CHILD = "villagerpatrols_traveler_family_child";
   private static final int CHILD_SPAWN_RADIUS = 3;
   private static Class<?> familyVillagerClass;
   private static Class<?> villagerAgeStageClass;
   private static Class<?> villagerRelationshipStageClass;
   private static Class<?> birthScreenDataClass;
   private static Class<?> villagerPersonalityClass;
   private static Class<?> worldSkinDataClass;
   private static Method setSpouseVillagerUuidMethod;
   private static Method setSpouseVillagerNameMethod;
   private static Method setRelationshipStageMethod;
   private static Method setParentVillagerUuidMethod;
   private static Method setParentVillagerNameMethod;
   private static Method setParentVillager2UuidMethod;
   private static Method setParentVillager2NameMethod;
   private static Method setBornFromFamilySystemMethod;
   private static Method setNaturalFamilyMemberMethod;
   private static Method setAgeStageWithTimerMethod;
   private static Method upsertVillagerMethod;
   private static Method getWorldSkinDataMethod;
   private static Method getWorldSkinThemeMethod;
   private static Method normalizeSkinTypeMethod;
   private static Method getRandomSkinIdMethod;
   private static Method getCuratedSkinResourcePathMethod;
   private static Method getRandomVillagerPersonalityMethod;
   private static boolean reflectionInitialized;
   private static boolean reflectionAvailable;

   private SlimPatchCompat() {
   }

   public static boolean isLoaded() {
      return FabricLoader.getInstance().isModLoaded("slimpatch");
   }

   public static EntityType<? extends Mob> resolveVillagerType(boolean female) {
      return resolveVillagerType(
         female ? "slimpatch:female_villager" : "slimpatch:male_villager",
         female ? (EntityType)ModEntities.FEMALE_VILLAGER.get() : (EntityType)ModEntities.MALE_VILLAGER.get()
      );
   }

   public static EntityType<? extends Mob> resolveMaleVillagerType() {
      return resolveVillagerType("slimpatch:male_villager", (EntityType<? extends Mob>)ModEntities.MALE_VILLAGER.get());
   }

   public static EntityType<? extends Mob> resolveFemaleVillagerType() {
      return resolveVillagerType("slimpatch:female_villager", (EntityType<? extends Mob>)ModEntities.FEMALE_VILLAGER.get());
   }

   public static EntityType<? extends Mob> resolveRandomVillagerType(ServerLevel level) {
      return level.random.nextBoolean() ? resolveFemaleVillagerType() : resolveMaleVillagerType();
   }

   public static void trySetPersistentMainHand(Mob mob, ItemStack stack) {
      if (isLoaded() && mob != null) {
         try {
            Method setPersistentMainHandItemMethod = mob.getClass().getMethod("setPersistentMainHandItem", ItemStack.class);
            Method syncPersistentMainHandMethod = mob.getClass().getMethod("syncPersistentMainHand");
            setPersistentMainHandItemMethod.invoke(mob, stack.copy());
            syncPersistentMainHandMethod.invoke(mob);
         } catch (ReflectiveOperationException var4) {
         }
      }
   }

   public static void assignRandomPersonalityIfAvailable(Mob mob) {
      if (isLoaded() && mob != null && !PersistentEntityData.get(mob).contains("slimpatch_personality") && initReflection()) {
         try {
            Object personality = getRandomVillagerPersonalityMethod.invoke(null, mob.getRandom());
            Method setPersonalityMethod = mob.getClass().getMethod("setPersonality", villagerPersonalityClass);
            setPersonalityMethod.invoke(mob, personality);
         } catch (ReflectiveOperationException var3) {
         }
      }
   }

   public static void trySetSavedSkinInput(Mob mob, String input) {
      tryInvokeStringSetter(mob, "setSavedSkinInput", input == null ? "" : input);
   }

   public static void trySetCustomSkinPath(Mob mob, String path) {
      tryInvokeStringSetter(mob, "setCustomSkinPath", path == null ? "" : path);
   }

   public static void trySetBaseMaxHealth(Mob mob, double baseMaxHealth) {
      if (mob != null) {
         try {
            Method setBaseMaxHealthMethod = mob.getClass().getMethod("setBaseMaxHealth", double.class);
            setBaseMaxHealthMethod.invoke(mob, baseMaxHealth);
         } catch (ReflectiveOperationException var4) {
         }
      }
   }

   public static void trySetCombatMode(Mob mob, String modeName) {
      if (isLoaded() && mob != null && modeName != null && !modeName.isEmpty()) {
         try {
            Class<?> combatModeClass = Class.forName("com.javic.slimpatch.entity.VillagerCombatMode");
            Object mode = Enum.valueOf(combatModeClass.asSubclass(Enum.class), modeName);
            Method setCombatModeMethod = mob.getClass().getMethod("setCombatMode", combatModeClass);
            setCombatModeMethod.invoke(mob, mode);
         } catch (ReflectiveOperationException var5) {
         }
      }
   }

   public static String getCommandStateName(Mob mob) {
      if (isLoaded() && mob != null) {
         try {
            Method method = mob.getClass().getMethod("getCommandState");
            Object state = method.invoke(mob);
            return state == null ? "NONE" : state.toString();
         } catch (ReflectiveOperationException ignored) {
            return PersistentEntityData.get(mob).getString("SlimPatchCommand");
         }
      } else {
         return "NONE";
      }
   }

   public static UUID getCommandOwnerUuid(Mob mob) {
      if (isLoaded() && mob != null) {
         try {
            Method method = mob.getClass().getMethod("getCommandOwnerUuid");
            return method.invoke(mob) instanceof UUID resolved ? resolved : null;
         } catch (ReflectiveOperationException ignored) {
            return readUuidTag(PersistentEntityData.get(mob), "SlimPatchCommandOwnerUuid");
         }
      } else {
         return null;
      }
   }

   public static UUID getCommandTargetUuid(Mob mob) {
      if (isLoaded() && mob != null) {
         try {
            Method method = mob.getClass().getMethod("getCommandTargetUuid");
            return method.invoke(mob) instanceof UUID resolved ? resolved : null;
         } catch (ReflectiveOperationException ignored) {
            return readUuidTag(PersistentEntityData.get(mob), "SlimPatchCommandTarget");
         }
      } else {
         return null;
      }
   }

   public static boolean isFollowingOrStaying(Mob mob) {
      String commandState = getCommandStateName(mob);
      return "FOLLOW".equals(commandState) || "STAY".equals(commandState);
   }

   public static void tryInitializeTravelerFamily(ServerLevel level, Mob mob) {
      if (isLoaded() && initReflection()) {
         if ("traveler_family_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))) {
            String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
            if (!groupId.isEmpty()) {
               List<Villager> adults = findFamilyAdults(level, mob, groupId);
               if (adults.size() >= 2) {
                  Villager leader = findFamilyLeader(adults);
                  Villager spouse = adults.stream().filter(candidate -> !candidate.getUUID().equals(leader.getUUID())).findFirst().orElse(null);
                  if (spouse != null) {
                     registerSpouses(level.getServer(), leader, spouse);
                     markFamilyRegistered(adults);
                     if (!adults.stream().anyMatch(candidate -> PersistentEntityData.get(candidate).getBoolean("villagerpatrols_traveler_family_children_rolled"))) {
                        boolean spawnChildren = level.random.nextBoolean();
                        markChildrenRolled(adults);
                        if (spawnChildren) {
                           int childCount = 1 + level.random.nextInt(3);

                           for (int index = 0; index < childCount; index++) {
                              Villager child = createFamilyChild(level, leader, spouse, groupId);
                              if (child != null) {
                                 adults.add(child);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean shouldUseTravelerFamilyFollowController(Mob mob) {
      return isLoaded()
         && mob instanceof PathfinderMob
         && "traveler_family_world_event".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_type"))
         && "traveler_companion".equals(PersistentEntityData.get(mob).getString("villagerpatrols_group_role"))
         && !PersistentEntityData.get(mob).getString("villagerpatrols_group_id").isEmpty()
         && !(mob instanceof MaleVillagerEntity)
         && !(mob instanceof FemaleVillagerEntity);
   }

   public static List<PathfinderMob> findTravelerFamilyCompanions(PathfinderMob mob) {
      String groupId = PersistentEntityData.get(mob).getString("villagerpatrols_group_id");
      List<PathfinderMob> companions = new ArrayList<>(
         mob.level()
            .getEntitiesOfClass(
               PathfinderMob.class,
               mob.getBoundingBox().inflate(48.0, 8.0, 48.0),
               candidate -> candidate.isAlive()
                  && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
                  && "traveler_family_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
                  && "traveler_companion".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"))
            )
      );
      companions.sort(Comparator.comparing(Entity::getUUID));
      return companions;
   }

   private static boolean initReflection() {
      if (reflectionInitialized) {
         return reflectionAvailable;
      }

      reflectionInitialized = true;

      try {
         familyVillagerClass = Class.forName("com.javic.slimpatch.entity.FamilyVillager");
         villagerAgeStageClass = Class.forName("com.javic.slimpatch.entity.VillagerAgeStage");
         villagerRelationshipStageClass = Class.forName("com.javic.slimpatch.entity.VillagerRelationshipStage");
         birthScreenDataClass = Class.forName("com.javic.slimpatch.entity.BirthScreenData");
         villagerPersonalityClass = Class.forName("com.javic.slimpatch.entity.VillagerPersonality");
         worldSkinDataClass = Class.forName("com.javic.slimpatch.data.WorldSkinData");
         setSpouseVillagerUuidMethod = familyVillagerClass.getMethod("setSpouseVillagerUuid", UUID.class);
         setSpouseVillagerNameMethod = familyVillagerClass.getMethod("setSpouseVillagerName", String.class);
         setRelationshipStageMethod = familyVillagerClass.getMethod("setRelationshipStage", villagerRelationshipStageClass);
         setParentVillagerUuidMethod = familyVillagerClass.getMethod("setParentVillagerUuid", UUID.class);
         setParentVillagerNameMethod = familyVillagerClass.getMethod("setParentVillagerName", String.class);
         setParentVillager2UuidMethod = familyVillagerClass.getMethod("setParentVillager2Uuid", UUID.class);
         setParentVillager2NameMethod = familyVillagerClass.getMethod("setParentVillager2Name", String.class);
         setBornFromFamilySystemMethod = familyVillagerClass.getMethod("setBornFromFamilySystem", boolean.class);
         setNaturalFamilyMemberMethod = familyVillagerClass.getMethod("setNaturalFamilyMember", boolean.class);
         Class<?> familyAgingHandlerClass = Class.forName("com.javic.slimpatch.entity.FamilyAgingHandler");
         setAgeStageWithTimerMethod = familyAgingHandlerClass.getMethod("setAgeStageWithTimer", familyVillagerClass, villagerAgeStageClass, long.class);
         Class<?> familyTreeTrackerClass = Class.forName("com.javic.slimpatch.familytree.FamilyTreeTracker");
         upsertVillagerMethod = familyTreeTrackerClass.getMethod("upsertVillager", MinecraftServer.class, Villager.class);
         getWorldSkinDataMethod = worldSkinDataClass.getMethod("get", ServerLevel.class);
         getWorldSkinThemeMethod = worldSkinDataClass.getMethod("getTheme");
         normalizeSkinTypeMethod = birthScreenDataClass.getMethod("normalizeSkinType", String.class);
         getRandomSkinIdMethod = birthScreenDataClass.getMethod("getRandomSkinId", String.class, String.class, RandomSource.class);
         getCuratedSkinResourcePathMethod = birthScreenDataClass.getMethod("getCuratedSkinResourcePath", String.class, String.class, int.class);
         getRandomVillagerPersonalityMethod = villagerPersonalityClass.getMethod("getRandom", RandomSource.class);
         reflectionAvailable = true;
      } catch (ReflectiveOperationException ignored) {
         reflectionAvailable = false;
      }

      return reflectionAvailable;
   }

   private static List<Villager> findFamilyAdults(ServerLevel level, Mob mob, String groupId) {
      return new ArrayList<>(
         level.getEntitiesOfClass(
            Villager.class,
            mob.getBoundingBox().inflate(64.0, 16.0, 64.0),
            candidate -> candidate.isAlive()
               && !candidate.isRemoved()
               && groupId.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_id"))
               && "traveler_family_world_event".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type"))
               && familyVillagerClass.isInstance(candidate)
               && !PersistentEntityData.get(candidate).getBoolean("villagerpatrols_traveler_family_child")
         )
      );
   }

   private static Villager findFamilyLeader(List<Villager> adults) {
      return adults.stream()
         .filter(candidate -> "traveler_leader".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role")))
         .findFirst()
         .orElse(adults.get(0));
   }

   private static void registerSpouses(MinecraftServer server, Villager first, Villager second) {
      try {
         setSpouseVillagerUuidMethod.invoke(first, second.getUUID());
         setSpouseVillagerNameMethod.invoke(first, second.getName().getString());
         setRelationshipStageMethod.invoke(first, Enum.valueOf(villagerRelationshipStageClass.asSubclass(Enum.class), "FRIENDSHIP"));
         setNaturalFamilyMemberMethod.invoke(first, true);
         setSpouseVillagerUuidMethod.invoke(second, first.getUUID());
         setSpouseVillagerNameMethod.invoke(second, first.getName().getString());
         setRelationshipStageMethod.invoke(second, Enum.valueOf(villagerRelationshipStageClass.asSubclass(Enum.class), "FRIENDSHIP"));
         setNaturalFamilyMemberMethod.invoke(second, true);
         upsertVillagerMethod.invoke(null, server, first);
         upsertVillagerMethod.invoke(null, server, second);
      } catch (ReflectiveOperationException var4) {
      }
   }

   private static void markFamilyRegistered(List<Villager> adults) {
      for (Villager adult : adults) {
         PersistentEntityData.get(adult).putBoolean("villagerpatrols_traveler_family_registered", true);
      }
   }

   private static void markChildrenRolled(List<Villager> adults) {
      for (Villager adult : adults) {
         PersistentEntityData.get(adult).putBoolean("villagerpatrols_traveler_family_children_rolled", true);
      }
   }

   private static Villager createFamilyChild(ServerLevel level, Villager leader, Villager spouse, String groupId) {
      EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE
         .get(new ResourceLocation(level.random.nextBoolean() ? "slimpatch:female_villager" : "slimpatch:male_villager"));
      if (type.create(level) instanceof Villager child && familyVillagerClass.isInstance(child)) {
         Vec3 spawnPos = findChildSpawnPosition(level, child, leader);
         child.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, leader.getYRot(), 0.0F);
         child.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spawnPos)), MobSpawnType.EVENT, null, null);
         PersistentEntityData.get(child).putBoolean("villagerpatrols_event_entity", true);
         PersistentEntityData.get(child).putLong("villagerpatrols_event_spawn_time", level.getGameTime());
         PersistentEntityData.get(child).putString("villagerpatrols_group_id", groupId);
         PersistentEntityData.get(child).putString("villagerpatrols_group_type", "traveler_family_world_event");
         PersistentEntityData.get(child).putString("villagerpatrols_group_role", "traveler_companion");
         PersistentEntityData.get(child).putBoolean("slimpatch_skip_natural_family_spawn", true);
         PersistentEntityData.get(child).putBoolean("villagerpatrols_traveler_family_registered", true);
         PersistentEntityData.get(child).putBoolean("villagerpatrols_traveler_family_children_rolled", true);
         PersistentEntityData.get(child).putBoolean("villagerpatrols_traveler_family_child", true);

         try {
            setParentVillagerUuidMethod.invoke(child, leader.getUUID());
            setParentVillagerNameMethod.invoke(child, leader.getName().getString());
            setParentVillager2UuidMethod.invoke(child, spouse.getUUID());
            setParentVillager2NameMethod.invoke(child, spouse.getName().getString());
            setBornFromFamilySystemMethod.invoke(child, false);
            setNaturalFamilyMemberMethod.invoke(child, true);

            Object ageStage = switch (level.random.nextInt(3)) {
               case 0 -> Enum.valueOf(villagerAgeStageClass.asSubclass(Enum.class), "TODDLER");
               case 1 -> Enum.valueOf(villagerAgeStageClass.asSubclass(Enum.class), "CHILD");
               default -> Enum.valueOf(villagerAgeStageClass.asSubclass(Enum.class), "TEEN");
            };
            setAgeStageWithTimerMethod.invoke(null, child, ageStage, level.getGameTime());
            setRelationshipStageMethod.invoke(child, Enum.valueOf(villagerRelationshipStageClass.asSubclass(Enum.class), "FRIENDSHIP"));
            applyChildSkin(level, child);
         } catch (ReflectiveOperationException ignored) {
            return null;
         }

         if (!level.addFreshEntity(child)) {
            return null;
         }

         try {
            upsertVillagerMethod.invoke(null, level.getServer(), child);
            upsertVillagerMethod.invoke(null, level.getServer(), leader);
            upsertVillagerMethod.invoke(null, level.getServer(), spouse);
         } catch (ReflectiveOperationException var8) {
         }

         return child;
      } else {
         return null;
      }
   }

   private static void applyChildSkin(ServerLevel level, Villager child) throws ReflectiveOperationException {
      String gender = "female".equals(PersistentEntityData.get(child).getString("slimpatch_gender")) ? "female" : "male";
      Object worldSkinData = getWorldSkinDataMethod.invoke(null, level);
      String skinTheme = worldSkinData == null ? "modern" : (String)getWorldSkinThemeMethod.invoke(worldSkinData);
      String normalizedSkinType = (String)normalizeSkinTypeMethod.invoke(null, skinTheme);
      int skinId = (Integer)getRandomSkinIdMethod.invoke(null, gender, normalizedSkinType, level.random);
      String curatedSkinPath = (String)getCuratedSkinResourcePathMethod.invoke(null, gender, normalizedSkinType, skinId);
      Method setSkinIndexMethod = child.getClass().getMethod("setSkinIndex", int.class);
      Method setSavedSkinInputMethod = child.getClass().getMethod("setSavedSkinInput", String.class);
      Method setCustomSkinPathMethod = child.getClass().getMethod("setCustomSkinPath", String.class);
      setSkinIndexMethod.invoke(child, skinId);
      setSavedSkinInputMethod.invoke(child, "");
      setCustomSkinPathMethod.invoke(child, curatedSkinPath == null ? "" : curatedSkinPath);
   }

   private static Vec3 findChildSpawnPosition(ServerLevel level, Villager child, Villager leader) {
      BlockPos origin = leader.blockPosition();

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            BlockPos sample = origin.offset(dx, 0, dz);
            if (level.isLoaded(sample)) {
               BlockPos surface = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, sample);
               Vec3 pos = Vec3.atBottomCenterOf(surface);
               child.moveTo(pos.x, pos.y, pos.z, leader.getYRot(), 0.0F);
               if (level.noCollision(child) && level.getFluidState(surface).isEmpty() && level.getFluidState(surface.above()).isEmpty()) {
                  return pos;
               }
            }
         }
      }

      return leader.position();
   }

   private static EntityType<? extends Mob> resolveVillagerType(String id, EntityType<? extends Mob> fallback) {
      if (!isLoaded()) {
         return fallback;
      }

      ResourceLocation key = ResourceLocation.tryParse(id);
      if (key == null) {
         return fallback;
      }

      EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(key);
      return (EntityType<? extends Mob>)(type != null && type != EntityType.PIG ? type : fallback);
   }

   private static void tryInvokeStringSetter(Mob mob, String methodName, String value) {
      if (isLoaded() && mob != null) {
         try {
            Method method = mob.getClass().getMethod(methodName, String.class);
            method.invoke(mob, value);
         } catch (ReflectiveOperationException var4) {
         }
      }
   }

   private static UUID readUuidTag(CompoundTag tag, String key) {
      try {
         if (tag.hasUUID(key)) {
            return tag.getUUID(key);
         }
      } catch (Exception var3) {
      }

      return null;
   }
}
