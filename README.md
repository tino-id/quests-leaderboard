# QuestsLeaderboard

[![Paper Version](https://img.shields.io/badge/Paper-1.21.11-blue.svg)](https://papermc.io/)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A lightweight Paper/Spigot plugin that provides placeholders for the [Quests Plugin](https://www.spigotmc.org/resources/quests-1-8-1-21-set-up-goals-for-players.23696/) to generate a leaderboard.

## Features

- **Real-time Leaderboard**: Automatically updates player rankings based on completed quests
- **MySQL Database Integration**: Uses HikariCP connection pooling for efficient database operations
- **PlaceholderAPI Support**: Provides flexible placeholders for use in scoreboards, chat, TAB lists, and more
- **Configurable**: Customizable update intervals, leaderboard size, and database timeouts
- **Performance Optimized**: Asynchronous database queries prevent server lag
- **Reload Command**: Update configuration without restarting the server

## Requirements

- **Minecraft Server**: Paper 1.21.10+ (or compatible Spigot/Bukkit fork)
- **Java**: Java 21 or higher
- **Required Dependencies**:
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
  - [Quests](https://www.spigotmc.org/resources/quests-1-8-1-21-set-up-goals-for-players.23696/)
- **Database**: MySQL 5.7+ or MariaDB 10.2+

## Installation

1. **Download** the latest `quests-leaderboard-X.X.jar` from the [Releases](https://github.com/tino-id/quest-leaderboard/releases) page
2. **Place** the JAR file in your server's `plugins/` folder
3. **Install** PlaceholderAPI if not already installed
4. **Start** your server to generate the default configuration
5. **Configure** the database settings in `plugins/QuestsLeaderboard/config.yml`
6. **Restart** the server or use `/questleaderboard reload`

## Configuration

### config.yml

```yaml
database:
  host: "127.0.0.1"
  port: 3306
  user: "root"
  password: "password"
  database: "minecraft"
  # Connection pool settings
  connection-timeout: 30000 # Maximum time to wait for a connection from pool (in ms, default: 30s)
  max-lifetime: 1800000 # Maximum lifetime of a connection in the pool (in ms, default: 30min)

settings:
  update-interval: 300 # in seconds (5 minutes)
  query-timeout: 10 # Maximum time for a single query to execute (in seconds, default: 10s)
  leaderboard-size: 10 # Number of top players to display (default: 10)
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `database.host` | MySQL server hostname or IP address | `127.0.0.1` |
| `database.port` | MySQL server port | `3306` |
| `database.user` | Database username | `root` |
| `database.password` | Database password | `password` |
| `database.database` | Database name | `minecraft` |
| `database.connection-timeout` | Max wait time for connection from pool (ms) | `30000` |
| `database.max-lifetime` | Max lifetime of a connection in pool (ms) | `1800000` |
| `settings.update-interval` | How often to refresh the leaderboard (seconds) | `300` |
| `settings.query-timeout` | Max execution time for database queries (seconds) | `10` |
| `settings.leaderboard-size` | Number of top players to track | `10` |

## Placeholders

The plugin provides the following PlaceholderAPI placeholders:

### Player Name Only

Returns only the player's name for a specific rank.

```
%questleaderboard_top_1_name%
%questleaderboard_top_2_name%
...
%questleaderboard_top_10_name%
```

**Output Example:** `Steve`

### Quest Count Only

Returns only the number of completed quests for a specific rank.

```
%questleaderboard_top_1_quests%
%questleaderboard_top_2_quests%
...
%questleaderboard_top_10_quests%
```

**Output Example:** `42`

### Fallback Values

- If a rank doesn't exist (e.g., only 5 players but requesting `top_10`): `---`
- If an invalid rank is requested: `Invalid Rank`
- If an invalid placeholder format is used: `Invalid Placeholder`

### Usage Examples

#### In TAB, Scoreboard, Hologram Plugins (DeluxeTablist, TAB, etc.)

```yaml
  - "&6&lTop Quest Players"
  - "&e#1 &f%questleaderboard_top_1_name% &7- &a%questleaderboard_top_1_quests%"
  - "&e#2 &f%questleaderboard_top_2_name% &7- &a%questleaderboard_top_2_quests%"
  - "&e#3 &f%questleaderboard_top_3_name% &7- &a%questleaderboard_top_3_quests%"
```

#### In Chat Messages

```
/papi bcparse --null Congratulations to our top player: %questleaderboard_top_1_name% with %questleaderboard_top_1_quests% completed quests!
```

## Commands

### `/questleaderboard reload`

Reloads the plugin configuration and forces an immediate leaderboard update.

**Permission:** `questleaderboard.reload`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `questleaderboard.use` | Allows using the `/questleaderboard` command | `op` |
| `questleaderboard.reload` | Allows reloading the plugin configuration | `op` |

## Building from Source

### Prerequisites

- **Java 21 JDK**
- **Maven 3.8+** ([Apache Maven](https://maven.apache.org/))
- **Git** (optional, for cloning)

### Build Steps

1. **Clone the repository:**

```bash
git clone https://github.com/tino-id/quest-leaderboard.git
cd quests-leaderboard
```

2. **Build with Maven:**

```bash
mvn clean package
```

3. **Find the compiled JAR:**

```
target/quests-leaderboard-X.X.X.jar
```

## Troubleshooting

### Plugin won't load

**Problem:** Server shows `Could not load 'plugins/quests-leaderboard-X.X.jar'`

**Solution:**
- Ensure you're running Java 21 or higher: `/version`
- Check that PlaceholderAPI is installed and enabled
- Verify the plugin file isn't corrupted (re-download if necessary)

### Database connection errors

**Problem:** `Could not connect to database!` in console

**Solutions:**
1. **Verify credentials** in `config.yml` match your MySQL server
2. **Check MySQL is running:** `systemctl status mysql` (Linux) or Task Manager (Windows)
3. **Test connection manually:**
```bash
mysql -h 127.0.0.1 -u root -p minecraft
```
4. **Check firewall rules** if MySQL is on a remote server
5. **Increase connection timeout** in config if network is slow

### Placeholders showing as `---`

**Problem:** Placeholders display `---` instead of player data

**Possible Causes:**
- Not enough players in the database (e.g., requesting `top_10` but only 5 players exist)
- Database tables are empty
- Leaderboard hasn't updated yet (wait for next update interval)

**Solutions:**
- Force an update: `/questleaderboard reload`
- Check database has quest completion data
- Verify the SQL query returns results (run manually in MySQL)

### Placeholders not working at all

**Problem:** Placeholders show as raw text `%questleaderboard_top_1_name%`

**Solutions:**
1. **Install PlaceholderAPI** if not present
2. **Reload PlaceholderAPI:** `/papi reload`
3. **Re-register expansion:** `/questleaderboard reload`
4. **Check plugin load order** - ensure PlaceholderAPI loads before QuestsLeaderboard
5. **Verify placeholder syntax** - use `/papi parse me %questleaderboard_top_1_name%` to test

## Compatibility

### Tested Minecraft Versions

- ✅ Paper 1.21.X
- ⚠️ Spigot 1.21.x (should work, not extensively tested)
- ❌ 1.20.x and below (not supported)

## Support & Contributing

### Getting Help

1. **Check this README** for common issues
2. **Search existing issues** on GitHub
3. **Create a new issue** with full details:
   - Server version (`/version`)
   - Plugin version
   - Full error logs
   - Configuration file

### Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Reporting Bugs

When reporting bugs, include:

- **Server Version:** Output of `/version`
- **Plugin Version:** Check plugin JAR name or `/plugins`
- **Java Version:** Output of `java -version`
- **Error Logs:** Full stack trace from console
- **Steps to Reproduce:** Detailed steps
- **Expected Behavior:** What should happen
- **Actual Behavior:** What actually happens

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

- **HikariCP:** Fast, simple, reliable connection pooling
- **PlaceholderAPI:** Flexible placeholder framework
- **Paper Project:** High-performance Minecraft server
- **Quests:** Quest plugin for Minecraft

---

**Made with ❤️ for the Minecraft community**
