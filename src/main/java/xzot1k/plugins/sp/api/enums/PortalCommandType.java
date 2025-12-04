/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.enums;

public enum PortalCommandType {
    CONSOLE, PLAYER, CHAT;

    public static PortalCommandType getType(String typeString) {
        String commandType = typeString.replace(" ", "_").replace("-", "_");

        for (PortalCommandType portalCommandType : PortalCommandType.values())
            if (portalCommandType.name().equalsIgnoreCase(commandType))
                return portalCommandType;
        return null;
    }

}