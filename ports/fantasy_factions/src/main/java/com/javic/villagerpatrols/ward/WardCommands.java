package com.javic.villagerpatrols.ward;

import com.javic.villagerpatrols.Config;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class WardCommands {
   private WardCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("fantasyfactions")
         .then(Commands.literal("remove")
            .then(Commands.literal("mobs")
               .requires(source -> source.hasPermission(2))
               .executes(context -> removeMobs(context.getSource())))));
   }

   private static int removeMobs(CommandSourceStack source) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      int removed = WardMobRemovalService.removeMobs(player.serverLevel(), player.blockPosition(), Config.wardBannerRadius());
      if (removed > 0) {
         source.sendSuccess(() -> Component.translatable("message.villagerpatrols.ward.remove_mobs.removed", removed), false);
      } else {
         source.sendSuccess(() -> Component.translatable("message.villagerpatrols.ward.remove_mobs.none"), false);
      }
      return removed;
   }
}
