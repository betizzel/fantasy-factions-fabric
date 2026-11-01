package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.OrcEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class OrcRenderer extends KnightRenderer<OrcEntity> {
   public OrcRenderer(Context context) {
      super(context);
   }

   public void render(OrcEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.scale(1.15F, 1.15F, 1.15F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
