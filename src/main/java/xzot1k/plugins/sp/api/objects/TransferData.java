/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xzot1k.plugins.sp.SimplePortals;

import java.util.ArrayList;
import java.util.List;

public class TransferData {

    private String originServerName;
    private SerializableLocation destination;
    private List<String> commands, extras;

    public TransferData(@NotNull String serverName, @NotNull String coords, @Nullable String commandLine, @Nullable String extra) {
        setOriginServerName(serverName);
        setDestination(new SerializableLocation(SimplePortals.getPluginInstance(), coords));
        setCommands(new ArrayList<>());
        setExtras(new ArrayList<>());

        if (commandLine != null && !commandLine.isEmpty()) {
            if (commandLine.contains("_{S}_")) {
                String[] commands = commandLine.split("_\\{S}_");
                for (int i = -1; ++i < commands.length; ) getCommands().add(commands[i]);
            } else getCommands().add(commandLine);
        }

        if (extra != null && !extra.isEmpty()) {
            if (extra.contains("_{S}_")) {
                String[] args = extra.split("_\\{S}_");
                for (int i = -1; ++i < args.length; ) getExtras().add(args[i]);
            } else getExtras().add(extra);
        }
    }


    // getters & setters
    public SerializableLocation getDestination() {return destination;}

    public void setDestination(SerializableLocation destination) {this.destination = destination;}

    public String getOriginServerName() {return originServerName;}

    public void setOriginServerName(String originServerName) {this.originServerName = originServerName;}

    public List<String> getCommands() {return commands;}

    public void setCommands(List<String> commands) {this.commands = commands;}

    public List<String> getExtras() {return extras;}

    public void setExtras(List<String> extras) {this.extras = extras;}
}