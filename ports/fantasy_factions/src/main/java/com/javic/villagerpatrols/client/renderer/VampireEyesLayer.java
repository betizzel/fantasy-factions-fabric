package com.javic.villagerpatrols.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Predicate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class VampireEyesLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
   private static final RenderType VAMPIRE_EYES = RenderType.eyes(
      new ResourceLocation("villagerpatrols", "textures/entity/vampire/vampire_eyes.png")
   );
   private final Predicate<T> shouldRender;

   public VampireEyesLayer(RenderLayerParent<T, M> renderer, Predicate<T> shouldRender) {
      super(renderer);
      this.shouldRender = shouldRender;
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (this.shouldRender.test(entity)) {
         super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
      }
   }

   public RenderType renderType() {
      return VAMPIRE_EYES;
   }
}
