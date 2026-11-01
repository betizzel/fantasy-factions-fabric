package com.javic.villagerpatrols.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ServerboundWardSettingsActionMessage(BlockPos pos, Action action, boolean value) {
    public static void encode(ServerboundWardSettingsActionMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeVarInt(message.action.ordinal());
        buffer.writeBoolean(message.value);
    }

    public static ServerboundWardSettingsActionMessage decode(FriendlyByteBuf buffer) {
        return new ServerboundWardSettingsActionMessage(buffer.readBlockPos(), Action.fromId(buffer.readVarInt()), buffer.readBoolean());
    }

    public enum Action {
        SET_MUTED, SET_SHOW_CRYSTAL, SET_SHOW_PARTICLES, REMOVE_MOBS;

        private static Action fromId(int id) {
            Action[] values = values();
            return id >= 0 && id < values.length ? values[id] : SET_MUTED;
        }
    }
}
