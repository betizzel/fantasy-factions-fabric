package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.client.model.CustomVillagerModelMale;
import com.javic.villagerpatrols.client.model.MaleWizardCastingModel;
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;

public class MaleWizardRenderer extends MobRenderer<MaleWizardEntity, HumanoidModel<MaleWizardEntity>> {
   public MaleWizardRenderer(Context context) {
      super(context, new MaleWizardCastingModel(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(new WizardCastingGlowLayer<>(this, WizardCastingClientState::isCasting));
   }

   public ResourceLocation getTextureLocation(MaleWizardEntity entity) {
      return entity.getSkinTexture();
   }

   protected boolean shouldShowName(MaleWizardEntity entity) {
      return false;
   }

   public void render(MaleWizardEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      if (entity.getVehicle() instanceof Horse) {
         poseStack.translate(0.0F, -0.42F, 0.0F);
      } else if (entity.getVehicle() instanceof Camel) {
         poseStack.translate(0.0F, -0.32F, 0.0F);
      }

      poseStack.scale(1.0F, 1.0F, 0.995F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
