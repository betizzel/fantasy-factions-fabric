package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.block.entity.WardBannerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public final class WardBannerBlockEntityRenderer implements BlockEntityRenderer<WardBannerBlockEntity> {
   private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
   private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
   private final ModelPart cube;
   private final ModelPart glass;

   public WardBannerBlockEntityRenderer(Context context) {
      ModelPart modelPart = context.bakeLayer(ModelLayers.END_CRYSTAL);
      this.glass = modelPart.getChild("glass");
      this.cube = modelPart.getChild("cube");
   }

   public void render(
      WardBannerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay
   ) {
      if (blockEntity.getLevel() != null && blockEntity.shouldShowCrystal()) {
         float time = (float)blockEntity.getLevel().getGameTime() + partialTick;
         float bob = getY(time);
         float rotation = time * 3.0F;
         VertexConsumer vertexConsumer = bufferSource.getBuffer(RENDER_TYPE);
         int overlay = OverlayTexture.NO_OVERLAY;
         poseStack.pushPose();
         poseStack.translate(0.5F, 1.15F + bob * 0.08F, 0.5F);
         poseStack.scale(0.3F, 0.3F, 0.3F);
         poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
         poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
         this.glass.render(poseStack, vertexConsumer, packedLight, overlay);
         poseStack.scale(0.875F, 0.875F, 0.875F);
         poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
         poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
         this.glass.render(poseStack, vertexConsumer, packedLight, overlay);
         poseStack.scale(0.875F, 0.875F, 0.875F);
         poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
         poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
         this.cube.render(poseStack, vertexConsumer, packedLight, overlay);
         poseStack.popPose();
      }
   }

   private static float getY(float time) {
      float value = Mth.sin(time * 0.2F) / 2.0F + 0.5F;
      value = (value * value + value) * 0.4F;
      return value - 1.4F;
   }
}
