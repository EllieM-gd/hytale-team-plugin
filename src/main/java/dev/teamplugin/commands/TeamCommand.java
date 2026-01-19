package dev.teamplugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.teamplugin.TeamPlugin;
import dev.teamplugin.data.PlayerTeam;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TeamCommand extends AbstractPlayerCommand {
    private final TeamPlugin plugin;

    public TeamCommand(TeamPlugin pluginRef){
        super("team", "Use /team help for more commands");
        setAllowsExtraArguments(true);
        this.plugin = pluginRef;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        // Get the full input string and split it to find sub-commands
        String[] args = commandContext.getInputString().split(" ");

        UUID playerUUID = playerRef.getUuid();

        // args[0] is "team", so we check args[1]
        if (args.length < 2) {
            PlayerTeam team = plugin.getStorage().getTeamByPlayer(playerUUID);
            if (team == null){
                send(playerRef, "You are not in a team. Use '/team help' for available commands");
            }
            else{
                send(playerRef, "Team Name: " + team.getTeamName(), plugin.parseColor(team.getColor()));
                send(playerRef, "Player Count: " + team.getTeamSize(), plugin.parseColor(team.getColor()));
                send(playerRef, "PvpEnabled: " + team.getFriendlyPVP(), plugin.parseColor(team.getColor()));
            }
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(playerRef, playerUUID, args);
                break;

            case "color":
                handleColor(playerRef, playerUUID, args);
                break;

            case "leave":
                handleLeave(playerRef, playerUUID, world);
                break;

            case "help":
                sendHelp(playerRef);
                break;

            case "transfer":
                handleTransfer(playerRef, playerUUID, args, world);
                break;

            case "kick":
                handleKick(playerRef, playerUUID, args, world, store);
                break;

            case "invite":
                handleInvite(playerRef, playerUUID, args, world);
                break;

            case "accept":
                handleAccept(playerRef, playerUUID, world);
                break;

            case "pvp":
                handlePVPSet(playerRef, playerUUID, args, world);
                break;

            default:
                send(playerRef, "Unknown sub-command. Use /team help", Color.RED);
                break;
        }
    }

    // Sub Commands

    private void handleCreate(PlayerRef player, UUID uuid, String[] args) {
        if (args.length < 3) {
            send(player, "Usage: /team create <name>", Color.RED);
            return;
        }
        if (args[2].length() > 12 ){
            send(player, "Team name is too long. Please use 12 or less characters", Color.RED);
            return;
        }
        if (plugin.getStorage().getTeamByPlayer(uuid) != null){
            send(player, "Please leave your current team before creating a new one. Usage: /team leave", Color.RED);
            return;
        }
        if (plugin.getStorage().doesTeamExist(args[2])){
            send(player, "Team name already exists", Color.red);
            return;
        }
        // Create a new team
        PlayerTeam newTeam = new PlayerTeam(uuid, args[2]);
        // Save team to storage
        plugin.getStorage().saveTeam(newTeam);
        // Send message to player
        send(player, "Team " + args[2] + " created!", Color.GREEN);
    }

    private void handlePVPSet(PlayerRef player, UUID uuid, String[] args, World world){
        if (args.length < 3 || (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            send(player, "Usage: /team pvp <true/false>", Color.RED);
            return;
        }
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid);
        if (team == null || !team.isOwner(uuid)){
            send(player, "You must be the owner of a team to use this command", Color.RED);
            return;
        }

        if (args[2].equalsIgnoreCase("true")){
            if (team.getFriendlyPVP()){
                send(player, "PVP is already enabled", plugin.parseColor(team.getColor()));
                return;
            }
            team.setFriendlyPVP(true);
            sendTeam(team, world, "Team PVP has been enabled", plugin.parseColor(team.getColor()));
        } else if (args[2].equalsIgnoreCase("false")) {
            if (!team.getFriendlyPVP()){
                send(player, "PVP is already disabled", plugin.parseColor(team.getColor()));
                return;
            }
            team.setFriendlyPVP(false);
            sendTeam(team, world, "Team PVP has been disabled", plugin.parseColor(team.getColor()));
        }


    }

    private void handleInvite(PlayerRef player, UUID uuid, String[] args, World world) {
        if (args.length < 3) {
            send(player, "Usage: /team invite <name>", Color.RED);
            return;
        }
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid); // Grab our team
        if (team == null || !team.isOwner(uuid)){
            send(player, "Only team owners can invite players to a team", Color.RED);
            return;
        }

        PlayerRef targetPlayer = getPlayerRefFromID(player, args[2], world);
        if (targetPlayer == null) return;
        if (team.hasPlayer(targetPlayer.getUuid())){
            send(player, "Player is already in your team");
            return;
        }

        plugin.getInviteManager().sendInvite(targetPlayer, targetPlayer.getUuid(), team, world);
        send(player, "Invite sent to " + targetPlayer.getUsername() + "! They have 60 seconds to accept.", Color.green);
        send(targetPlayer, "You were invited to " + team.getTeamName() + "! Type '/team accept' to join.", plugin.parseColor(team.getColor()));

    }

    private void handleAccept(PlayerRef player, UUID uuid, World world){
        PlayerTeam team = plugin.getInviteManager().getPendingInvite(uuid);

        if (team == null){
            send(player, "You have no invites at this time", Color.RED);
            return;
        }
        if (plugin.getStorage().getTeamByPlayer(uuid) != null){
            send(player, "Please leave your current team before joining a new one", Color.yellow);
            return;
        }

        team.addMember(uuid);
        plugin.getStorage().saveTeam(team);
        plugin.getInviteManager().removeInvite(uuid);

        send(player, "You have successfully joined " + team.getTeamName() + "!", plugin.parseColor(team.getColor()));

        sendTeam(team, world, player.getUsername() + " has joined your team!", plugin.parseColor(team.getColor()));


    }

    private void handleTransfer(PlayerRef player, UUID uuid, String[] args, World world) {
        if (args.length < 3) {
            send(player, "Usage: /team transfer <username>", Color.RED);
            return;
        }

        PlayerRef targetPlayer = getPlayerRefFromID(player, args[2], world);
        if (targetPlayer == null) return;

        // Check if the player is in your team
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid);
        if (team.hasPlayer(targetPlayer.getUuid())){
            // Change team owner
            team.changeOwner(targetPlayer.getUuid());
            // Send messages
            send(player, "You are no longer the owner of " + team.getTeamName());
            send(targetPlayer, "You are now the owner of " + team.getTeamName());
            // Save to storage
            plugin.getStorage().saveTeam(team);
        }
        else{
            // Inform player
            send(player, targetPlayer.getUsername() + " is not in your team", Color.RED);
        }
    }

    private void handleKick(PlayerRef player, UUID uuid, String[] args, World world, Store<EntityStore> store) {
        if (args.length < 3) {
            send(player, "Usage: /team kick <username>", Color.RED);
            return;
        }

        PlayerRef targetPlayer = getPlayerRefFromID(player, args[2], world);
        if (targetPlayer == null) return;

        // Check if the player is in your team
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid);
        if (team.hasPlayer(targetPlayer.getUuid())){
            // Change team owner
            team.removeMember(targetPlayer.getUuid());
            // Send messages
            send(player, targetPlayer.getUsername() + " is no longer a member of " + team.getTeamName());
            send(targetPlayer, "You were kicked from " + team.getTeamName());
            // Save to storage
            plugin.getStorage().saveTeam(team);
        }
        else{
            // Inform player
            send(player, targetPlayer.getUsername() + " is not in your team", Color.RED);
        }
    }

    private void handleColor(PlayerRef player, UUID uuid, String[] args) {
        if (args.length < 3) {
            send(player, "Usage: /team color <color>", Color.RED);
            return;
        }
        // Check if valid color
        Color newColor = plugin.parseColor(args[2]);
        if (newColor == null){
            send(player, "Invalid Color, Sorry", Color.RED);
            return;
        }
        // Check if in a team.
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid);
        if (team == null){
            send(player, "You must be in a team to use this command", Color.RED);
            return;
        }
        // Check if team owner
        if (!team.isOwner(uuid)){
            send(player, "You must be the owner of the team to change the color", Color.RED);
            return;
        }
        // Update team color
        team.setTeamColor(args[2]);
        // Save changes
        plugin.getStorage().saveTeam(team);
        // Send confirmation message
        send(player, "Team color updated!", newColor);
    }

    private void handleLeave(PlayerRef player, UUID uuid, World world) {
        // Check if in a team.
        PlayerTeam team = plugin.getStorage().getTeamByPlayer(uuid);
        if (team == null){
            send(player, "You must be in a team to use this command", Color.RED);
            return;
        }


        if (team.getTeamSize() == 1){
            // Remove member
            team.removeMember(uuid);
            // Delete empty team
            plugin.getStorage().deleteTeam(team);
            send(player, team.getTeamName() + " has been disbanded.", Color.GREEN);
        }
        else{
            // Don't allow the owner to leave if other members are in there
            if (team.isOwner(uuid)){
                send(player, "Please promote a new owner before leaving your team", Color.RED);
                return;
            }

            team.removeMember(uuid);
            sendTeam(team, world, player.getUsername() + " has left your team", plugin.parseColor(team.getColor()));
            send(player, "You left " + team.getTeamName());
        }
    }


    private void sendHelp(PlayerRef player) {
        send(player, "--- Team Commands ---", Color.YELLOW);
        send(player, "/team");
        send(player, "/team create <name>");
        send(player, "/team color <color>");
        send(player, "/team leave");
        send(player, "/team kick <username>");
        send(player, "/team transfer <username>");
        send(player, "/team invite <username>");
        send(player, "/team accept");
        send(player, "/team pvp {true/false}");
    }


    // Helpers -----
    private void send(PlayerRef player, String text) {
        player.sendMessage(Message.raw(text));
    }

    private void send(PlayerRef player, String text, Color color) {
        player.sendMessage(Message.raw(text).color(color));
    }

    private void sendTeam(PlayerTeam team, World world, String message, Color color){
        ArrayList<UUID> players = team.getMembers();
        Collection<PlayerRef> worldPlayers = world.getPlayerRefs();
        //Check every player in world
        for (PlayerRef player : worldPlayers){
            //If their ID matches one in our list
            if (players.contains(player.getUuid())){
                //Send message
                send(player, message, color);
            }
        }

    }

    private PlayerRef getPlayerRefFromID(PlayerRef sender, String user, World world){
        // Grab all players
        Collection<PlayerRef> players = world.getPlayerRefs();
        for (PlayerRef tempPlayer : players){
            // Look for specific player
            if (tempPlayer.getUsername().equals(user)){
                return tempPlayer;
            }
        }
        if (sender == null){
            return null;
        }

        send(sender, "Player not found in server", Color.red);
        return null;
    }
}
