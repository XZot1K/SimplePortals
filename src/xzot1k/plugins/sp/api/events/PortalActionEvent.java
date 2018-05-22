package xzot1k.plugins.sp.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xzot1k.plugins.sp.api.objects.Portal;

public class PortalActionEvent extends Event implements Cancellable
{

    private final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player player;
    private Portal portal;
    private Location teleportLocation;

    public PortalActionEvent(Player player, Portal portal, Location teleportLocation)
    {
        this.player = player;
        this.portal = portal;
        this.teleportLocation = teleportLocation;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public Portal getPortal()
    {
        return portal;
    }

    public Player getPlayer()
    {
        return player;
    }

    public Location getTeleportLocation()
    {
        return teleportLocation;
    }
}
