package com.javic.villagerpatrols;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraft.core.registries.BuiltInRegistries;

public final class Config {
   public static final Builder BUILDER = new Builder();
   public static final BooleanValue ENABLE_WORLD_EVENTS = BUILDER.comment(
         "Enables random world encounters such as travelers, camps, mounted groups, prisoners and trader meetings."
      )
      .translation("villagerpatrols.config.enableWorldEvents")
      .define("enableWorldEvents", true);
   public static final IntValue WORLD_EVENT_MIN_PLAYER_DISTANCE = BUILDER.comment("Minimum distance from the player where world events can appear.")
      .translation("villagerpatrols.config.worldEventMinPlayerDistance")
      .defineInRange("worldEventMinPlayerDistance", 16, 8, Integer.MAX_VALUE);
   public static final IntValue WORLD_EVENT_MAX_PLAYER_DISTANCE = BUILDER.comment("Maximum distance from the player where world events can appear.")
      .translation("villagerpatrols.config.worldEventMaxPlayerDistance")
      .defineInRange("worldEventMaxPlayerDistance", 35, 18, Integer.MAX_VALUE);
   public static final IntValue WORLD_EVENT_ATTEMPT_DELAY_MIN_TICKS = BUILDER.comment(
         "Minimum delay in ticks before trying to spawn another world event after a failed attempt."
      )
      .translation("villagerpatrols.config.worldEventAttemptDelayMinTicks")
      .defineInRange("worldEventAttemptDelayMinTicks", 2500, 100, Integer.MAX_VALUE);
   public static final IntValue WORLD_EVENT_ATTEMPT_DELAY_MAX_TICKS = BUILDER.comment(
         "Maximum delay in ticks before trying to spawn another world event after a failed attempt."
      )
      .translation("villagerpatrols.config.worldEventAttemptDelayMaxTicks")
      .defineInRange("worldEventAttemptDelayMaxTicks", 3600, 100, Integer.MAX_VALUE);
   public static final IntValue WORLD_EVENT_SUCCESS_COOLDOWN_MIN_TICKS = BUILDER.comment("Minimum cooldown in ticks after a world event successfully spawns.")
      .translation("villagerpatrols.config.worldEventSuccessCooldownMinTicks")
      .defineInRange("worldEventSuccessCooldownMinTicks", 5200, 100, Integer.MAX_VALUE);
   public static final IntValue WORLD_EVENT_SUCCESS_COOLDOWN_MAX_TICKS = BUILDER.comment("Maximum cooldown in ticks after a world event successfully spawns.")
      .translation("villagerpatrols.config.worldEventSuccessCooldownMaxTicks")
      .defineInRange("worldEventSuccessCooldownMaxTicks", 6000, 100, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_TRAVELER_GROUP_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableTravelerGroupWorldEvent")
      .define("enableTravelerGroupWorldEvent", true);
   public static final IntValue TRAVELER_GROUP_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.travelerGroupWorldEventWeight")
      .defineInRange("travelerGroupWorldEventWeight", 6, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ARMED_TRAVELER_GROUP_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableArmedTravelerGroupWorldEvent")
      .define("enableArmedTravelerGroupWorldEvent", true);
   public static final IntValue ARMED_TRAVELER_GROUP_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.armedTravelerGroupWorldEventWeight")
      .defineInRange("armedTravelerGroupWorldEventWeight", 6, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_TRAVELER_FAMILY_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableTravelerFamilyWorldEvent")
      .define("enableTravelerFamilyWorldEvent", true);
   public static final IntValue TRAVELER_FAMILY_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.travelerFamilyWorldEventWeight")
      .defineInRange("travelerFamilyWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_LONE_DESERT_TRAVELER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableLoneDesertTravelerWorldEvent")
      .define("enableLoneDesertTravelerWorldEvent", true);
   public static final IntValue LONE_DESERT_TRAVELER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.loneDesertTravelerWorldEventWeight")
      .defineInRange("loneDesertTravelerWorldEventWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_LONE_ICE_TRAVELER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableLoneIceTravelerWorldEvent")
      .define("enableLoneIceTravelerWorldEvent", true);
   public static final IntValue LONE_ICE_TRAVELER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.loneIceTravelerWorldEventWeight")
      .defineInRange("loneIceTravelerWorldEventWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_VILLAGER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedVillagerWorldEvent")
      .define("enableMountedVillagerWorldEvent", true);
   public static final IntValue MOUNTED_VILLAGER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedVillagerWorldEventWeight")
      .defineInRange("mountedVillagerWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_KNIGHT_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedKnightWorldEvent")
      .define("enableMountedKnightWorldEvent", true);
   public static final IntValue MOUNTED_KNIGHT_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedKnightWorldEventWeight")
      .defineInRange("mountedKnightWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_EVIL_KNIGHT_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedEvilKnightWorldEvent")
      .define("enableMountedEvilKnightWorldEvent", true);
   public static final IntValue MOUNTED_EVIL_KNIGHT_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedEvilKnightWorldEventWeight")
      .defineInRange("mountedEvilKnightWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_ILLAGER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedIllagerWorldEvent")
      .define("enableMountedIllagerWorldEvent", true);
   public static final IntValue MOUNTED_ILLAGER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedIllagerWorldEventWeight")
      .defineInRange("mountedIllagerWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedRavagerIllagerWorldEvent")
      .define("enableMountedRavagerIllagerWorldEvent", true);
   public static final IntValue MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedRavagerIllagerWorldEventWeight")
      .defineInRange("mountedRavagerIllagerWorldEventWeight", 2, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_MOUNTED_CAMEL_VILLAGER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableMountedCamelVillagerWorldEvent")
      .define("enableMountedCamelVillagerWorldEvent", true);
   public static final IntValue MOUNTED_CAMEL_VILLAGER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.mountedCamelVillagerWorldEventWeight")
      .defineInRange("mountedCamelVillagerWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_CAMPING_VILLAGERS_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableCampingVillagersWorldEvent")
      .define("enableCampingVillagersWorldEvent", true);
   public static final IntValue CAMPING_VILLAGERS_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.campingVillagersWorldEventWeight")
      .defineInRange("campingVillagersWorldEventWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_BARD_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableBardWorldEvent")
      .define("enableBardWorldEvent", true);
   public static final IntValue BARD_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.bardWorldEventWeight")
      .defineInRange("bardWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_FISHING_VILLAGER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableFishingVillagerWorldEvent")
      .define("enableFishingVillagerWorldEvent", true);
   public static final IntValue FISHING_VILLAGER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.fishingVillagerWorldEventWeight")
      .defineInRange("fishingVillagerWorldEventWeight", 6, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_MINER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableVillagerMinerWorldEvent")
      .define("enableVillagerMinerWorldEvent", true);
   public static final IntValue VILLAGER_MINER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerMinerWorldEventWeight")
      .defineInRange("villagerMinerWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_SHEPHERD_MOVING_ANIMALS_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableShepherdMovingAnimalsWorldEvent")
      .define("enableShepherdMovingAnimalsWorldEvent", true);
   public static final IntValue SHEPHERD_MOVING_ANIMALS_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.shepherdMovingAnimalsWorldEventWeight")
      .defineInRange("shepherdMovingAnimalsWorldEventWeight", 7, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableShepherdDonkeyTravelerWorldEvent")
      .define("enableShepherdDonkeyTravelerWorldEvent", true);
   public static final IntValue SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.shepherdDonkeyTravelerWorldEventWeight")
      .defineInRange("shepherdDonkeyTravelerWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_DESERT_CAMEL_TRAVELER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableDesertCamelTravelerWorldEvent")
      .define("enableDesertCamelTravelerWorldEvent", true);
   public static final IntValue DESERT_CAMEL_TRAVELER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.desertCamelTravelerWorldEventWeight")
      .defineInRange("desertCamelTravelerWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_HEALER_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableHealerWorldEvent")
      .define("enableHealerWorldEvent", true);
   public static final IntValue HEALER_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.healerWorldEventWeight")
      .defineInRange("healerWorldEventWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_WANDERING_TRADER_MEETING_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableWanderingTraderMeetingWorldEvent")
      .define("enableWanderingTraderMeetingWorldEvent", true);
   public static final IntValue WANDERING_TRADER_MEETING_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.wanderingTraderMeetingWorldEventWeight")
      .defineInRange("wanderingTraderMeetingWorldEventWeight", 3, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_KNIGHT_PRISONERS_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableEvilKnightPrisonersWorldEvent")
      .define("enableEvilKnightPrisonersWorldEvent", true);
   public static final IntValue EVIL_KNIGHT_PRISONERS_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.evilKnightPrisonersWorldEventWeight")
      .defineInRange("evilKnightPrisonersWorldEventWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_HUMAN_WITCH_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableHumanWitchWorldEvent")
      .define("enableHumanWitchWorldEvent", true);
   public static final IntValue HUMAN_WITCH_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.humanWitchWorldEventWeight")
      .defineInRange("humanWitchWorldEventWeight", 2, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ZOMBIE_HORDE_WORLD_EVENT = BUILDER.comment("Enables or disables this world event.")
      .translation("villagerpatrols.config.enableZombieHordeWorldEvent")
      .define("enableZombieHordeWorldEvent", true);
   public static final IntValue ZOMBIE_HORDE_WORLD_EVENT_WEIGHT = BUILDER.comment(
         "Selection weight for this world event. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.zombieHordeWorldEventWeight")
      .defineInRange("zombieHordeWorldEventWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_SKIRMISHES = BUILDER.comment("Enables random battle encounters between groups.")
      .translation("villagerpatrols.config.enableSkirmishes")
      .define("enableSkirmishes", true);
   public static final IntValue SKIRMISH_MIN_PLAYER_DISTANCE = BUILDER.comment("Minimum distance from the player where skirmishes can appear.")
      .translation("villagerpatrols.config.skirmishMinPlayerDistance")
      .defineInRange("skirmishMinPlayerDistance", 25, 8, Integer.MAX_VALUE);
   public static final IntValue SKIRMISH_MAX_PLAYER_DISTANCE = BUILDER.comment("Maximum distance from the player where skirmishes can appear.")
      .translation("villagerpatrols.config.skirmishMaxPlayerDistance")
      .defineInRange("skirmishMaxPlayerDistance", 45, 25, Integer.MAX_VALUE);
   public static final IntValue SKIRMISH_ATTEMPT_DELAY_MIN_TICKS = BUILDER.comment(
         "Minimum delay in ticks before trying to spawn another skirmish after a failed attempt."
      )
      .translation("villagerpatrols.config.skirmishAttemptDelayMinTicks")
      .defineInRange("skirmishAttemptDelayMinTicks", 3600, 100, Integer.MAX_VALUE);
   public static final IntValue SKIRMISH_ATTEMPT_DELAY_MAX_TICKS = BUILDER.comment(
         "Maximum delay in ticks before trying to spawn another skirmish after a failed attempt."
      )
      .translation("villagerpatrols.config.skirmishAttemptDelayMaxTicks")
      .defineInRange("skirmishAttemptDelayMaxTicks", 6000, 100, Integer.MAX_VALUE);
   public static final IntValue SKIRMISH_SUCCESS_COOLDOWN_MIN_TICKS = BUILDER.comment("Minimum cooldown in ticks after a skirmish successfully spawns.")
      .translation("villagerpatrols.config.skirmishSuccessCooldownMinTicks")
      .defineInRange("skirmishSuccessCooldownMinTicks", 10000, 100, Integer.MAX_VALUE);
   public static final IntValue SKIRMISH_SUCCESS_COOLDOWN_MAX_TICKS = BUILDER.comment("Maximum cooldown in ticks after a skirmish successfully spawns.")
      .translation("villagerpatrols.config.skirmishSuccessCooldownMaxTicks")
      .defineInRange("skirmishSuccessCooldownMaxTicks", 12000, 100, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_KNIGHT_ILLAGER_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableKnightIllagerSkirmish")
      .define("enableKnightIllagerSkirmish", true);
   public static final IntValue KNIGHT_ILLAGER_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.knightIllagerSkirmishWeight")
      .defineInRange("knightIllagerSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_KNIGHT_EVIL_KNIGHT_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableKnightEvilKnightSkirmish")
      .define("enableKnightEvilKnightSkirmish", true);
   public static final IntValue KNIGHT_EVIL_KNIGHT_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.knightEvilKnightSkirmishWeight")
      .defineInRange("knightEvilKnightSkirmishWeight", 15, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_KNIGHT_ZOMBIE_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableKnightZombieSkirmish")
      .define("enableKnightZombieSkirmish", true);
   public static final IntValue KNIGHT_ZOMBIE_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.knightZombieSkirmishWeight")
      .defineInRange("knightZombieSkirmishWeight", 12, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_ILLAGER_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableVillagerIllagerSkirmish")
      .define("enableVillagerIllagerSkirmish", true);
   public static final IntValue VILLAGER_ILLAGER_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerIllagerSkirmishWeight")
      .defineInRange("villagerIllagerSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_ZOMBIE_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableVillagerZombieSkirmish")
      .define("enableVillagerZombieSkirmish", true);
   public static final IntValue VILLAGER_ZOMBIE_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerZombieSkirmishWeight")
      .defineInRange("villagerZombieSkirmishWeight", 15, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_SPIDER_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableVillagerSpiderSkirmish")
      .define("enableVillagerSpiderSkirmish", true);
   public static final IntValue VILLAGER_SPIDER_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerSpiderSkirmishWeight")
      .defineInRange("villagerSpiderSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_KNIGHT_ORC_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableKnightOrcSkirmish")
      .define("enableKnightOrcSkirmish", true);
   public static final IntValue KNIGHT_ORC_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.knightOrcSkirmishWeight")
      .defineInRange("knightOrcSkirmishWeight", 10, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ELF_ORC_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableElfOrcSkirmish")
      .define("enableElfOrcSkirmish", true);
   public static final IntValue ELF_ORC_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.elfOrcSkirmishWeight")
      .defineInRange("elfOrcSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_DWARF_ORC_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableDwarfOrcSkirmish")
      .define("enableDwarfOrcSkirmish", true);
   public static final IntValue DWARF_ORC_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.dwarfOrcSkirmishWeight")
      .defineInRange("dwarfOrcSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_KNIGHT_ORC_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableEvilKnightOrcSkirmish")
      .define("enableEvilKnightOrcSkirmish", true);
   public static final IntValue EVIL_KNIGHT_ORC_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.evilKnightOrcSkirmishWeight")
      .defineInRange("evilKnightOrcSkirmishWeight", 10, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_KNIGHT_BANDIT_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableKnightBanditSkirmish")
      .define("enableKnightBanditSkirmish", true);
   public static final IntValue KNIGHT_BANDIT_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.knightBanditSkirmishWeight")
      .defineInRange("knightBanditSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_BANDIT_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableVillagerBanditSkirmish")
      .define("enableVillagerBanditSkirmish", true);
   public static final IntValue VILLAGER_BANDIT_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerBanditSkirmishWeight")
      .defineInRange("villagerBanditSkirmishWeight", 6, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_KNIGHT_ELF_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableEvilKnightElfSkirmish")
      .define("enableEvilKnightElfSkirmish", true);
   public static final IntValue EVIL_KNIGHT_ELF_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.evilKnightElfSkirmishWeight")
      .defineInRange("evilKnightElfSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_KNIGHT_DWARF_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableEvilKnightDwarfSkirmish")
      .define("enableEvilKnightDwarfSkirmish", true);
   public static final IntValue EVIL_KNIGHT_DWARF_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.evilKnightDwarfSkirmishWeight")
      .defineInRange("evilKnightDwarfSkirmishWeight", 8, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_WIZARD_EVIL_WIZARD_SKIRMISH = BUILDER.comment("Enables or disables Wizards vs Evil Wizards skirmishes.")
      .translation("villagerpatrols.config.enableWizardEvilWizardSkirmish")
      .define("enableWizardEvilWizardSkirmish", true);
   public static final IntValue WIZARD_EVIL_WIZARD_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for Wizards vs Evil Wizards skirmishes. Higher values make them more likely to be selected. Set to 0 to disable them."
      )
      .translation("villagerpatrols.config.wizardEvilWizardSkirmishWeight")
      .defineInRange("wizardEvilWizardSkirmishWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGER_ORC_SKIRMISH = BUILDER.comment("Enables or disables this skirmish.")
      .translation("villagerpatrols.config.enableVillagerOrcSkirmish")
      .define("enableVillagerOrcSkirmish", true);
   public static final IntValue VILLAGER_ORC_SKIRMISH_WEIGHT = BUILDER.comment(
         "Selection weight for this skirmish. Higher values make it more likely to be selected. Set to 0 to disable it."
      )
      .translation("villagerpatrols.config.villagerOrcSkirmishWeight")
      .defineInRange("villagerOrcSkirmishWeight", 6, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_NATURAL_SPAWNS = BUILDER.comment("Enables natural spawns added by this mod where supported.")
      .translation("villagerpatrols.config.enableNaturalSpawns")
      .define("enableNaturalSpawns", true);
   public static final BooleanValue ENABLE_KNIGHT_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for knight.")
      .translation("villagerpatrols.config.enableKnightNaturalSpawns")
      .define("enableKnightNaturalSpawns", true);
   public static final IntValue KNIGHT_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for knight. Higher values make it more common. Set to 0 to disable it where supported."
      )
      .translation("villagerpatrols.config.knightNaturalSpawnWeight")
      .defineInRange("knightNaturalSpawnWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_KNIGHT_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for evil knight.")
      .translation("villagerpatrols.config.enableEvilKnightNaturalSpawns")
      .define("enableEvilKnightNaturalSpawns", true);
   public static final IntValue EVIL_KNIGHT_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for evil knight. Higher values make it more common. Set to 0 to disable it where supported."
      )
      .translation("villagerpatrols.config.evilKnightNaturalSpawnWeight")
      .defineInRange("evilKnightNaturalSpawnWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ORC_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for orc.")
      .translation("villagerpatrols.config.enableOrcNaturalSpawns")
      .define("enableOrcNaturalSpawns", true);
   public static final IntValue ORC_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for orc. Higher values make it more common. Set to 0 to disable it where supported."
      )
      .translation("villagerpatrols.config.orcNaturalSpawnWeight")
      .defineInRange("orcNaturalSpawnWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_HUMAN_PILLAGER_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for human pillager.")
      .translation("villagerpatrols.config.enableHumanPillagerNaturalSpawns")
      .define("enableHumanPillagerNaturalSpawns", true);
   public static final IntValue HUMAN_PILLAGER_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for human pillager. Higher values make it more common. Set to 0 to disable it where supported."
      )
      .translation("villagerpatrols.config.humanPillagerNaturalSpawnWeight")
      .defineInRange("humanPillagerNaturalSpawnWeight", 4, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ILLAGER_SWORDSMAN_VARIANTS = BUILDER.comment(
         "Allows Illager Swordsmen to replace some Human Pillagers in natural illager patrols and illager skirmishes."
      )
      .translation("villagerpatrols.config.enableIllagerSwordsmanVariants")
      .define("enableIllagerSwordsmanVariants", true);
   public static final BooleanValue ENABLE_ILLAGER_ARCHER_VARIANTS = BUILDER.comment(
         "Allows Illager Archers to replace some Human Pillagers in natural illager patrols and illager skirmishes."
      )
      .translation("villagerpatrols.config.enableIllagerArcherVariants")
      .define("enableIllagerArcherVariants", true);
   public static final BooleanValue ENABLE_HUMAN_WITCH_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for human witch.")
      .translation("villagerpatrols.config.enableHumanWitchNaturalSpawns")
      .define("enableHumanWitchNaturalSpawns", true);
   public static final IntValue HUMAN_WITCH_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for human witch. Higher values make it more common. Set to 0 to disable it where supported."
      )
      .translation("villagerpatrols.config.humanWitchNaturalSpawnWeight")
      .defineInRange("humanWitchNaturalSpawnWeight", 15, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_ELF_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for elves.")
      .translation("villagerpatrols.config.enableElfNaturalSpawns")
      .define("enableElfNaturalSpawns", true);
   public static final IntValue ELF_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for elves. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.elfNaturalSpawnWeight")
      .defineInRange("elfNaturalSpawnWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_DWARF_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for dwarves.")
      .translation("villagerpatrols.config.enableDwarfNaturalSpawns")
      .define("enableDwarfNaturalSpawns", true);
   public static final IntValue DWARF_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for dwarves. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.dwarfNaturalSpawnWeight")
      .defineInRange("dwarfNaturalSpawnWeight", 5, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_BANDIT_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for bandits.")
      .translation("villagerpatrols.config.enableBanditNaturalSpawns")
      .define("enableBanditNaturalSpawns", true);
   public static final IntValue BANDIT_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for bandits. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.banditNaturalSpawnWeight")
      .defineInRange("banditNaturalSpawnWeight", 3, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_EVIL_WIZARD_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for evil wizards.")
      .translation("villagerpatrols.config.enableEvilWizardNaturalSpawns")
      .define("enableEvilWizardNaturalSpawns", true);
   public static final IntValue EVIL_WIZARD_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for evil wizards. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.evilWizardNaturalSpawnWeight")
      .defineInRange("evilWizardNaturalSpawnWeight", 20, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_WIZARD_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for wizards.")
      .translation("villagerpatrols.config.enableWizardNaturalSpawns")
      .define("enableWizardNaturalSpawns", true);
   public static final IntValue WIZARD_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for wizards. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.wizardNaturalSpawnWeight")
      .defineInRange("wizardNaturalSpawnWeight", 20, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VAMPIRE_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for vampires.")
      .translation("villagerpatrols.config.enableVampireNaturalSpawns")
      .define("enableVampireNaturalSpawns", true);
   public static final IntValue VAMPIRE_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for vampires. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.vampireNaturalSpawnWeight")
      .defineInRange("vampireNaturalSpawnWeight", 30, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_SHADOW_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for shadow.")
      .translation("villagerpatrols.config.enableShadowNaturalSpawns")
      .define("enableShadowNaturalSpawns", true);
   public static final IntValue SHADOW_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for shadow. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.shadowNaturalSpawnWeight")
      .defineInRange("shadowNaturalSpawnWeight", 100, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_SHADOW_RIDER_NATURAL_SPAWNS = BUILDER.comment("Enables or disables natural spawning for shadow rider.")
      .translation("villagerpatrols.config.enableShadowRiderNaturalSpawns")
      .define("enableShadowRiderNaturalSpawns", true);
   public static final IntValue SHADOW_RIDER_NATURAL_SPAWN_WEIGHT = BUILDER.comment(
         "Spawn weight for shadow rider. Higher values make them more common. Set to 0 to disable them where supported."
      )
      .translation("villagerpatrols.config.shadowRiderNaturalSpawnWeight")
      .defineInRange("shadowRiderNaturalSpawnWeight", 10, 0, Integer.MAX_VALUE);
   public static final BooleanValue ENABLE_VILLAGE_GUARDS = BUILDER.comment("Enables village guards near village bells.")
      .translation("villagerpatrols.config.enableVillageGuards")
      .define("enableVillageGuards", true);
   public static final BooleanValue ENABLE_WARD_BANNERS = BUILDER.comment("Enables ward banners that block Villager Patrols spawn content nearby.")
      .translation("villagerpatrols.config.enableWardBanners")
      .define("enableWardBanners", true);
   public static final IntValue INITIAL_SPAWN_DELAY_MINUTES = BUILDER.comment(
         "Initial delay in minutes before this mod allows natural and chunk generation spawn placement checks. Set to 0 to disable."
      )
      .translation("villagerpatrols.config.initialSpawnDelayMinutes")
      .defineInRange("initialSpawnDelayMinutes", 2, 0, Integer.MAX_VALUE);
   public static final IntValue INITIAL_WORLD_EVENT_AND_SKIRMISH_DELAY_MINUTES = BUILDER.comment(
         "Initial delay in minutes before this mod allows world events and skirmishes. Set to 0 to disable."
      )
      .translation("villagerpatrols.config.initialWorldEventAndSkirmishDelayMinutes")
      .defineInRange("initialWorldEventAndSkirmishDelayMinutes", 2, 0, Integer.MAX_VALUE);
   public static final IntValue WARD_BANNER_RADIUS = BUILDER.comment("Radius in blocks where the total ward banner blocks Villager Patrols spawn content.")
      .translation("villagerpatrols.config.wardBannerRadius")
      .defineInRange("wardBannerRadius", 96, 0, Integer.MAX_VALUE);
   public static final ForgeConfigSpec SPEC = BUILDER.build();

   private Config() {
   }

   public static int worldEventMinPlayerDistance() {
      return (Integer)WORLD_EVENT_MIN_PLAYER_DISTANCE.get();
   }

   public static int worldEventMaxPlayerDistance() {
      return Math.max((Integer)WORLD_EVENT_MAX_PLAYER_DISTANCE.get(), worldEventMinPlayerDistance());
   }

   public static int worldEventAttemptDelayMinTicks() {
      return (Integer)WORLD_EVENT_ATTEMPT_DELAY_MIN_TICKS.get();
   }

   public static int worldEventAttemptDelayMaxTicks() {
      return Math.max((Integer)WORLD_EVENT_ATTEMPT_DELAY_MAX_TICKS.get(), worldEventAttemptDelayMinTicks());
   }

   public static int worldEventSuccessCooldownMinTicks() {
      return (Integer)WORLD_EVENT_SUCCESS_COOLDOWN_MIN_TICKS.get();
   }

   public static int worldEventSuccessCooldownMaxTicks() {
      return Math.max((Integer)WORLD_EVENT_SUCCESS_COOLDOWN_MAX_TICKS.get(), worldEventSuccessCooldownMinTicks());
   }

   public static int skirmishMinPlayerDistance() {
      return (Integer)SKIRMISH_MIN_PLAYER_DISTANCE.get();
   }

   public static int skirmishMaxPlayerDistance() {
      return Math.max((Integer)SKIRMISH_MAX_PLAYER_DISTANCE.get(), skirmishMinPlayerDistance());
   }

   public static int skirmishAttemptDelayMinTicks() {
      return (Integer)SKIRMISH_ATTEMPT_DELAY_MIN_TICKS.get();
   }

   public static int skirmishAttemptDelayMaxTicks() {
      return Math.max((Integer)SKIRMISH_ATTEMPT_DELAY_MAX_TICKS.get(), skirmishAttemptDelayMinTicks());
   }

   public static int skirmishSuccessCooldownMinTicks() {
      return (Integer)SKIRMISH_SUCCESS_COOLDOWN_MIN_TICKS.get();
   }

   public static int skirmishSuccessCooldownMaxTicks() {
      return Math.max((Integer)SKIRMISH_SUCCESS_COOLDOWN_MAX_TICKS.get(), skirmishSuccessCooldownMinTicks());
   }

   public static boolean isWorldEventEnabled(String groupType) {
      return switch (groupType) {
         case "traveler_group_world_event" -> ENABLE_TRAVELER_GROUP_WORLD_EVENT.get();
         case "armed_traveler_group_world_event" -> ENABLE_ARMED_TRAVELER_GROUP_WORLD_EVENT.get();
         case "traveler_family_world_event" -> ENABLE_TRAVELER_FAMILY_WORLD_EVENT.get();
         case "lone_desert_traveler_world_event" -> ENABLE_LONE_DESERT_TRAVELER_WORLD_EVENT.get();
         case "lone_ice_traveler_world_event" -> ENABLE_LONE_ICE_TRAVELER_WORLD_EVENT.get();
         case "mounted_villager_world_event" -> ENABLE_MOUNTED_VILLAGER_WORLD_EVENT.get();
         case "mounted_knight_world_event" -> ENABLE_MOUNTED_KNIGHT_WORLD_EVENT.get();
         case "mounted_evil_knight_world_event" -> ENABLE_MOUNTED_EVIL_KNIGHT_WORLD_EVENT.get();
         case "mounted_illager_world_event" -> ENABLE_MOUNTED_ILLAGER_WORLD_EVENT.get();
         case "mounted_ravager_illager_world_event" -> ENABLE_MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT.get();
         case "mounted_camel_villager_world_event" -> ENABLE_MOUNTED_CAMEL_VILLAGER_WORLD_EVENT.get();
         case "camping_villagers_world_event" -> ENABLE_CAMPING_VILLAGERS_WORLD_EVENT.get();
         case "bard_world_event" -> ENABLE_BARD_WORLD_EVENT.get();
         case "fishing_villager_world_event" -> ENABLE_FISHING_VILLAGER_WORLD_EVENT.get();
         case "villager_miner_world_event" -> ENABLE_VILLAGER_MINER_WORLD_EVENT.get();
         case "shepherd_moving_animals_world_event" -> ENABLE_SHEPHERD_MOVING_ANIMALS_WORLD_EVENT.get();
         case "shepherd_donkey_traveler_world_event" -> ENABLE_SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT.get();
         case "desert_camel_traveler_world_event" -> ENABLE_DESERT_CAMEL_TRAVELER_WORLD_EVENT.get();
         case "healer_world_event" -> ENABLE_HEALER_WORLD_EVENT.get();
         case "wandering_trader_meeting_world_event" -> ENABLE_WANDERING_TRADER_MEETING_WORLD_EVENT.get();
         case "evil_knight_prisoners_world_event" -> ENABLE_EVIL_KNIGHT_PRISONERS_WORLD_EVENT.get();
         case "human_witch_world_event" -> ENABLE_HUMAN_WITCH_WORLD_EVENT.get();
         case "zombie_horde_world_event" -> ENABLE_ZOMBIE_HORDE_WORLD_EVENT.get();
         default -> true;
      };
   }

   public static int worldEventWeight(String groupType) {
      if (!isWorldEventEnabled(groupType)) {
         return 0;
      }

      return switch (groupType) {
         case "traveler_group_world_event" -> TRAVELER_GROUP_WORLD_EVENT_WEIGHT.get();
         case "armed_traveler_group_world_event" -> ARMED_TRAVELER_GROUP_WORLD_EVENT_WEIGHT.get();
         case "traveler_family_world_event" -> TRAVELER_FAMILY_WORLD_EVENT_WEIGHT.get();
         case "lone_desert_traveler_world_event" -> LONE_DESERT_TRAVELER_WORLD_EVENT_WEIGHT.get();
         case "lone_ice_traveler_world_event" -> LONE_ICE_TRAVELER_WORLD_EVENT_WEIGHT.get();
         case "mounted_villager_world_event" -> MOUNTED_VILLAGER_WORLD_EVENT_WEIGHT.get();
         case "mounted_knight_world_event" -> MOUNTED_KNIGHT_WORLD_EVENT_WEIGHT.get();
         case "mounted_evil_knight_world_event" -> MOUNTED_EVIL_KNIGHT_WORLD_EVENT_WEIGHT.get();
         case "mounted_illager_world_event" -> MOUNTED_ILLAGER_WORLD_EVENT_WEIGHT.get();
         case "mounted_ravager_illager_world_event" -> MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT_WEIGHT.get();
         case "mounted_camel_villager_world_event" -> MOUNTED_CAMEL_VILLAGER_WORLD_EVENT_WEIGHT.get();
         case "camping_villagers_world_event" -> CAMPING_VILLAGERS_WORLD_EVENT_WEIGHT.get();
         case "bard_world_event" -> BARD_WORLD_EVENT_WEIGHT.get();
         case "fishing_villager_world_event" -> FISHING_VILLAGER_WORLD_EVENT_WEIGHT.get();
         case "villager_miner_world_event" -> VILLAGER_MINER_WORLD_EVENT_WEIGHT.get();
         case "shepherd_moving_animals_world_event" -> SHEPHERD_MOVING_ANIMALS_WORLD_EVENT_WEIGHT.get();
         case "shepherd_donkey_traveler_world_event" -> SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT_WEIGHT.get();
         case "desert_camel_traveler_world_event" -> DESERT_CAMEL_TRAVELER_WORLD_EVENT_WEIGHT.get();
         case "healer_world_event" -> HEALER_WORLD_EVENT_WEIGHT.get();
         case "wandering_trader_meeting_world_event" -> WANDERING_TRADER_MEETING_WORLD_EVENT_WEIGHT.get();
         case "evil_knight_prisoners_world_event" -> EVIL_KNIGHT_PRISONERS_WORLD_EVENT_WEIGHT.get();
         case "human_witch_world_event" -> HUMAN_WITCH_WORLD_EVENT_WEIGHT.get();
         case "zombie_horde_world_event" -> ZOMBIE_HORDE_WORLD_EVENT_WEIGHT.get();
         default -> 0;
      };
   }

   public static boolean isSkirmishEnabled(String groupType) {
      return switch (groupType) {
         case "knight_illager_skirmish" -> ENABLE_KNIGHT_ILLAGER_SKIRMISH.get();
         case "knight_evil_knight_skirmish" -> ENABLE_KNIGHT_EVIL_KNIGHT_SKIRMISH.get();
         case "knight_zombie_skirmish" -> ENABLE_KNIGHT_ZOMBIE_SKIRMISH.get();
         case "villager_illager_skirmish" -> ENABLE_VILLAGER_ILLAGER_SKIRMISH.get();
         case "villager_zombie_skirmish" -> ENABLE_VILLAGER_ZOMBIE_SKIRMISH.get();
         case "villager_spider_skirmish" -> ENABLE_VILLAGER_SPIDER_SKIRMISH.get();
         case "knight_orc_skirmish" -> ENABLE_KNIGHT_ORC_SKIRMISH.get();
         case "elf_orc_skirmish" -> ENABLE_ELF_ORC_SKIRMISH.get();
         case "dwarf_orc_skirmish" -> ENABLE_DWARF_ORC_SKIRMISH.get();
         case "evil_knight_orc_skirmish" -> ENABLE_EVIL_KNIGHT_ORC_SKIRMISH.get();
         case "knight_bandit_skirmish" -> ENABLE_KNIGHT_BANDIT_SKIRMISH.get();
         case "villager_bandit_skirmish" -> ENABLE_VILLAGER_BANDIT_SKIRMISH.get();
         case "evil_knight_elf_skirmish" -> ENABLE_EVIL_KNIGHT_ELF_SKIRMISH.get();
         case "evil_knight_dwarf_skirmish" -> ENABLE_EVIL_KNIGHT_DWARF_SKIRMISH.get();
         case "wizard_evil_wizard_skirmish" -> ENABLE_WIZARD_EVIL_WIZARD_SKIRMISH.get();
         case "villager_orc_skirmish" -> ENABLE_VILLAGER_ORC_SKIRMISH.get();
         default -> true;
      };
   }

   public static int skirmishWeight(String groupType) {
      if (!isSkirmishEnabled(groupType)) {
         return 0;
      }

      return switch (groupType) {
         case "knight_illager_skirmish" -> KNIGHT_ILLAGER_SKIRMISH_WEIGHT.get();
         case "knight_evil_knight_skirmish" -> KNIGHT_EVIL_KNIGHT_SKIRMISH_WEIGHT.get();
         case "knight_zombie_skirmish" -> KNIGHT_ZOMBIE_SKIRMISH_WEIGHT.get();
         case "villager_illager_skirmish" -> VILLAGER_ILLAGER_SKIRMISH_WEIGHT.get();
         case "villager_zombie_skirmish" -> VILLAGER_ZOMBIE_SKIRMISH_WEIGHT.get();
         case "villager_spider_skirmish" -> VILLAGER_SPIDER_SKIRMISH_WEIGHT.get();
         case "knight_orc_skirmish" -> KNIGHT_ORC_SKIRMISH_WEIGHT.get();
         case "elf_orc_skirmish" -> ELF_ORC_SKIRMISH_WEIGHT.get();
         case "dwarf_orc_skirmish" -> DWARF_ORC_SKIRMISH_WEIGHT.get();
         case "evil_knight_orc_skirmish" -> EVIL_KNIGHT_ORC_SKIRMISH_WEIGHT.get();
         case "knight_bandit_skirmish" -> KNIGHT_BANDIT_SKIRMISH_WEIGHT.get();
         case "villager_bandit_skirmish" -> VILLAGER_BANDIT_SKIRMISH_WEIGHT.get();
         case "evil_knight_elf_skirmish" -> EVIL_KNIGHT_ELF_SKIRMISH_WEIGHT.get();
         case "evil_knight_dwarf_skirmish" -> EVIL_KNIGHT_DWARF_SKIRMISH_WEIGHT.get();
         case "wizard_evil_wizard_skirmish" -> WIZARD_EVIL_WIZARD_SKIRMISH_WEIGHT.get();
         case "villager_orc_skirmish" -> VILLAGER_ORC_SKIRMISH_WEIGHT.get();
         default -> 0;
      };
   }

   public static boolean isNaturalSpawnEnabled(EntityType<?> entityType) {
      if (!(Boolean)ENABLE_NATURAL_SPAWNS.get()) {
         return false;
      } else if (isEntityType(entityType, "knight")) {
         return (Boolean)ENABLE_KNIGHT_NATURAL_SPAWNS.get() && (Integer)KNIGHT_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "evil_knight")) {
         return (Boolean)ENABLE_EVIL_KNIGHT_NATURAL_SPAWNS.get() && (Integer)EVIL_KNIGHT_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "orc")) {
         return (Boolean)ENABLE_ORC_NATURAL_SPAWNS.get() && (Integer)ORC_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "human_pillager")) {
         return (Boolean)ENABLE_HUMAN_PILLAGER_NATURAL_SPAWNS.get() && (Integer)HUMAN_PILLAGER_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "human_witch")) {
         return (Boolean)ENABLE_HUMAN_WITCH_NATURAL_SPAWNS.get() && (Integer)HUMAN_WITCH_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "elf")) {
         return (Boolean)ENABLE_ELF_NATURAL_SPAWNS.get() && (Integer)ELF_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "dwarf")) {
         return (Boolean)ENABLE_DWARF_NATURAL_SPAWNS.get() && (Integer)DWARF_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "bandit")) {
         return (Boolean)ENABLE_BANDIT_NATURAL_SPAWNS.get() && (Integer)BANDIT_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "evil_wizard")) {
         return (Boolean)ENABLE_EVIL_WIZARD_NATURAL_SPAWNS.get() && (Integer)EVIL_WIZARD_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "male_wizard")) {
         return (Boolean)ENABLE_WIZARD_NATURAL_SPAWNS.get() && (Integer)WIZARD_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "vampire")) {
         return (Boolean)ENABLE_VAMPIRE_NATURAL_SPAWNS.get() && (Integer)VAMPIRE_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else if (isEntityType(entityType, "shadow")) {
         return (Boolean)ENABLE_SHADOW_NATURAL_SPAWNS.get() && (Integer)SHADOW_NATURAL_SPAWN_WEIGHT.get() > 0;
      } else {
         return !isEntityType(entityType, "shadow_rider_spawn")
            ? (Boolean)ENABLE_NATURAL_SPAWNS.get()
            : (Boolean)ENABLE_SHADOW_RIDER_NATURAL_SPAWNS.get() && (Integer)SHADOW_RIDER_NATURAL_SPAWN_WEIGHT.get() > 0;
      }
   }

   public static int naturalSpawnWeight(EntityType<?> entityType) {
      if (!(Boolean)ENABLE_NATURAL_SPAWNS.get()) {
         return 0;
      } else if (isEntityType(entityType, "knight")) {
         return ENABLE_KNIGHT_NATURAL_SPAWNS.get() ? (Integer)KNIGHT_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "evil_knight")) {
         return ENABLE_EVIL_KNIGHT_NATURAL_SPAWNS.get() ? (Integer)EVIL_KNIGHT_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "orc")) {
         return ENABLE_ORC_NATURAL_SPAWNS.get() ? (Integer)ORC_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "human_pillager")) {
         return ENABLE_HUMAN_PILLAGER_NATURAL_SPAWNS.get() ? (Integer)HUMAN_PILLAGER_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "human_witch")) {
         return ENABLE_HUMAN_WITCH_NATURAL_SPAWNS.get() ? (Integer)HUMAN_WITCH_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "elf")) {
         return ENABLE_ELF_NATURAL_SPAWNS.get() ? (Integer)ELF_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "dwarf")) {
         return ENABLE_DWARF_NATURAL_SPAWNS.get() ? (Integer)DWARF_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "bandit")) {
         return ENABLE_BANDIT_NATURAL_SPAWNS.get() ? (Integer)BANDIT_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "evil_wizard")) {
         return ENABLE_EVIL_WIZARD_NATURAL_SPAWNS.get() ? (Integer)EVIL_WIZARD_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "male_wizard")) {
         return ENABLE_WIZARD_NATURAL_SPAWNS.get() ? (Integer)WIZARD_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "vampire")) {
         return ENABLE_VAMPIRE_NATURAL_SPAWNS.get() ? (Integer)VAMPIRE_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "shadow")) {
         return ENABLE_SHADOW_NATURAL_SPAWNS.get() ? (Integer)SHADOW_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else if (isEntityType(entityType, "shadow_rider_spawn")) {
         return ENABLE_SHADOW_RIDER_NATURAL_SPAWNS.get() ? (Integer)SHADOW_RIDER_NATURAL_SPAWN_WEIGHT.get() : 0;
      } else {
         return 0;
      }
   }

   public static int wardBannerRadius() {
      return Math.max(0, (Integer)WARD_BANNER_RADIUS.get());
   }

   public static int initialSpawnDelayTicks() {
      return Math.max(0, (Integer)INITIAL_SPAWN_DELAY_MINUTES.get()) * 20 * 60;
   }

   public static int initialWorldEventAndSkirmishDelayTicks() {
      return Math.max(0, (Integer)INITIAL_WORLD_EVENT_AND_SKIRMISH_DELAY_MINUTES.get()) * 20 * 60;
   }

   private static boolean isEntityType(EntityType<?> entityType, String path) {
      if (entityType == null) {
         return false;
      }

      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
      return key != null && "villagerpatrols".equals(key.getNamespace()) && path.equals(key.getPath());
   }

   static {
      BUILDER.translation("villagerpatrols.config.category.events").push("Events");
      BUILDER.pop();
      BUILDER.translation("villagerpatrols.config.category.skirmishes").push("Skirmishes");
      BUILDER.pop();
      BUILDER.translation("villagerpatrols.config.category.spawns").push("Spawns");
      BUILDER.pop();
      BUILDER.translation("villagerpatrols.config.category.wards").push("Wards");
      BUILDER.pop();
   }
}
