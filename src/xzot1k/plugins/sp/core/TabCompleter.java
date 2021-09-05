/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PortalCommandType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private SimplePortals pluginInstance;

    final List<String> values;
    final List<String> partialValues;

    public TabCompleter(SimplePortals pluginInstance) {
        setPluginInstance(pluginInstance);
        values = new ArrayList<>();
        partialValues = new ArrayList<>();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if ( command.getName().equalsIgnoreCase("simpleportals") && commandSender.hasPermission("simpleportals.use") ) {
            values.clear();
            partialValues.clear();
            if (args.length == 1) {

                values.add("switchserver");
                values.add("selectionmode");
                values.add("reload");
                values.add("showregion");
                values.add("setlocation");
                values.add("info");
                values.add("create");
                values.add("delete");
                values.add("fill");
                values.add("relocate");
                values.add("addcommand");
                values.add("clearcommands");
                values.add("togglecommandonly");
                values.add("commands");
                values.add("enable");
                values.add("disable");
                values.add("message");
                values.add("disablemessages");
                values.add("list");

            } else if (args.length == 2 || (args.length == 3 && (args[0].equalsIgnoreCase("setlocation") || args[0].equalsIgnoreCase("sl")))    ){
                values.addAll(getPluginInstance().getManager().getPortalNames(false));
            } else if (args.length == 3) {
                int colonCount = 0;
                for (char character : args[2].toCharArray()) if (character == ':') colonCount++;

                if (colonCount == 1) for (PortalCommandType portalCommandType : PortalCommandType.values())
                    values.add(portalCommandType.name());
                else if (colonCount == 2) for (int i = 0; ++i < 100; )
                    values.add(String.valueOf(i));
            } else if (args.length == 4 && (args[0].equalsIgnoreCase("setlocation") || args[0].equalsIgnoreCase("sl"))) {
                int colonCount = 0;
                for (char character : args[2].toCharArray()) if (character == ':') colonCount++;

                if (colonCount == 1) {
                    values.add("BAR");
                    values.add("TITLE");
                    values.add("SUBTITLE");
                    values.add("NORMAL");
                } else if (colonCount == 2) for (int i = 0; ++i < 100; )
                    values.add(String.valueOf(i));
            }

            StringUtil.copyPartialMatches(args[args.length - 1], values, partialValues);



            //if (values.size() > 0) Collections.sort(values);
            return partialValues;
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
