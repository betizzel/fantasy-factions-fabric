package com.javic.villagerpatrols.client.model;

import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class WizardCastingHumanoidModel<T extends LivingEntity> extends HumanoidModel<T> {
   private final HumanoidModel<T> delegate;

   public WizardCastingHumanoidModel(HumanoidModel<T> delegate) {
      super(
         new ModelPart(
            List.of(),
            Map.of(
               "head",
               delegate.head,
               "hat",
               delegate.hat,
               "body",
               delegate.body,
               "right_arm",
               delegate.rightArm,
               "left_arm",
               delegate.leftArm,
               "right_leg",
               delegate.rightLeg,
               "left_leg",
               delegate.leftLeg
            )
         )
      );
      this.delegate = delegate;
   }

   public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
      this.delegate.attackTime = this.attackTime;
      this.delegate.riding = this.riding;
      this.delegate.young = this.young;
      this.delegate.crouching = this.crouching;
      this.delegate.swimAmount = this.swimAmount;
      this.delegate.leftArmPose = this.leftArmPose;
      this.delegate.rightArmPose = this.rightArmPose;
      this.delegate.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.delegate.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      if (WizardCastingClientState.isCasting(entity)) {
         applyCastingPose(this.delegate, entity);
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.delegate.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public static void applyCastingPose(HumanoidModel<?> model, LivingEntity entity) {
      boolean female = entity instanceof Mob mob && WizardEntitySupport.isFemale(mob);
      model.rightArm.xRot = (float)Math.toRadians(female ? -125.0 : -130.0);
      model.rightArm.yRot = (float)Math.toRadians(female ? 18.0 : 20.0);
      model.leftArm.xRot = (float)Math.toRadians(female ? -125.0 : -130.0);
      model.leftArm.yRot = (float)Math.toRadians(female ? -18.0 : -20.0);
   }
}
