package xzot1k.plugins.sp;

import org.bukkit.plugin.java.JavaPlugin;
import xzot1k.plugins.sp.api.Manager;
import xzot1k.plugins.sp.core.Commands;
import xzot1k.plugins.sp.core.Listeners;
import xzot1k.plugins.sp.core.UpdateChecker;

public class SimplePortals extends JavaPlugin
{
    private static SimplePortals pluginInstance;
    private Manager manager;
    private UpdateChecker updateChecker;

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

        /*if (updateChecker.isOutdated())
            getManager().sendConsoleMessage("&cThe version &c" + getDescription().getVersion() + " &cdoes not match the latest version found on spigot, therefore it may be outdated!");
        else getManager().sendConsoleMessage("&aEverything looks like it is up to date!");*/
    }

    @Override
    public void onDisable()
    {
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
