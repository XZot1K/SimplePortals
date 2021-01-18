/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.events.PortalEnterEvent;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.SerializableLocation;

import java.util.List;

public class Listeners implements Listener {

    private final SimplePortals pluginInstance;

    public Listeners(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getBlock().getLocation());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null
                && pluginInstance.getManager().isInSelectionMode(e.getPlayer())) {
            e.setCancelled(true);
            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(), PointType.POINT_ONE)) {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer(), PointType.POINT_ONE);
                String message = pluginInstance.getLangConfig().getString("point-1-set-message");
                if (message != null && !message.equalsIgnoreCase(""))
                    e.getPlayer().sendMessage(pluginInstance.getManager()
                            .colorText(pluginInstance.getLangConfig().getString("prefix") + message));
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && pluginInstance.getManager().isInSelectionMode(e.getPlayer())) {
            e.setCancelled(true);

            if (pluginInstance.getServerVersion().toLowerCase().startsWith("v1_16") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_15")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_14") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_13")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_12") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_11")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_10") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_9"))
                if (e.getHand() != EquipmentSlot.HAND)
                    return;

            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(), PointType.POINT_TWO)) {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer(), PointType.POINT_TWO);

                String message = pluginInstance.getLangConfig().getString("point-2-set-message");
                if (message != null && !message.equalsIgnoreCase(""))
                    e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix") + message));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
            initiatePortalStuff(e.getTo(), e.getFrom(), e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (pluginInstance.getConfig().getBoolean("join-protection") && pluginInstance.getConfig().getBoolean("use-portal-cooldown")
                && !e.getPlayer().hasPermission("simpleportals.cdbypass")) {

            Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getPlayer().getLocation());
            if (portal == null || portal.isDisabled()) return;

            pluginInstance.getManager().updatePlayerPortalCooldown(e.getPlayer(), "join-protection");
            double tv = pluginInstance.getConfig().getDouble("throw-velocity");
            if (!(tv <= -1))
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(e.getPlayer().getLocation().getDirection().getY() / 2).multiply(-tv));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pluginInstance.getManager().getSmartTransferMap().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (!e.getCause().name().toUpperCase().contains("PORTAL")) return;

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
    }

    @EventHandler
    public void onMove(VehicleMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
            initiatePortalStuff(e.getTo(), e.getFrom(), e.getVehicle());
    }

    @EventHandler
    public void onPortalEntryFirst(PlayerPortalEvent e) {
        if (pluginInstance.getConfig().getBoolean("block-creative-portal-entrance") && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            e.setCancelled(true);
            e.setCanCreatePortal(false);
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
    }

    @EventHandler
    public void onPortalEntryLast(PortalEnterEvent e) {
        if (e.isCancelled()) return;

        if (pluginInstance.getConfig().getBoolean("block-creative-portal-entrance") && (e.getEntity() instanceof Player
                && ((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE)) {
            e.setCancelled(true);
            e.setTargetLocation(null);
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getInitialLocation());
        if (portal != null && !portal.isDisabled()) {
            e.setCancelled(true);
            e.setTargetLocation(null);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (pluginInstance.getConfig().getBoolean("item-transfer"))
            pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance,
                    () -> initiatePortalStuff(e.getEntity().getLocation(), e.getLocation(), e.getEntity()),
                    20L * pluginInstance.getConfig().getInt("item-teleport-delay"));
    }

    @EventHandler
    public void onItemSpawn(PlayerDropItemEvent e) {
        if (pluginInstance.getConfig().getBoolean("item-transfer")) {
            final Location startLocation = e.getItemDrop().getLocation().clone();
            pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance,
                    () -> initiatePortalStuff(e.getItemDrop().getLocation(), startLocation, e.getItemDrop()),
                    20L * pluginInstance.getConfig().getInt("item-teleport-delay"));
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        List<String> blockedMobs = pluginInstance.getConfig().getStringList("creature-spawning-blacklist");
        for (int i = -1; ++i < blockedMobs.size(); ) {
            String blockedMob = blockedMobs.get(i);
            if (blockedMob.replace(" ", "_").replace("-", "_").equalsIgnoreCase(e.getEntity().getType().name())) {
                Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getLocation());
                if (portal != null && !portal.isDisabled()) e.setCancelled(true);
                break;
            }
        }
    }

    private void initiatePortalStuff(Location toLocation, Location fromLocation, Entity entity) {
        final boolean isPlayer = (entity instanceof Player);
        Portal portal = pluginInstance.getManager().getPortalAtLocation(toLocation);
        if (portal != null && !portal.isDisabled()) {
            if (isPlayer) {
                final Player player = (Player) entity;
                if (pluginInstance.getManager().getPortalLinkMap().containsKey(player.getUniqueId())
                        && pluginInstance.getManager().getPortalLinkMap().get(player.getUniqueId()).equalsIgnoreCase(portal.getPortalId()))
                    return;
                else pluginInstance.getManager().getPortalLinkMap().remove(player.getUniqueId());
            }

            PortalEnterEvent portalEnterEvent = new PortalEnterEvent(entity, portal, fromLocation, portal.getTeleportLocation().asBukkitLocation());
            pluginInstance.getServer().getPluginManager().callEvent(portalEnterEvent);
            if (portalEnterEvent.isCancelled()) return;

            final boolean canBypassCooldown = entity.hasPermission("simpleportals.cdbypass");
            if (isPlayer) {
                final Player player = (Player) entity;
                final boolean cooldownFail = (pluginInstance.getConfig().getBoolean("use-portal-cooldown") && (pluginInstance.getManager().isPlayerOnCooldown(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration"))
                        || pluginInstance.getManager().isPlayerOnCooldown(player, "join-protection", pluginInstance.getConfig().getInt("join-protection-cooldown"))) && !canBypassCooldown),
                        permissionFail = !pluginInstance.getConfig().getBoolean("bypass-portal-permissions") && (!player.hasPermission("simpleportals.portal." + portal.getPortalId())
                                && !player.hasPermission("simpleportals.portals." + portal.getPortalId()) && !player.hasPermission("simpleportals.portal.*") && !player.hasPermission("simpleportals.portals.*"));
                if (cooldownFail || permissionFail) {
                    double tv = pluginInstance.getConfig().getDouble("throw-velocity");
                    if (!(tv <= -1))
                        player.setVelocity(player.getLocation().getDirection().setY(player.getLocation().getDirection().getY() / 2).multiply(-tv));

                    String message = cooldownFail ? pluginInstance.getLangConfig().getString("enter-cooldown-message")
                            : pluginInstance.getLangConfig().getString("enter-no-permission-message");
                    if (message != null && !message.equalsIgnoreCase(""))
                        player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix")
                                + message.replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal",
                                pluginInstance.getConfig().getInt("portal-cooldown-duration"))))));
                    return;
                }
            }

            if (isPlayer && pluginInstance.getConfig().getBoolean("use-portal-cooldown") && !canBypassCooldown)
                pluginInstance.getManager().updatePlayerPortalCooldown((Player) entity, "normal");

            if (isPlayer) portal.invokeCommands((Player) entity, toLocation);
            if (!portal.isCommandsOnly() || portal.getTeleportLocation() == null) {
                String particleEffect = pluginInstance.getConfig().getString("teleport-visual-effect");
                if (particleEffect != null && !particleEffect.isEmpty()) {
                    final String particleFixed = particleEffect.toUpperCase().replace(" ", "_").replace("-", "_");
                    pluginInstance.getManager().getParticleHandler().broadcastParticle(entity.getLocation(), 1, 2, 1, 0, particleFixed, 50);
                }

                final String sound = pluginInstance.getConfig().getString("teleport-sound");
                if (sound != null && !sound.equalsIgnoreCase("")) {
                    final String soundFixed = sound.toUpperCase().replace(" ", "_").replace("-", "_");
                    for (int i = -1; ++i < Sound.values().length; ) {
                        Sound currentSound = Sound.values()[i];
                        if (currentSound.name().equalsIgnoreCase(soundFixed)) {
                            entity.getWorld().playSound(entity.getLocation(), currentSound, 1, 1);
                            break;
                        }
                    }
                }

                if (isPlayer) {
                    final Player player = (Player) entity;
                    if (portal.getMessage() != null && !portal.getMessage().isEmpty())
                        player.sendMessage(pluginInstance.getManager().colorText(portal.getMessage().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration"))))));

                    if (portal.getBarMessage() != null && !portal.getBarMessage().isEmpty())
                        pluginInstance.getManager().sendBarMessage(player, portal.getBarMessage().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration")))));

                    if ((portal.getTitle() != null && !portal.getTitle().isEmpty()) && (portal.getSubTitle() != null && !portal.getSubTitle().isEmpty()))
                        pluginInstance.getManager().sendTitle(player, portal.getTitle().replace("{name}", portal.getPortalId())
                                        .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration")))),
                                portal.getSubTitle().replace("{name}", portal.getPortalId())
                                        .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration")))));
                    else if (portal.getTitle() != null && !portal.getTitle().isEmpty())
                        pluginInstance.getManager().sendTitle(player, portal.getTitle().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration")))), null);
                    else if (portal.getSubTitle() != null && !portal.getSubTitle().isEmpty())
                        pluginInstance.getManager().sendTitle(player, null, portal.getSubTitle().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration")))));

                }

                portal.performAction(entity);
            }
        } else {

            if (!isPlayer) return;
            final Player player = (Player) entity;

            pluginInstance.getManager().getPortalLinkMap().remove(player.getUniqueId());
            if (!pluginInstance.getManager().getSmartTransferMap().isEmpty() && pluginInstance.getManager().getSmartTransferMap().containsKey(player.getUniqueId())) {
                SerializableLocation serializableLocation = pluginInstance.getManager().getSmartTransferMap().get(player.getUniqueId());
                if (serializableLocation != null) {
                    Location location = player.getLocation();
                    if (location.getWorld() != null) {
                        serializableLocation.setWorldName(location.getWorld().getName());
                        serializableLocation.setX(location.getX());
                        serializableLocation.setY(location.getY());
                        serializableLocation.setZ(location.getZ());
                        serializableLocation.setYaw(location.getYaw());
                        serializableLocation.setPitch(location.getPitch());
                        return;
                    }
                }
            }

            pluginInstance.getManager().getSmartTransferMap().put(player.getUniqueId(), new SerializableLocation(pluginInstance, fromLocation));
        }
    }

}
