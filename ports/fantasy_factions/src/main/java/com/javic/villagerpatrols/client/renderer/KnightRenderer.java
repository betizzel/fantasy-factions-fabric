package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.client.model.CustomVillagerModelMale;
import com.javic.villagerpatrols.client.model.FemaleVillagerModel;
import com.javic.villagerpatrols.entity.GenderedTextureEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.Horse;

public class KnightRenderer<T extends Mob & GenderedTextureEntity> extends MobRenderer<T, HumanoidModel<T>> {
   private final HumanoidModel<T> maleModel;
   private final HumanoidModel<T> femaleModel;

   public KnightRenderer(Context context) {
      super(context, new CustomVillagerModelMale(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION)), 0.5F);
      this.maleModel = new CustomVillagerModelMale(context.bakeLayer(CustomVillagerModelMale.LAYER_LOCATION));
      this.femaleModel = new FemaleVillagerModel(context.bakeLayer(FemaleVillagerModel.LAYER_LOCATION));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(T entity) {
      return entity.getSkinTexture();
   }

   public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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

      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
      poseStack.popPose();
   }
}
