package com.javic.villagerpatrols.client.model;

import com.javic.villagerpatrols.entity.EvilWizardEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class EvilWizardModelFemale extends HumanoidModel<EvilWizardEntity> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      new ResourceLocation("villagerpatrols", "evil_wizard_female"), "main"
   );

   public EvilWizardModelFemale(ModelPart root) {
      super(root);
   }

   public static LayerDefinition createBodyLayer() {
      return FemaleVillagerModel.createBodyLayer();
   }

   public void setupAnim(EvilWizardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      if (entity.isCastingFangs()) {
         this.rightArm.xRot = (float)Math.toRadians(-125.0);
         this.rightArm.yRot = (float)Math.toRadians(18.0);
         this.leftArm.xRot = (float)Math.toRadians(-125.0);
         this.leftArm.yRot = (float)Math.toRadians(-18.0);
      }
   }
}
