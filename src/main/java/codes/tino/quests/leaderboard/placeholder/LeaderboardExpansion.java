package codes.tino.quests.leaderboard.placeholder;

import codes.tino.quests.leaderboard.QuestsLeaderboardPlugin;
import codes.tino.quests.leaderboard.database.TopPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderboardExpansion extends PlaceholderExpansion {

    private final QuestsLeaderboardPlugin plugin;

    public LeaderboardExpansion(QuestsLeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "questleaderboard";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tino-id";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Supported placeholders:
        // %questleaderboard_top_<rank>_name% - Only player name
        // %questleaderboard_top_<rank>_quests% - Only quest count

        if (params.startsWith("top_")) {
            try {
                String[] parts = params.split("_");
                if (parts.length != 3) {
                    return "Invalid Placeholder";
                }

                int rank = Integer.parseInt(parts[1]);
                List<TopPlayer> topPlayers = plugin.getCachedTopPlayers();

                // Get leaderboard size from config
                int leaderboardSize = plugin.getConfig().getInt("settings.leaderboard-size", 10);

                if (rank < 1 || rank > leaderboardSize) {
                    return "Invalid Rank";
                }

                if (topPlayers == null || topPlayers.size() < rank) {
                    return "---"; // Not enough data or loading
                }

                TopPlayer topPlayer = topPlayers.get(rank - 1);
                String field = parts[2];

                if (field.equals("name")) {
                    return topPlayer.name();
                } else if (field.equals("quests")) {
                    return String.valueOf(topPlayer.completedQuests());
                }

                return null;

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return "Invalid Placeholder";
            }
        }

        return null;
    }
}