/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.config;

public enum LangKey {

    PREFIX("prefix"),
    NO_PERMISSION("no-permission-message"),
    MUST_BE_PLAYER("must-be-player-message"),
    PORTAL_EXISTS_AT_LOCATION("portal-location-exists-message"),
    PORTAL_EXISTS("portal-exists-message"),
    INVALID_REGION("selected-region-invalid-message"),
    INVALID_PORTAL("portal-invalid-message"),
    INVALID_WORLD("world-invalid-message"),
    SELECTION_MODE("selection-mode-message"),
    NOT_SAME_WORLD("not-same-world-message"),
    PORTAL_CREATED("portal-created-message"),
    PORTAL_DELETED("portal-deleted-message"),
    MESSAGE_SET("portal-message-set"),
    PORTAL_LINKED("portal-link-message"),
    SWITCH_SERVER_SET("switch-server-set-message"),
    RELOADED("reload-message"),
    LOCATION_SET("location-set-message"),
    SWITCH_LOCATION_SET("switch-location-set-message"),
    REGION_DISPLAYED("region-displayed-message"),
    POINT1_SET("point-1-set-message"),
    POINT2_SET("point-2-set-message"),
    REGION_RELOCATED("region-relocated-message"),
    COMMAND_ADDED("portal-command-added-message"),
    COMMANDS_CLEARED("portal-commands-cleared-message"),
    COMMAND_ONLY_TOGGLED("portal-command-only-toggle-message"),
    INVALID_PAGE("invalid-page-message"),
    INVALID_MATERIAL("invalid-material-message"),
    PORTAL_FILLED("portal-filled-message"),
    ENTER_COOLDOWN("enter-cooldown-message"),
    ENTER_NO_PERMISSION("enter-no-permission-message"),
    PORTAL_COMMANDS("portal-commands-message"),
    DM_DISABLED("portal-dm-message"),
    ALREADY_DISABLED("already-disabled-message"),
    ALREADY_ENABLED("already-enabled-message"),
    PORTAL_ENABLED("portal-enabled-message"),
    PORTAL_DISABLED("portal-disabled-message"),
    PORTAL_LIST("portal-list-message"),
    PORTAL_FIND("portal-find-message"),
    INVALID_RANGE("invalid-range"),
    INVALID_COOLDOWN("invalid-cooldown"),
    INVALID_DELAY("invalid-delay"),
    INVALID_WORLD_KEY("invalid-world"),
    INVALID_COORDINATE("invalid-coordinate"),
    NO_FIND_RESULTS("no-find-results"),
    COOLDOWN_SET("cooldown-set-message"),
    DELAY_SET("delay-set-message"),

    TELEPORT_TITLE("teleport.title"),
    TELEPORT_SUBTITLE("teleport.sub-title"),

    TELEPORT_CANCELLED_TITLE("teleport-cancelled.title"),
    TELEPORT_CANCELLED_SUBTITLE("teleport-cancelled.sub-title"),

    TELEPORT_DELAY_TITLE("teleport-delay.title"),
    TELEPORT_DELAY_SUBTITLE("teleport-delay.sub-title");

    private final String path;

    LangKey(String path) { this.path = path; }

    public String getPath() { return path; }
}
