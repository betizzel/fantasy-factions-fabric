package com.javic.villagerpatrols.entity;

import net.minecraft.resources.ResourceLocation;

public interface GenderedTextureEntity {
   boolean isFemale();

   ResourceLocation getSkinTexture();
}
