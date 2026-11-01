package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.client.model.FemaleVillagerModel;
import com.javic.villagerpatrols.client.model.FemaleWizardCastingModel;
import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;

public class FemaleWizardRenderer extends MobRenderer<FemaleWizardEntity, HumanoidModel<FemaleWizardEntity>> {
   public FemaleWizardRenderer(Context context) {
      super(context, new FemaleWizardCastingModel(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(new WizardCastingGlowLayer<>(this, WizardCastingClientState::isCasting));
   }

   public ResourceLocation getTextureLocation(FemaleWizardEntity entity) {
      return entity.getSkinTexture();
   }

   protected boolean shouldShowName(FemaleWizardEntity entity) {
      return false;
   }

   public void render(FemaleWizardEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      if (entity.getVehicle() instanceof Horse) {
         poseStack.translate(0.0F, -0.4F, 0.0F);
      } else if (entity.getVehicle() instanceof Camel) {
         poseStack.translate(0.0F, -0.3F, 0.0F);
      }

      poseStack.scale(0.95F, 0.95F, 0.95F);
      poseStack.scale(0.93F, 1.0F, 0.93F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
