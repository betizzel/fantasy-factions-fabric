package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.ModBlockEntities;
import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.ModParticles;
import com.javic.villagerpatrols.client.model.*;
import com.javic.villagerpatrols.client.particle.*;
import com.javic.villagerpatrols.client.renderer.*;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.VampireEntitySupport;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;

public final class ClientSetup {
    private ClientSetup() {}

    public static void register() {
        BlockEntityRendererRegistry.register(ModBlockEntities.WARD_BANNER.get(), WardBannerBlockEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.MALE_VILLAGER.get(), MaleVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.FEMALE_VILLAGER.get(), FemaleVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.KNIGHT.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.KNIGHT_SHIELD.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.KNIGHT_LEADER.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.EVIL_KNIGHT.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.EVIL_KNIGHT_SHIELD.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.EVIL_KNIGHT_LEADER.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.VILLAGE_GUARD.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.VILLAGE_GUARD_LEADER.get(), KnightRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_PILLAGER.get(), HumanPillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_SWORDSMAN.get(), HumanSwordsmanRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_ARCHER.get(), HumanArcherRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_HORSEMAN.get(), HumanHorsemanRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_RAVAGER_RIDER.get(), HumanRavagerRiderRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_VINDICATOR.get(), HumanVindicatorRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_EVOKER.get(), HumanEvokerRenderer::new);
        EntityRendererRegistry.register(ModEntities.HUMAN_WITCH.get(), HumanWitchRenderer::new);
        EntityRendererRegistry.register(ModEntities.ORC.get(), OrcRenderer::new);
        EntityRendererRegistry.register(ModEntities.ELF.get(), ElfRenderer::new);
        EntityRendererRegistry.register(ModEntities.DWARF.get(), DwarfRenderer::new);
        EntityRendererRegistry.register(ModEntities.BANDIT.get(), BanditRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHADOW.get(), ShadowRenderer::new);
        EntityRendererRegistry.register(ModEntities.EVIL_WIZARD.get(), EvilWizardRenderer::new);
        EntityRendererRegistry.register(ModEntities.MALE_WIZARD.get(), MaleWizardRenderer::new);
        EntityRendererRegistry.register(ModEntities.FEMALE_WIZARD.get(), FemaleWizardRenderer::new);
        EntityRendererRegistry.register(ModEntities.MALE_HEALER.get(), MaleVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.FEMALE_HEALER.get(), FemaleVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.VAMPIRE.get(), VampireRenderer::new);
        EntityRendererRegistry.register(ModEntities.MALE_VAMPIRE_VILLAGER.get(), MaleVampireVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.FEMALE_VAMPIRE_VILLAGER.get(), FemaleVampireVillagerRenderer::new);
        EntityRendererRegistry.register(ModEntities.WIZARD_MAGIC_PROJECTILE.get(), WizardMagicProjectileRenderer::new);
        EntityRendererRegistry.register(ModEntities.CAMPING_SEAT.get(), CampingSeatRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(CustomVillagerModelMale.LAYER_LOCATION, CustomVillagerModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FemaleVillagerModel.LAYER_LOCATION, FemaleVillagerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanPillagerModelMale.LAYER_LOCATION, HumanPillagerModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanPillagerModelFemale.LAYER_LOCATION, HumanPillagerModelFemale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanVindicatorModelMale.LAYER_LOCATION, HumanVindicatorModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanVindicatorModelFemale.LAYER_LOCATION, HumanVindicatorModelFemale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanEvokerModelMale.LAYER_LOCATION, HumanEvokerModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HumanEvokerModelFemale.LAYER_LOCATION, HumanEvokerModelFemale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(EvilWizardModelMale.LAYER_LOCATION, EvilWizardModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(EvilWizardModelFemale.LAYER_LOCATION, EvilWizardModelFemale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ElfModelMale.LAYER_LOCATION, ElfModelMale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ElfModelFemale.LAYER_LOCATION, ElfModelFemale::createBodyLayer);
        ParticleFactoryRegistry.getInstance().register(ModParticles.VAMPIRE_PORTAL.get(), VampirePortalParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.WIZARD_MAGIC.get(), WizardMagicParticle.Provider::new);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (SlimPatchCompat.isLoaded() && id != null && id.getNamespace().equals("slimpatch")
                && (id.getPath().equals("male_villager") || id.getPath().equals("female_villager"))
                && renderer.getModel() instanceof HumanoidModel<?>) {
                helper.register(new VampireEyesLayer(renderer, entity -> entity instanceof Mob mob && VampireEntitySupport.isVisualExternalVampireVillager(mob)));
                helper.register(new WizardCastingGlowLayer(renderer, entity -> entity instanceof Mob mob && WizardEntitySupport.isVisualExternalWizardVillager(mob) && WizardCastingClientState.isCasting(mob)));
            }
        });
    }
}
