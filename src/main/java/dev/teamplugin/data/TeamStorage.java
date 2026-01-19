package dev.teamplugin.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 *  Manages persistent storage of teams using JSON files
 */
public class TeamStorage {
    private final Path teamsDirectory;
    private final Gson gson;
    private final List<PlayerTeam> cache;

    public TeamStorage(Path dataDirectory) {
        this.teamsDirectory = dataDirectory.resolve("teams");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ArrayList<>();

        try {
            Files.createDirectories(teamsDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTeam(PlayerTeam team) {
        // Use UUID for filename: "550e8400-e29b-41d4-a716-446655440000.json"
        Path file = teamsDirectory.resolve(team.getTeamID().toString() + ".json");

        try {
            Files.writeString(file, gson.toJson(team));
            if (!cache.contains(team)) cache.add(team);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAllTeams() {
        cache.clear();
        try {
            if (!Files.exists(teamsDirectory)) return;

            Files.list(teamsDirectory)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(file -> {
                        try {
                            PlayerTeam team = gson.fromJson(Files.readString(file), PlayerTeam.class);
                            if (team != null) cache.add(team);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerTeam getTeamByPlayer(UUID playerID) {
        for (PlayerTeam team : cache) {
            if (team.isInTeam(playerID)) {
                return team;
            }
        }
        return null; // Player is lonely and has no team
    }

    public PlayerTeam getTeamByName(String name){
        for (PlayerTeam team : cache) {
            if (team.getTeamName().equals(name)){
                return team;
            }
        }
        return null;
    }

    public boolean doesTeamExist(String teamName){
        for (PlayerTeam team : cache){
            if (Objects.equals(team.getTeamName(), teamName)) return true;
        }
        return false;
    }

    public void saveAll() {
        System.out.println("Saving all " + cache.size() + " teams...");
        for (PlayerTeam team : cache) {
            saveTeam(team);
        }
    }

    public void deleteTeam(PlayerTeam team) {
        // Remove from list
        cache.remove(team);

        // Get the file path
        Path file = teamsDirectory.resolve(team.getTeamID().toString() + ".json");

        // Delete the file
        try {
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                System.out.println("Deleted team file: " + team.getTeamName());
            }
        } catch (IOException e) {
            System.err.println("Failed to delete team file for: " + team.getTeamName());
            e.printStackTrace();
        }
    }

}
