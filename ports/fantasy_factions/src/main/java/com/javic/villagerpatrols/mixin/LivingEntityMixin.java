package com.javic.villagerpatrols.mixin;

import com.javic.villagerpatrols.FabricHooks;
import com.javic.villagerpatrols.entity.WizardCompatEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void villagerpatrols$tick(CallbackInfo ci) {
        FabricHooks.onLivingTick((LivingEntity) (Object) this);
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"), cancellable = true)
    private void villagerpatrols$cancelHurt(DamageSource source, float amount, CallbackInfo ci) {
        if (amount <= 0.0F) ci.cancel();
    }

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float villagerpatrols$hurtAmount(float amount, DamageSource source, float originalAmount) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isInvulnerableTo(source) || amount <= 0.0F) return amount;
        if (FabricHooks.cancelLivingHurt(entity, source, amount)) return 0.0F;
        return WizardCompatEvents.onLivingHurt(entity, source, amount);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void villagerpatrols$death(DamageSource source, CallbackInfo ci) {
        WizardCompatEvents.onLivingDeath((LivingEntity) (Object) this);
    }
}
