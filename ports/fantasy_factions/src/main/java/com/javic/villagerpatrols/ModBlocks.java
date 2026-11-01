package com.javic.villagerpatrols;

import com.javic.villagerpatrols.block.WardBannerBlock;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final Supplier<Block> WARD_BANNER = NativeRegistry.register(BuiltInRegistries.BLOCK, "ward_banner",
            () -> new WardBannerBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion()));

    private ModBlocks() {}
    public static void init() { WARD_BANNER.get(); }
}
