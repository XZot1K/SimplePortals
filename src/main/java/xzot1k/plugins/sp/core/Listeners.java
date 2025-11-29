/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.Config;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.events.PortalEnterEvent;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.SerializableLocation;
import xzot1k.plugins.sp.api.objects.TransferData;
import xzot1k.plugins.sp.core.tasks.TeleportTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.bukkit.GameMode.CREATIVE;

public class Listeners implements Listener {

    private final SimplePortals pluginInstance;
    private final HashSet<UUID> ptpProtectionPlayers;
    private final HashMap<UUID, TransferData> transferData;

    public Listeners(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
        this.ptpProtectionPlayers = new HashSet<>();
        this.transferData = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getBlock().getLocation());
        if (portal != null && !portal.isDisabled()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

            if (!pluginInstance.getServerVersion().toLowerCase().startsWith("v1_8"))
                if (e.getHand() != EquipmentSlot.HAND) return;

            if (pluginInstance.getManager().updateCurrentSelection(e.getPlayer(), e.getClickedBlock().getLocation(), PointType.POINT_TWO)) {
                pluginInstance.getManager().highlightBlock(e.getClickedBlock(), e.getPlayer(), PointType.POINT_TWO);

                String message = pluginInstance.getLangConfig().getString("point-2-set-message");
                if (message != null && !message.equalsIgnoreCase(""))
                    e.getPlayer().sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix") + message));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() != null && e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) initiatePortalStuff(e.getTo(), e.getFrom(), e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if (getTransferData().containsKey(e.getPlayer().getUniqueId())) {
            final TransferData data = getTransferData().get(e.getPlayer().getUniqueId());
            final Location dest = data.getDestination().asBukkitLocation();

            e.getPlayer().teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN);
            Portal.invokeCommands(data.getCommands(), e.getPlayer(), dest);
        }

