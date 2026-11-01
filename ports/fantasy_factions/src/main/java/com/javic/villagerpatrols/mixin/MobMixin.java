package com.javic.villagerpatrols.mixin;

import com.javic.villagerpatrols.FabricHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Mob.class)
abstract class MobMixin {
    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity villagerpatrols$target(LivingEntity target) {
        return FabricHooks.onLivingChangeTarget((Mob) (Object) this, target);
    }
}
