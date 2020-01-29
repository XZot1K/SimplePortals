/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.events.PortalEnterEvent;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.SerializableLocation;

import java.util.List;
import java.util.Objects;

public class Listeners implements Listener {

    private SimplePortals pluginInstance;

    public Listeners(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        Portal portalFrom = pluginInstance.getManager().getPortalAtLocation(e.getBlock().getLocation());
        if (portalFrom != null && !portalFrom.isDisabled()) {
            e.setCancelled(true);
            return;
        }

        Portal portalTo = pluginInstance.getManager().getPortalAtLocation(e.getToBlock().getLocation());
        if (portalTo != null && !portalTo.isDisabled())
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null
                && pluginInstance.getManager().isInSelectionMode(e.getPlayer())) {
            e.setCancelled(true);
            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(),
                    PointType.POINT_ONE)) {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer(), PointType.POINT_ONE);
                String message = pluginInstance.getLangConfig().getString("point-1-set-message");
                if (message != null && !message.equalsIgnoreCase(""))
                    e.getPlayer().sendMessage(pluginInstance.getManager()
                            .colorText(pluginInstance.getLangConfig().getString("prefix") + message));
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null
                && pluginInstance.getManager().isInSelectionMode(e.getPlayer())) {
            e.setCancelled(true);

            if (pluginInstance.getServerVersion().toLowerCase().startsWith("v1_15") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_14")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_13") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_12")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_11") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_10")
                    || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_9"))
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

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != Objects.requireNonNull(e.getTo()).getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getTo());
            if (portal != null && !portal.isDisabled()) {
                PortalEnterEvent portalEnterEvent = new PortalEnterEvent(e.getPlayer(), portal, e.getFrom(), portal.getTeleportLocation().asBukkitLocation());
                pluginInstance.getServer().getPluginManager().callEvent(portalEnterEvent);
                if (portalEnterEvent.isCancelled()) return;

                final boolean canBypassCooldown = e.getPlayer().hasPermission("simpleportals.cdbypass"),
                        cooldownFail = (pluginInstance.getConfig().getBoolean("use-portal-cooldown")
                                && (pluginInstance.getManager().isPlayerOnCooldown(e.getPlayer(), "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration"))
                                || pluginInstance.getManager().isPlayerOnCooldown(e.getPlayer(), "join-protection", pluginInstance.getConfig().getInt("join-protection-cooldown")))
                                && !canBypassCooldown),
                        permissionFail = !pluginInstance.getConfig().getBoolean("bypass-portal-permissions")
                                && !e.getPlayer().hasPermission("simpleportals.portal." + portal.getPortalId())
                                && !e.getPlayer().hasPermission("simpleportals.portals." + portal.getPortalId())
                                && !e.getPlayer().hasPermission("simpleportals.portal.*") && !e.getPlayer().hasPermission("simpleportals.portals.*");
                if (cooldownFail || permissionFail) {
                    double tv = pluginInstance.getConfig().getDouble("throw-velocity");
                    if (!(tv <= -1))
                        e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(e.getPlayer().getLocation().getDirection().getY() / 2).multiply(-tv));

                    String message = cooldownFail ? pluginInstance.getLangConfig().getString("enter-cooldown-message") : pluginInstance.getLangConfig().getString("enter-no-permission-message");
                    if (message != null && !message.equalsIgnoreCase(""))
                        e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix")
                                + message.replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(e.getPlayer(), "normal", -1)))));
                    return;
                }

                if (pluginInstance.getConfig().getBoolean("use-portal-cooldown") && !canBypassCooldown)
                    pluginInstance.getManager().updatePlayerPortalCooldown(e.getPlayer(), "normal");

                if (canBypassCooldown) {
                    final String message = pluginInstance.getLangConfig().getString("cd-bypass");
                    if (message != null && !message.equalsIgnoreCase(""))
                        e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix") + message));
                }

                for (int i = -1; ++i < portal.getCommands().size(); ) {
                    String commandLine = portal.getCommands().get(i);
                    if (commandLine.toUpperCase().endsWith(":PLAYER")) {
                        commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                                .replaceAll("(?i):chat", "");
                        pluginInstance.getServer().dispatchCommand(e.getPlayer(), commandLine.replace("{player}", e.getPlayer().getName()));
                    } else if (commandLine.toUpperCase().endsWith(":CONSOLE")) {
                        commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                                .replaceAll("(?i):chat", "");
                        pluginInstance.getServer().dispatchCommand(pluginInstance.getServer().getConsoleSender(), commandLine.replace("{player}", e.getPlayer().getName()));
                    } else if (commandLine.toUpperCase().endsWith(":CHAT")) {
                        commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                                .replaceAll("(?i):chat", "");
                        e.getPlayer().chat(commandLine.replace("{player}", e.getPlayer().getName()));
                    } else
                        pluginInstance.getServer().dispatchCommand(pluginInstance.getServer().getConsoleSender(), commandLine.replace("{player}", e.getPlayer().getName()));
                }

                if (!portal.isCommandsOnly() || portal.getTeleportLocation() == null) {
                    String particleEffect = pluginInstance.getConfig().getString("teleport-visual-effect");
                    if (particleEffect != null && !particleEffect.equalsIgnoreCase("")) {
                        final String particleFixed = particleEffect.toUpperCase().replace(" ", "_").replace("-", "_");
                        for (int i = -1; ++i < Particle.values().length; ) {
                            Particle currentParticle = Particle.values()[i];
                            if (currentParticle.name().equalsIgnoreCase(particleFixed)) {
                                pluginInstance.getManager().getParticleHandler().broadcastParticle(e.getPlayer().getLocation(),
                                        1, 2, 1, 0, currentParticle.name(), 50);
                                break;
                            }
                        }
                    }

                    final String sound = pluginInstance.getConfig().getString("teleport-sound");
                    if (sound != null && !sound.equalsIgnoreCase("")) {
                        final String soundFixed = sound.toUpperCase().replace(" ", "_").replace("-", "_");
                        for (int i = -1; ++i < Sound.values().length; ) {
                            Sound currentSound = Sound.values()[i];
                            if (currentSound.name().equalsIgnoreCase(soundFixed)) {
                                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), currentSound, 1, 1);
                                break;
                            }
                        }
                    }

                    String message = pluginInstance.getLangConfig().getString("portal-message");
                    if (message != null && !message.equalsIgnoreCase(""))
                        e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix") + message
                                .replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(e.getPlayer(),
                                        "normal", pluginInstance.getConfig().getInt("portal-cooldown-duration"))))));

                    portal.performAction(e.getPlayer());
                }
            } else {
                if (!pluginInstance.getManager().getSmartTransferMap().isEmpty() && pluginInstance.getManager().getSmartTransferMap().containsKey(e.getPlayer().getUniqueId())) {
                    SerializableLocation serializableLocation = pluginInstance.getManager().getSmartTransferMap().get(e.getPlayer().getUniqueId());
                    if (serializableLocation != null) {
                        Location location = e.getPlayer().getLocation();
                        serializableLocation.setWorldName(Objects.requireNonNull(location.getWorld()).getName());
                        serializableLocation.setX(location.getX());
                        serializableLocation.setY(location.getY());
                        serializableLocation.setZ(location.getZ());
                        serializableLocation.setYaw(location.getYaw());
                        serializableLocation.setPitch(location.getPitch());
                        return;
                    }
                }

                pluginInstance.getManager().getSmartTransferMap().put(e.getPlayer().getUniqueId(), new SerializableLocation(pluginInstance, e.getFrom()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if (pluginInstance.getConfig().getBoolean("join-protection") && pluginInstance.getConfig().getBoolean("use-portal-cooldown")
                && !e.getPlayer().hasPermission("simpleportals.cdbypass")) {

            Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getPlayer().getLocation());
            if (portal == null || portal.isDisabled()) return;

            pluginInstance.getManager().updatePlayerPortalCooldown(e.getPlayer(), "join-protection");
            double tv = pluginInstance.getConfig().getDouble("throw-velocity");
            if (!(tv <= -1)) e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection()
                    .setY(e.getPlayer().getLocation().getDirection().getY() / 2).multiply(-tv));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        pluginInstance.getManager().getSmartTransferMap().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(EntityPortalEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if (pluginInstance.getConfig().getBoolean("block-creative-portal-entrance") && player.getGameMode() == GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerPortalEvent e) {
        if (pluginInstance.getConfig().getBoolean("block-creative-portal-entrance") && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
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

}
