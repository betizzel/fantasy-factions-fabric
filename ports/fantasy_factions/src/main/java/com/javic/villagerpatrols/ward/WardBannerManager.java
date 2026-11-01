package com.javic.villagerpatrols.ward;

import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.ModBlocks;
import com.javic.villagerpatrols.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WardBannerManager {
   private WardBannerManager() {
   }

   public static void register(ServerLevel level, BlockPos pos, WardBannerType type) {
      getData(level).register(pos, type);
   }

   public static void unregister(ServerLevel level, BlockPos pos) {
      getData(level).unregister(pos);
   }

   public static boolean isBlocked(ServerLevel level, BlockPos center) {
      return !Config.ENABLE_WARD_BANNERS.get() ? false : getData(level).isBlocked(level, center, Config.wardBannerRadius());
   }

   public static boolean blocksNaturalSpawn(EntityType<?> entityType) {
      return entityType == ModEntities.KNIGHT.get()
         || entityType == ModEntities.EVIL_KNIGHT.get()
         || entityType == ModEntities.ORC.get()
         || entityType == ModEntities.HUMAN_PILLAGER.get()
         || entityType == ModEntities.HUMAN_WITCH.get()
         || entityType == ModEntities.ELF.get()
         || entityType == ModEntities.DWARF.get()
         || entityType == ModEntities.BANDIT.get()
         || entityType == ModEntities.SHADOW.get()
         || entityType == ModEntities.SHADOW_RIDER_SPAWN.get()
         || entityType == ModEntities.EVIL_WIZARD.get()
         || entityType == ModEntities.MALE_WIZARD.get()
         || entityType == ModEntities.VAMPIRE.get();
   }

   static boolean isStoredWardStillValid(ServerLevel level, BlockPos pos, WardBannerType type) {
      if (!level.isLoaded(pos)) {
         return true;
      }

      BlockState state = level.getBlockState(pos);
      return type == WardBannerType.WARD_BANNER && state.is((Block)ModBlocks.WARD_BANNER.get());
   }

   private static WardBannerSavedData getData(ServerLevel level) {
      return (WardBannerSavedData)level.getDataStorage()
         .computeIfAbsent(WardBannerSavedData.loader(), WardBannerSavedData.supplier(), WardBannerSavedData.dataName());
   }
}
