/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import xzot1k.plugins.sp.SimplePortals;

public class ManagementTask extends BukkitRunnable {

    private final SimplePortals pluginInstance;

    public ManagementTask(SimplePortals pluginInstance) {this.pluginInstance = pluginInstance;}

    @Override
    public void run() {getPluginInstance().getManager().loadPortals();}

    // getters & setters
    private SimplePortals getPluginInstance() {return pluginInstance;}

}