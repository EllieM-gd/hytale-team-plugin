package dev.teamplugin;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.teamplugin.commands.TeamCommand;
import dev.teamplugin.data.InviteManager;
import dev.teamplugin.data.TeamStorage;
import dev.teamplugin.events.TeamChatFormatter;
import dev.teamplugin.systems.PreventDmgClass;

import javax.annotation.Nonnull;
import java.awt.*;

public class TeamPlugin extends JavaPlugin {
    // Save storage, for reference in commands
    private TeamStorage storage;
    private InviteManager invites;

    public TeamPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }


    @Override
    protected void setup() {
        // Create storage for teams
        storage = new TeamStorage(getDataDirectory());
        storage.loadAllTeams();
        invites = new InviteManager();
        TeamChatFormatter chatEvent = new TeamChatFormatter(this);

        // Add Command File
        this.getCommandRegistry().registerCommand(new TeamCommand(this));
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, chatEvent::onPlayerChat);

        this.getEntityStoreRegistry().registerSystem(new PreventDmgClass(this));
    }


    // Extra save on server shutdown
    @Override
    protected void shutdown(){
        if (storage != null){
            storage.saveAll();
        }
        if (invites != null){
            invites.shutdown();
        }
    }

    public InviteManager getInviteManager(){
        return invites;
    }

    public TeamStorage getStorage(){
        return storage;
    }

    public Color parseColor(String name) {
        if (name == null) return null;

        // Clean the input: lowercase and remove spaces/underscores
        String cleanName = name.toLowerCase().replace("_", "").replace(" ", "");

        return switch (cleanName) {
            // --- Standard Colors ---
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            case "white" -> Color.WHITE;
            case "black" -> Color.BLACK;
            case "gray", "grey" -> Color.GRAY;
            case "orange" -> Color.ORANGE;
            case "pink" -> Color.PINK;
            case "magenta" -> Color.MAGENTA;
            case "cyan" -> Color.CYAN;

            // --- Light / Dark Variants ---
            case "lightblue" -> new Color(173, 216, 230);
            case "lightgreen" -> new Color(144, 238, 144);
            case "darkred" -> new Color(139, 0, 0);
            case "darkblue" -> new Color(0, 0, 139);
            case "darkgreen" -> new Color(0, 100, 0);

            // --- Custom / Vibrant Colors ---
            case "purple" -> new Color(128, 0, 128);
            case "gold" -> new Color(255, 215, 0);
            case "aqua" -> new Color(0, 255, 255);
            case "lime" -> new Color(50, 205, 50);
            case "brown" -> new Color(139, 69, 19);
            case "silver" -> new Color(192, 192, 192);
            case "navy" -> new Color(0, 0, 128);
            case "teal" -> new Color(0, 128, 128);
            default -> null;
        };
    }

}