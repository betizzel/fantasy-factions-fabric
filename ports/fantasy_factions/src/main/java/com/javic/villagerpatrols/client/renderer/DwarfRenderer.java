package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.DwarfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class DwarfRenderer extends KnightRenderer<DwarfEntity> {
   public DwarfRenderer(Context context) {
      super(context);
   }

   public void render(DwarfEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.scale(0.95F, 0.65F, 0.95F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
