package com.javic.villagerpatrols;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static final Supplier<CreativeModeTab> FANTASY_FACTIONS = NativeRegistry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, "fantasy_factions", () -> FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.villagerpatrols.fantasy_factions"))
                    .icon(() -> new ItemStack(ModItems.LUTE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LUTE.get());
                        output.accept(ModItems.WARD_BANNER.get());
                        output.accept(ModItems.KNIGHT_SWORD.get());
                        output.accept(ModItems.KNIGHT_SHORT_SWORD.get());
                        output.accept(ModItems.KNIGHT_LEADER_SWORD.get());
                        output.accept(ModItems.KNIGHT_MACE.get());
                        output.accept(ModItems.KNIGHT_LANCE.get());
                        output.accept(ModItems.EVIL_KNIGHT_SWORD.get());
                        output.accept(ModItems.EVIL_KNIGHT_SHORT_SWORD.get());
                        output.accept(ModItems.EVIL_KNIGHT_LEADER_SWORD.get());
                        output.accept(ModItems.EVIL_KNIGHT_MACE.get());
                        output.accept(ModItems.BANDIT_DAGGER.get());
                        output.accept(ModItems.WAR_AXE.get());
                        output.accept(ModItems.HEAVY_WAR_AXE.get());
                        output.accept(ModItems.ELVEN_SWORD.get());
                        output.accept(ModItems.DWARF_AXE.get());
                        output.accept(ModItems.MALE_VILLAGER_SPAWN_EGG.get());
                        output.accept(ModItems.FEMALE_VILLAGER_SPAWN_EGG.get());
                        output.accept(ModItems.KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.KNIGHT_SHIELD_SPAWN_EGG.get());
                        output.accept(ModItems.KNIGHT_LEADER_SPAWN_EGG.get());
                        output.accept(ModItems.VILLAGE_GUARD_SPAWN_EGG.get());
                        output.accept(ModItems.VILLAGE_GUARD_LEADER_SPAWN_EGG.get());
                        output.accept(ModItems.EVIL_KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.EVIL_KNIGHT_SHIELD_SPAWN_EGG.get());
                        output.accept(ModItems.EVIL_KNIGHT_LEADER_SPAWN_EGG.get());
                        output.accept(ModItems.ORC_SPAWN_EGG.get());
                        output.accept(ModItems.ELF_SPAWN_EGG.get());
                        output.accept(ModItems.DWARF_SPAWN_EGG.get());
                        output.accept(ModItems.BANDIT_SPAWN_EGG.get());
                        output.accept(ModItems.SHADOW_SPAWN_EGG.get());
                        output.accept(ModItems.EVIL_WIZARD_SPAWN_EGG.get());
                        output.accept(ModItems.WIZARD_SPAWN_EGG.get());
                        output.accept(ModItems.VAMPIRE_SPAWN_EGG.get());
                        output.accept(ModItems.HEALER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_PILLAGER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_SWORDSMAN_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_ARCHER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_HORSEMAN_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_RAVAGER_RIDER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_VINDICATOR_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_EVOKER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_WITCH_SPAWN_EGG.get());
                        output.accept(ModItems.WIZARD_STAFF.get());
                    }).build());

    private ModCreativeTabs() {}

    public static void init() { FANTASY_FACTIONS.get(); }
}
