package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.client.model.WizardCastingHumanoidModel;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import com.javic.villagerpatrols.network.ClientboundWizardCastingMessage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class WizardCastingClientState {
    private static final Set<Integer> CASTING = ConcurrentHashMap.newKeySet();
    private WizardCastingClientState() {}

    public static void handleMessage(ClientboundWizardCastingMessage message) {
        if (message.casting()) CASTING.add(message.entityId());
        else CASTING.remove(message.entityId());
    }

    public static boolean isCasting(LivingEntity entity) { return CASTING.contains(entity.getId()); }

    public static EntityModel<?> modelFor(LivingEntity entity, EntityModel<?> model) {
        if (SlimPatchCompat.isLoaded() && isCasting(entity) && entity instanceof Mob mob
            && FantasyVillagerClientState.isSlimPatchVillager(entity) && WizardEntitySupport.isVisualExternalWizardVillager(mob)
            && model instanceof HumanoidModel<?> humanoid && !(model instanceof WizardCastingHumanoidModel)) {
            return new WizardCastingHumanoidModel(humanoid);
        }
        return model;
    }

    public static void remove(int entityId) { CASTING.remove(entityId); }
    public static void clear() { CASTING.clear(); }
}
