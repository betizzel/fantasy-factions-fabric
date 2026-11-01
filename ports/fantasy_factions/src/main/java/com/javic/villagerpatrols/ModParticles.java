package com.javic.villagerpatrols;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
    public static final Supplier<SimpleParticleType> VAMPIRE_PORTAL = NativeRegistry.register(
            BuiltInRegistries.PARTICLE_TYPE, "vampire_portal", FabricParticleTypes::simple);
    public static final Supplier<SimpleParticleType> WIZARD_MAGIC = NativeRegistry.register(
            BuiltInRegistries.PARTICLE_TYPE, "wizard_magic", FabricParticleTypes::simple);

    private ModParticles() {}
    public static void init() { VAMPIRE_PORTAL.get(); }
}
