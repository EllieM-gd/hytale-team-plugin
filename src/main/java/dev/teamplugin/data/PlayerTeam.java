package dev.teamplugin.data;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerTeam {
    // Create an array of player IDs
    private final ArrayList<UUID> memberIDs = new ArrayList<UUID>();
    private UUID ownerID;
    private final UUID teamID;
    private String teamName;
    private String chatColor;
    private boolean friendlyPVP = false;

    public PlayerTeam(UUID Owner, String teamName){
        this.teamID = UUID.randomUUID();
        this.memberIDs.add(Owner);
        this.ownerID = Owner;
        this.teamName = teamName;
        this.chatColor = "WHITE";
    }

    public UUID getTeamID() {
        return teamID;
    }

    public boolean isInTeam(UUID user){
        return this.memberIDs.contains(user);
    }

    public boolean isOwner(UUID user){
        if (user == null || this.ownerID == null) return false;
        return this.ownerID.equals(user); // Compares the actual ID strings
    }

    public UUID getOwnerID(){
        return this.ownerID;
    }

    public String getTeamName(){
        return this.teamName;
    }

    public String getColor() {
        return this.chatColor;
    }

    public void setTeamColor(String colorName) {
        this.chatColor = colorName.toUpperCase();
    }

    public void changeOwner(UUID newOwner){
        this.ownerID = newOwner;
    }

    public void changeName(String newName){
        this.teamName = newName;
    }

    public void addMember(UUID newPlayer){
        if (!memberIDs.contains(newPlayer)){
            memberIDs.add(newPlayer);
        }
    }

    public boolean hasPlayer(UUID playerID){
        for (UUID ID : this.memberIDs){
            if (ID.equals(playerID)) return true;
        }
        return false;
    }

    public void removeMember(UUID player){
        memberIDs.remove(player);
    }

    public int getTeamSize(){
        return this.memberIDs.size();
    }

    public ArrayList<UUID> getMembers(){
        return this.memberIDs;
    }

    public boolean getFriendlyPVP(){
        return this.friendlyPVP;
    }

    public void setFriendlyPVP(boolean val){
        this.friendlyPVP = val;
    }



}
