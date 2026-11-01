package com.javic.villagerpatrols.worldevent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class EventVillagerProfessions {
   private EventVillagerProfessions() {
   }

   public static void apply(ServerLevel level, Mob mob, VillagerProfession profession) {
      if (mob instanceof Villager villager) {
         villager.setVillagerData(villager.getVillagerData().setProfession(profession).setLevel(1));
         villager.setVillagerXp(1);
         villager.getOffers();
         villager.refreshBrain(level);
      }
   }

   public static void applyRandom(ServerLevel level, Mob mob, float chance, VillagerProfession... professions) {
      if (professions.length != 0 && !(level.random.nextFloat() >= chance)) {
         apply(level, mob, professions[level.random.nextInt(professions.length)]);
      }
   }
}
