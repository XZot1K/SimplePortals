/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xzot1k.plugins.sp.api.objects.Portal;

public class PortalEnterEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Entity entity;
    private final Portal portal;
    private Location targetLocation, initialLocation;

    public PortalEnterEvent(Entity entity, Portal portal, Location initialLocation, Location targetLocation) {
        this.entity = entity;
        this.portal = portal;
        setTargetLocation(targetLocation);
        setInitialLocation(initialLocation);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Portal getPortal() {
        return portal;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
    }

    public Entity getEntity() {
        return entity;
    }
}
