package com.javic.villagerpatrols.network;

import net.minecraft.network.FriendlyByteBuf;

public record ClientboundLutePlaybackMessage(int entityId, boolean active, int trackIndex, int sessionId) {
    public static void encode(ClientboundLutePlaybackMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.active);
        buffer.writeVarInt(message.trackIndex);
        buffer.writeVarInt(message.sessionId);
    }

    public static ClientboundLutePlaybackMessage decode(FriendlyByteBuf buffer) {
        return new ClientboundLutePlaybackMessage(buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt());
    }
}
