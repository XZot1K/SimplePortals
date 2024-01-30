/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.api.Manager;
import xzot1k.plugins.sp.core.Commands;
import xzot1k.plugins.sp.core.Listeners;
import xzot1k.plugins.sp.core.TabCompleter;
import xzot1k.plugins.sp.core.tasks.ManagementTask;
import xzot1k.plugins.sp.core.utils.UpdateChecker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class SimplePortals extends JavaPlugin {
    private static SimplePortals pluginInstance;
    private Manager manager;
    private String serverVersion;
    private Connection databaseConnection;
    private ManagementTask managementTask;
    private FileConfiguration langConfig;
    private File langFile;
    private boolean prismaInstalled;

    // getters & setters
    public static SimplePortals getPluginInstance() {
        return pluginInstance;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    private void updateConfigs() {
        long startTime = System.currentTimeMillis();
        int totalUpdates = 0;

        String[] configNames = {"config", "lang"};
        for (int i = -1; ++i < configNames.length; ) {
            String name = configNames[i];

            InputStream inputStream = getClass().getResourceAsStream("/" + name + ".yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
            int updateCount = updateKeys(yaml, name.equalsIgnoreCase("config") ? getConfig() : getLangConfig());

            if (name.equalsIgnoreCase("config")) {
                String createSound = getConfig().getString("teleport-sound");
                if (getServerVersion().startsWith("v1_8") || getServerVersion().startsWith("v1_7")) {
                    if (createSound != null && createSound.equalsIgnoreCase("ENTITY_GHAST_SHOOT")) {
                        getConfig().set("teleport-sound", "GHAST_CHARGE");
                        updateCount++;
                    }
                } else if (createSound != null && createSound.equalsIgnoreCase("GHAST_CHARGE")) {
                    getConfig().set("teleport-sound", "ENTITY_GHAST_SHOOT");
                    updateCount++;
                }
            }

            try {
                inputStream.close();
                reader.close();
            } catch (IOException e) {
                log(Level.WARNING, e.getMessage());
            }

            if (updateCount > 0)
                switch (name) {
                    case "config":
                        saveConfig();
                        break;
                    case "lang":
                        saveLangConfig();
                        break;
                    default:
                        break;
                }

            if (updateCount > 0) {
                totalUpdates += updateCount;
                log(Level.INFO, updateCount + " things were fixed, updated, or removed in the '" + name
                        + ".yml' configuration file. (Took " + (System.currentTimeMillis() - startTime) + "ms)");
            }
        }

        if (totalUpdates > 0) {
            reloadConfigs();
            log(Level.INFO, "A total of " + totalUpdates + " thing(s) were fixed, updated, or removed from all the " +
                    "configuration together. (Took " + (System.currentTimeMillis() - startTime) + "ms)");
            log(Level.WARNING, "Please go checkout the configuration files as they are no longer the same as their default counterparts.");
        } else
            log(Level.INFO, "Everything inside the configuration seems to be up to date. (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private int updateKeys(FileConfiguration jarYaml, FileConfiguration currentYaml) {
        int updateCount = 0;
        ConfigurationSection currentConfigurationSection = currentYaml.getConfigurationSection(""),
                latestConfigurationSection = jarYaml.getConfigurationSection("");
        if (currentConfigurationSection != null && latestConfigurationSection != null) {
            Set<String> newKeys = latestConfigurationSection.getKeys(true), currentKeys = currentConfigurationSection.getKeys(true);
            for (String updatedKey : newKeys)
                if (!currentKeys.contains(updatedKey)) {
                    currentYaml.set(updatedKey, jarYaml.get(updatedKey));
                    updateCount++;
                }

            for (String currentKey : currentKeys)
                if (!newKeys.contains(currentKey)) {
                    currentYaml.set(currentKey, null);
                    updateCount++;
                }
        }

        return updateCount;
    }

    // custom configurations

    /**
     * Reloads all configs associated with DisplayShops.
     */
    public void reloadConfigs() {
        reloadConfig();

        if (langFile == null) langFile = new File(getDataFolder(), "lang.yml");
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream path = this.getResource("lang.yml");
        internalReloadConfig(path, langConfig);
    }

    private void internalReloadConfig(InputStream path, FileConfiguration portalsConfig) {
        Reader defConfigStream;
        if (path != null) {
            defConfigStream = new InputStreamReader(path, StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            portalsConfig.setDefaults(defConfig);

            try {
                path.close();
                defConfigStream.close();
            } catch (IOException e) {
                log(Level.WARNING, e.getMessage());
            }
        }
    }

    /**
     * Gets the language file configuration.
     *
     * @return The FileConfiguration found.
     */
    public FileConfiguration getLangConfig() {
        if (langConfig == null) reloadConfigs();
        return langConfig;
    }

    /**
     * Saves the default configuration files (Doesn't replace existing).
     */
    public void saveDefaultConfigs() {
        saveDefaultConfig();
        if (langFile == null) langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) saveResource("lang.yml", false);
        reloadConfigs();
    }

    private void saveLangConfig() {
        if (langConfig == null || langFile == null) return;
        try {
            getLangConfig().save(langFile);
        } catch (IOException e) {
            log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        pluginInstance = this;
        setServerVersion(getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);

        File file = new File(getDataFolder(), "/config.yml");
        if (file.exists()) {
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection cs = yaml.getConfigurationSection("");
            if (cs != null && cs.contains("language-section"))
                file.renameTo(new File(getDataFolder(), "/old-config.yml"));
        }

        saveDefaultConfigs();
        reloadConfigs();
        updateConfigs();

        setupDatabase();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        setPrismaInstalled(getServer().getPluginManager().getPlugin("Prisma") != null);
        manager = new Manager(this);

        TabCompleter tabCompleter = new TabCompleter(this);
        Commands commands = new Commands(this);
        for (Map.Entry<String, Map<String, Object>> entry : getDescription().getCommands().entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            if (command == null) continue;

            command.setTabCompleter(tabCompleter);
            command.setExecutor(commands);
        }

        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getManager().convertFromPortalsFile();

        if (getConfig().getBoolean("management-task")) {
            setManagementTask(new ManagementTask(this));
            getManagementTask().runTaskTimerAsynchronously(this, 0, 200);
        }

        try {
            final UpdateChecker updateChecker = new UpdateChecker(this, 56772);
            if (updateChecker.checkForUpdates()) log(Level.INFO, "The version " + getDescription().getVersion()
                    + " doesn't match the latest version!");
            else log(Level.INFO, "Everything looks like it is up to date!");
        } catch (Exception e) {log(Level.INFO, "Unable to check for updates ('" + e.getMessage() + "').");}
    }

    public void log(Level level, String text) {getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + text);}

    // cross server
    private synchronized void setupDatabase() {
        final String host = getConfig().getString("mysql.host");
        if (host == null || host.isEmpty()) return;

        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                Class.forName("com.mysql.jdbc.Driver");
            }
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {return;}

        final boolean useSSL = getConfig().getBoolean("mysql.use-ssl");
        final String databaseName = getConfig().getString("mysql.database"),
                port = getConfig().getString("mysql.port"), username = getConfig().getString("mysql.username"),
                password = getConfig().getString("mysql.password"), syntax = ("jdbc:mysql://" + host + ":" + port + "/"
                + databaseName + "?useSSL=" + (useSSL ? "true" : "false") + "&autoReconnect=true&useUnicode=yes"),
                transferTable = getConfig().getString("mysql.transfer-table");

        try {
            databaseConnection = DriverManager.getConnection(syntax, username, password);

            try (PreparedStatement statement = getDatabaseConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + transferTable + " (uuid VARCHAR(100) PRIMARY KEY NOT NULL, server LONGTEXT, coordinates LONGTEXT, commands LONGTEXT, extra LONGTEXT);")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                log(Level.WARNING, "Unable to create the \"" + transferTable + "\" table.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            log(Level.WARNING, "Unable to establish a connection to the database.");
        }
    }

    public synchronized void sendTransferMessage(@NotNull Player player, @NotNull String serverName, @NotNull String coordsString,
                                                 @NotNull List<String> commands, @NotNull String... extra) {
        if (getDatabaseConnection() == null) return;

        StringBuilder commandLine = new StringBuilder();
        if (commands != null) for (int i = -1; ++i < commands.size(); ) {
            if (commandLine.length() > 0) commandLine.append("_{S}_");
            commandLine.append(commands.get(i));
        }

        StringBuilder extraLine = new StringBuilder();
        if (extra != null) for (int i = -1; ++i < extra.length; ) {
            if (extraLine.length() > 0) extraLine.append("_{S}_");
            extraLine.append(extra[i]);
        }

        final String table = getConfig().getString("mysql.transfer-table");
        try (PreparedStatement statement = getDatabaseConnection().prepareStatement("INSERT INTO " + table
                + "(uuid, server, coordinates, commands, extra) VALUES( '" + player.getUniqueId() + "', '" + serverName + "', '" + coordsString + "', '" + commandLine + "', '" + extraLine + "')"
                + " ON DUPLICATE KEY UPDATE uuid = '" + player.getUniqueId() + "', server = '" + serverName + "', coordinates = '" + coordsString + "', commands = '" + commandLine
                + "', extra = '" + extraLine + "';")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log(Level.WARNING, "Unable to update the \"" + table + "\" with transfer data for the player \"" + player.getName() + "\".");
        }
    }

    public Manager getManager() {
        return manager;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    private void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public boolean isPrismaInstalled() {
        return prismaInstalled;
    }

    private void setPrismaInstalled(boolean prismaInstalled) {
        this.prismaInstalled = prismaInstalled;
    }

    public ManagementTask getManagementTask() {
        return managementTask;
    }

    public void setManagementTask(ManagementTask managementTask) {
        this.managementTask = managementTask;
    }

    public Connection getDatabaseConnection() {return databaseConnection;}
}