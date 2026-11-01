package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.WizardMagicProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class WizardMagicProjectileRenderer extends EntityRenderer<WizardMagicProjectileEntity> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

   public WizardMagicProjectileRenderer(Context context) {
      super(context);
   }

   protected int getBlockLightLevel(WizardMagicProjectileEntity entity, BlockPos pos) {
      return 15;
   }

   public void render(WizardMagicProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.scale(2.0F, 2.0F, 2.0F);
      poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      Pose pose = poseStack.last();
      VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
      vertex(consumer, pose, packedLight, 0.0F, 0, 0, 1);
      vertex(consumer, pose, packedLight, 1.0F, 0, 1, 1);
      vertex(consumer, pose, packedLight, 1.0F, 1, 1, 0);
      vertex(consumer, pose, packedLight, 0.0F, 1, 0, 0);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }

   private static void vertex(VertexConsumer consumer, Pose pose, int packedLight, float x, int y, int u, int v) {
      consumer.vertex(pose.pose(), x - 0.5F, y - 0.25F, 0.0F)
         .color(255, 255, 255, 255)
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(packedLight)
         .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
         .endVertex();
   }

   public ResourceLocation getTextureLocation(WizardMagicProjectileEntity entity) {
      return TEXTURE_LOCATION;
   }
}
