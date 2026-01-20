package codes.tino.quests.leaderboard;

import codes.tino.quests.leaderboard.commands.ReloadCommand;
import codes.tino.quests.leaderboard.database.DatabaseManager;
import codes.tino.quests.leaderboard.database.TopPlayer;
import codes.tino.quests.leaderboard.placeholder.LeaderboardExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestsLeaderboardPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private List<TopPlayer> cachedTopPlayers = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEnable() {
        // Load Config
        saveDefaultConfig();

        // Connect to Database
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        // Register Commands
        ReloadCommand reloadCommand = new ReloadCommand(this);
        getCommand("questleaderboard").setExecutor(reloadCommand);
        getCommand("questleaderboard").setTabCompleter(reloadCommand);

        // Register Placeholder Expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LeaderboardExpansion(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }

        // Start Update Task
        int interval = getConfig().getInt("settings.update-interval", 300);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::updateLeaderboard, 0L, interval * 20L);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void updateLeaderboard() {
        try {
            List<TopPlayer> newTopPlayers = databaseManager.fetchTopPlayers();
            cachedTopPlayers = newTopPlayers; // Swap reference, thread-safe enough for read-heavy
            // getLogger().info("Leaderboard updated. " + newTopPlayers.size() + " entries
            // found.");
        } catch (Exception e) {
            getLogger().severe("Failed to update leaderboard: " + e.getMessage());
        }
    }

    public List<TopPlayer> getCachedTopPlayers() {
        return cachedTopPlayers;
    }

    public void forceUpdateLeaderboard() {
        Bukkit.getScheduler().runTaskAsynchronously(this, this::updateLeaderboard);
    }
}
