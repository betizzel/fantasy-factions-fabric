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

public class WizardCastingGlowLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
   private static final RenderType WIZARD_CASTING_GLOW = RenderType.eyes(
      new ResourceLocation("villagerpatrols", "textures/entity/wizard/wizard_casting_glow.png")
   );
   private final Predicate<T> shouldRender;

   public WizardCastingGlowLayer(RenderLayerParent<T, M> renderer, Predicate<T> shouldRender) {
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
      return WIZARD_CASTING_GLOW;
   }
}
