package com.javic.villagerpatrols.ward;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public final class WardBannerSavedData extends SavedData {
   private static final String DATA_NAME = "villagerpatrols_ward_banners";
   private static final String TAG_ENTRIES = "entries";
   private static final String TAG_X = "x";
   private static final String TAG_Y = "y";
   private static final String TAG_Z = "z";
   private static final String TAG_TYPE = "type";
   private final Map<Long, Map<BlockPos, WardBannerType>> entriesByChunk = new HashMap<>();

   public static Function<CompoundTag, WardBannerSavedData> loader() {
      return WardBannerSavedData::load;
   }

   public static Supplier<WardBannerSavedData> supplier() {
      return WardBannerSavedData::new;
   }

   public static String dataName() {
      return "villagerpatrols_ward_banners";
   }

   private static WardBannerSavedData load(CompoundTag tag) {
      WardBannerSavedData data = new WardBannerSavedData();
      ListTag entries = tag.getList("entries", 10);

      for (int i = 0; i < entries.size(); i++) {
         CompoundTag entry = entries.getCompound(i);
         WardBannerType type = WardBannerType.byName(entry.getString("type"));
         if (type != null) {
            BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            data.entriesByChunk.computeIfAbsent(chunkKey(pos), ignored -> new HashMap<>()).put(pos, type);
         }
      }

      return data;
   }

   public void register(BlockPos pos, WardBannerType type) {
      this.entriesByChunk.computeIfAbsent(chunkKey(pos), ignored -> new HashMap<>()).put(pos.immutable(), type);
      this.setDirty();
   }

   public void unregister(BlockPos pos) {
      long chunkKey = chunkKey(pos);
      Map<BlockPos, WardBannerType> entries = this.entriesByChunk.get(chunkKey);
      if (entries != null && entries.remove(pos) != null) {
         if (entries.isEmpty()) {
            this.entriesByChunk.remove(chunkKey);
         }

         this.setDirty();
      }
   }

   public boolean isBlocked(ServerLevel level, BlockPos center, int searchRadius) {
      if (searchRadius <= 0) {
         return false;
      }

      boolean dirty = false;
      int minChunkX = center.getX() - searchRadius >> 4;
      int maxChunkX = center.getX() + searchRadius >> 4;
      int minChunkZ = center.getZ() - searchRadius >> 4;
      int maxChunkZ = center.getZ() + searchRadius >> 4;

      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
         for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            Map<BlockPos, WardBannerType> entries = this.entriesByChunk.get(ChunkPos.asLong(chunkX, chunkZ));
            if (entries != null && !entries.isEmpty()) {
               Iterator<Entry<BlockPos, WardBannerType>> iterator = entries.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<BlockPos, WardBannerType> entry = iterator.next();
                  BlockPos pos = entry.getKey();
                  WardBannerType type = entry.getValue();
                  if (!level.hasChunkAt(pos)) {
                     if (type.isEnabled() && pos.closerThan(center, type.radius())) {
                        return true;
                     }
                  } else if (!WardBannerManager.isStoredWardStillValid(level, pos, type)) {
                     iterator.remove();
                     dirty = true;
                  } else {
                     int radius = type.radius();
                     if (type.isEnabled() && radius > 0 && pos.distSqr(center) <= (double)radius * radius) {
                        return true;
                     }
                  }
               }

               if (entries.isEmpty()) {
                  this.entriesByChunk.remove(ChunkPos.asLong(chunkX, chunkZ));
                  dirty = true;
               }
            }
         }
      }

      if (dirty) {
         this.setDirty();
      }

      return false;
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag entries = new ListTag();

      for (Map<BlockPos, WardBannerType> positions : this.entriesByChunk.values()) {
         for (Entry<BlockPos, WardBannerType> entry : positions.entrySet()) {
            CompoundTag value = new CompoundTag();
            BlockPos pos = entry.getKey();
            value.putInt("x", pos.getX());
            value.putInt("y", pos.getY());
            value.putInt("z", pos.getZ());
            value.putString("type", entry.getValue().serializedName());
            entries.add(value);
         }
      }

      tag.put("entries", entries);
      return tag;
   }

   private static long chunkKey(BlockPos pos) {
      return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
   }
}
