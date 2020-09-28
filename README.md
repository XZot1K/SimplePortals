# How to use SimplePortals API in your plugin

In order to properly use SimplePortals you will need to be able to retrieve the plugin's instance. You can do this by following these instructions:

1. Download the SimplePortals plugin and add it to your plugin's dependencies.  
2. Make sure your plugin can grab the SimplePortals instance from your Main class file like below:  

```
import xzot1k.plugins.sp.SimplePortals;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class Main extends JavaPlugin
{

    private satic SimplePortals simplePortals;

    @Override
    public void onEnable()
    {
        if (!isSimplePortalsInstalled())
        {
            getServer().getPluginManager().disablePlugin(this);
            
            return; // This plugin is now disabled since SimplePortals was not installed.
        }

        // SimplePortals was found and now can be accessed with the getSimplePortals() getter.
    }

    // This method tells you whether SimplePortals is installed or not.
    private boolean isSimplePortalsInstalled()
    {
        SimplePortals simplePortals = (SimplePortals) getServer().getPluginManager().getPlugin("SimplePortals");
        
        if(simplePortals != null)
        {
            setSimplePortals(simplePortals);
            return true;
        }

        return false;
    }

    public static SimplePortals getSimplePortals() { return simplePortals; }

    private static void setSimplePortals(SimplePortals simplePortals) { Main.simplePortals = simplePortals; }

}
```

3. Once 1 and 2 are completed, add "depend: [SimplePortals]" or related thigns inside your plugin.yml (This step is optional, but never hurts to make sure SimplePortals is installed).  
4. Everything should be all set. As a test, call the getSimplePortals() method from your Main class and you will be able to access the Manager class!

***
I currently use DedicatedMC as the official ZotWare testing server. If you need something powerful in terms of hardware without digging a hole in your pocket give them a shot and let me know how you like them!
[![image](https://imgur.com/oqEKWO3.png)](https://dedimc.promo/XZot1K)
