package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanVindicatorModelFemale;
import com.javic.villagerpatrols.client.model.HumanVindicatorModelMale;
import com.javic.villagerpatrols.entity.HumanVindicatorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanVindicatorRenderer extends MobRenderer<HumanVindicatorEntity, HumanoidModel<HumanVindicatorEntity>> {
   private final HumanoidModel<HumanVindicatorEntity> maleModel;
   private final HumanoidModel<HumanVindicatorEntity> femaleModel;

   public HumanVindicatorRenderer(Context context) {
      super(context, new HumanVindicatorModelMale(context.bakeLayer(HumanVindicatorModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanVindicatorModelMale<>(context.bakeLayer(HumanVindicatorModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanVindicatorModelFemale<>(context.bakeLayer(HumanVindicatorModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanVindicatorEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanVindicatorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      boolean female = entity.isFemale();
      this.model = female ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      poseStack.scale(female ? 0.95F : 1.0F, female ? 0.95F : 1.0F, female ? 0.95F : 1.0F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
