package com.javic.villagerpatrols.network;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class ModNetworking {
    static final ResourceLocation WIZARD_CASTING = id("wizard_casting");
    static final ResourceLocation PRISONER_RESTRAINED = id("prisoner_restrained");
    static final ResourceLocation LUTE_PLAYBACK = id("lute_playback");
    static final ResourceLocation OPEN_WARD_SCREEN = id("open_ward_screen");
    static final ResourceLocation WARD_SETTINGS = id("ward_settings");

    private ModNetworking() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation("villagerpatrols", path);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(WARD_SETTINGS, (server, player, handler, buffer, responseSender) -> {
            ServerboundWardSettingsActionMessage message = ServerboundWardSettingsActionMessage.decode(buffer);
            server.execute(() -> WardNetworking.handleServerboundSettingsAction(message, player));
        });
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            WizardCastingNetworking.onStartTracking(player, entity);
            PrisonerPoseNetworking.onStartTracking(player, entity);
        });
    }

    public static void registerClient() {
        ModClientNetworking.register();
    }

    public static void sendToServer(ServerboundWardSettingsActionMessage message) {
        ModClientNetworking.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundOpenWardScreenMessage message) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        ClientboundOpenWardScreenMessage.encode(message, buffer);
        ServerPlayNetworking.send(player, OPEN_WARD_SCREEN, buffer);
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundWizardCastingMessage message) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        ClientboundWizardCastingMessage.encode(message, buffer);
        ServerPlayNetworking.send(player, WIZARD_CASTING, buffer);
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundPrisonerRestrainedMessage message) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        ClientboundPrisonerRestrainedMessage.encode(message, buffer);
        ServerPlayNetworking.send(player, PRISONER_RESTRAINED, buffer);
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundLutePlaybackMessage message) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        ClientboundLutePlaybackMessage.encode(message, buffer);
        ServerPlayNetworking.send(player, LUTE_PLAYBACK, buffer);
    }

    public static void sendToTracking(Entity entity, ClientboundWizardCastingMessage message) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) sendToPlayer(player, message);
    }

    public static void sendToTracking(Entity entity, ClientboundPrisonerRestrainedMessage message) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) sendToPlayer(player, message);
    }

    public static void sendToTrackingAndSelf(Entity entity, ClientboundLutePlaybackMessage message) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) sendToPlayer(player, message);
        if (entity instanceof ServerPlayer player) sendToPlayer(player, message);
    }
}
