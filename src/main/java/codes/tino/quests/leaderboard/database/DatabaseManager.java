package codes.tino.quests.leaderboard.database;

import codes.tino.quests.leaderboard.QuestsLeaderboardPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final QuestsLeaderboardPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(QuestsLeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.user");
        String password = config.getString("database.password");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool timeout settings
        hikariConfig.setConnectionTimeout(config.getLong("database.connection-timeout", 30000));
        hikariConfig.setMaxLifetime(config.getLong("database.max-lifetime", 1800000));

        // Recommended settings for HikariCP
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(hikariConfig);
            plugin.getLogger().info("Database connection established.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to database!", e);
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public List<TopPlayer> fetchTopPlayers() {
        List<TopPlayer> topPlayers = new ArrayList<>();

        // Get leaderboard size from config
        int leaderboardSize = plugin.getConfig().getInt("settings.leaderboard-size", 10);

        String query = """
                SELECT
                    q.uuid,
                    COUNT(q.quest_id) AS completed_quests
                FROM quests_quest_progress q
                WHERE q.completed = 1
                GROUP BY q.uuid
                ORDER BY completed_quests DESC
                LIMIT ?;
                """;

        if (dataSource == null) {
            return topPlayers;
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            // Set LIMIT parameter
            statement.setInt(1, leaderboardSize);

            // Set query timeout from config (in seconds)
            int queryTimeout = plugin.getConfig().getInt("settings.query-timeout", 10);
            statement.setQueryTimeout(queryTimeout);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    int completedQuests = resultSet.getInt("completed_quests");

                    // Get player name from Bukkit's cache (usercache.json)
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        String name = offlinePlayer.getName();

                        // Only add if player has a valid name
                        if (name != null && !name.isEmpty()) {
                            topPlayers.add(new TopPlayer(name, completedQuests));
                        }
                    } catch (IllegalArgumentException e) {
                        // Invalid UUID format, skip this entry
                        plugin.getLogger().warning("Invalid UUID in database: " + uuidString);
                    }
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error fetching top players", e);
        }

        return topPlayers;
    }
}
