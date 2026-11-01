package com.javic.villagerpatrols.network;

import com.javic.villagerpatrols.persistence.PersistentEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class PrisonerPoseNetworking {
    private static final String TAG_GROUP_TYPE = "villagerpatrols_group_type";
    private static final String TAG_GROUP_ROLE = "villagerpatrols_group_role";

    private PrisonerPoseNetworking() {}

    public static void syncPrisonerState(Mob prisoner, boolean restrained) {
        if (isPrisoner(prisoner)) {
            ModNetworking.sendToTracking(prisoner, new ClientboundPrisonerRestrainedMessage(prisoner.getId(), restrained));
        }
    }

    public static void onStartTracking(ServerPlayer player, Entity entity) {
        if (entity instanceof Mob mob && isPrisoner(mob) && PersistentEntityData.get(mob).getBoolean("villagerpatrols_prisoner_restrained")) {
            ModNetworking.sendToPlayer(player, new ClientboundPrisonerRestrainedMessage(mob.getId(), true));
        }
    }

    private static boolean isPrisoner(Mob mob) {
        return "evil_knight_prisoners_world_event".equals(PersistentEntityData.get(mob).getString(TAG_GROUP_TYPE))
            && "evil_knight_prisoner_prisoner".equals(PersistentEntityData.get(mob).getString(TAG_GROUP_ROLE));
    }
}
