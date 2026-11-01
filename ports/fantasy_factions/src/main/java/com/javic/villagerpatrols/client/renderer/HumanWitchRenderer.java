package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.FemaleVillagerModel;
import com.javic.villagerpatrols.entity.HumanWitchEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class HumanWitchRenderer extends MobRenderer<HumanWitchEntity, HumanoidModel<HumanWitchEntity>> {
   public HumanWitchRenderer(Context context) {
      super(context, new FemaleVillagerModel(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(HumanWitchEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanWitchEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.scale(0.95F, 0.95F, 0.95F);
      poseStack.scale(0.93F, 1.0F, 0.93F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
