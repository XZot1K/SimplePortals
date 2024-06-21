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
        //removed command.getName().equalsIgnoreCase("simpleportals") && because not necessary
        if (commandSender.hasPermission("simpleportals.use") || commandSender.hasPermission("simpleportals.admin")) {
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
                values.add("togglecommandsonly");
                values.add("commands");
                values.add("enable");
                values.add("disable");
                values.add("message");
                values.add("disablemessages");
                values.add("list");
                values.add("cooldown");
                values.add("delay");


            } else if (args.length == 2 || (args.length == 3 && (args[0].equalsIgnoreCase("setlocation")
                    || args[0].equalsIgnoreCase("sl")))) {
                //The portal name should not be completed for every argument
                if (!args[0].equalsIgnoreCase("selectionmode") && !args[0].equalsIgnoreCase("sm")
                        && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
                    values.addAll(getPluginInstance().getManager().getPortalNames(false));
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("cooldown") || args[0].equalsIgnoreCase("delay")) {
                    for (int i = 0; i <= 12; i++) {
                        values.add("" + i);
                    }
                } else {
                    int colonCount = 0;
                    for (char character : args[2].toCharArray()) {
                        if (character == ':') {
                            colonCount++;
                        }
                    }

                    if (colonCount == 1) {
                        for (PortalCommandType portalCommandType : PortalCommandType.values()) {
                            values.add(portalCommandType.name());
                        }
                        //Return immediately, because copyPartialMatches breaks this for some reason
                        return values;
                    } else if (colonCount == 2) {
                        for (int i = 0; ++i < 100; ) {
                            values.add(String.valueOf(i));
                        }
                        //Return immediately, because copyPartialMatches breaks this for some reason
                        return values;

                    }
                }

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

                //Return immediately, because copyPartialMatches breaks this for some reason
                return values;

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