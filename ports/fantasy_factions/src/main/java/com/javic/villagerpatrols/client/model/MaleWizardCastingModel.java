package com.javic.villagerpatrols.client.model;

import com.javic.villagerpatrols.client.WizardCastingClientState;
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import net.minecraft.client.model.geom.ModelPart;

public class MaleWizardCastingModel extends CustomVillagerModelMale<MaleWizardEntity> {
   public MaleWizardCastingModel(ModelPart root) {
      super(root);
   }

   public void setupAnim(MaleWizardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      if (WizardCastingClientState.isCasting(entity)) {
         WizardCastingHumanoidModel.applyCastingPose(this, entity);
      }
   }
}
