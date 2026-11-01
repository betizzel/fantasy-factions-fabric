package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanPillagerModelFemale;
import com.javic.villagerpatrols.client.model.HumanPillagerModelMale;
import com.javic.villagerpatrols.entity.HumanArcherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanArcherRenderer extends MobRenderer<HumanArcherEntity, HumanoidModel<HumanArcherEntity>> {
   private final HumanoidModel<HumanArcherEntity> maleModel;
   private final HumanoidModel<HumanArcherEntity> femaleModel;

   public HumanArcherRenderer(Context context) {
      super(context, new HumanPillagerModelMale(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanArcherEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanArcherEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      this.model = entity.isFemale() ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      if (entity.isFemale()) {
         poseStack.scale(0.95F, 0.95F, 0.95F);
      } else {
         poseStack.scale(1.0F, 1.0F, 0.995F);
      }

      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
