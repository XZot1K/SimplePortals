package xzot1k.plugins.sp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xzot1k.plugins.sp.api.Manager;
import xzot1k.plugins.sp.core.Commands;
import xzot1k.plugins.sp.core.Listeners;
import xzot1k.plugins.sp.core.utils.UpdateChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SimplePortals extends JavaPlugin
{
    private static SimplePortals pluginInstance;
    private Manager manager;
    private UpdateChecker updateChecker;

    private FileConfiguration portalsConfig;
    private File portalsFile;

    @Override
    public void onEnable()
    {
        pluginInstance = this;
        manager = new Manager(getPluginInstance());
        updateChecker = new UpdateChecker(getPluginInstance(), 56772);
        saveDefaultConfig();
        saveDefaultPortalsConfig();

        Objects.requireNonNull(getCommand("simpleportals")).setExecutor(new Commands(getPluginInstance()));
        getServer().getPluginManager().registerEvents(new Listeners(pluginInstance), this);


        getManager().loadPortals();
        getManager().sendConsoleMessage("&aThe plugin has enabled successfully!");

        try
        {
            if (updateChecker.checkForUpdates())
                getManager().sendConsoleMessage("&cThe version &e" + getDescription().getVersion()
                        + " &cis doesn't match the latest version!");
            else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");
        } catch (Exception ignored) {}

        int generalTaskDuration = getConfig().getInt("general-task-duration");
        if (!(generalTaskDuration <= -1))
            getServer().getScheduler().runTaskTimerAsynchronously(getPluginInstance(), () ->
            {
                if (getConfig().getBoolean("update-checker"))
                    if (updateChecker.checkForUpdates())
                        getManager().sendConsoleMessage("&cThe version &e" + getDescription().getVersion()
                                + " &cis doesn't match the latest version!");
                    else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");

                getManager().savePortals();
                if (getConfig().getBoolean("reload-plugin-timer"))
                {
                    getManager().getPortals().clear();
                    getManager().loadPortals();
                }

                getManager().sendConsoleMessage("&aAll portals have been saved!");
            }, 20 * generalTaskDuration, 20 * generalTaskDuration);
    }

    @Override
    public void onDisable()
    {
        getManager().savePortals();
        getManager().sendConsoleMessage("&aAll portals have been saved!");
        getManager().sendConsoleMessage("&cThe plugin has been disabled.");
    }

    public static SimplePortals getPluginInstance()
    {
        return pluginInstance;
    }

    public Manager getManager()
    {
        return manager;
    }

    public void reloadPortalsConfig()
    {
        if (portalsFile == null) portalsFile = new File(getDataFolder(), "portals.yml");
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
        Reader defConfigStream = new InputStreamReader(this.getResource("portals.yml"), StandardCharsets.UTF_8);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        portalsConfig.setDefaults(defConfig);
    }

    public FileConfiguration getPortalsConfig()
    {
        if (portalsConfig == null) reloadPortalsConfig();
        return portalsConfig;
    }

    public void savePortalsConfig()
    {
        if (portalsConfig == null || portalsFile == null) return;

        try
        {
            getPortalsConfig().save(portalsFile);
        } catch (IOException ignored) {}
    }

    public void saveDefaultPortalsConfig()
    {
        if (portalsFile == null) portalsFile = new File(getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) this.saveResource("portals.yml", false);
    }
}
