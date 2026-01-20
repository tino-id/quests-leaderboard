package codes.tino.quests.leaderboard.commands;

import codes.tino.quests.leaderboard.QuestsLeaderboardPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final QuestsLeaderboardPlugin plugin;

    public ReloadCommand(QuestsLeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("questleaderboard.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§eReloading QuestsLeaderboard...");

            try {
                // Reload config
                plugin.reloadConfig();

                // Trigger immediate leaderboard update
                plugin.forceUpdateLeaderboard();

                sender.sendMessage("§aQuestsLeaderboard has been reloaded successfully!");
                sender.sendMessage("§7- Config reloaded");
                sender.sendMessage("§7- Leaderboard updated");
            } catch (Exception e) {
                sender.sendMessage("§cFailed to reload plugin: " + e.getMessage());
                plugin.getLogger().severe("Error during reload: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        // Show help if no valid subcommand
        sender.sendMessage("§6QuestsLeaderboard Commands:");
        sender.sendMessage("§e/questleaderboard reload §7- Reload the plugin configuration");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("questleaderboard.reload")) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