        forceJoin(e.getPlayer());
        checkPTPProtection(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreJoin(AsyncPlayerPreLoginEvent e) {
        if (pluginInstance.getDatabaseConnection() == null) return;

        final String serverName = Config.get().mysqlServerName,
                table = Config.get().mysqlTransferTable;

        CompletableFuture<TransferData> destinationFuture = CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = pluginInstance.getDatabaseConnection().prepareStatement("SELECT * FROM " + table + " WHERE uuid = '" + e.getUniqueId() + "';");
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    final String server = resultSet.getString("server"), coords = resultSet.getString("coordinates"),
                            commandLine = resultSet.getString("commands"), extra = resultSet.getString("extra");

                    if (server.equalsIgnoreCase(serverName)) return new TransferData(server, coords, commandLine, extra);
                }
            } catch (SQLException ex) {pluginInstance.log(Level.WARNING, "There was an issue reading from the \"" + table + "\" table.");}
            return null;
        });

        TransferData transferData = destinationFuture.thenApply(data -> {
            if (data != null) {
                Player player = pluginInstance.getServer().getPlayer(e.getUniqueId());
                if (player != null && player.isOnline()) {
                    final Location dest = data.getDestination().asBukkitLocation();
                    player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Portal.invokeCommands(data.getCommands(), player, dest);
                } else getTransferData().put(e.getUniqueId(), data);
            }
            return data;
        }).join();

        if (transferData != null) {
            destinationFuture.thenRunAsync(() -> {
                try (PreparedStatement removalStatement = pluginInstance.getDatabaseConnection().prepareStatement("DELETE FROM " + table + " WHERE UUID = '" + e.getUniqueId() + "';")) {
                    removalStatement.executeUpdate();
                } catch (SQLException ex) {pluginInstance.log(Level.WARNING, "There was an issue reading from the \"" + table + "\" table.");}
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        pluginInstance.getManager().getSmartTransferMap().remove(e.getPlayer().getUniqueId());
        getPTPProtectionPlayers().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getBedSpawnLocation() == null) forceJoin(e.getPlayer());
        checkPTPProtection(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(EntityPortalEvent e) {
        if (!(e.getEntity() instanceof Player) || (e.getTo().getWorld().getEnvironment() != World.Environment.THE_END
                && e.getTo().getWorld().getEnvironment() != World.Environment.NETHER)) return;

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal != null && !portal.isDisabled()) {
            e.setCancelled(true);
            e.setTo(e.getFrom());
            return;
        }

        Location location = pluginInstance.getManager().handleVanillaPortalReplacements(e.getFrom().getWorld(),
                (e.getTo().getWorld().getEnvironment() == World.Environment.NETHER) ? PortalType.NETHER : PortalType.ENDER);
        if (location != null) e.setTo(location);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortal(PlayerRespawnEvent e) {
        if (e.getPlayer().getWorld().getEnvironment() != World.Environment.THE_END) return;

        // we don't want it to mess with deaths too if that config option is set to false
        if (!Config.get().endOverrideDeath) return;

        Location respawnLocation = pluginInstance.getManager().handleVanillaPortalReplacements(e.getPlayer().getWorld(), PortalType.ENDER);
        if (respawnLocation != null) e.setRespawnLocation(respawnLocation);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(VehicleMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
            initiatePortalStuff(e.getTo(), e.getFrom(), e.getVehicle());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleportReader(PlayerTeleportEvent e) {
        pluginInstance.getManager().getEntitiesInTeleportationAndPortals().remove(e.getPlayer().getUniqueId());

        final TeleportTask teleportTask = pluginInstance.getManager().getTeleportTasks().getOrDefault(e.getPlayer().getUniqueId(), null);
        if (teleportTask != null) teleportTask.cancel();
        pluginInstance.getManager().getTeleportTasks().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleportDestinationCheck(PlayerTeleportEvent e) {
        Portal fromPortal = pluginInstance.getManager().getPortalAtLocation(e.getFrom()),
                toPortal = pluginInstance.getManager().getPortalAtLocation(e.getTo());
        if (fromPortal != null && toPortal != null && !toPortal.isDisabled()) getPTPProtectionPlayers().add(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {vanillaPortalHelper(e);}

    @EventHandler(ignoreCancelled = true)
    public void onPortalEntry(PlayerPortalEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE && pluginInstance.getManager().isPortalNearby(e.getFrom(), 5)) e.setCancelled(true);

        if (Config.get().blockCreativePortal && e.getPlayer().getGameMode() == CREATIVE) {
            for (Portal portal : pluginInstance.getManager().getPortalMap().values()) {
                SerializableLocation centerPortal = new SerializableLocation(pluginInstance, portal.getRegion().getPoint1().getWorldName(),
                        ((portal.getRegion().getPoint1().getX() + portal.getRegion().getPoint2().getX()) / 2),
                        ((portal.getRegion().getPoint1().getY() + portal.getRegion().getPoint2().getY()) / 2),
                        ((portal.getRegion().getPoint1().getZ() + portal.getRegion().getPoint2().getZ()) / 2), 0, 0);
                if (centerPortal.distance(e.getFrom(), true) <= 2) {
                    e.setCancelled(true);
                    try {
                        Method method = e.getClass().getMethod("setCanCreatePortal", Boolean.class);
                        if (method != null) method.invoke(e, false);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
                }
            }
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getFrom());
        if (portal == null || portal.isDisabled()) return;

        e.setCancelled(true);
        e.setTo(e.getFrom());

        try {
            Method method = e.getClass().getMethod("setCanCreatePortal", Boolean.class);
            if (method != null) method.invoke(e, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}

        vanillaPortalHelper(e);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (Config.get().itemTransfer)
            pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance,
                    () -> initiatePortalStuff(e.getEntity().getLocation(), e.getLocation(), e.getEntity()),
                    20L * Config.get().itemTeleportDelay);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(PlayerDropItemEvent e) {
        if (Config.get().itemTransfer) {
            final Location startLocation = e.getItemDrop().getLocation().clone();
            pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance,
                    () -> initiatePortalStuff(e.getItemDrop().getLocation(), startLocation, e.getItemDrop()),
                    20L * Config.get().itemTeleportDelay);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        if (!Config.get().creatureBlacklist.contains(e.getEntityType()))
            return;

        Portal portal = pluginInstance.getManager().getPortalAtLocation(e.getLocation());
        if (portal != null && !portal.isDisabled())
            e.setCancelled(true);
    }

    // helpers
    private void initiatePortalStuff(Location toLocation, Location fromLocation, Entity entity) {
        final boolean isPlayer = (entity instanceof Player);
        Portal portal = pluginInstance.getManager().getPortalAtLocation(toLocation);
        if (portal == null) {
            if (isPlayer) {
                final Player player = ((Player) entity);
                getPTPProtectionPlayers().remove(player.getUniqueId());
            }

            final Portal foundPortal = pluginInstance.getManager().getEntitiesInTeleportationAndPortals().getOrDefault(entity.getUniqueId(), null);
            if (foundPortal != null && isPlayer) {
                final Player player = ((Player) entity);

                pluginInstance.getManager().getEntitiesInTeleportationAndPortals().remove(player.getUniqueId());

                final TeleportTask teleportTask = pluginInstance.getManager().getTeleportTasks().getOrDefault(player.getUniqueId(), null);
                if (teleportTask != null) teleportTask.cancel();
                pluginInstance.getManager().getTeleportTasks().remove(player.getUniqueId());

                String title = pluginInstance.getLangConfig().getString("teleport-cancelled.title"),
                        subTitle = pluginInstance.getLangConfig().getString("teleport-cancelled.sub-title");
                if ((title != null && !title.isEmpty()) || (subTitle != null && !subTitle.isEmpty())) {
                    player.sendTitle(pluginInstance.getManager().colorText(title),
                            pluginInstance.getManager().colorText(subTitle), 0, 40, 0);
                }
            }
        }

        if (portal != null && !portal.isDisabled()) {
            if (isPlayer) {
                final Player player = (Player) entity;

                // prevent teleporting due to join protection
                if (Config.get().ptpProtection && getPTPProtectionPlayers().contains(player.getUniqueId())) return;

                TeleportTask teleportTask = pluginInstance.getManager().getTeleportTasks().getOrDefault(player.getUniqueId(), null);
                if (teleportTask != null && !teleportTask.isCancelled()) return;

                if (pluginInstance.getManager().getPortalLinkMap().containsKey(player.getUniqueId())
                        && pluginInstance.getManager().getPortalLinkMap().get(player.getUniqueId()).equalsIgnoreCase(portal.getPortalId()))
                    return;
                else pluginInstance.getManager().getPortalLinkMap().remove(player.getUniqueId());
            }

            PortalEnterEvent portalEnterEvent = new PortalEnterEvent(entity, portal, fromLocation, portal.getTeleportLocation().asBukkitLocation());
            pluginInstance.getServer().getPluginManager().callEvent(portalEnterEvent);
            if (portalEnterEvent.isCancelled()) return;

            if (isPlayer) {
                final Player player = (Player) entity;
                final boolean canBypassCooldown = (player.hasPermission("simpleportals.cdbypass") || player.hasPermission("simpleportals.admin")),
                        cooldownFail = (Config.get().usePortalCooldown
                                && (pluginInstance.getManager().isPlayerOnCooldown(player, "normal", Config.get().cooldownDuration)) && !canBypassCooldown),
                        permissionFail = !Config.get().bypassPortalPermissions
                                && (!player.hasPermission("simpleportals.portal." + portal.getPortalId())
                                && !player.hasPermission("simpleportals.portals." + portal.getPortalId())
                                && !player.hasPermission("simpleportals.portal.*")
                                && !player.hasPermission("simpleportals.portals.*")
                                && !player.hasPermission("simpleportals.admin"));

                if (cooldownFail || permissionFail) {
                    double tv = Config.get().throwVelocity;
                    if (!(tv <= -1)) {
                        final Vector direction = new Vector(fromLocation.getX() - toLocation.getX(),
                                ((fromLocation.getY() - toLocation.getY()) + (player.getVelocity().getY() / 2)),
                                fromLocation.getZ() - toLocation.getZ()).multiply(tv);
                        player.setVelocity(direction);
                    }

                    String message = cooldownFail ? pluginInstance.getLangConfig().getString("enter-cooldown-message")
                            : pluginInstance.getLangConfig().getString("enter-no-permission-message");
                    if (message != null && !message.equalsIgnoreCase(""))
                        player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix")
                                + message.replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal",
                                Config.get().cooldownDuration)))));
                    return;
                }
            }

            if (isPlayer && Config.get().usePortalCooldown) {
                final Player player = (Player) entity;
                final boolean canBypassCooldown = player.hasPermission("simpleportals.cdbypass");
                if (!canBypassCooldown) pluginInstance.getManager().updatePlayerPortalCooldown((Player) entity, "normal");
            }

            if (portal.getTeleportLocation() != null) {
                if (isPlayer) {
                    final Player player = (Player) entity;
                    if (portal.getMessage() != null && !portal.getMessage().isEmpty())
                        player.sendMessage(pluginInstance.getManager().colorText(portal.getMessage().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal",
                                        Config.get().cooldownDuration)))));

                    if (portal.getBarMessage() != null && !portal.getBarMessage().isEmpty())
                        pluginInstance.getManager().sendBarMessage(player, portal.getBarMessage().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player, "normal",
                                        Config.get().cooldownDuration))));

                    if ((portal.getTitle() != null && !portal.getTitle().isEmpty()) && (portal.getSubTitle() != null && !portal.getSubTitle().isEmpty()))
                        pluginInstance.getManager().sendTitle(player, portal.getTitle().replace("{name}", portal.getPortalId())
                                        .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player,
                                                "normal", Config.get().cooldownDuration))),
                                portal.getSubTitle().replace("{name}", portal.getPortalId())
                                        .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player,
                                                "normal", Config.get().cooldownDuration))));
                    else if (portal.getTitle() != null && !portal.getTitle().isEmpty())
                        pluginInstance.getManager().sendTitle(player, portal.getTitle().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player,
                                        "normal", Config.get().cooldownDuration))), null);
                    else if (portal.getSubTitle() != null && !portal.getSubTitle().isEmpty())
                        pluginInstance.getManager().sendTitle(player, null, portal.getSubTitle().replace("{name}", portal.getPortalId())
                                .replace("{time}", String.valueOf(pluginInstance.getManager().getCooldownTimeLeft(player,
                                        "normal", Config.get().cooldownDuration))));
                }

                portal.performAction(entity);
            }
        } else {
            if (!isPlayer) return;
            final Player player = (Player) entity;

            pluginInstance.getManager().getPortalLinkMap().remove(player.getUniqueId());
            if (!pluginInstance.getManager().getSmartTransferMap().isEmpty()
                    && pluginInstance.getManager().getSmartTransferMap().containsKey(player.getUniqueId())) {
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

    private void vanillaPortalHelper(PlayerTeleportEvent e) {
        if (!e.getCause().name().toUpperCase().contains("PORTAL") && !e.getCause().name().toUpperCase().contains("GATEWAY")) return;

        PortalType portalType = PortalType.NETHER;
        switch (e.getCause()) {
            case NETHER_PORTAL:
                portalType = PortalType.NETHER;
                break;
            case END_PORTAL:
                // case END_GATEWAY:
                portalType = PortalType.ENDER;
                break;
            default:
                break;
        }

        Location location = pluginInstance.getManager().handleVanillaPortalReplacements(e.getFrom().getWorld(), portalType);
        if (location != null) e.setTo(location);
    }

    private void forceJoin(@NotNull Player player) {
        if (Config.get().forceJoin) {
            final String worldName = Config.get().forceJoinWorld;
            if (worldName != null && worldName.isEmpty()) {
                if (player.getWorld().getSpawnLocation() != null) player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                final World world = pluginInstance.getServer().getWorld(worldName);
                if (world != null && world.getSpawnLocation() != null) player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    private void checkPTPProtection(@NotNull Player player) {
        if (Config.get().ptpProtection) {
            Portal portal = pluginInstance.getManager().getPortalAtLocation(player.getLocation());
            if (portal == null || portal.isDisabled()) return;

            getPTPProtectionPlayers().add(player.getUniqueId());
        }
    }

    // getters & setters

    /**
     * @return The Set of users who will be blocked from teleporting in a portal they join, spawn, or teleport into
     */
    public HashSet<UUID> getPTPProtectionPlayers() {return ptpProtectionPlayers;}

    public HashMap<UUID, TransferData> getTransferData() {return transferData;}
}