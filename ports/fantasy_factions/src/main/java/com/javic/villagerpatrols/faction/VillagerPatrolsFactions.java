package com.javic.villagerpatrols.faction;

import com.javic.villagerpatrols.entity.BanditEntity;
import com.javic.villagerpatrols.entity.DwarfEntity;
import com.javic.villagerpatrols.entity.ElfEntity;
import com.javic.villagerpatrols.entity.EvilKnightEntity;
import com.javic.villagerpatrols.entity.EvilKnightLeaderEntity;
import com.javic.villagerpatrols.entity.EvilKnightShieldEntity;
import com.javic.villagerpatrols.entity.EvilWizardEntity;
import com.javic.villagerpatrols.entity.FantasyVillagerSupport;
import com.javic.villagerpatrols.entity.FemaleHealerEntity;
import com.javic.villagerpatrols.entity.FemaleVampireVillagerEntity;
import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import com.javic.villagerpatrols.entity.HealerEntitySupport;
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
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import com.javic.villagerpatrols.entity.OrcEntity;
import com.javic.villagerpatrols.entity.ShadowEntity;
import com.javic.villagerpatrols.entity.VampireEntity;
import com.javic.villagerpatrols.entity.VampireEntitySupport;
import com.javic.villagerpatrols.entity.VillageGuardEntity;
import com.javic.villagerpatrols.entity.VillageGuardLeaderEntity;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public final class VillagerPatrolsFactions {
   private static final String SLIMPATCH_NAMESPACE = "slimpatch";
   private static final String MALE_VILLAGER_PATH = "male_villager";
   private static final String FEMALE_VILLAGER_PATH = "female_villager";
   private static final String GUARD_VILLAGERS_GUARD_CLASS = "tallestegg.guardvillagers.common.entities.Guard";

   private VillagerPatrolsFactions() {
   }

   public static boolean isVillagerPatrolsFriendly(Entity entity) {
      return entity instanceof KnightEntity
         || entity instanceof KnightShieldEntity
         || entity instanceof KnightLeaderEntity
         || entity instanceof VillageGuardEntity
         || entity instanceof VillageGuardLeaderEntity
         || entity instanceof ElfEntity
         || entity instanceof DwarfEntity
         || entity instanceof MaleWizardEntity
         || entity instanceof FemaleWizardEntity
         || entity instanceof MaleHealerEntity
         || entity instanceof FemaleHealerEntity
         || entity instanceof MaleVampireVillagerEntity
         || entity instanceof FemaleVampireVillagerEntity
         || isVillagerPatrolsConvertedFriendly(entity);
   }

   public static boolean isVillagerPatrolsHostile(Entity entity) {
      return entity instanceof EvilKnightEntity
         || entity instanceof EvilKnightShieldEntity
         || entity instanceof EvilKnightLeaderEntity
         || entity instanceof HumanPillagerEntity
         || entity instanceof HumanVindicatorEntity
         || entity instanceof HumanEvokerEntity
         || entity instanceof HumanWitchEntity
         || entity instanceof HumanSwordsmanEntity
         || entity instanceof HumanArcherEntity
         || entity instanceof HumanHorsemanEntity
         || entity instanceof HumanRavagerRiderEntity
         || entity instanceof OrcEntity
         || entity instanceof BanditEntity
         || entity instanceof ShadowEntity
         || entity instanceof EvilWizardEntity
         || entity instanceof VampireEntity;
   }

   public static boolean isVillagerPatrolsConvertedFriendly(Entity entity) {
      return !(entity instanceof Mob mob)
         ? false
         : WizardEntitySupport.isWizardVillager(mob)
            || HealerEntitySupport.isHealerVillager(mob)
            || VampireEntitySupport.isVampireVillager(mob)
            || FantasyVillagerSupport.isFantasyVillager(mob);
   }

   public static boolean isFriendlyProtector(Entity entity) {
      return entity instanceof Player
         ? false
         : entity instanceof IronGolem || isVillagerPatrolsFriendly(entity) || isSlimPatchVillager(entity) || isGuardVillagersGuard(entity);
   }

   public static boolean shouldPreventFriendlyTargeting(LivingEntity attacker, LivingEntity target) {
      if (attacker == null || target == null || attacker instanceof Player) {
         return false;
      } else if (!isVillagerPatrolsFriendly(target)) {
         return false;
      } else {
         return isVillagerPatrolsHostile(attacker) ? false : isFriendlyProtector(attacker);
      }
   }

   private static boolean isSlimPatchVillager(Entity entity) {
      if (!(entity instanceof Villager)) {
         return false;
      } else {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         if (key != null && "slimpatch".equals(key.getNamespace())) {
            String path = key.getPath();
            return "male_villager".equals(path) || "female_villager".equals(path);
         } else {
            return false;
         }
      }
   }

   private static boolean isGuardVillagersGuard(Entity entity) {
      return entity != null && "tallestegg.guardvillagers.common.entities.Guard".equals(entity.getClass().getName());
   }
}
