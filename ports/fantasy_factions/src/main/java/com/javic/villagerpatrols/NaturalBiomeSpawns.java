package com.javic.villagerpatrols;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

final class NaturalBiomeSpawns {
    private NaturalBiomeSpawns() {}

    static void register() {
        add("has_bandit_spawns", ModEntities.BANDIT.get(), 6, 1, 3);
        add("has_dwarf_spawns", ModEntities.DWARF.get(), 8, 1, 5);
        add("has_elf_spawns", ModEntities.ELF.get(), 8, 1, 4);
        add("has_evil_knight_patrols", ModEntities.EVIL_KNIGHT.get(), 10, 1, 1);
        add("has_evil_wizard_spawns", ModEntities.EVIL_WIZARD.get(), 3, 1, 1);
        add("has_human_witch_spawns", ModEntities.HUMAN_WITCH.get(), 5, 1, 2);
        add("has_illager_patrols", ModEntities.HUMAN_PILLAGER.get(), 5, 1, 1);
        add("has_knight_patrols", ModEntities.KNIGHT.get(), 10, 1, 1);
        add("has_orc_spawns", ModEntities.ORC.get(), 5, 1, 4);
        add("has_shadow_spawns", ModEntities.SHADOW_RIDER_SPAWN.get(), 1, 1, 2);
        add("has_shadow_spawns", ModEntities.SHADOW.get(), 1, 1, 4);
        add("has_vampire_spawns", ModEntities.VAMPIRE.get(), 1, 1, 1);
        add("has_wizard_spawns", ModEntities.MALE_WIZARD.get(), 3, 1, 1);
    }

    private static void add(String tag, EntityType<?> type, int weight, int min, int max) {
        BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.create(Registries.BIOME,
                new ResourceLocation(VillagerPatrols.MODID, tag))), type.getCategory(), type, weight, min, max);
    }
}
