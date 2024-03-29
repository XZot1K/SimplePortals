# How to use SimplePortals API in your plugin

In order to properly use SimplePortals you will need to be able to retrieve the plugin's instance. You can do this by following these instructions:

1. Download the SimplePortals plugin and add it to your plugin's dependencies.  
2. Make sure your plugin can grab the SimplePortals instance from your Main class file like below:  

```
import org.bukkit.plugin.java.JavaPlugin;
import xzot1k.plugins.sp.SimplePortals;

public class Main extends JavaPlugin {

    private SimplePortals simplePortals;

    @Override
    public void onEnable() {
        if (!isSimplePortalsInstalled()) {
            getServer().getPluginManager().disablePlugin(this); // disable plugin since SimplePortals is NOT installed.
            return;
        }

        // SimplePortals was found and now can be accessed with the getSimplePortals() getter.
    }

    // This method tells you whether SimplePortals is installed or not.
    private boolean isSimplePortalsInstalled() {
        if (getServer().getPluginManager().getPlugin("SimplePortals") != null) {
            this.simplePortals = SimplePortals.getPluginInstance();
            return true;
        }
        return false;
    }

    public SimplePortals getSimplePortals() { return simplePortals; }

}
```

3. Once 1 and 2 are completed, add "depend: [SimplePortals]" or related thigns inside your plugin.yml (This step is optional, but never hurts to make sure SimplePortals is installed).  
4. Everything should be all set. As a test, call the getSimplePortals() method from your Main class and you will be able to access the Manager class!
