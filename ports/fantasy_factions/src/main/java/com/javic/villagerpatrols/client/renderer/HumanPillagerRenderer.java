package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanPillagerModelFemale;
import com.javic.villagerpatrols.client.model.HumanPillagerModelMale;
import com.javic.villagerpatrols.entity.HumanPillagerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanPillagerRenderer extends MobRenderer<HumanPillagerEntity, HumanoidModel<HumanPillagerEntity>> {
   private final HumanoidModel<HumanPillagerEntity> maleModel;
   private final HumanoidModel<HumanPillagerEntity> femaleModel;

   public HumanPillagerRenderer(Context context) {
      super(context, new HumanPillagerModelMale(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanPillagerEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanPillagerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      boolean female = entity.isFemale();
      this.model = female ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      if (female) {
         poseStack.scale(0.95F, 0.95F, 0.95F);
      } else {
         poseStack.scale(1.0F, 1.0F, 0.995F);
      }

      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
