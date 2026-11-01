package com.javic.villagerpatrols.spawn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class PlayerAreaPresenceTracker {
   private final Map<ResourceKey<Level>, Map<UUID, PlayerAreaPresenceTracker.PresenceState>> statesByLevel = new HashMap<>();

   public boolean isBlocked(ServerLevel level, ServerPlayer player, long gameTime, PlayerAreaPresenceTracker.PresenceProfile profile) {
      Map<UUID, PlayerAreaPresenceTracker.PresenceState> states = this.statesByLevel.computeIfAbsent(level.dimension(), key -> new HashMap<>());
      UUID playerId = player.getUUID();
      PlayerAreaPresenceTracker.PresenceState state = states.get(playerId);
      BlockPos currentPos = player.blockPosition();
      if (state != null && state.profileId == profile.profileId()) {
         double distanceToAnchorSqr = state.anchor.distSqr(currentPos);
         if (state.blocked) {
            if (distanceToAnchorSqr > profile.resetDistance() * profile.resetDistance()) {
               states.put(playerId, new PlayerAreaPresenceTracker.PresenceState(currentPos, gameTime, profile.profileId(), false));
               return false;
            } else {
               return true;
            }
         } else if (distanceToAnchorSqr > profile.stationaryRadius() * profile.stationaryRadius()) {
            states.put(playerId, new PlayerAreaPresenceTracker.PresenceState(currentPos, gameTime, profile.profileId(), false));
            return false;
         } else if (gameTime - state.startTime >= profile.stationaryTimeTicks()) {
            states.put(playerId, new PlayerAreaPresenceTracker.PresenceState(state.anchor, state.startTime, profile.profileId(), true));
            return true;
         } else {
            return false;
         }
      } else {
         states.put(playerId, new PlayerAreaPresenceTracker.PresenceState(currentPos, gameTime, profile.profileId(), false));
         return false;
      }
   }

   public void clearLevel(ResourceKey<Level> levelKey) {
      this.statesByLevel.remove(levelKey);
   }

   public record PresenceProfile(int profileId, int stationaryRadius, int resetDistance, long stationaryTimeTicks) {
   }

   private record PresenceState(BlockPos anchor, long startTime, int profileId, boolean blocked) {
   }
}
