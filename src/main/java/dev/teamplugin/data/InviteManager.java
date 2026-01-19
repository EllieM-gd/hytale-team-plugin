package dev.teamplugin.data;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InviteManager {
    // Maps the Invited Player's UUID -> The Team they were invited to
    private final Map<UUID, PlayerTeam> pendingInvites = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Sends an invite that lasts for 60 seconds
     */
    public void sendInvite(PlayerRef player, UUID invitedPlayerId, PlayerTeam team, World world) {
        if (pendingInvites.get(invitedPlayerId) != null) {
            player.sendMessage(Message.raw("Couldn't recieve invite to " + team.getTeamName() + " because there is already an active invite" ));
            return;
        }
        pendingInvites.put(invitedPlayerId, team);

        // Schedule the invite to expire in 60 seconds
        scheduler.schedule(() -> {
            pendingInvites.remove(invitedPlayerId);

            world.execute(() -> {
                player.sendMessage(Message.raw("Your invite to " + team.getTeamName() + " has expired."));
            });
        }, 60, TimeUnit.SECONDS);
    }

    public PlayerTeam getPendingInvite(UUID playerId) {
        return pendingInvites.get(playerId);
    }

    public void removeInvite(UUID playerId) {
        pendingInvites.remove(playerId);
    }

    public void shutdown() {
        scheduler.shutdownNow(); // Stops all pending invite expiration tasks
    }
}
