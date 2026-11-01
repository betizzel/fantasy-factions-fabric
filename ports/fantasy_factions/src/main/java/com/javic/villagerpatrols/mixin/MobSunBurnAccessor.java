package com.javic.villagerpatrols.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobSunBurnAccessor {
    @Invoker("isSunBurnTick")
    boolean villagerpatrols$isSunBurnTick();
}
