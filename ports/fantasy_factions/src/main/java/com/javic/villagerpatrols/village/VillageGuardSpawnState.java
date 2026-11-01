package com.javic.villagerpatrols.village;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;

public final class VillageGuardSpawnState extends SavedData {
   private static final String DATA_NAME = "villagerpatrols_village_guards";
   private static final String TAG_ENTRIES = "entries";
   private static final String TAG_GROUP_ID = "group_id";
   private static final String TAG_NEXT_RESPAWN = "next_respawn";
   private final Map<String, Long> nextRespawnByGroupId = new HashMap<>();

   public static Function<CompoundTag, VillageGuardSpawnState> loader() {
      return VillageGuardSpawnState::load;
   }

   public static Supplier<VillageGuardSpawnState> supplier() {
      return VillageGuardSpawnState::new;
   }

   public static String dataName() {
      return "villagerpatrols_village_guards";
   }

   private static VillageGuardSpawnState load(CompoundTag tag) {
      VillageGuardSpawnState state = new VillageGuardSpawnState();
      ListTag entries = tag.getList("entries", 10);

      for (int i = 0; i < entries.size(); i++) {
         CompoundTag entry = entries.getCompound(i);
         String groupId = entry.getString("group_id");
         if (!groupId.isEmpty()) {
            state.nextRespawnByGroupId.put(groupId, entry.getLong("next_respawn"));
         }
      }

      return state;
   }

   public long getNextRespawn(String groupId) {
      return this.nextRespawnByGroupId.getOrDefault(groupId, 0L);
   }

   public void setNextRespawn(String groupId, long nextRespawn) {
      this.nextRespawnByGroupId.put(groupId, nextRespawn);
      this.setDirty();
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag entries = new ListTag();

      for (Entry<String, Long> entry : this.nextRespawnByGroupId.entrySet()) {
         CompoundTag value = new CompoundTag();
         value.putString("group_id", entry.getKey());
         value.putLong("next_respawn", entry.getValue());
         entries.add(value);
      }

      tag.put("entries", entries);
      return tag;
   }
}
