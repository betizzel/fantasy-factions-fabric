package com.javic.villagerpatrols.network;

import net.minecraft.network.FriendlyByteBuf;

public record ClientboundWizardCastingMessage(int entityId, boolean casting) {
    public static void encode(ClientboundWizardCastingMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeBoolean(message.casting);
    }

    public static ClientboundWizardCastingMessage decode(FriendlyByteBuf buffer) {
        return new ClientboundWizardCastingMessage(buffer.readVarInt(), buffer.readBoolean());
    }
}
