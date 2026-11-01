package com.javic.villagerpatrols.network;

import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.block.entity.WardBannerBlockEntity;
import com.javic.villagerpatrols.ward.WardMobRemovalService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class WardNetworking {
    private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0;

    private WardNetworking() {}

    public static void handleServerboundSettingsAction(ServerboundWardSettingsActionMessage message, ServerPlayer player) {
        Level level = player.level();
        if (!level.isLoaded(message.pos())) return;
        if (player.distanceToSqr(message.pos().getX() + 0.5, message.pos().getY() + 0.5, message.pos().getZ() + 0.5) > MAX_INTERACTION_DISTANCE_SQR) {
            if (message.action() == ServerboundWardSettingsActionMessage.Action.REMOVE_MOBS) {
                player.sendSystemMessage(Component.translatable("message.villagerpatrols.ward.remove_mobs.too_far"));
            }
            return;
        }
        if (!(level.getBlockEntity(message.pos()) instanceof WardBannerBlockEntity ward)) return;
        switch (message.action()) {
            case SET_MUTED -> ward.setMuted(message.value());
            case SET_SHOW_CRYSTAL -> ward.setShowCrystal(message.value());
            case SET_SHOW_PARTICLES -> ward.setShowParticles(message.value());
            case REMOVE_MOBS -> {
                long gameTime = level.getGameTime();
                if (gameTime < ward.getNextRemoveMobsTick()) {
                    long remainingTicks = ward.getNextRemoveMobsTick() - gameTime;
                    player.sendSystemMessage(Component.translatable("message.villagerpatrols.ward.remove_mobs.cooldown", Math.max(1L, (remainingTicks + 19L) / 20L)));
                    return;
                }
                int removed = WardMobRemovalService.removeMobs(player.serverLevel(), message.pos(), Config.wardBannerRadius());
                ward.setNextRemoveMobsTick(gameTime + 1200L);
                if (removed > 0) {
                    player.sendSystemMessage(Component.translatable("message.villagerpatrols.ward.remove_mobs.removed", removed));
                } else {
                    player.sendSystemMessage(Component.translatable("message.villagerpatrols.ward.remove_mobs.none"));
                }
            }
        }
        level.playSound(null, message.pos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
    }
}
