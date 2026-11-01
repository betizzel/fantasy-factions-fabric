package com.javic.villagerpatrols.client.mixin;

import com.javic.villagerpatrols.client.FantasyVillagerClientState;
import com.javic.villagerpatrols.client.PrisonerPoseClientState;
import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected M model;
    @Unique private M villagerpatrols$originalModel;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void villagerpatrols$beforeRender(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        villagerpatrols$originalModel = model;
        EntityModel<?> replacement = FantasyVillagerClientState.modelFor(entity, model);
        replacement = PrisonerPoseClientState.modelFor(entity, replacement);
        replacement = WizardCastingClientState.modelFor(entity, replacement);
        model = (M) replacement;
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void villagerpatrols$afterRender(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        model = villagerpatrols$originalModel;
        villagerpatrols$originalModel = null;
    }
}
