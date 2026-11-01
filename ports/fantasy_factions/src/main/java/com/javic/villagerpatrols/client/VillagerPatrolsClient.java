package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.client.sound.LuteClientSoundManager;
import com.javic.villagerpatrols.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class VillagerPatrolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.register();
        ModNetworking.registerClient();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WizardParticleClientState.tick(client);
            LuteClientSoundManager.tick(client);
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            PrisonerPoseClientState.remove(entity.getId());
            WizardCastingClientState.remove(entity.getId());
            LuteClientSoundManager.remove(entity.getId());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearState());
    }

    public static void clearState() {
        PrisonerPoseClientState.clear();
        WizardCastingClientState.clear();
        LuteClientSoundManager.clear();
    }
}
