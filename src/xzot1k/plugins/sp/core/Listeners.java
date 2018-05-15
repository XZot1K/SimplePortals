package xzot1k.plugins.sp.core;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.objects.Portal;

public class Listeners implements Listener
{

    private SimplePortals pluginInstance;

    public Listeners(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e)
    {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null
                && pluginInstance.getManager().isInSelectionMode(e.getPlayer()))
        {
            e.setCancelled(true);
            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(), PointType.POINT_ONE))
            {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer());
                e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("point-1-set-message")));
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null
                && pluginInstance.getManager().isInSelectionMode(e.getPlayer()))
        {
            e.setCancelled(true);
            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(), PointType.POINT_TWO))
            {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer());
                e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("point-2-set-message")));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
        {
            Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getTo());
            if (portal != null)
            {
                if (pluginInstance.getManager().isPlayerOnCooldown(e.getPlayer())) return;
                if (!e.getPlayer().hasPermission("simpleportals.portal." + portal.getPortalId())
                        || !e.getPlayer().hasPermission("simpleportals.portal.*")) return;

                try
                {
                    String particleEffect = pluginInstance.getConfig().getString("teleport-visual-effect")
                            .toUpperCase().replace(" ", "_").replace("-", "_");
                    e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf(pluginInstance.getConfig().getString("teleport-sound")
                            .toUpperCase().replace(" ", "_").replace("-", "_")), 1, 1);
                    pluginInstance.getManager().getParticleHandler().broadcastParticle(e.getPlayer().getLocation(), 1, 2, 1, 0, particleEffect, 50);
                } catch (Exception ignored) {}

                if (pluginInstance.getConfig().getBoolean("use-portal-cooldown"))
                    pluginInstance.getManager().updatePlayerPortalCooldown(e.getPlayer());

                e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-message")
                        .replace("{name}", portal.getPortalId())
                        .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(e.getPlayer())))));

                portal.performAction(e.getPlayer());
            }
        }
    }

}
