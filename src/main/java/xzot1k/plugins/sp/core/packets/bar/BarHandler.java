/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.bar;

import org.bukkit.entity.Player;

public interface BarHandler {
    void sendActionBar(Player player, String message);
}
