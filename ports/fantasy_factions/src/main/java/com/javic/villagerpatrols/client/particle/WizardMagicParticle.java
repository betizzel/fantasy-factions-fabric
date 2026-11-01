package com.javic.villagerpatrols.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class WizardMagicParticle extends TextureSheetParticle {
   private static final float RED = 0.08F;
   private static final float GREEN = 0.18F;
   private static final float BLUE = 0.75F;
   private final double xStart;
   private final double yStart;
   private final double zStart;

   protected WizardMagicParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      super(level, x, y, z);
      this.xd = xSpeed;
      this.yd = ySpeed;
      this.zd = zSpeed;
      this.x = x;
      this.y = y;
      this.z = z;
      this.xStart = this.x;
      this.yStart = this.y;
      this.zStart = this.z;
      this.quadSize = 0.11F * (this.random.nextFloat() * 0.25F + 0.5F);
      float brightness = this.random.nextFloat() * 0.2F + 0.8F;
      this.rCol = 0.08F * brightness;
      this.gCol = 0.18F * brightness;
      this.bCol = 0.75F * brightness;
      this.lifetime = (int)(Math.random() * 10.0) + 36;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double x, double y, double z) {
      this.setBoundingBox(this.getBoundingBox().move(x, y, z));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float scaleFactor) {
      float progress = (this.age + scaleFactor) / this.lifetime;
      progress = 1.0F - progress;
      progress *= progress;
      progress = 1.0F - progress;
      return this.quadSize * progress;
   }

   public int getLightColor(float partialTick) {
      int light = super.getLightColor(partialTick);
      float progress = (float)this.age / this.lifetime;
      progress *= progress;
      progress *= progress;
      int blockLight = light & 0xFF;
      int skyLight = light >> 16 & 0xFF;
      skyLight += (int)(progress * 15.0F * 16.0F);
      if (skyLight > 240) {
         skyLight = 240;
      }

      return blockLight | skyLight << 16;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float progress = (float)this.age / this.lifetime;
         float curve = -progress + progress * progress * 2.0F;
         float inverseCurve = 1.0F - curve;
         this.x = this.xStart + this.xd * inverseCurve;
         this.y = this.yStart + this.yd * inverseCurve + (1.0F - progress);
         this.z = this.zStart + this.zd * inverseCurve;
         this.setPos(this.x, this.y, this.z);
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         WizardMagicParticle particle = new WizardMagicParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.pickSprite(this.sprite);
         return particle;
      }
   }
}
