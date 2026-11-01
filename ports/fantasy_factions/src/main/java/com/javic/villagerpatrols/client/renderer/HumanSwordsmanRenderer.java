package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanPillagerModelFemale;
import com.javic.villagerpatrols.client.model.HumanPillagerModelMale;
import com.javic.villagerpatrols.entity.HumanSwordsmanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HumanSwordsmanRenderer extends MobRenderer<HumanSwordsmanEntity, HumanoidModel<HumanSwordsmanEntity>> {
   private final HumanoidModel<HumanSwordsmanEntity> maleModel;
   private final HumanoidModel<HumanSwordsmanEntity> femaleModel;

   public HumanSwordsmanRenderer(Context context) {
      super(context, new HumanPillagerModelMale(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(
         new HumanoidArmorLayer(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
   }

   public ResourceLocation getTextureLocation(HumanSwordsmanEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanSwordsmanEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
