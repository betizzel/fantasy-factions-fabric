package com.javic.villagerpatrols.client.model;

import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import net.minecraft.client.model.geom.ModelPart;

public class FemaleWizardCastingModel extends FemaleVillagerModel<FemaleWizardEntity> {
   public FemaleWizardCastingModel(ModelPart root) {
      super(root);
   }

   public void setupAnim(FemaleWizardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      if (WizardCastingClientState.isCasting(entity)) {
         WizardCastingHumanoidModel.applyCastingPose(this, entity);
      }
   }
}
