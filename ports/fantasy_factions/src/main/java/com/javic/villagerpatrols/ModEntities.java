package com.javic.villagerpatrols;

import com.javic.villagerpatrols.entity.BanditEntity;
import com.javic.villagerpatrols.entity.CampingSeatEntity;
import com.javic.villagerpatrols.entity.DwarfEntity;
import com.javic.villagerpatrols.entity.ElfEntity;
import com.javic.villagerpatrols.entity.EvilKnightEntity;
import com.javic.villagerpatrols.entity.EvilKnightLeaderEntity;
import com.javic.villagerpatrols.entity.EvilKnightShieldEntity;
import com.javic.villagerpatrols.entity.EvilWizardEntity;
import com.javic.villagerpatrols.entity.FemaleHealerEntity;
import com.javic.villagerpatrols.entity.FemaleVampireVillagerEntity;
import com.javic.villagerpatrols.entity.FemaleVillagerEntity;
import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import com.javic.villagerpatrols.entity.HumanArcherEntity;
import com.javic.villagerpatrols.entity.HumanEvokerEntity;
import com.javic.villagerpatrols.entity.HumanHorsemanEntity;
import com.javic.villagerpatrols.entity.HumanPillagerEntity;
import com.javic.villagerpatrols.entity.HumanRavagerRiderEntity;
import com.javic.villagerpatrols.entity.HumanSwordsmanEntity;
import com.javic.villagerpatrols.entity.HumanVindicatorEntity;
import com.javic.villagerpatrols.entity.HumanWitchEntity;
import com.javic.villagerpatrols.entity.KnightEntity;
import com.javic.villagerpatrols.entity.KnightLeaderEntity;
import com.javic.villagerpatrols.entity.KnightShieldEntity;
import com.javic.villagerpatrols.entity.MaleHealerEntity;
import com.javic.villagerpatrols.entity.MaleVampireVillagerEntity;
import com.javic.villagerpatrols.entity.MaleVillagerEntity;
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import com.javic.villagerpatrols.entity.OrcEntity;
import com.javic.villagerpatrols.entity.ShadowEntity;
import com.javic.villagerpatrols.entity.ShadowRiderSpawnEntity;
import com.javic.villagerpatrols.entity.VampireEntity;
import com.javic.villagerpatrols.entity.VillageGuardEntity;
import com.javic.villagerpatrols.entity.VillageGuardLeaderEntity;
import com.javic.villagerpatrols.entity.WizardMagicProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModEntities {
   public static final Supplier<EntityType<MaleVillagerEntity>> MALE_VILLAGER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "male_villager", () -> Builder.of(MaleVillagerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "male_villager").toString()));
   public static final Supplier<EntityType<FemaleVillagerEntity>> FEMALE_VILLAGER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "female_villager", () -> Builder.of(FemaleVillagerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "female_villager").toString()));
   public static final Supplier<EntityType<HumanPillagerEntity>> HUMAN_PILLAGER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_pillager", () -> Builder.of(HumanPillagerEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_pillager").toString()));
   public static final Supplier<EntityType<HumanVindicatorEntity>> HUMAN_VINDICATOR = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_vindicator", () -> Builder.of(HumanVindicatorEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_vindicator").toString()));
   public static final Supplier<EntityType<HumanEvokerEntity>> HUMAN_EVOKER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_evoker", () -> Builder.of(HumanEvokerEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_evoker").toString()));
   public static final Supplier<EntityType<HumanSwordsmanEntity>> HUMAN_SWORDSMAN = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_swordsman", () -> Builder.of(HumanSwordsmanEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_swordsman").toString()));
   public static final Supplier<EntityType<HumanArcherEntity>> HUMAN_ARCHER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_archer", () -> Builder.of(HumanArcherEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_archer").toString()));
   public static final Supplier<EntityType<HumanHorsemanEntity>> HUMAN_HORSEMAN = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_horseman", () -> Builder.of(HumanHorsemanEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_horseman").toString()));
   public static final Supplier<EntityType<HumanRavagerRiderEntity>> HUMAN_RAVAGER_RIDER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_ravager_rider", () -> Builder.of(HumanRavagerRiderEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_ravager_rider").toString()));
   public static final Supplier<EntityType<HumanWitchEntity>> HUMAN_WITCH = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "human_witch", () -> Builder.of(HumanWitchEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "human_witch").toString()));
   public static final Supplier<EntityType<KnightEntity>> KNIGHT = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "knight", () -> Builder.of(KnightEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "knight").toString()));
   public static final Supplier<EntityType<KnightShieldEntity>> KNIGHT_SHIELD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "knight_shield", () -> Builder.of(KnightShieldEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "knight_shield").toString()));
   public static final Supplier<EntityType<KnightLeaderEntity>> KNIGHT_LEADER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "knight_leader", () -> Builder.of(KnightLeaderEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "knight_leader").toString()));
   public static final Supplier<EntityType<EvilKnightShieldEntity>> EVIL_KNIGHT_SHIELD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "evil_knight_shield", () -> Builder.of(EvilKnightShieldEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "evil_knight_shield").toString()));
   public static final Supplier<EntityType<EvilKnightEntity>> EVIL_KNIGHT = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "evil_knight", () -> Builder.of(EvilKnightEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "evil_knight").toString()));
   public static final Supplier<EntityType<EvilKnightLeaderEntity>> EVIL_KNIGHT_LEADER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "evil_knight_leader", () -> Builder.of(EvilKnightLeaderEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "evil_knight_leader").toString()));
   public static final Supplier<EntityType<VillageGuardEntity>> VILLAGE_GUARD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "village_guard", () -> Builder.of(VillageGuardEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "village_guard").toString()));
   public static final Supplier<EntityType<VillageGuardLeaderEntity>> VILLAGE_GUARD_LEADER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "village_guard_leader", () -> Builder.of(VillageGuardLeaderEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "village_guard_leader").toString()));
   public static final Supplier<EntityType<OrcEntity>> ORC = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "orc", () -> Builder.of(OrcEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "orc").toString()));
   public static final Supplier<EntityType<ElfEntity>> ELF = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "elf", () -> Builder.of(ElfEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "elf").toString()));
   public static final Supplier<EntityType<DwarfEntity>> DWARF = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "dwarf", () -> Builder.of(DwarfEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "dwarf").toString()));
   public static final Supplier<EntityType<BanditEntity>> BANDIT = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "bandit", () -> Builder.of(BanditEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "bandit").toString()));
   public static final Supplier<EntityType<ShadowEntity>> SHADOW = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "shadow", () -> Builder.of(ShadowEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "shadow").toString()));
   public static final Supplier<EntityType<ShadowRiderSpawnEntity>> SHADOW_RIDER_SPAWN = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "shadow_rider_spawn", () -> Builder.of(ShadowRiderSpawnEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "shadow_rider_spawn").toString()));
   public static final Supplier<EntityType<EvilWizardEntity>> EVIL_WIZARD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "evil_wizard", () -> Builder.of(EvilWizardEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "evil_wizard").toString()));
   public static final Supplier<EntityType<MaleWizardEntity>> MALE_WIZARD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "male_wizard", () -> Builder.of(MaleWizardEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "male_wizard").toString()));
   public static final Supplier<EntityType<FemaleWizardEntity>> FEMALE_WIZARD = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "female_wizard", () -> Builder.of(FemaleWizardEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "female_wizard").toString()));
   public static final Supplier<EntityType<MaleHealerEntity>> MALE_HEALER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "male_healer", () -> Builder.of(MaleHealerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "male_healer").toString()));
   public static final Supplier<EntityType<FemaleHealerEntity>> FEMALE_HEALER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "female_healer", () -> Builder.of(FemaleHealerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "female_healer").toString()));
   public static final Supplier<EntityType<VampireEntity>> VAMPIRE = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "vampire", () -> Builder.of(VampireEntity::new, MobCategory.MONSTER)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "vampire").toString()));
   public static final Supplier<EntityType<MaleVampireVillagerEntity>> MALE_VAMPIRE_VILLAGER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "male_vampire_villager", () -> Builder.of(MaleVampireVillagerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "male_vampire_villager").toString()));
   public static final Supplier<EntityType<FemaleVampireVillagerEntity>> FEMALE_VAMPIRE_VILLAGER = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "female_vampire_villager", () -> Builder.of(FemaleVampireVillagerEntity::new, MobCategory.CREATURE)
      .sized(0.6F, 1.95F)
      .build(new ResourceLocation("villagerpatrols", "female_vampire_villager").toString()));
   public static final Supplier<EntityType<WizardMagicProjectileEntity>> WIZARD_MAGIC_PROJECTILE = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "wizard_magic_projectile", () -> Builder.<WizardMagicProjectileEntity>of(WizardMagicProjectileEntity::new, MobCategory.MISC)
      .sized(1.0F, 1.0F)
      .clientTrackingRange(4)
      .updateInterval(10)
      .build(new ResourceLocation("villagerpatrols", "wizard_magic_projectile").toString()));
   public static final Supplier<EntityType<CampingSeatEntity>> CAMPING_SEAT = NativeRegistry.register(BuiltInRegistries.ENTITY_TYPE, "camping_seat", () -> Builder.of(CampingSeatEntity::new, MobCategory.MISC)
      .sized(0.01F, 0.01F)
      .clientTrackingRange(4)
      .updateInterval(20)
      .build(new ResourceLocation("villagerpatrols", "camping_seat").toString()));

   private ModEntities() {
   }

   public static void init() { MALE_VILLAGER.get(); }
}
