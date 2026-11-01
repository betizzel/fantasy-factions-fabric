package com.javic.villagerpatrols.client.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ShadowEyesLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
   private static final RenderType SHADOW_EYES = RenderType.eyes(
      new ResourceLocation("villagerpatrols", "textures/entity/shadow/shadow_eyes.png")
   );

   public ShadowEyesLayer(RenderLayerParent<T, M> renderer) {
      super(renderer);
   }

   public RenderType renderType() {
      return SHADOW_EYES;
   }
}
