package xzot1k.plugins.sp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xzot1k.plugins.sp.api.Manager;
import xzot1k.plugins.sp.core.Commands;
import xzot1k.plugins.sp.core.Listeners;
import xzot1k.plugins.sp.core.utils.Metrics;
import xzot1k.plugins.sp.core.utils.UpdateChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class SimplePortals extends JavaPlugin {
    private static SimplePortals pluginInstance;
    private Manager manager;
    private UpdateChecker updateChecker;
    private String serverVersion;

    private FileConfiguration portalsConfig;
    private File portalsFile;

    @Override
    public void onEnable() {
        pluginInstance = this;
        setServerVersion(pluginInstance.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
        saveDefaultVersionConfig();
        updateConfig();

        manager = new Manager(getPluginInstance());
        updateChecker = new UpdateChecker(getPluginInstance(), 56772);
        saveDefaultConfig();
        saveDefaultPortalsConfig();

        Objects.requireNonNull(getCommand("simpleportals")).setExecutor(new Commands(getPluginInstance()));
        getServer().getPluginManager().registerEvents(new Listeners(pluginInstance), this);


        getManager().loadPortals();
        log(Level.INFO, "The plugin has enabled successfully!");

        try {
            if (updateChecker.checkForUpdates())
                log(Level.INFO, "The version " + getDescription().getVersion() + " is doesn't match the latest version!");
            else log(Level.INFO, "Everything looks like it is up to date!");
        } catch (Exception ignored) {
        }

        int generalTaskDuration = getConfig().getInt("general-task-duration");
        if (!(generalTaskDuration <= -1))
            getServer().getScheduler().runTaskTimerAsynchronously(getPluginInstance(), () ->
            {
                if (getConfig().getBoolean("update-checker"))
                    if (updateChecker.checkForUpdates())
                        log(Level.INFO, "The version " + getDescription().getVersion() + " is doesn't match the latest version!");
                    else log(Level.INFO, "Everything looks like it is up to date!");

                getManager().savePortals();
                if (getConfig().getBoolean("reload-plugin-timer")) {
                    getManager().getPortals().clear();
                    getManager().loadPortals();
                }

                log(Level.INFO, "All portals have been saved!");
            }, 20 * generalTaskDuration, 20 * generalTaskDuration);

        new Metrics(this);
    }

    @Override
    public void onDisable() {
        getManager().savePortals();
        log(Level.INFO, "All portals have been saved!");
        log(Level.INFO, "The plugin has been disabled.");
    }

    private void saveDefaultVersionConfig() {
        if (new File(getDataFolder(), "config.yml").exists()) return;

        if (getServerVersion().startsWith("v1_14") || getServerVersion().startsWith("v1_13") || getServerVersion().startsWith("v1_12")
                || getServerVersion().startsWith("v1_11") || getServerVersion().startsWith("v1_10") || getServerVersion().startsWith("v1_9")) {
            saveResource("config (1.9-1.14).yml", false);
            File file = new File(getDataFolder(), "config (1.9-1.14).yml");
            file.renameTo(new File(getDataFolder(), "config.yml"));
        } else {
            saveResource("config (Legacy).yml", false);
            File file = new File(getDataFolder(), "config (Legacy).yml");
            file.renameTo(new File(getDataFolder(), "config.yml"));
        }

        log(Level.INFO, getServerVersion() + " has been detected. Configuration created!");
    }

    private void updateConfig() {
        int updateCount = 0;
        File latestConfigFile;

        if (getServerVersion().startsWith("v1_14") || getServerVersion().startsWith("v1_13") || getServerVersion().startsWith("v1_12")
                || getServerVersion().startsWith("v1_11") || getServerVersion().startsWith("v1_10") || getServerVersion().startsWith("v1_9")) {
            saveResource("config (1.9-1.14).yml", true);
            latestConfigFile = new File(getDataFolder(), "config (1.9-1.14).yml");
        } else {
            saveResource("config (Legacy).yml", true);
            latestConfigFile = new File(getDataFolder(), "config (Legacy).yml");
        }

        FileConfiguration updatedYaml = YamlConfiguration.loadConfiguration(latestConfigFile);
        List<String> currentKeys = new ArrayList<>(Objects.requireNonNull(getConfig().getConfigurationSection("")).getKeys(true)),
                updatedKeys = new ArrayList<>(Objects.requireNonNull(updatedYaml.getConfigurationSection("")).getKeys(true));
        for (int i = -1; ++i < updatedKeys.size(); ) {
            String updatedKey = updatedKeys.get(i);
            if (!currentKeys.contains(updatedKey)) {
                getConfig().set(updatedKey, updatedYaml.get(updatedKey));
                updateCount += 1;
                log(Level.INFO, "Updated the '" + updatedKey + "' key within the configuration since it wasn't found.");
            }
        }

        for (int i = -1; ++i < currentKeys.size(); ) {
            String currentKey = currentKeys.get(i);
            if (!updatedKeys.contains(currentKey)) {
                getConfig().set(currentKey, null);
                updateCount += 1;
                log(Level.INFO, "Removed the '" + currentKey + "' key within the configuration since it was invalid.");
            }
        }

        if (updateCount > 0) {
            saveConfig();
            log(Level.INFO, "The configuration has been updated using the " + latestConfigFile.getName() + " file.");
            log(Level.WARNING, "Please go check out the configuration and customize these newly generated options to your liking. " +
                    "Messages and similar values may not appear the same as they did in the default configuration " +
                    "(P.S. Configuration comments have more than likely been removed to ensure proper syntax).");
        } else log(Level.INFO, "Everything inside the configuration seems to be up to date.");
        latestConfigFile.delete();
    }

    public void log(Level level, String text) {
        getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + text);
    }

    public static SimplePortals getPluginInstance() {
        return pluginInstance;
    }

    public Manager getManager() {
        return manager;
    }

    public void reloadPortalsConfig() {
        if (portalsFile == null) portalsFile = new File(getDataFolder(), "portals.yml");
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
        Reader defConfigStream = new InputStreamReader(Objects.requireNonNull(this.getResource("portals.yml")), StandardCharsets.UTF_8);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        portalsConfig.setDefaults(defConfig);
    }

    public FileConfiguration getPortalsConfig() {
        if (portalsConfig == null) reloadPortalsConfig();
        return portalsConfig;
    }

    public void savePortalsConfig() {
        if (portalsConfig == null || portalsFile == null) return;

        try {
            getPortalsConfig().save(portalsFile);
        } catch (IOException ignored) {
        }
    }

    private void saveDefaultPortalsConfig() {
        if (portalsFile == null) portalsFile = new File(getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) this.saveResource("portals.yml", false);
    }

    public String getServerVersion() {
        return serverVersion;
    }

    private void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }
}
