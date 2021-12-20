/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.enums;

import org.bukkit.entity.Player;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public static Direction getYaw(Player player) {
        return values()[Math.round(player.getLocation().getYaw() / 90f) & 0x3];
    }
}
