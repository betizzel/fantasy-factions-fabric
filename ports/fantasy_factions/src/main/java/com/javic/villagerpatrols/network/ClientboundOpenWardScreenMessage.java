package com.javic.villagerpatrols.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ClientboundOpenWardScreenMessage(BlockPos pos, boolean muted, boolean showCrystal, boolean showParticles) {
    public static void encode(ClientboundOpenWardScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeBoolean(message.muted);
        buffer.writeBoolean(message.showCrystal);
        buffer.writeBoolean(message.showParticles);
    }

    public static ClientboundOpenWardScreenMessage decode(FriendlyByteBuf buffer) {
        return new ClientboundOpenWardScreenMessage(buffer.readBlockPos(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
}
