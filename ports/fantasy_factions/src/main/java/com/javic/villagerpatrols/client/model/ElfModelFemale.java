package com.javic.villagerpatrols.client.model;

import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class ElfModelFemale<T extends LivingEntity> extends HumanoidModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      new ResourceLocation("villagerpatrols", "elf_female"), "main"
   );

   public ElfModelFemale(ModelPart root) {
      super(root);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition root = meshDefinition.getRoot();
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(32, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "left_ear",
         CubeListBuilder.create()
            .texOffs(1, 0)
            .addBox(0.0F, 0.0F, -3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(2, 1)
            .addBox(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(1, 0)
            .addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(4.7F, -5.0F, 2.0F, 0.0F, 0.3927F, 0.0F)
      );
      head.addOrReplaceChild(
         "right_ear",
         CubeListBuilder.create()
            .texOffs(1, 0)
            .addBox(-2.0F, 0.0F, -3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(2, 1)
            .addBox(-2.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(1, 0)
            .addBox(-2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-4.0F, -5.0F, 2.0F, 0.0F, -0.3927F, 0.0F)
      );
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(16, 16)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(16, 32)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.ZERO
      );
      body.addOrReplaceChild(
         "cube", CubeListBuilder.create().texOffs(20, 21).addBox(-3.0F, 2.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO
      );
      body.addOrReplaceChild(
         "cube2", CubeListBuilder.create().texOffs(23, 21).addBox(0.0F, 2.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO
      );
      root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create()
            .texOffs(40, 16)
            .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(40, 32)
            .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create()
            .texOffs(32, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(48, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.9F, 12.0F, 0.0F)
      );
      root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(1.9F, 12.0F, 0.0F)
      );
      root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
      return LayerDefinition.create(meshDefinition, 64, 64);
   }

   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      boolean isChild = entity.isBaby();
      ModelPart chestLeft = this.body.getChild("cube");
      ModelPart chestRight = this.body.getChild("cube2");
      chestLeft.visible = !isChild;
      chestRight.visible = !isChild;
      if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof CrossbowItem && entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
         this.rightArm.xRot = (float)Math.toRadians(-60.0);
         this.rightArm.yRot = 0.0F;
         this.rightArm.zRot = 0.0F;
         this.leftArm.xRot = (float)Math.toRadians(-30.0);
         this.leftArm.yRot = (float)Math.toRadians(-12.0);
         this.leftArm.zRot = (float)Math.toRadians(8.0);
      } else if (entity.getMainHandItem().getItem() instanceof CrossbowItem && entity instanceof Mob mob && mob.getTarget() != null) {
         this.rightArm.xRot = (float)Math.toRadians(-85.0);
         this.rightArm.yRot = 0.0F;
         this.rightArm.zRot = 0.0F;
         this.leftArm.xRot = (float)Math.toRadians(-28.0);
         this.leftArm.yRot = (float)Math.toRadians(12.0);
         this.leftArm.zRot = 0.0F;
      } else if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof BowItem && entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
         this.rightArm.xRot = (float)Math.toRadians(-90.0) + this.head.xRot;
         this.rightArm.yRot = this.head.yRot - 0.1F;
         this.rightArm.zRot = 0.0F;
         this.leftArm.xRot = (float)Math.toRadians(-70.0) + this.head.xRot;
         this.leftArm.yRot = this.head.yRot + 0.45F;
         this.leftArm.zRot = 0.0F;
      }

      if (entity instanceof FemaleVillagerEntity femaleVillager && femaleVillager.isPrisonerRestrained()) {
         this.rightArm.xRot = (float)Math.toRadians(-45.0);
         this.rightArm.yRot = (float)Math.toRadians(-32.0);
         this.rightArm.zRot = (float)Math.toRadians(8.0);
         this.leftArm.xRot = (float)Math.toRadians(-45.0);
         this.leftArm.yRot = (float)Math.toRadians(32.0);
         this.leftArm.zRot = (float)Math.toRadians(-8.0);
      }
   }
}
