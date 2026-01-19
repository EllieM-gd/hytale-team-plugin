package dev.teamplugin.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.teamplugin.TeamPlugin;
import dev.teamplugin.data.PlayerTeam;

import java.awt.*;
import java.util.UUID;

public class TeamChatFormatter {
    TeamPlugin plugin;
    public TeamChatFormatter(TeamPlugin ref){
        plugin = ref;
    }


    public void onPlayerChat(PlayerChatEvent event){
        PlayerRef player = event.getSender();
        UUID playerID = player.getUuid();
        if(event.getContent().equalsIgnoreCase("67")) {
            event.setCancelled(true);
            player.sendMessage(Message.raw("Shut up jong.").color(Color.RED));
            return;
        }
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(playerID);
        if (team != null){

            event.setFormatter(( sender, message) ->
                Message.join(
                        Message.raw("[" + team.getTeamName() + "] " + player.getUsername()).color(plugin.parseColor(team.getColor())),
                        Message.raw(" : " + message).color(Color.WHITE)
                ));



    }
    }
}
