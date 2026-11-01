package com.javic.villagerpatrols.network;

import com.javic.villagerpatrols.entity.FemaleWizardEntity;
import com.javic.villagerpatrols.entity.MaleWizardEntity;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class WizardCastingNetworking {
    private static final String TAG_WIZARD_CASTING_ACTIVE = "villagerpatrols_wizard_casting_active";

    private WizardCastingNetworking() {}

    public static void setCastingActive(Mob mob, boolean casting) {
        if (isWizard(mob)) {
            boolean changed = PersistentEntityData.get(mob).getBoolean(TAG_WIZARD_CASTING_ACTIVE) != casting;
            PersistentEntityData.get(mob).putBoolean(TAG_WIZARD_CASTING_ACTIVE, casting);
            if (changed) {
                ModNetworking.sendToTracking(mob, new ClientboundWizardCastingMessage(mob.getId(), casting));
            }
        }
    }

    public static void onStartTracking(ServerPlayer player, Entity entity) {
        if (entity instanceof Mob mob && isWizard(mob) && PersistentEntityData.get(mob).getBoolean(TAG_WIZARD_CASTING_ACTIVE)) {
            ModNetworking.sendToPlayer(player, new ClientboundWizardCastingMessage(mob.getId(), true));
        }
    }

    private static boolean isWizard(Mob mob) {
        return mob instanceof MaleWizardEntity || mob instanceof FemaleWizardEntity || WizardEntitySupport.isExternalWizardVillager(mob);
    }
}
