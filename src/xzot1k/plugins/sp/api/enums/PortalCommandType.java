/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.enums;

public enum PortalCommandType {
    CONSOLE, PLAYER, CHAT;

    public static PortalCommandType getType(String typeString) {
        for (PortalCommandType portalCommandType : PortalCommandType.values())
            if (portalCommandType.name().equalsIgnoreCase(typeString.replace(" ", "_").replace("-", "_")))
                return portalCommandType;
        return null;
    }

}
