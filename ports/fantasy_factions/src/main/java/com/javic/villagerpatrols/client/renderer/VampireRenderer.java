package com.javic.villagerpatrols.client.renderer;

import com.javic.villagerpatrols.entity.VampireEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class VampireRenderer extends KnightRenderer<VampireEntity> {
   public VampireRenderer(Context context) {
      super(context);
      this.addLayer(new VampireEyesLayer(this, entity -> true));
   }
}
