package xzot1k.plugins.sp.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;
import xzot1k.plugins.sp.core.objects.TaskHolder;
import xzot1k.plugins.sp.core.packets.jsonmsgs.JSONHandler;
import xzot1k.plugins.sp.core.packets.jsonmsgs.versions.*;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;
import xzot1k.plugins.sp.core.packets.particles.versions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Manager
{
    private String serverVersion;
    private SimplePortals pluginInstance;
    private HashMap<UUID, Region> currentSelections;
    private HashMap<UUID, Boolean> selectionMode;
    private HashMap<UUID, Long> playerPortalCooldowns;
    private List<Portal> portals;
    private HashMap<UUID, TaskHolder> visualTasks;

    private ParticleHandler particleHandler;
    private JSONHandler jsonHandler;

    public Manager(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
        currentSelections = new HashMap<>();
        selectionMode = new HashMap<>();
        playerPortalCooldowns = new HashMap<>();
        visualTasks = new HashMap<>();
        portals = new ArrayList<>();

        serverVersion = pluginInstance.getServer().getClass().getPackage().getName()
                .replace(".", ",").split(",")[3];

        setupPackets();
    }

    private void setupPackets()
    {
        boolean success = false;
        switch (serverVersion)
        {
            case "v1_13_R1":
                particleHandler = new PH1_13R1(pluginInstance);
                setJSONHandler(new JSONHandler1_13R1());
                success = true;
                break;
            case "v1_12_R1":
                particleHandler = new PH1_12R1(pluginInstance);
                setJSONHandler(new JSONHandler1_12R1());
                success = true;
                break;
            case "v1_11_R1":
                particleHandler = new PH1_11R1(pluginInstance);
                setJSONHandler(new JSONHandler1_11R1());
                success = true;
                break;
            case "v1_10_R1":
                particleHandler = new PH1_10R1(pluginInstance);
                setJSONHandler(new JSONHandler1_10R1());
                success = true;
                break;
            case "v1_9_R2":
                particleHandler = new PH1_9R2(pluginInstance);
                setJSONHandler(new JSONHandler1_9R2());
                success = true;
                break;
            case "v1_9_R1":
                particleHandler = new PH1_9R1(pluginInstance);
                setJSONHandler(new JSONHandler1_9R1());
                success = true;
                break;
            case "v1_8_R3":
                particleHandler = new PH1_8R3(pluginInstance);
                setJSONHandler(new JSONHandler1_8R3());
                success = true;
                break;
            case "v1_8_R2":
                particleHandler = new PH1_8R2(pluginInstance);
                setJSONHandler(new JSONHandler1_8R2());
                success = true;
                break;
            case "v1_8_R1":
                particleHandler = new PH1_8R1(pluginInstance);
                setJSONHandler(new JSONHandler1_8R1());
                success = true;
                break;
            default:
                break;
        }

        if (success)
            sendConsoleMessage("&aAll packets have been successfully setup for &e" + serverVersion + "&a!");
        else
            sendConsoleMessage("&cYour server version (&e" + serverVersion + "&c) is not yet supported. " +
                    "Most packet features will be disabled until official release.");

    }

    public String colorText(String text) {return ChatColor.translateAlternateColorCodes('&', text);}

    public void sendConsoleMessage(String text)
    {
        pluginInstance.getServer().getConsoleSender().sendMessage(colorText(pluginInstance.getConfig().getString("prefix") + text));
    }

    public boolean updateCurrentSelection(Player player, Location location, PointType pointType)
    {
        if (!getCurrentSelections().isEmpty() && getCurrentSelections().containsKey(player.getUniqueId()))
        {
            Region region = getCurrentSelections().get(player.getUniqueId());
            if (region != null)
            {
                switch (pointType)
                {
                    case POINT_ONE:
                        region.setPoint1(location);
                        break;
                    case POINT_TWO:
                        region.setPoint2(location);
                        break;
                    default:
                        break;
                }

                if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName()))
                {
                    player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                            + pluginInstance.getConfig().getString("not-same-world-message")));
                    return false;
                }

                getCurrentSelections().put(player.getUniqueId(), region);
                return true;
            }
        }


        Region region = null;
        switch (pointType)
        {
            case POINT_ONE:
                region = new Region(pluginInstance, location, location);
                break;
            case POINT_TWO:
                region = new Region(pluginInstance, location, location);
                break;
            default:
                break;
        }

        if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName()))
        {
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("not-same-world-message")));
            return false;
        }

        getCurrentSelections().put(player.getUniqueId(), region);
        return true;
    }

    public Region getCurrentSelection(Player player)
    {
        if (!getCurrentSelections().isEmpty() && getCurrentSelections().containsKey(player.getUniqueId()))
            return getCurrentSelections().get(player.getUniqueId());
        return null;
    }

    public void clearCurrentSelection(Player player)
    {
        if (!getCurrentSelections().isEmpty()) getCurrentSelections().remove(player.getUniqueId());
    }

    public void setSelectionMode(Player player, boolean selectionMode)
    {
        getSelectionMode().put(player.getUniqueId(), selectionMode);
    }

    public boolean isInSelectionMode(Player player)
    {
        if (!getSelectionMode().isEmpty() && getSelectionMode().containsKey(player.getUniqueId()))
            return getSelectionMode().get(player.getUniqueId());
        return false;
    }

    public void updatePlayerPortalCooldown(Player player)
    {
        getPlayerPortalCooldowns().put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isPlayerOnCooldown(Player player)
    {
        if (!getPlayerPortalCooldowns().isEmpty() && getPlayerPortalCooldowns().containsKey(player.getUniqueId()))
            return getCooldownTimeLeft(player) > 0;

        return false;
    }

    public long getCooldownTimeLeft(Player player)
    {
        if (!getPlayerPortalCooldowns().isEmpty() && getPlayerPortalCooldowns().containsKey(player.getUniqueId()))
            return ((getPlayerPortalCooldowns().get(player.getUniqueId()) / 1000) + pluginInstance.getConfig().getInt("portal-cooldown-duration")) - (System.currentTimeMillis() / 1000);
        return 0;
    }

    public Portal getPortalAtLocation(Location location)
    {
        for (int i = -1; ++i < getPortals().size(); )
        {
            Portal portal = getPortals().get(i);
            if (portal.getRegion().isInRegion(location)) return portal;
        }

        return null;
    }

    public Portal getPortalById(String portalName)
    {
        for (int i = -1; ++i < getPortals().size(); )
        {
            Portal portal = getPortals().get(i);
            if (portal.getPortalId().equalsIgnoreCase(portalName)) return portal;
        }

        return null;
    }

    public boolean doesPortalExist(String portalName)
    {
        for (int i = -1; ++i < getPortals().size(); )
        {
            Portal portal = getPortals().get(i);
            if (portal.getPortalId().equalsIgnoreCase(portalName)) return true;
        }

        return false;
    }

    public void teleportPlayerWithEntity(Player player, Location location)
    {
        if (player.isInsideVehicle())
        {
            if (player.getVehicle() instanceof Horse)
            {
                Horse horse = (Horse) player.getVehicle();
                horse.eject();
                horse.setOwner(player);

                try
                {
                    horse.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } catch (Exception e)
                {
                    horse.teleport(location);
                    player.teleport(location);
                }

                pluginInstance.getServer().getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> horse.setPassenger(player), 10);
            } else
            {
                Entity ve = player.getVehicle();
                ve.eject();

                try
                {
                    ve.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                } catch (Exception e)
                {
                    ve.teleport(location);
                    player.teleport(location);
                }

                pluginInstance.getServer().getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> ve.setPassenger(player), 10);
            }
        } else
        {
            try
            {
                player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } catch (Exception e)
            {
                player.teleport(location);
            }
        }
    }

    public void highlightBlock(Block block, Player player, PointType pointType)
    {
        if (particleHandler == null) return;

        String particleEffect = pluginInstance.getConfig().getString("selection-visual-effect")
                .toUpperCase().replace(" ", "_").replace("-", "_");

        BukkitTask bukkitTask = new BukkitRunnable()
        {
            int duration = pluginInstance.getConfig().getInt("selection-visual-duration");
            double lifetime = 0;
            Location blockLocation = block.getLocation().clone();

            @Override
            public void run()
            {
                if (lifetime >= duration)
                {
                    cancel();
                    return;
                }

                for (double y = blockLocation.getBlockY() - 0.2; (y += 0.2) < (blockLocation.getBlockY() + 1.1); )
                    for (double x = blockLocation.getBlockX() - 0.2; (x += 0.2) < (blockLocation.getBlockX() + 1.1); )
                        for (double z = blockLocation.getBlockZ() - 0.2; (z += 0.2) < (blockLocation.getBlockZ() + 1.1); )
                        {
                            Location location = new Location(blockLocation.getWorld(), x, y, z);

                            if ((y < (blockLocation.getBlockY() + 0.2) || y > (blockLocation.getBlockY() + 0.9))
                                    && (z < (blockLocation.getBlockZ() + 0.2) || z > (blockLocation.getBlockZ() + 0.9)))
                                particleHandler.displayParticle(player, location, 0, 0, 0, 0, particleEffect, 1);

                            if ((x < (blockLocation.getBlockX() + 0.2) || x > (blockLocation.getBlockX() + 0.9))
                                    && (z < (blockLocation.getBlockZ() + 0.2) || z > (blockLocation.getBlockZ() + 0.9)))
                                particleHandler.displayParticle(player, location, 0, 0, 0, 0, particleEffect, 1);

                            if ((y < (blockLocation.getBlockY() + 0.2) || y > (blockLocation.getBlockY() + 0.9))
                                    && (x < (blockLocation.getBlockX() + 0.2) || x > (blockLocation.getBlockX() + 0.9)))
                                particleHandler.displayParticle(player, location, 0, 0, 0, 0, particleEffect, 1);
                        }

                lifetime += 0.25;
            }
        }.runTaskTimer(pluginInstance, 0, 5);


        if (!getVisualTasks().isEmpty() && getVisualTasks().containsKey(player.getUniqueId()))
        {
            TaskHolder taskHolder = getVisualTasks().get(player.getUniqueId());
            if (taskHolder != null)
            {
                if (taskHolder.getRegionDisplay() != null) taskHolder.getRegionDisplay().cancel();
                if (pointType == PointType.POINT_ONE) taskHolder.setSelectionPointOne(bukkitTask);
                else taskHolder.setSelectionPointTwo(bukkitTask);
                return;
            }
        }

        TaskHolder taskHolder = new TaskHolder(pluginInstance);
        if (pointType == PointType.POINT_ONE) taskHolder.setSelectionPointOne(bukkitTask);
        else taskHolder.setSelectionPointTwo(bukkitTask);
        getVisualTasks().put(player.getUniqueId(), taskHolder);
    }

    public void loadPortals()
    {
        getPortals().clear();
        File dir = new File(pluginInstance.getDataFolder(), "/portals");
        dir.mkdirs();
        File[] files = dir.listFiles();
        if (files == null) return;

        for (int i = -1; ++i < files.length; )
        {
            File file = files[i];
            if (file != null && file.getName().toLowerCase().endsWith(".yml"))
            {
                try
                {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                    if (!doesPortalExist(yamlConfiguration.getString("portal-id")))
                    {
                        Location point1 = new Location(pluginInstance.getServer().getWorld(yamlConfiguration.getString("point-1.world")),
                                (float) yamlConfiguration.getDouble("point-1.x"), (float) yamlConfiguration.getDouble("point-1.y"), (float) yamlConfiguration.getDouble("point-1.z")),
                                point2 = new Location(pluginInstance.getServer().getWorld(yamlConfiguration.getString("point-2.world")),
                                        (float) yamlConfiguration.getDouble("point-2.x"), (float) yamlConfiguration.getDouble("point-2.y"), (float) yamlConfiguration.getDouble("point-2.z"));
                        Region region = new Region(pluginInstance, point1, point2);
                        Portal portal = new Portal(pluginInstance, yamlConfiguration.getString("portal-id"), region);
                        try
                        {
                            portal.setServerSwitchName(yamlConfiguration.getString("portal-server"));
                            portal.setCommandsOnly(yamlConfiguration.getBoolean("commands-only"));
                            portal.setCommands(yamlConfiguration.getStringList("commands"));
                        } catch (Exception ignored) {}
                        portal.register();
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        }
    }

    public void savePortals()
    {
        for (int i = -1; ++i < getPortals().size(); )
        {
            Portal portal = getPortals().get(i);
            portal.save();
        }
    }

    public void clearAllVisuals(Player player)
    {
        if (!getVisualTasks().isEmpty() && getVisualTasks().containsKey(player.getUniqueId()))
        {
            TaskHolder taskHolder = getVisualTasks().get(player.getUniqueId());
            if (taskHolder.getRegionDisplay() != null) taskHolder.getRegionDisplay().cancel();
            if (taskHolder.getSelectionPointOne() != null) taskHolder.getSelectionPointOne().cancel();
            if (taskHolder.getSelectionPointTwo() != null) taskHolder.getSelectionPointTwo().cancel();
        }
    }

    public boolean switchServer(Player player, String serverName)
    {
        try
        {
            Bukkit.getMessenger().registerOutgoingPluginChannel(pluginInstance, "BungeeCord");
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(pluginInstance, "BungeeCord", byteArray.toByteArray());
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            sendConsoleMessage("&cThere seems to have been a issue when switching the player to the &e" + serverName + " &cserver.");
            return false;
        }
    }

    private HashMap<UUID, Region> getCurrentSelections()
    {
        return currentSelections;
    }

    public List<Portal> getPortals()
    {
        return portals;
    }

    private HashMap<UUID, Boolean> getSelectionMode()
    {
        return selectionMode;
    }

    private HashMap<UUID, Long> getPlayerPortalCooldowns()
    {
        return playerPortalCooldowns;
    }

    public ParticleHandler getParticleHandler()
    {
        return particleHandler;
    }

    public HashMap<UUID, TaskHolder> getVisualTasks()
    {
        return visualTasks;
    }

    public String getServerVersion()
    {
        return serverVersion;
    }

    public JSONHandler getJSONHandler()
    {
        return jsonHandler;
    }

    private void setJSONHandler(JSONHandler jsonHandler)
    {
        this.jsonHandler = jsonHandler;
    }
}