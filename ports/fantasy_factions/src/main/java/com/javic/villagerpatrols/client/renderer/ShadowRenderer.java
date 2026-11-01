package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.ShadowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class ShadowRenderer extends KnightRenderer<ShadowEntity> {
   public ShadowRenderer(Context context) {
      super(context);
      this.addLayer(new ShadowEyesLayer(this));
   }
}
