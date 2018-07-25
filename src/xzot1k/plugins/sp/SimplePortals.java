package xzot1k.plugins.sp;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xzot1k.plugins.sp.api.Manager;
import xzot1k.plugins.sp.core.Commands;
import xzot1k.plugins.sp.core.Listeners;
import xzot1k.plugins.sp.core.utils.UpdateChecker;

public class SimplePortals extends JavaPlugin
{
    private static SimplePortals pluginInstance;
    private Manager manager;
    private UpdateChecker updateChecker;
    private BukkitTask generalTask;

    @Override
    public void onEnable()
    {
        pluginInstance = this;
        manager = new Manager(getPluginInstance());
        updateChecker = new UpdateChecker(getPluginInstance(), 56772);
        saveDefaultConfig();

        getCommand("simpleportals").setExecutor(new Commands(getPluginInstance()));
        getServer().getPluginManager().registerEvents(new Listeners(pluginInstance), this);

        getManager().loadPortals();
        getManager().sendConsoleMessage("&aThe plugin has enabled successfully!");

        if (generalTask != null) generalTask.cancel();

        try
        {
            if (updateChecker.checkForUpdates())
                getManager().sendConsoleMessage("&cThe version &e" + getDescription().getVersion()
                        + " &cis doesn't match the latest version!");
            else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");
        } catch (Exception ignored) {}

        int generalTaskDuration = getConfig().getInt("general-task-duration");
        if (!(generalTaskDuration <= -1))
            generalTask = getServer().getScheduler().runTaskTimerAsynchronously(getPluginInstance(), () ->
            {
                try
                {
                    if (updateChecker.checkForUpdates())
                        getManager().sendConsoleMessage("&cThe version &e" + getDescription().getVersion()
                                + " &cis doesn't match the latest version!");
                    else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");
                } catch (Exception ignored) {}

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
        if (generalTask != null) generalTask.cancel();
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
}
