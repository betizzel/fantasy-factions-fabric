package com.javic.villagerpatrols.client;

import com.javic.villagerpatrols.client.model.ElfModelFemale;
import com.javic.villagerpatrols.client.model.ElfModelMale;
import com.javic.villagerpatrols.compat.SlimPatchCompat;
import com.javic.villagerpatrols.entity.FantasyVillagerSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class FantasyVillagerClientState {
    private FantasyVillagerClientState() {}

    public static boolean isSlimPatchVillager(LivingEntity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && key.getNamespace().equals("slimpatch")
            && (key.getPath().equals("male_villager") || key.getPath().equals("female_villager"));
    }

    public static EntityModel<?> modelFor(LivingEntity entity, EntityModel<?> model) {
        if (SlimPatchCompat.isLoaded() && entity instanceof Mob mob && isSlimPatchVillager(entity)
            && FantasyVillagerSupport.isVisualElfVillager(mob) && model instanceof HumanoidModel<?> && !(model instanceof ElfModelMale) && !(model instanceof ElfModelFemale)) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            var models = Minecraft.getInstance().getEntityModels();
            return key.getPath().equals("female_villager")
                ? new ElfModelFemale(models.bakeLayer(ElfModelFemale.LAYER_LOCATION))
                : new ElfModelMale(models.bakeLayer(ElfModelMale.LAYER_LOCATION));
        }
        return model;
    }
}
