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
        updateChecker = new UpdateChecker(getPluginInstance());
        saveDefaultConfig();

        getCommand("simpleportals").setExecutor(new Commands(getPluginInstance()));
        getServer().getPluginManager().registerEvents(new Listeners(pluginInstance), this);

        getManager().loadPortals();
        getManager().sendConsoleMessage("&aThe plugin has enabled successfully!");

        if (generalTask != null) generalTask.cancel();

        int generalTaskDuration = getConfig().getInt("general-task-duration");
        if (!(generalTaskDuration <= -1))
            generalTask = getServer().getScheduler().runTaskTimerAsynchronously(getPluginInstance(), () ->
            {
                try
                {
                    if (updateChecker.isOutdated())
                        getManager().sendConsoleMessage("&cThe version &c" + getDescription().getVersion() + " &cdoes not match the latest version found on spigot, therefore it may be outdated!");
                    else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");
                } catch (Exception ignored) {}

                getManager().savePortals();
                getManager().sendConsoleMessage("&aAll portals have been saved!");
            }, 0, 20 * generalTaskDuration);
    }

    @Override
    public void onDisable()
    {
        if (generalTask != null) generalTask.cancel();
        getManager().savePortals();
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
