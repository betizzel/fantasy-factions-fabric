package com.javic.villagerpatrols.mixin;

import com.javic.villagerpatrols.persistence.ServerTimeAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface ServerTimeMixin extends ServerTimeAccess {
    @Override
    @Invoker("haveTime")
    boolean villagerpatrols$haveTime();
}
