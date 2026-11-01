package com.javic.villagerpatrols;

import com.javic.villagerpatrols.block.entity.WardBannerBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final Supplier<BlockEntityType<WardBannerBlockEntity>> WARD_BANNER = NativeRegistry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, "ward_banner",
            () -> BlockEntityType.Builder.of(WardBannerBlockEntity::new, ModBlocks.WARD_BANNER.get()).build(null));

    private ModBlockEntities() {}
    public static void init() { WARD_BANNER.get(); }
}
