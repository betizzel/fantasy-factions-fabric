package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.HumanPillagerModelFemale;
import com.javic.villagerpatrols.client.model.HumanPillagerModelMale;
import com.javic.villagerpatrols.entity.HumanHorsemanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

public class HumanHorsemanRenderer extends MobRenderer<HumanHorsemanEntity, HumanoidModel<HumanHorsemanEntity>> {
   private final HumanoidModel<HumanHorsemanEntity> maleModel;
   private final HumanoidModel<HumanHorsemanEntity> femaleModel;

   public HumanHorsemanRenderer(Context context) {
      super(context, new HumanPillagerModelMale(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new HumanPillagerModelMale<>(context.bakeLayer(HumanPillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new HumanPillagerModelFemale<>(context.bakeLayer(HumanPillagerModelFemale.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(HumanHorsemanEntity entity) {
      return entity.getSkinTexture();
   }

   public void render(HumanHorsemanEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      this.model = entity.isFemale() ? this.femaleModel : this.maleModel;
      poseStack.pushPose();
      if (entity.getVehicle() instanceof Horse) {
         poseStack.translate(0.0F, entity.isFemale() ? -0.1F : -0.12F, 0.0F);
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
