package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanPillagerModelFemale;
import com.javic.villagerpatrols.client.model.HumanPillagerModelMale;
import com.javic.villagerpatrols.entity.HumanRavagerRiderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

public class HumanRavagerRiderRenderer extends MobRenderer<HumanRavagerRiderEntity, HumanoidModel<HumanRavagerRiderEntity>> {
   private final HumanoidModel<HumanRavagerRiderEntity> maleModel;
   private final HumanoidModel<HumanRavagerRiderEntity> femaleModel;

   public HumanRavagerRiderRenderer(Context context) {
      super(context, new HumanPillagerModelMale(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanRavagerRiderEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanRavagerRiderEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      this.model = entity.isFemale() ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      if (entity.getVehicle() instanceof Ravager) {
         poseStack.translate(0.0F, -0.15F, 0.0F);
      }

      if (entity.isFemale()) {
         poseStack.scale(0.95F, 0.95F, 0.95F);
      } else {
         poseStack.scale(1.0F, 1.0F, 0.995F);
      }

      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
