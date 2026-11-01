package com.javic.villagerpatrols;

import com.javic.villagerpatrols.item.HealerSpawnEggItem;
import com.javic.villagerpatrols.item.LuteItem;
import com.javic.villagerpatrols.item.VampireSpawnEggItem;
import com.javic.villagerpatrols.item.WizardSpawnEggItem;
import com.javic.villagerpatrols.item.WizardStaffItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
   public static final Supplier<Item> LUTE = NativeRegistry.register(BuiltInRegistries.ITEM, "lute", () -> new LuteItem(new Properties().stacksTo(1).durability(256)));
   public static final Supplier<BlockItem> WARD_BANNER = NativeRegistry.register(BuiltInRegistries.ITEM, "ward_banner", () -> new BlockItem((Block)ModBlocks.WARD_BANNER.get(), new Properties()));
   public static final Supplier<SwordItem> KNIGHT_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> KNIGHT_SHORT_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_short_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> KNIGHT_LEADER_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_leader_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> EVIL_KNIGHT_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> EVIL_KNIGHT_SHORT_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_short_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> EVIL_KNIGHT_LEADER_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_leader_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> KNIGHT_MACE = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_mace", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> EVIL_KNIGHT_MACE = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_mace", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> KNIGHT_LANCE = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_lance", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<SwordItem> BANDIT_DAGGER = NativeRegistry.register(BuiltInRegistries.ITEM, "bandit_dagger", () -> new SwordItem(Tiers.IRON, 2, -1.8F, new Properties()));
   public static final Supplier<AxeItem> WAR_AXE = NativeRegistry.register(BuiltInRegistries.ITEM, "war_axe", () -> new AxeItem(Tiers.IRON, 6.0F, -3.1F, new Properties()) {});
   public static final Supplier<AxeItem> HEAVY_WAR_AXE = NativeRegistry.register(BuiltInRegistries.ITEM, "heavy_war_axe", () -> new AxeItem(Tiers.IRON, 7.0F, -3.3F, new Properties()) {});
   public static final Supplier<SwordItem> ELVEN_SWORD = NativeRegistry.register(BuiltInRegistries.ITEM, "elven_sword", () -> new SwordItem(Tiers.IRON, 3, -2.4F, new Properties()));
   public static final Supplier<AxeItem> DWARF_AXE = NativeRegistry.register(BuiltInRegistries.ITEM, "dwarf_axe", () -> new AxeItem(Tiers.IRON, 6.0F, -3.0F, new Properties()) {});
   public static final Supplier<SpawnEggItem> MALE_VILLAGER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "male_villager_spawn_egg", () -> new SpawnEggItem(ModEntities.MALE_VILLAGER.get(), 9137493, 14205604, new Properties()));
   public static final Supplier<SpawnEggItem> FEMALE_VILLAGER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "female_villager_spawn_egg", () -> new SpawnEggItem(ModEntities.FEMALE_VILLAGER.get(), 9334376, 14664633, new Properties()));
   public static final Supplier<SpawnEggItem> KNIGHT_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_spawn_egg", () -> new SpawnEggItem(ModEntities.KNIGHT.get(), 8028041, 13883357, new Properties()));
   public static final Supplier<SpawnEggItem> KNIGHT_SHIELD_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_shield_spawn_egg", () -> new SpawnEggItem(ModEntities.KNIGHT_SHIELD.get(), 7041918, 13028563, new Properties()));
   public static final Supplier<SpawnEggItem> KNIGHT_LEADER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "knight_leader_spawn_egg", () -> new SpawnEggItem(ModEntities.KNIGHT_LEADER.get(), 8226192, 14738407, new Properties()));
   public static final Supplier<SpawnEggItem> EVIL_KNIGHT_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_spawn_egg", () -> new SpawnEggItem(ModEntities.EVIL_KNIGHT.get(), 5065557, 11838628, new Properties()));
   public static final Supplier<SpawnEggItem> EVIL_KNIGHT_SHIELD_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_shield_spawn_egg", () -> new SpawnEggItem(ModEntities.EVIL_KNIGHT_SHIELD.get(), 4145739, 10458774, new Properties()));
   public static final Supplier<SpawnEggItem> EVIL_KNIGHT_LEADER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_knight_leader_spawn_egg", () -> new SpawnEggItem(ModEntities.EVIL_KNIGHT_LEADER.get(), 4934230, 12628144, new Properties()));
   public static final Supplier<SpawnEggItem> VILLAGE_GUARD_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "village_guard_spawn_egg", () -> new SpawnEggItem(ModEntities.VILLAGE_GUARD.get(), 7304298, 7244373, new Properties()));
   public static final Supplier<SpawnEggItem> VILLAGE_GUARD_LEADER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "village_guard_leader_spawn_egg", () -> new SpawnEggItem(ModEntities.VILLAGE_GUARD_LEADER.get(), 5725539, 13148746, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_PILLAGER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_pillager_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_PILLAGER.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_SWORDSMAN_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_swordsman_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_SWORDSMAN.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_ARCHER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_archer_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_ARCHER.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_HORSEMAN_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_horseman_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_HORSEMAN.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_RAVAGER_RIDER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_ravager_rider_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_RAVAGER_RIDER.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_VINDICATOR_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_vindicator_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_VINDICATOR.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_EVOKER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_evoker_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_EVOKER.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> HUMAN_WITCH_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "human_witch_spawn_egg", () -> new SpawnEggItem(ModEntities.HUMAN_WITCH.get(), 1380368, 9116186, new Properties()));
   public static final Supplier<SpawnEggItem> ORC_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "orc_spawn_egg", () -> new SpawnEggItem(ModEntities.ORC.get(), 3099183, 5917237, new Properties()));
   public static final Supplier<SpawnEggItem> ELF_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "elf_spawn_egg", () -> new SpawnEggItem(ModEntities.ELF.get(), 9420666, 14075018, new Properties()));
   public static final Supplier<SpawnEggItem> DWARF_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "dwarf_spawn_egg", () -> new SpawnEggItem(ModEntities.DWARF.get(), 9067051, 11575424, new Properties()));
   public static final Supplier<SpawnEggItem> BANDIT_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "bandit_spawn_egg", () -> new SpawnEggItem(ModEntities.BANDIT.get(), 3811876, 9117214, new Properties()));
   public static final Supplier<SpawnEggItem> SHADOW_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "shadow_spawn_egg", () -> new SpawnEggItem(ModEntities.SHADOW.get(), 1315860, 13223310, new Properties()));
   public static final Supplier<SpawnEggItem> EVIL_WIZARD_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "evil_wizard_spawn_egg", () -> new SpawnEggItem(ModEntities.EVIL_WIZARD.get(), 2758712, 8072383, new Properties()));
   public static final Supplier<WizardSpawnEggItem> WIZARD_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "wizard_spawn_egg", () -> new WizardSpawnEggItem(3877464, 12094184, new Properties()));
   public static final Supplier<HealerSpawnEggItem> HEALER_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "healer_spawn_egg", () -> new HealerSpawnEggItem(15986403, 14537922, new Properties()));
   public static final Supplier<VampireSpawnEggItem> VAMPIRE_SPAWN_EGG = NativeRegistry.register(BuiltInRegistries.ITEM, "vampire_spawn_egg", () -> new VampireSpawnEggItem(2820626, 9379116, new Properties()));
   public static final Supplier<WizardStaffItem> WIZARD_STAFF = NativeRegistry.register(BuiltInRegistries.ITEM, "wizard_staff", () -> new WizardStaffItem(Tiers.WOOD, new Properties().stacksTo(1).durability(500)));

   private ModItems() {
   }

   public static void init() { LUTE.get(); }
}
