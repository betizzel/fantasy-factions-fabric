package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.client.model.PrisonerPoseHumanoidModel;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.network.ClientboundPrisonerRestrainedMessage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public final class PrisonerPoseClientState {
    private static final Set<Integer> RESTRAINED = ConcurrentHashMap.newKeySet();
    private PrisonerPoseClientState() {}

    public static void handleMessage(ClientboundPrisonerRestrainedMessage message) {
        if (message.restrained()) RESTRAINED.add(message.entityId());
        else RESTRAINED.remove(message.entityId());
    }

    public static EntityModel<?> modelFor(LivingEntity entity, EntityModel<?> model) {
        if (SlimPatchCompat.isLoaded() && RESTRAINED.contains(entity.getId())
            && FantasyVillagerClientState.isSlimPatchVillager(entity)
            && model instanceof HumanoidModel<?> humanoid && !(model instanceof PrisonerPoseHumanoidModel)) {
            return new PrisonerPoseHumanoidModel(humanoid);
        }
        return model;
    }

    public static void remove(int entityId) { RESTRAINED.remove(entityId); }
    public static void clear() { RESTRAINED.clear(); }
}
