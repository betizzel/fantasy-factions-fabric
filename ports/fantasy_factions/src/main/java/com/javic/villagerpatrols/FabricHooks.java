package com.javic.villagerpatrols;

import com.javic.villagerpatrols.entity.*;
import com.javic.villagerpatrols.faction.VillagerPatrolsFactions;
import com.javic.villagerpatrols.persistence.PersistentEntityData;
import com.javic.villagerpatrols.spawn.SessionSpawnDelayManager;
import com.javic.villagerpatrols.spawn.SpawnSurfaceValidator;
import com.javic.villagerpatrols.ward.WardBannerManager;
import com.javic.villagerpatrols.mixin.SpawnPlacementsInvoker;
import com.javic.villagerpatrols.worldevent.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class FabricHooks {
    private static final Map<SpawnerData, SpawnerData> REPLACEMENTS = new IdentityHashMap<>();

    private FabricHooks() {}

    public static void registerSpawnPlacements() {
        register(ModEntities.KNIGHT.get(), (type, level, reason, pos, random) -> Config.isNaturalSpawnEnabled(ModEntities.KNIGHT.get()) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random));
        register(ModEntities.KNIGHT_SHIELD.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.EVIL_KNIGHT.get(), (type, level, reason, pos, random) -> Config.isNaturalSpawnEnabled(ModEntities.EVIL_KNIGHT.get()) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random));
        register(ModEntities.EVIL_KNIGHT_SHIELD.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.KNIGHT_LEADER.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.EVIL_KNIGHT_LEADER.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.HUMAN_PILLAGER.get(), (type, level, reason, pos, random) -> Config.isNaturalSpawnEnabled(ModEntities.HUMAN_PILLAGER.get()) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random));
        register(ModEntities.HUMAN_VINDICATOR.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.HUMAN_EVOKER.get(), FabricHooks::checkNaturalMonster);
        register(ModEntities.HUMAN_WITCH.get(), (type, level, reason, pos, random) -> Config.isNaturalSpawnEnabled(ModEntities.HUMAN_WITCH.get()) && Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random));
        register(ModEntities.ORC.get(), (type, level, reason, pos, random) -> Config.isNaturalSpawnEnabled(ModEntities.ORC.get()) && OrcEntity.checkOrcSpawnRules(type, level, reason, pos, random));
        register(ModEntities.ELF.get(), ElfEntity::checkElfSpawnRules);
        register(ModEntities.DWARF.get(), DwarfEntity::checkDwarfSpawnRules);
        register(ModEntities.BANDIT.get(), BanditEntity::checkBanditSpawnRules);
        register(ModEntities.SHADOW.get(), ShadowEntity::checkShadowSpawnRules);
        register(ModEntities.SHADOW_RIDER_SPAWN.get(), ShadowRiderSpawnEntity::checkShadowRiderSpawnRules);
        register(ModEntities.EVIL_WIZARD.get(), EvilWizardEntity::checkEvilWizardSpawnRules);
        register(ModEntities.MALE_WIZARD.get(), MaleWizardEntity::checkWizardSpawnRules);
        register(ModEntities.VAMPIRE.get(), (type, level, reason, pos, random) -> {
            if (!Config.isNaturalSpawnEnabled(ModEntities.VAMPIRE.get()) || !Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) return false;
            boolean cave = pos.getY() <= 50 && !level.getLevel().canSeeSky(pos);
            return cave ? random.nextInt(3) == 0 : !level.getLevel().isDay();
        });
    }

    private static <T extends Mob> void register(EntityType<T> type, SpawnPlacements.SpawnPredicate<T> predicate) {
        SpawnPlacementsInvoker.villagerpatrols$register(type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate);
    }

    private static boolean checkNaturalMonster(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Config.ENABLE_NATURAL_SPAWNS.get() && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public static WeightedRandomList<SpawnerData> adjustPotentialSpawns(ServerLevel level, MobCategory category, WeightedRandomList<SpawnerData> original) {
        if (category != MobCategory.MONSTER && category != MobCategory.CREATURE) return original;
        List<SpawnerData> adjusted = null;
        for (SpawnerData entry : original.unwrap()) {
            if (!isNaturalType(entry.type)) continue;
            int weight = Config.naturalSpawnWeight(entry.type);
            if (weight == entry.getWeight().asInt()) continue;
            if (adjusted == null) adjusted = new ArrayList<>(original.unwrap());
            adjusted.remove(entry);
            if (weight > 0) {
                SpawnerData replacement = REPLACEMENTS.get(entry);
                if (replacement == null || replacement.getWeight().asInt() != weight) {
                    replacement = new SpawnerData(entry.type, weight, entry.minCount, entry.maxCount);
                    REPLACEMENTS.put(entry, replacement);
                }
                adjusted.add(replacement);
            }
        }
        return adjusted == null ? original : WeightedRandomList.create(adjusted);
    }

    public static boolean allowSpawnPlacement(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if ((reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) || !isNaturalType(type)) return true;
        ServerLevel server = level.getLevel();
        if (SessionSpawnDelayManager.isSpawnDelayActive(server.getServer()) || !Config.isNaturalSpawnEnabled(type)) return false;
        if (isHostile(type) && Math.max(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.BLOCK, pos.above())) >= 1) return false;
        if (WardBannerManager.blocksNaturalSpawn(type) && (WardBannerManager.isBlocked(server, pos) || !SpawnSurfaceValidator.hasValidSpawnSupport(level, pos))) return false;
        float chance = extraChance(type);
        return chance >= 1.0F || chance > 0.0F && random.nextFloat() < chance;
    }

    public static boolean replaceNaturalSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, CompoundTag tag) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return false;
        ServerLevel server = level.getLevel();
        if (mob.getType() == ModEntities.MALE_WIZARD.get()) {
            boolean female = level.getRandom().nextBoolean();
            EntityType<? extends Mob> type = WizardEntitySupport.resolveFriendlyType(server, female);
            if (female || type != mob.getType()) {
                Mob replacement = type.create(server);
                if (replacement != null) {
                    replacement.moveTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot());
                    replacement.finalizeSpawn(level, difficulty, reason, null, tag);
                    WizardEntitySupport.prepareSpawnedEntity(replacement, female, true);
                    server.addFreshEntity(replacement);
                    return true;
                }
            }
        } else if (mob.getType() == ModEntities.SHADOW_RIDER_SPAWN.get()) {
            return ShadowRiderMountSupport.spawnMountedShadowRider(server, mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot(), reason) != null;
        } else if (mob.getType() == ModEntities.VAMPIRE.get() && !level.getRandom().nextBoolean()) {
            VampireEntitySupport.SpawnChoice choice = VampireEntitySupport.chooseFriendlySpawn(server);
            Mob replacement = choice.type().create(server);
            if (replacement != null) {
                replacement.moveTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot());
                replacement.finalizeSpawn(level, difficulty, reason, null, tag);
                VampireEntitySupport.prepareSpawnedEntity(replacement, choice, true);
                server.addFreshEntity(replacement);
                return true;
            }
        }
        return false;
    }

    private static boolean isNaturalType(EntityType<?> t) {
        return t == ModEntities.KNIGHT.get() || t == ModEntities.EVIL_KNIGHT.get() || t == ModEntities.HUMAN_PILLAGER.get() || t == ModEntities.HUMAN_WITCH.get() || t == ModEntities.ORC.get() || t == ModEntities.ELF.get() || t == ModEntities.DWARF.get() || t == ModEntities.BANDIT.get() || t == ModEntities.SHADOW_RIDER_SPAWN.get() || t == ModEntities.EVIL_WIZARD.get() || t == ModEntities.MALE_WIZARD.get() || t == ModEntities.VAMPIRE.get() || t == ModEntities.SHADOW.get();
    }

    private static boolean isHostile(EntityType<?> t) {
        return t == ModEntities.EVIL_KNIGHT.get() || t == ModEntities.EVIL_KNIGHT_SHIELD.get() || t == ModEntities.EVIL_KNIGHT_LEADER.get() || t == ModEntities.HUMAN_PILLAGER.get() || t == ModEntities.HUMAN_VINDICATOR.get() || t == ModEntities.HUMAN_EVOKER.get() || t == ModEntities.HUMAN_WITCH.get() || t == ModEntities.ORC.get() || t == ModEntities.BANDIT.get() || t == ModEntities.SHADOW.get() || t == ModEntities.SHADOW_RIDER_SPAWN.get() || t == ModEntities.EVIL_WIZARD.get() || t == ModEntities.VAMPIRE.get();
    }

    private static float extraChance(EntityType<?> t) {
        if (t == ModEntities.KNIGHT.get() || t == ModEntities.EVIL_KNIGHT.get() || t == ModEntities.ELF.get()) return 0.95F;
        if (t == ModEntities.ORC.get() || t == ModEntities.DWARF.get()) return 0.9F;
        return t == ModEntities.BANDIT.get() ? 0.8F : 1.0F;
    }

    public static boolean cancelLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (entity.level().isClientSide || amount <= 0.0F) return false;
        if (entity.getVehicle() instanceof CampingSeatEntity seat) {
            entity.stopRiding();
            seat.discard();
        }
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (protectedDamage(attacker, entity) || protectedDamage(direct, entity)) return true;
        LivingEntity living = attacker instanceof LivingEntity a ? a : direct instanceof LivingEntity d ? d : null;
        ShadowRiderMountSupport.alertMountTarget(entity, living);
        return false;
    }

    public static LivingEntity onLivingChangeTarget(LivingEntity attacker, LivingEntity target) {
        if (attacker.level().isClientSide) return target;
        if (protectedTarget(attacker, target) || VillagerPatrolsFactions.shouldPreventFriendlyTargeting(attacker, target)) return null;
        boolean friendly = target instanceof KnightEntity || target instanceof KnightShieldEntity || target instanceof KnightLeaderEntity || target instanceof VillageGuardEntity || target instanceof VillageGuardLeaderEntity || target instanceof DwarfEntity || target instanceof ElfEntity;
        if (friendly && (attacker instanceof IronGolem || attacker instanceof Villager && !(attacker instanceof MaleVillagerEntity) && !(attacker instanceof FemaleVillagerEntity))) return null;
        return target;
    }

    private static boolean protectedDamage(Entity attacker, LivingEntity target) {
        return attacker instanceof LivingEntity living && protectedTarget(living, target);
    }

    private static boolean protectedTarget(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof AbstractKnightEntity) || !(target instanceof Villager)) return false;
        CompoundTag a = PersistentEntityData.get(attacker), b = PersistentEntityData.get(target);
        return "evil_knight_prisoners_world_event".equals(a.getString("villagerpatrols_group_type")) && "evil_knight_prisoners_world_event".equals(b.getString("villagerpatrols_group_type")) && "evil_knight_prisoner_prisoner".equals(b.getString("villagerpatrols_group_role")) && a.getString("villagerpatrols_group_id").equals(b.getString("villagerpatrols_group_id"));
    }

    public static void onLivingTick(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || mob.level().isClientSide) return;
        MountedVillagerWorldEventController.tickMob(mob);
        CampingVillagersWorldEventController.tickMob(mob);
        BardWorldEventController.tickMob(mob);
        VillagerMinerWorldEventController.tickMob(mob);
        ShepherdMovingAnimalsWorldEventController.tickMob(mob);
        DesertCamelTravelerWorldEventController.tickMob(mob);
        ShepherdDonkeyTravelerWorldEventController.tickMob(mob);
        LoneIceTravelerWorldEventController.tickMob(mob);
        ArmedTravelerGroupWorldEventController.tickMob(mob);
        TravelerFamilyWorldEventController.tickMob(mob);
        WanderingTraderMeetingWorldEventController.tickMob(mob);
        EvilKnightPrisonersWorldEventController.tickMob(mob);
        if (VillagerIllagerSkirmishEquipment.isSupportedSkirmishFighter(mob)) {
            VillagerIllagerSkirmishEquipment.ensureSkirmishWeapon(mob);
            if (mob.tickCount % 10 == mob.getId() % 10) advanceSkirmishFighter(mob);
        }
        if ((mob instanceof Zombie || mob instanceof Spider) && mob.getTarget() == null && mob.tickCount % 10 == mob.getId() % 10) assignVanillaSkirmishTarget(mob);
    }

    private static void advanceSkirmishFighter(Mob mob) {
        LivingEntity current = mob.getTarget();
        if (current != null && current.isAlive()) return;
        LivingEntity target = nearest(mob, candidate -> isSkirmishTarget(mob, candidate));
        if (target == null) return;
        if (mob.distanceToSqr(target) <= 256.0) mob.setTarget(target);
        else {
            mob.getNavigation().moveTo(target, 0.75);
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private static boolean isSkirmishTarget(Mob mob, LivingEntity target) {
        if (target == mob || !target.isAlive() || target.isRemoved()) return false;
        boolean matches = switch (PersistentEntityData.get(mob).getString("villagerpatrols_group_type")) {
            case "villager_illager_skirmish" -> target instanceof HumanPillagerEntity || target instanceof HumanVindicatorEntity || target instanceof HumanEvokerEntity;
            case "villager_zombie_skirmish" -> target instanceof Zombie;
            case "villager_spider_skirmish" -> target instanceof Spider;
            case "villager_orc_skirmish" -> target instanceof OrcEntity;
            case "villager_bandit_skirmish" -> target instanceof BanditEntity;
            default -> false;
        };
        return matches && mob.hasLineOfSight(target);
    }

    private static void assignVanillaSkirmishTarget(Mob mob) {
        String group = PersistentEntityData.get(mob).getString("villagerpatrols_group_type");
        boolean knights = mob instanceof Zombie && "knight_zombie_skirmish".equals(group);
        if (!knights && !(mob instanceof Zombie && "villager_zombie_skirmish".equals(group)) && !(mob instanceof Spider && "villager_spider_skirmish".equals(group))) return;
        LivingEntity target = nearest(mob, candidate -> {
            if (candidate == mob || !candidate.isAlive() || candidate.isRemoved()) return false;
            boolean matches = knights
                ? candidate instanceof KnightEntity || candidate instanceof KnightShieldEntity || candidate instanceof KnightLeaderEntity || candidate instanceof VillageGuardEntity || candidate instanceof VillageGuardLeaderEntity
                : candidate instanceof Villager && group.equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_type")) && "villager_fighter".equals(PersistentEntityData.get(candidate).getString("villagerpatrols_group_role"));
            return matches && mob.hasLineOfSight(candidate);
        });
        if (target != null) mob.setTarget(target);
    }

    private static LivingEntity nearest(Mob mob, java.util.function.Predicate<LivingEntity> predicate) {
        AABB box = mob.getBoundingBox().inflate(32.0, 8.0, 32.0);
        LivingEntity nearest = null;
        double distance = Double.MAX_VALUE;
        for (LivingEntity candidate : mob.level().getEntitiesOfClass(LivingEntity.class, box, predicate)) {
            double d = mob.distanceToSqr(candidate);
            if (d < distance) { nearest = candidate; distance = d; }
        }
        return nearest;
    }
}
