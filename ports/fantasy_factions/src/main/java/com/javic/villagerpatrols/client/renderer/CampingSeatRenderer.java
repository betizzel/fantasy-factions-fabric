package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.CampingSeatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public final class CampingSeatRenderer extends EntityRenderer<CampingSeatEntity> {
   public CampingSeatRenderer(Context context) {
      super(context);
   }

   public boolean shouldRender(CampingSeatEntity entity, Frustum camera, double camX, double camY, double camZ) {
      return false;
   }

   public void render(CampingSeatEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
   }

   public ResourceLocation getTextureLocation(CampingSeatEntity entity) {
      return new ResourceLocation("textures/misc/empty.png");
   }
}
