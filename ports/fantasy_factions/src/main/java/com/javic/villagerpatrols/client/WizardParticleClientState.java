package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.ModParticles;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class WizardParticleClientState {
    private WizardParticleClientState() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.isPaused()) return;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof Mob mob && mob.isAlive() && !mob.isRemoved()
                && WizardEntitySupport.isVisualWizardVillager(mob) && mob.getRandom().nextInt(4) == 0) {
                minecraft.level.addParticle(ModParticles.WIZARD_MAGIC.get(), mob.getRandomX(0.5),
                    mob.getY() + mob.getBbHeight() * (0.35 + mob.getRandom().nextDouble() * 0.45), mob.getRandomZ(0.5),
                    (mob.getRandom().nextDouble() - 0.5) * 1.6, -mob.getRandom().nextDouble() * 0.8,
                    (mob.getRandom().nextDouble() - 0.5) * 1.6);
            }
        }
    }
}
