package com.javic.villagerpatrols.network;

import com.javic.villagerpatrols.client.PrisonerPoseClientState;
import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.client.screen.WardBannerScreen;
import com.javic.villagerpatrols.client.sound.LuteClientSoundManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

final class ModClientNetworking {
    private ModClientNetworking() {}

    static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.WIZARD_CASTING, (client, handler, buffer, responseSender) -> {
            ClientboundWizardCastingMessage message = ClientboundWizardCastingMessage.decode(buffer);
            client.execute(() -> WizardCastingClientState.handleMessage(message));
        });
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.PRISONER_RESTRAINED, (client, handler, buffer, responseSender) -> {
            ClientboundPrisonerRestrainedMessage message = ClientboundPrisonerRestrainedMessage.decode(buffer);
            client.execute(() -> PrisonerPoseClientState.handleMessage(message));
        });
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.LUTE_PLAYBACK, (client, handler, buffer, responseSender) -> {
            ClientboundLutePlaybackMessage message = ClientboundLutePlaybackMessage.decode(buffer);
            client.execute(() -> LuteClientSoundManager.handleMessage(message));
        });
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OPEN_WARD_SCREEN, (client, handler, buffer, responseSender) -> {
            ClientboundOpenWardScreenMessage message = ClientboundOpenWardScreenMessage.decode(buffer);
            client.execute(() -> WardBannerScreen.open(message));
        });
    }

    static void sendToServer(ServerboundWardSettingsActionMessage message) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        ServerboundWardSettingsActionMessage.encode(message, buffer);
        ClientPlayNetworking.send(ModNetworking.WARD_SETTINGS, buffer);
    }
}
