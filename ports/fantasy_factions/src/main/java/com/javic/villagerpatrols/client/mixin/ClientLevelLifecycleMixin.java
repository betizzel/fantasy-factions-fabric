package com.javic.villagerpatrols.client.mixin;

import com.javic.villagerpatrols.client.VillagerPatrolsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class ClientLevelLifecycleMixin {
    @Shadow public ClientLevel level;

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void villagerpatrols$beforeLevelReplacement(ClientLevel newLevel, CallbackInfo ci) {
        if (level != newLevel) {
            VillagerPatrolsClient.clearState();
        }
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void villagerpatrols$beforeDisconnect(Screen screen, CallbackInfo ci) {
        VillagerPatrolsClient.clearState();
    }
}
