package com.javic.villagerpatrols.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class PrisonerPoseHumanoidModel<T extends LivingEntity> extends HumanoidModel<T> {
   private final HumanoidModel<T> delegate;

   public PrisonerPoseHumanoidModel(HumanoidModel<T> delegate) {
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
      this.rightArm.xRot = (float)Math.toRadians(-45.0);
      this.rightArm.yRot = (float)Math.toRadians(-32.0);
      this.rightArm.zRot = (float)Math.toRadians(8.0);
      this.leftArm.xRot = (float)Math.toRadians(-45.0);
      this.leftArm.yRot = (float)Math.toRadians(32.0);
      this.leftArm.zRot = (float)Math.toRadians(-8.0);
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.delegate.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
   }
}
