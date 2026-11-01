package com.javic.villagerpatrols.network;

import net.minecraft.network.FriendlyByteBuf;

public record ClientboundPrisonerRestrainedMessage(int entityId, boolean restrained) {
    public static void encode(ClientboundPrisonerRestrainedMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.restrained);
    }

    public static ClientboundPrisonerRestrainedMessage decode(FriendlyByteBuf buffer) {
        return new ClientboundPrisonerRestrainedMessage(buffer.readVarInt(), buffer.readBoolean());
    }
}
