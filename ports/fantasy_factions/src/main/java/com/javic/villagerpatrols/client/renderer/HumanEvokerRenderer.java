package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanEvokerModelFemale;
import com.javic.villagerpatrols.client.model.HumanEvokerModelMale;
import com.javic.villagerpatrols.entity.HumanEvokerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanEvokerRenderer extends MobRenderer<HumanEvokerEntity, HumanoidModel<HumanEvokerEntity>> {
   private final HumanoidModel<HumanEvokerEntity> maleModel;
   private final HumanoidModel<HumanEvokerEntity> femaleModel;

   public HumanEvokerRenderer(Context context) {
      super(context, new HumanEvokerModelMale(context.bakeLayer(HumanEvokerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanEvokerModelMale<>(context.bakeLayer(HumanEvokerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanEvokerModelFemale<>(context.bakeLayer(HumanEvokerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanEvokerEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanEvokerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      boolean female = entity.isFemale();
      this.model = female ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      poseStack.scale(female ? 0.95F : 1.0F, female ? 0.95F : 1.0F, female ? 0.95F : 1.0F);
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
