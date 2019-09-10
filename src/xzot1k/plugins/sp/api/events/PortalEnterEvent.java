package xzot1k.plugins.sp.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xzot1k.plugins.sp.api.objects.Portal;

public class PortalEnterEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player player;
    private Portal portal;

    public PortalEnterEvent(Player player, Portal portal)
    {
        this.player = player;
        this.portal = portal;
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

    public static HandlerList getHandlerList() {
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
}
