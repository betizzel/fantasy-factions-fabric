package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.ElfModelFemale;
import com.javic.villagerpatrols.client.model.ElfModelMale;
import com.javic.villagerpatrols.entity.ElfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

public class ElfRenderer extends MobRenderer<ElfEntity, HumanoidModel<ElfEntity>> {
   private final HumanoidModel<ElfEntity> maleModel;
   private final HumanoidModel<ElfEntity> femaleModel;

   public ElfRenderer(Context context) {
      super(context, new ElfModelMale(context.bakeLayer(ElfModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new ElfModelMale(context.bakeLayer(ElfModelMale.LAYER_LOCATION));
      this.femaleModel = new ElfModelFemale(context.bakeLayer(ElfModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(ElfEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(ElfEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      boolean female = entity.isFemale();
      this.model = female ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      if (entity.getVehicle() instanceof Horse) {
         poseStack.translate(0.0F, female ? -0.4F : -0.42F, 0.0F);
      }

      if (female) {
         poseStack.scale(0.95F, 0.95F, 0.95F);
         poseStack.scale(0.93F, 1.0F, 0.93F);
      } else {
         poseStack.scale(1.0F, 1.0F, 0.995F);
      }

      poseStack.scale(0.94F, 1.06F, 0.94F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
