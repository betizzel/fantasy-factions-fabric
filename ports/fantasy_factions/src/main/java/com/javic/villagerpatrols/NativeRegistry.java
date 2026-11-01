package com.javic.villagerpatrols;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

final class NativeRegistry {
    private NativeRegistry() {}

    static <T, V extends T> Supplier<V> register(Registry<T> registry, String path, Supplier<V> factory) {
        V value = Registry.register(registry, new ResourceLocation(VillagerPatrols.MODID, path), factory.get());
        return () -> value;
    }
}
