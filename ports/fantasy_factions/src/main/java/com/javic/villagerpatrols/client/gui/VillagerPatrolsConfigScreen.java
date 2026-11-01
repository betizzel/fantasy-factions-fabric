package com.javic.villagerpatrols.client.gui;

import com.javic.villagerpatrols.Config;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public final class VillagerPatrolsConfigScreen extends Screen {
   private static final int ROW_HEIGHT = 24;
   private static final int TOP_MARGIN = 48;
   private static final int BOTTOM_MARGIN = 36;
   private static final int SCROLL_STEP = 24;
   private final Screen parent;
   private final VillagerPatrolsConfigScreen.ViewMode viewMode;
   private final VillagerPatrolsConfigScreen.Category category;
   private final List<VillagerPatrolsConfigScreen.OptionRow> rows = new ArrayList<>();
   private int scrollOffset;
   private int contentHeight;

   public VillagerPatrolsConfigScreen(Screen parent) {
      this(parent, VillagerPatrolsConfigScreen.ViewMode.ROOT, null);
   }

   private VillagerPatrolsConfigScreen(Screen parent, VillagerPatrolsConfigScreen.ViewMode viewMode, VillagerPatrolsConfigScreen.Category category) {
      super(resolveTitle(viewMode, category));
      this.parent = parent;
      this.viewMode = viewMode;
      this.category = category;
   }

   private static Component resolveTitle(VillagerPatrolsConfigScreen.ViewMode viewMode, VillagerPatrolsConfigScreen.Category category) {
      return viewMode == VillagerPatrolsConfigScreen.ViewMode.ROOT
         ? Component.literal("Villager Patrols Config")
         : Component.literal("Villager Patrols Config > " + (category == null ? "" : category.label));
   }

   protected void init() {
      super.init();
      this.rows.clear();
      this.scrollOffset = 0;
      if (this.viewMode == VillagerPatrolsConfigScreen.ViewMode.ROOT) {
         this.initRoot();
      } else {
         this.initCategory();
      }
   }

   private void initRoot() {
      int centerX = this.width / 2 - 100;
      int y = this.height / 2 - 52;
      this.addCategoryButton(centerX, y, VillagerPatrolsConfigScreen.Category.EVENTS);
      this.addCategoryButton(centerX, y + 28, VillagerPatrolsConfigScreen.Category.SKIRMISHES);
      this.addCategoryButton(centerX, y + 56, VillagerPatrolsConfigScreen.Category.SPAWNS);
      this.addCategoryButton(centerX, y + 84, VillagerPatrolsConfigScreen.Category.WARDS);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(centerX, this.height - 26, 200, 20).build());
   }

   private void addCategoryButton(int x, int y, VillagerPatrolsConfigScreen.Category targetCategory) {
      this.addRenderableWidget(
         Button.builder(
               Component.literal(targetCategory.label),
               button -> this.minecraft.setScreen(new VillagerPatrolsConfigScreen(this, VillagerPatrolsConfigScreen.ViewMode.CATEGORY, targetCategory))
            )
            .bounds(x, y, 200, 20)
            .build()
      );
   }

   private void initCategory() {
      if (this.category != null) {
         switch (this.category) {
            case EVENTS:
               this.buildEventsRows();
               break;
            case SKIRMISHES:
               this.buildSkirmishesRows();
               break;
            case SPAWNS:
               this.buildSpawnsRows();
               break;
            case WARDS:
               this.buildWardsRows();
         }

         this.contentHeight = this.rows.isEmpty() ? 0 : this.rows.get(this.rows.size() - 1).bottom();
         this.addRowWidgets();
         this.updateRowPositions();
         this.addRenderableWidget(
            Button.builder(Component.literal("Undo"), button -> this.undoCategory()).bounds(this.width / 2 - 152, this.height - 26, 98, 20).build()
         );
         this.addRenderableWidget(
            Button.builder(Component.literal("Reset"), button -> this.resetCategory()).bounds(this.width / 2 - 50, this.height - 26, 98, 20).build()
         );
         this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, button -> this.saveAndClose()).bounds(this.width / 2 + 52, this.height - 26, 98, 20).build()
         );
      }
   }

   private void buildEventsRows() {
      this.addBoolean("villagerpatrols.config.enableWorldEvents", Config.ENABLE_WORLD_EVENTS, true);
      this.addInt("villagerpatrols.config.worldEventMinPlayerDistance", Config.WORLD_EVENT_MIN_PLAYER_DISTANCE, 16, 8, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.worldEventMaxPlayerDistance", Config.WORLD_EVENT_MAX_PLAYER_DISTANCE, 35, 18, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.worldEventAttemptDelayMinTicks", Config.WORLD_EVENT_ATTEMPT_DELAY_MIN_TICKS, 2500, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.worldEventAttemptDelayMaxTicks", Config.WORLD_EVENT_ATTEMPT_DELAY_MAX_TICKS, 3600, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.worldEventSuccessCooldownMinTicks", Config.WORLD_EVENT_SUCCESS_COOLDOWN_MIN_TICKS, 5200, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.worldEventSuccessCooldownMaxTicks", Config.WORLD_EVENT_SUCCESS_COOLDOWN_MAX_TICKS, 6000, 100, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableTravelerGroupWorldEvent", Config.ENABLE_TRAVELER_GROUP_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.travelerGroupWorldEventWeight", Config.TRAVELER_GROUP_WORLD_EVENT_WEIGHT, 6, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableArmedTravelerGroupWorldEvent", Config.ENABLE_ARMED_TRAVELER_GROUP_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.armedTravelerGroupWorldEventWeight", Config.ARMED_TRAVELER_GROUP_WORLD_EVENT_WEIGHT, 6, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableTravelerFamilyWorldEvent", Config.ENABLE_TRAVELER_FAMILY_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.travelerFamilyWorldEventWeight", Config.TRAVELER_FAMILY_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableLoneDesertTravelerWorldEvent", Config.ENABLE_LONE_DESERT_TRAVELER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.loneDesertTravelerWorldEventWeight", Config.LONE_DESERT_TRAVELER_WORLD_EVENT_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableLoneIceTravelerWorldEvent", Config.ENABLE_LONE_ICE_TRAVELER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.loneIceTravelerWorldEventWeight", Config.LONE_ICE_TRAVELER_WORLD_EVENT_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedVillagerWorldEvent", Config.ENABLE_MOUNTED_VILLAGER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedVillagerWorldEventWeight", Config.MOUNTED_VILLAGER_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedKnightWorldEvent", Config.ENABLE_MOUNTED_KNIGHT_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedKnightWorldEventWeight", Config.MOUNTED_KNIGHT_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedEvilKnightWorldEvent", Config.ENABLE_MOUNTED_EVIL_KNIGHT_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedEvilKnightWorldEventWeight", Config.MOUNTED_EVIL_KNIGHT_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedIllagerWorldEvent", Config.ENABLE_MOUNTED_ILLAGER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedIllagerWorldEventWeight", Config.MOUNTED_ILLAGER_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedRavagerIllagerWorldEvent", Config.ENABLE_MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedRavagerIllagerWorldEventWeight", Config.MOUNTED_RAVAGER_ILLAGER_WORLD_EVENT_WEIGHT, 2, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableMountedCamelVillagerWorldEvent", Config.ENABLE_MOUNTED_CAMEL_VILLAGER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.mountedCamelVillagerWorldEventWeight", Config.MOUNTED_CAMEL_VILLAGER_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableCampingVillagersWorldEvent", Config.ENABLE_CAMPING_VILLAGERS_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.campingVillagersWorldEventWeight", Config.CAMPING_VILLAGERS_WORLD_EVENT_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableBardWorldEvent", Config.ENABLE_BARD_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.bardWorldEventWeight", Config.BARD_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableFishingVillagerWorldEvent", Config.ENABLE_FISHING_VILLAGER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.fishingVillagerWorldEventWeight", Config.FISHING_VILLAGER_WORLD_EVENT_WEIGHT, 6, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerMinerWorldEvent", Config.ENABLE_VILLAGER_MINER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.villagerMinerWorldEventWeight", Config.VILLAGER_MINER_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableShepherdMovingAnimalsWorldEvent", Config.ENABLE_SHEPHERD_MOVING_ANIMALS_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.shepherdMovingAnimalsWorldEventWeight", Config.SHEPHERD_MOVING_ANIMALS_WORLD_EVENT_WEIGHT, 7, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableShepherdDonkeyTravelerWorldEvent", Config.ENABLE_SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.shepherdDonkeyTravelerWorldEventWeight", Config.SHEPHERD_DONKEY_TRAVELER_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableDesertCamelTravelerWorldEvent", Config.ENABLE_DESERT_CAMEL_TRAVELER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.desertCamelTravelerWorldEventWeight", Config.DESERT_CAMEL_TRAVELER_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableHealerWorldEvent", Config.ENABLE_HEALER_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.healerWorldEventWeight", Config.HEALER_WORLD_EVENT_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableWanderingTraderMeetingWorldEvent", Config.ENABLE_WANDERING_TRADER_MEETING_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.wanderingTraderMeetingWorldEventWeight", Config.WANDERING_TRADER_MEETING_WORLD_EVENT_WEIGHT, 3, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilKnightPrisonersWorldEvent", Config.ENABLE_EVIL_KNIGHT_PRISONERS_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.evilKnightPrisonersWorldEventWeight", Config.EVIL_KNIGHT_PRISONERS_WORLD_EVENT_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableHumanWitchWorldEvent", Config.ENABLE_HUMAN_WITCH_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.humanWitchWorldEventWeight", Config.HUMAN_WITCH_WORLD_EVENT_WEIGHT, 2, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableZombieHordeWorldEvent", Config.ENABLE_ZOMBIE_HORDE_WORLD_EVENT, true);
      this.addInt("villagerpatrols.config.zombieHordeWorldEventWeight", Config.ZOMBIE_HORDE_WORLD_EVENT_WEIGHT, 5, 0, Integer.MAX_VALUE);
   }

   private void buildSkirmishesRows() {
      this.addBoolean("villagerpatrols.config.enableSkirmishes", Config.ENABLE_SKIRMISHES, true);
      this.addInt("villagerpatrols.config.skirmishMinPlayerDistance", Config.SKIRMISH_MIN_PLAYER_DISTANCE, 25, 8, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.skirmishMaxPlayerDistance", Config.SKIRMISH_MAX_PLAYER_DISTANCE, 45, 25, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.skirmishAttemptDelayMinTicks", Config.SKIRMISH_ATTEMPT_DELAY_MIN_TICKS, 3600, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.skirmishAttemptDelayMaxTicks", Config.SKIRMISH_ATTEMPT_DELAY_MAX_TICKS, 6000, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.skirmishSuccessCooldownMinTicks", Config.SKIRMISH_SUCCESS_COOLDOWN_MIN_TICKS, 10000, 100, Integer.MAX_VALUE);
      this.addInt("villagerpatrols.config.skirmishSuccessCooldownMaxTicks", Config.SKIRMISH_SUCCESS_COOLDOWN_MAX_TICKS, 12000, 100, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableKnightIllagerSkirmish", Config.ENABLE_KNIGHT_ILLAGER_SKIRMISH, true);
      this.addInt("villagerpatrols.config.knightIllagerSkirmishWeight", Config.KNIGHT_ILLAGER_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableKnightEvilKnightSkirmish", Config.ENABLE_KNIGHT_EVIL_KNIGHT_SKIRMISH, true);
      this.addInt("villagerpatrols.config.knightEvilKnightSkirmishWeight", Config.KNIGHT_EVIL_KNIGHT_SKIRMISH_WEIGHT, 15, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableKnightZombieSkirmish", Config.ENABLE_KNIGHT_ZOMBIE_SKIRMISH, true);
      this.addInt("villagerpatrols.config.knightZombieSkirmishWeight", Config.KNIGHT_ZOMBIE_SKIRMISH_WEIGHT, 12, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerIllagerSkirmish", Config.ENABLE_VILLAGER_ILLAGER_SKIRMISH, true);
      this.addInt("villagerpatrols.config.villagerIllagerSkirmishWeight", Config.VILLAGER_ILLAGER_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerZombieSkirmish", Config.ENABLE_VILLAGER_ZOMBIE_SKIRMISH, true);
      this.addInt("villagerpatrols.config.villagerZombieSkirmishWeight", Config.VILLAGER_ZOMBIE_SKIRMISH_WEIGHT, 15, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerSpiderSkirmish", Config.ENABLE_VILLAGER_SPIDER_SKIRMISH, true);
      this.addInt("villagerpatrols.config.villagerSpiderSkirmishWeight", Config.VILLAGER_SPIDER_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableKnightOrcSkirmish", Config.ENABLE_KNIGHT_ORC_SKIRMISH, true);
      this.addInt("villagerpatrols.config.knightOrcSkirmishWeight", Config.KNIGHT_ORC_SKIRMISH_WEIGHT, 10, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableElfOrcSkirmish", Config.ENABLE_ELF_ORC_SKIRMISH, true);
      this.addInt("villagerpatrols.config.elfOrcSkirmishWeight", Config.ELF_ORC_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableDwarfOrcSkirmish", Config.ENABLE_DWARF_ORC_SKIRMISH, true);
      this.addInt("villagerpatrols.config.dwarfOrcSkirmishWeight", Config.DWARF_ORC_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilKnightOrcSkirmish", Config.ENABLE_EVIL_KNIGHT_ORC_SKIRMISH, true);
      this.addInt("villagerpatrols.config.evilKnightOrcSkirmishWeight", Config.EVIL_KNIGHT_ORC_SKIRMISH_WEIGHT, 10, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableKnightBanditSkirmish", Config.ENABLE_KNIGHT_BANDIT_SKIRMISH, true);
      this.addInt("villagerpatrols.config.knightBanditSkirmishWeight", Config.KNIGHT_BANDIT_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerBanditSkirmish", Config.ENABLE_VILLAGER_BANDIT_SKIRMISH, true);
      this.addInt("villagerpatrols.config.villagerBanditSkirmishWeight", Config.VILLAGER_BANDIT_SKIRMISH_WEIGHT, 6, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilKnightElfSkirmish", Config.ENABLE_EVIL_KNIGHT_ELF_SKIRMISH, true);
      this.addInt("villagerpatrols.config.evilKnightElfSkirmishWeight", Config.EVIL_KNIGHT_ELF_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilKnightDwarfSkirmish", Config.ENABLE_EVIL_KNIGHT_DWARF_SKIRMISH, true);
      this.addInt("villagerpatrols.config.evilKnightDwarfSkirmishWeight", Config.EVIL_KNIGHT_DWARF_SKIRMISH_WEIGHT, 8, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableWizardEvilWizardSkirmish", Config.ENABLE_WIZARD_EVIL_WIZARD_SKIRMISH, true);
      this.addInt("villagerpatrols.config.wizardEvilWizardSkirmishWeight", Config.WIZARD_EVIL_WIZARD_SKIRMISH_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillagerOrcSkirmish", Config.ENABLE_VILLAGER_ORC_SKIRMISH, true);
      this.addInt("villagerpatrols.config.villagerOrcSkirmishWeight", Config.VILLAGER_ORC_SKIRMISH_WEIGHT, 6, 0, Integer.MAX_VALUE);
   }

   private void buildSpawnsRows() {
      this.addBoolean("villagerpatrols.config.enableNaturalSpawns", Config.ENABLE_NATURAL_SPAWNS, true);
      this.addBoolean("villagerpatrols.config.enableKnightNaturalSpawns", Config.ENABLE_KNIGHT_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.knightNaturalSpawnWeight", Config.KNIGHT_NATURAL_SPAWN_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilKnightNaturalSpawns", Config.ENABLE_EVIL_KNIGHT_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.evilKnightNaturalSpawnWeight", Config.EVIL_KNIGHT_NATURAL_SPAWN_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableOrcNaturalSpawns", Config.ENABLE_ORC_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.orcNaturalSpawnWeight", Config.ORC_NATURAL_SPAWN_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableHumanPillagerNaturalSpawns", Config.ENABLE_HUMAN_PILLAGER_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.humanPillagerNaturalSpawnWeight", Config.HUMAN_PILLAGER_NATURAL_SPAWN_WEIGHT, 4, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableIllagerSwordsmanVariants", Config.ENABLE_ILLAGER_SWORDSMAN_VARIANTS, true);
      this.addBoolean("villagerpatrols.config.enableIllagerArcherVariants", Config.ENABLE_ILLAGER_ARCHER_VARIANTS, true);
      this.addBoolean("villagerpatrols.config.enableHumanWitchNaturalSpawns", Config.ENABLE_HUMAN_WITCH_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.humanWitchNaturalSpawnWeight", Config.HUMAN_WITCH_NATURAL_SPAWN_WEIGHT, 15, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableElfNaturalSpawns", Config.ENABLE_ELF_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.elfNaturalSpawnWeight", Config.ELF_NATURAL_SPAWN_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableDwarfNaturalSpawns", Config.ENABLE_DWARF_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.dwarfNaturalSpawnWeight", Config.DWARF_NATURAL_SPAWN_WEIGHT, 5, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableBanditNaturalSpawns", Config.ENABLE_BANDIT_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.banditNaturalSpawnWeight", Config.BANDIT_NATURAL_SPAWN_WEIGHT, 3, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableEvilWizardNaturalSpawns", Config.ENABLE_EVIL_WIZARD_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.evilWizardNaturalSpawnWeight", Config.EVIL_WIZARD_NATURAL_SPAWN_WEIGHT, 20, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableWizardNaturalSpawns", Config.ENABLE_WIZARD_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.wizardNaturalSpawnWeight", Config.WIZARD_NATURAL_SPAWN_WEIGHT, 20, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVampireNaturalSpawns", Config.ENABLE_VAMPIRE_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.vampireNaturalSpawnWeight", Config.VAMPIRE_NATURAL_SPAWN_WEIGHT, 30, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableShadowNaturalSpawns", Config.ENABLE_SHADOW_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.shadowNaturalSpawnWeight", Config.SHADOW_NATURAL_SPAWN_WEIGHT, 100, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableShadowRiderNaturalSpawns", Config.ENABLE_SHADOW_RIDER_NATURAL_SPAWNS, true);
      this.addInt("villagerpatrols.config.shadowRiderNaturalSpawnWeight", Config.SHADOW_RIDER_NATURAL_SPAWN_WEIGHT, 10, 0, Integer.MAX_VALUE);
      this.addBoolean("villagerpatrols.config.enableVillageGuards", Config.ENABLE_VILLAGE_GUARDS, true);
   }

   private void buildWardsRows() {
      this.addBoolean("villagerpatrols.config.enableWardBanners", Config.ENABLE_WARD_BANNERS, true);
      this.addInt("villagerpatrols.config.initialSpawnDelayMinutes", Config.INITIAL_SPAWN_DELAY_MINUTES, 2, 0, Integer.MAX_VALUE);
      this.addInt(
         "villagerpatrols.config.initialWorldEventAndSkirmishDelayMinutes", Config.INITIAL_WORLD_EVENT_AND_SKIRMISH_DELAY_MINUTES, 2, 0, Integer.MAX_VALUE
      );
      this.addInt("villagerpatrols.config.wardBannerRadius", Config.WARD_BANNER_RADIUS, 96, 0, Integer.MAX_VALUE);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
      if (this.viewMode != VillagerPatrolsConfigScreen.ViewMode.CATEGORY) {
         return super.mouseScrolled(mouseX, mouseY, scrollY);
      } else {
         int nextOffset = Math.max(0, Math.min(this.getMaxScroll(), this.scrollOffset - (int)Math.signum(scrollY) * 24));
         if (nextOffset != this.scrollOffset) {
            this.scrollOffset = nextOffset;
            this.updateRowPositions();
            return true;
         } else {
            return super.mouseScrolled(mouseX, mouseY, scrollY);
         }
      }
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      int left = 12;
      int right = this.width - 12;
      guiGraphics.fill(left, 34, right, 35, -2130706433);
      guiGraphics.fill(left, this.height - 34, right, this.height - 33, -2141891243);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 16777215);
      super.render(guiGraphics, mouseX, mouseY, partialTick);

      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.renderLabel(guiGraphics);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   private void saveAndClose() {
      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.save();
      }

      Config.SPEC.save();
      this.onClose();
   }

   private void undoCategory() {
      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.undo();
      }
   }

   private void resetCategory() {
      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.reset();
      }
   }

   private void addBoolean(String translationKey, BooleanValue value, boolean defaultValue) {
      this.rows.add(new VillagerPatrolsConfigScreen.BooleanRow(Component.translatable(translationKey), this.nextRowTop(), value, defaultValue));
   }

   private void addInt(String translationKey, IntValue value, int defaultValue, int min, int max) {
      this.rows.add(new VillagerPatrolsConfigScreen.IntRow(Component.translatable(translationKey), this.nextRowTop(), value, defaultValue, min, max));
   }

   private int nextRowTop() {
      return this.rows.isEmpty() ? 48 : this.rows.get(this.rows.size() - 1).bottom() + 2;
   }

   private void addRowWidgets() {
      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.addWidgets();
      }
   }

   private void updateRowPositions() {
      for (VillagerPatrolsConfigScreen.OptionRow row : this.rows) {
         row.updatePosition(this.scrollOffset);
      }
   }

   private int getMaxScroll() {
      return Math.max(0, this.contentHeight - (this.height - 36));
   }

   private final class BooleanRow extends VillagerPatrolsConfigScreen.OptionRow {
      private final Button button;
      private final BooleanValue configValue;
      private final boolean originalValue;
      private final boolean defaultValue;
      private boolean value;

      private BooleanRow(Component label, int top, BooleanValue configValue, boolean defaultValue) {
         super(label, top);
         this.configValue = configValue;
         this.value = (Boolean)configValue.get();
         this.originalValue = this.value;
         this.defaultValue = defaultValue;
         this.button = Button.builder(this.getButtonText(), button -> {
            this.value = !this.value;
            button.setMessage(this.getButtonText());
         }).bounds(0, 0, 90, 20).build();
      }

      private Component getButtonText() {
         return Component.literal(this.value ? "ON" : "OFF");
      }

      @Override
      protected void addWidgets() {
         VillagerPatrolsConfigScreen.this.addRenderableWidget(this.button);
      }

      @Override
      protected void updatePosition(int scrollOffset) {
         this.y = this.top - scrollOffset;
         this.button.setX(VillagerPatrolsConfigScreen.this.width - 114);
         this.button.setY(this.y + 2);
         this.button.visible = this.y >= 40 && this.y <= VillagerPatrolsConfigScreen.this.height - 36;
         this.button.active = this.button.visible;
      }

      @Override
      protected void save() {
         this.configValue.set(this.value);
      }

      @Override
      protected void undo() {
         this.value = this.originalValue;
         this.button.setMessage(this.getButtonText());
      }

      @Override
      protected void reset() {
         this.value = this.defaultValue;
         this.button.setMessage(this.getButtonText());
      }
   }

   private enum Category {
      EVENTS("Events"),
      SKIRMISHES("Skirmishes"),
      SPAWNS("Spawns"),
      WARDS("Wards");

      private final String label;

      Category(String label) {
         this.label = label;
      }
   }

   private final class IntRow extends VillagerPatrolsConfigScreen.OptionRow {
      private final EditBox field;
      private final IntValue configValue;
      private final String originalValue;
      private final int defaultValue;
      private final int min;
      private final int max;

      private IntRow(Component label, int top, IntValue configValue, int defaultValue, int min, int max) {
         super(label, top);
         this.configValue = configValue;
         this.originalValue = Integer.toString((Integer)configValue.get());
         this.defaultValue = defaultValue;
         this.min = min;
         this.max = max;
         this.field = new EditBox(VillagerPatrolsConfigScreen.this.font, 0, 0, 90, 20, label);
         this.field.setValue(this.originalValue);
      }

      @Override
      protected void addWidgets() {
         VillagerPatrolsConfigScreen.this.addRenderableWidget(this.field);
      }

      @Override
      protected void updatePosition(int scrollOffset) {
         this.y = this.top - scrollOffset;
         this.field.setX(VillagerPatrolsConfigScreen.this.width - 114);
         this.field.setY(this.y + 2);
         this.field.visible = this.y >= 40 && this.y <= VillagerPatrolsConfigScreen.this.height - 36;
         this.field.setEditable(this.field.visible);
      }

      @Override
      protected void save() {
         this.configValue.set(this.parseValue(this.field.getValue(), (Integer)this.configValue.get()));
         this.field.setValue(Integer.toString((Integer)this.configValue.get()));
      }

      @Override
      protected void undo() {
         this.field.setValue(this.originalValue);
      }

      @Override
      protected void reset() {
         this.field.setValue(Integer.toString(this.defaultValue));
      }

      private int parseValue(String value, int fallback) {
         try {
            return this.clamp(Integer.parseInt(value.trim()));
         } catch (Exception ignored) {
            return fallback;
         }
      }

      private int clamp(int value) {
         return value < this.min ? this.min : Math.min(value, this.max);
      }
   }

   private abstract class OptionRow {
      protected final Component label;
      protected final int top;
      protected int y;

      protected OptionRow(Component label, int top) {
         this.label = label;
         this.top = top;
      }

      protected int height() {
         return 24;
      }

      protected int bottom() {
         return this.top + this.height();
      }

      protected abstract void addWidgets();

      protected abstract void updatePosition(int var1);

      protected abstract void save();

      protected abstract void undo();

      protected abstract void reset();

      protected void renderLabel(GuiGraphics guiGraphics) {
         if (this.y >= 40 && this.y <= VillagerPatrolsConfigScreen.this.height - 36) {
            guiGraphics.drawString(VillagerPatrolsConfigScreen.this.font, this.label, 24, this.y + 6, 16777215, false);
         }
      }
   }

   private enum ViewMode {
      ROOT,
      CATEGORY;
   }
}
