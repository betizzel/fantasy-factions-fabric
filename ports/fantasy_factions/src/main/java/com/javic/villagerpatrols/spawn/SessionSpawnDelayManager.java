package com.javic.villagerpatrols.spawn;

import com.javic.villagerpatrols.Config;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SessionSpawnDelayManager {
   private static final Map<MinecraftServer, SessionSpawnDelayManager.SessionState> STATE_BY_SERVER = new WeakHashMap<>();

   private SessionSpawnDelayManager() {
   }

   public static void update(MinecraftServer server) {
      SessionSpawnDelayManager.SessionState state = STATE_BY_SERVER.computeIfAbsent(server, SessionSpawnDelayManager.SessionState::new);
      boolean hasActivePlayers = hasActivePlayers(server);
      if (!state.hadActivePlayers && hasActivePlayers) {
         state.activeSessionStartTick = server.getTickCount();
      }

      state.hadActivePlayers = hasActivePlayers;
   }

   public static boolean isSpawnDelayActive(MinecraftServer server) {
      return isDelayActive(server, Config.initialSpawnDelayTicks());
   }

   public static boolean isWorldEventAndSkirmishDelayActive(MinecraftServer server) {
      return isDelayActive(server, Config.initialWorldEventAndSkirmishDelayTicks());
   }

   private static boolean isDelayActive(MinecraftServer server, int delayTicks) {
      if (delayTicks <= 0) {
         return false;
      }

      SessionSpawnDelayManager.SessionState state = STATE_BY_SERVER.computeIfAbsent(server, SessionSpawnDelayManager.SessionState::new);
      return server.getTickCount() - state.activeSessionStartTick < delayTicks;
   }

   private static boolean hasActivePlayers(MinecraftServer server) {
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         if (player.isAlive() && !player.isSpectator()) {
            return true;
         }
      }

      return false;
   }

   private static final class SessionState {
      private int activeSessionStartTick;
      private boolean hadActivePlayers;

      private SessionState(MinecraftServer server) {
         this.activeSessionStartTick = server.getTickCount();
      }
   }
}
