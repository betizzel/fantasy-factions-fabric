package com.javic.villagerpatrols;

import com.javic.villagerpatrols.entity.ShadowEntity;
import com.javic.villagerpatrols.entity.ShadowRiderMountSupport;
import com.javic.villagerpatrols.entity.WizardCompatEvents;
import com.javic.villagerpatrols.network.ModNetworking;
import com.javic.villagerpatrols.persistence.ServerTimeAccess;
import com.javic.villagerpatrols.skirmish.SkirmishManager;
import com.javic.villagerpatrols.sound.LutePlaybackManager;
import com.javic.villagerpatrols.spawn.SessionSpawnDelayManager;
import com.javic.villagerpatrols.village.VillageGuardManager;
import com.javic.villagerpatrols.ward.WardCommands;
import com.javic.villagerpatrols.worldevent.WorldEventManager;
import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

public final class VillagerPatrols implements ModInitializer {
    public static final String MODID = "villagerpatrols";
    public static final Logger LOGGER = LogUtils.getLogger();
    private final SkirmishManager skirmishes = new SkirmishManager();
    private final WorldEventManager worldEvents = new WorldEventManager();
    private final VillageGuardManager guards = new VillageGuardManager();
    private final LutePlaybackManager lute = new LutePlaybackManager();

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModItems.init();
        ModCreativeTabs.init();
        ModSounds.init();
        ModParticles.init();
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, Config.SPEC, "villagerpatrols.toml");
        EntityAttributes.register();
        FabricHooks.registerSpawnPlacements();
        NaturalBiomeSpawns.register();
        ModNetworking.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> WardCommands.register(dispatcher));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            WizardCompatEvents.onEntityInteract(player, entity, hand);
            return InteractionResult.PASS;
        });
        EntityTrackingEvents.START_TRACKING.register(lute::onStartTracking);
        ServerTickEvents.END_WORLD_TICK.register(this::onLevelTick);
        ServerWorldEvents.UNLOAD.register((server, level) -> {
            skirmishes.unload(level);
            worldEvents.unload(level);
        });
    }

    private void onLevelTick(ServerLevel level) {
        WizardCompatEvents.onLevelTick(level);
        if (!((ServerTimeAccess) level.getServer()).villagerpatrols$haveTime()) return;
        SessionSpawnDelayManager.update(level.getServer());
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Phantom phantom) ShadowRiderMountSupport.tickPhantomMount(phantom);
            if (entity instanceof ShadowEntity shadow) ShadowRiderMountSupport.tickMountedShadow(shadow);
        }
        guards.tick(level);
        skirmishes.tick(level);
        worldEvents.tick(level);
        lute.tick(level);
    }
}
