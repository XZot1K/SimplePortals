package xzot1k.plugins.sp.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import xzot1k.plugins.sp.SimplePortals;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private SimplePortals pluginInstance;

    public TabCompleter(SimplePortals pluginInstance) {
        setPluginInstance(pluginInstance);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("simpleportals")) {

            if (args.length == 2) {
                List<String> portalNames = new ArrayList<>();
                for (int i = -1; ++i < getPluginInstance().getManager().getPortals().size(); )
                    portalNames.add(getPluginInstance().getManager().getPortals().get(i).getPortalId());
                return portalNames;
            }

        }

        return null;
    }

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }

    private void setPluginInstance(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
    }
}
