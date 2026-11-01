package com.javic.villagerpatrols.persistence;

import io.github.fabricators_of_create.porting_lib.entity.extensions.EntityExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/** Uses Porting Lib's shared ForgeData compound for native entity persistence. */
public final class PersistentEntityData {
    private PersistentEntityData() {
    }

    public static CompoundTag get(Entity entity) {
        return ((EntityExtensions) entity).getCustomData();
    }
}
