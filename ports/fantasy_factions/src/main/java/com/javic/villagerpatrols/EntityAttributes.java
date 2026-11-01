package com.javic.villagerpatrols;

import com.javic.villagerpatrols.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

final class EntityAttributes {
    private EntityAttributes() {}

    static void register() {
        FabricDefaultAttributeRegistry.register(ModEntities.MALE_VILLAGER.get(), MaleVillagerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FEMALE_VILLAGER.get(), FemaleVillagerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_PILLAGER.get(), HumanPillagerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_VINDICATOR.get(), HumanVindicatorEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_EVOKER.get(), HumanEvokerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_SWORDSMAN.get(), HumanSwordsmanEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_ARCHER.get(), HumanArcherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_HORSEMAN.get(), HumanHorsemanEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_RAVAGER_RIDER.get(), HumanRavagerRiderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.HUMAN_WITCH.get(), HumanWitchEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.KNIGHT.get(), KnightEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.KNIGHT_SHIELD.get(), KnightShieldEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.KNIGHT_LEADER.get(), KnightLeaderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.EVIL_KNIGHT.get(), EvilKnightEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.EVIL_KNIGHT_SHIELD.get(), EvilKnightShieldEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.EVIL_KNIGHT_LEADER.get(), EvilKnightLeaderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.VILLAGE_GUARD.get(), VillageGuardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.VILLAGE_GUARD_LEADER.get(), VillageGuardLeaderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.ORC.get(), OrcEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.ELF.get(), ElfEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DWARF.get(), DwarfEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.BANDIT.get(), BanditEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SHADOW.get(), ShadowEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SHADOW_RIDER_SPAWN.get(), ShadowEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.EVIL_WIZARD.get(), EvilWizardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MALE_WIZARD.get(), MaleWizardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FEMALE_WIZARD.get(), FemaleWizardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MALE_HEALER.get(), MaleHealerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FEMALE_HEALER.get(), FemaleHealerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.VAMPIRE.get(), VampireEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MALE_VAMPIRE_VILLAGER.get(), MaleVampireVillagerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.FEMALE_VAMPIRE_VILLAGER.get(), FemaleVampireVillagerEntity.createAttributes());
    }
}
