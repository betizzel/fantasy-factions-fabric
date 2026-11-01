package com.javic.villagerpatrols.village;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class VillageGuardHome {
   public static final String TAG_HOME_X = "villagerpatrols_village_guard_home_x";
   public static final String TAG_HOME_Y = "villagerpatrols_village_guard_home_y";
   public static final String TAG_HOME_Z = "villagerpatrols_village_guard_home_z";
   public static final String TAG_BELL_X = "villagerpatrols_guard_bell_x";
   public static final String TAG_BELL_Y = "villagerpatrols_guard_bell_y";
   public static final String TAG_BELL_Z = "villagerpatrols_guard_bell_z";

   private VillageGuardHome() {
   }

   public static void setHome(Mob mob, BlockPos pos) {
      setBellAnchor(mob, pos);
   }

   public static void setBellAnchor(Mob mob, BlockPos pos) {
      CompoundTag data = PersistentEntityData.get(mob);
      data.putInt("villagerpatrols_village_guard_home_x", pos.getX());
      data.putInt("villagerpatrols_village_guard_home_y", pos.getY());
      data.putInt("villagerpatrols_village_guard_home_z", pos.getZ());
      data.putInt("villagerpatrols_guard_bell_x", pos.getX());
      data.putInt("villagerpatrols_guard_bell_y", pos.getY());
      data.putInt("villagerpatrols_guard_bell_z", pos.getZ());
   }

   public static boolean hasHome(Mob mob) {
      CompoundTag data = PersistentEntityData.get(mob);
      return data.contains("villagerpatrols_village_guard_home_x")
         && data.contains("villagerpatrols_village_guard_home_y")
         && data.contains("villagerpatrols_village_guard_home_z");
   }

   public static BlockPos getHome(Mob mob) {
      CompoundTag data = PersistentEntityData.get(mob);
      return hasHome(mob)
         ? new BlockPos(
            data.getInt("villagerpatrols_village_guard_home_x"),
            data.getInt("villagerpatrols_village_guard_home_y"),
            data.getInt("villagerpatrols_village_guard_home_z")
         )
         : null;
   }

   public static boolean hasBellAnchor(Mob mob) {
      CompoundTag data = PersistentEntityData.get(mob);
      return data.contains("villagerpatrols_guard_bell_x") && data.contains("villagerpatrols_guard_bell_y") && data.contains("villagerpatrols_guard_bell_z");
   }

   public static BlockPos getBellAnchor(Mob mob) {
      CompoundTag data = PersistentEntityData.get(mob);
      return hasBellAnchor(mob)
         ? new BlockPos(data.getInt("villagerpatrols_guard_bell_x"), data.getInt("villagerpatrols_guard_bell_y"), data.getInt("villagerpatrols_guard_bell_z"))
         : null;
   }

   public static BlockPos resolveHome(Mob mob, int bellSearchRadius) {
      if (mob.level() instanceof ServerLevel serverLevel) {
         BlockPos bellAnchor = getBellAnchor(mob);
         if (bellAnchor == null) {
            bellAnchor = getHome(mob);
         }

         if (bellAnchor == null) {
            return null;
         } else {
            BlockPos bell = findNearestBell(serverLevel, bellAnchor, bellSearchRadius);
            if (bell != null) {
               setBellAnchor(mob, bell);
               return bell;
            } else {
               BlockPos home = getHome(mob);
               if (home == null) {
                  return null;
               } else {
                  BlockPos fallback = serverLevel.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, home);
                  if (isValidSurface(serverLevel, fallback)) {
                     setHome(mob, fallback);
                     return fallback;
                  } else {
                     return null;
                  }
               }
            }
         }
      } else {
         return null;
      }
   }

   public static BlockPos findNearestBell(ServerLevel level, BlockPos center, int radius) {
      return (BlockPos)level.getPoiManager().findClosest(poi -> poi.is(PoiTypes.MEETING), center, radius, Occupancy.ANY).orElse(null);
   }

   public static boolean isValidSurface(ServerLevel level, BlockPos pos) {
      return level.isLoaded(pos)
         && level.isLoaded(pos.above())
         && level.canSeeSkyFromBelowWater(pos)
         && level.getBlockState(pos.below()).blocksMotion()
         && level.getFluidState(pos).isEmpty()
         && level.getFluidState(pos.above()).isEmpty();
   }

   public static void readHome(Mob mob, CompoundTag tag) {
      CompoundTag data = PersistentEntityData.get(mob);
      if (tag.contains("villagerpatrols_village_guard_home_x")) {
         data.putInt("villagerpatrols_village_guard_home_x", tag.getInt("villagerpatrols_village_guard_home_x"));
      }

      if (tag.contains("villagerpatrols_village_guard_home_y")) {
         data.putInt("villagerpatrols_village_guard_home_y", tag.getInt("villagerpatrols_village_guard_home_y"));
      }

      if (tag.contains("villagerpatrols_village_guard_home_z")) {
         data.putInt("villagerpatrols_village_guard_home_z", tag.getInt("villagerpatrols_village_guard_home_z"));
      }

      if (tag.contains("villagerpatrols_guard_bell_x")) {
         data.putInt("villagerpatrols_guard_bell_x", tag.getInt("villagerpatrols_guard_bell_x"));
      }

      if (tag.contains("villagerpatrols_guard_bell_y")) {
         data.putInt("villagerpatrols_guard_bell_y", tag.getInt("villagerpatrols_guard_bell_y"));
      }

      if (tag.contains("villagerpatrols_guard_bell_z")) {
         data.putInt("villagerpatrols_guard_bell_z", tag.getInt("villagerpatrols_guard_bell_z"));
      }
   }

   public static void writeHome(Mob mob, CompoundTag tag) {
      CompoundTag data = PersistentEntityData.get(mob);
      if (data.contains("villagerpatrols_village_guard_home_x")) {
         tag.putInt("villagerpatrols_village_guard_home_x", data.getInt("villagerpatrols_village_guard_home_x"));
      }

      if (data.contains("villagerpatrols_village_guard_home_y")) {
         tag.putInt("villagerpatrols_village_guard_home_y", data.getInt("villagerpatrols_village_guard_home_y"));
      }

      if (data.contains("villagerpatrols_village_guard_home_z")) {
         tag.putInt("villagerpatrols_village_guard_home_z", data.getInt("villagerpatrols_village_guard_home_z"));
      }

      if (data.contains("villagerpatrols_guard_bell_x")) {
         tag.putInt("villagerpatrols_guard_bell_x", data.getInt("villagerpatrols_guard_bell_x"));
      }

      if (data.contains("villagerpatrols_guard_bell_y")) {
         tag.putInt("villagerpatrols_guard_bell_y", data.getInt("villagerpatrols_guard_bell_y"));
      }

      if (data.contains("villagerpatrols_guard_bell_z")) {
         tag.putInt("villagerpatrols_guard_bell_z", data.getInt("villagerpatrols_guard_bell_z"));
      }
   }

   public static String createGroupId(ResourceKey<Level> dimension, BlockPos bellPos) {
      return dimension.location() + ":" + bellPos.getX() + ":" + bellPos.getY() + ":" + bellPos.getZ();
   }
}
