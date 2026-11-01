package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.CustomVillagerModelMale;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.Horse;

public class MaleVillagerRenderer extends MobRenderer<MaleVillagerEntity, HumanoidModel<MaleVillagerEntity>> {
   public MaleVillagerRenderer(Context context) {
      super(context, new CustomVillagerModelMale(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(MaleVillagerEntity entity) {
      return entity.getSkinTexture();
   }

   protected boolean shouldShowName(MaleVillagerEntity entity) {
      return false;
   }

   public void render(MaleVillagerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
