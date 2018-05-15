package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xzot1k.plugins.sp.SimplePortals;

import java.io.File;

public class Portal
{

    private SimplePortals pluginInstance;
    private Region region;
    private SerializableLocation teleportLocation;
    private String portalId, serverSwitchName;

    public Portal(SimplePortals pluginInstance, String portalId, Region region)
    {
        this.pluginInstance = pluginInstance;
        setRegion(region);
        setPortalId(portalId);
        if (getRegion() != null && getRegion().getPoint1() != null)
            setTeleportLocation(getRegion().getPoint1().add(0, 2, 0));
    }

    public void register()
    {
        if (!pluginInstance.getManager().getPortals().contains(this))
            pluginInstance.getManager().getPortals().add(this);
    }

    public void unregister()
    {
        pluginInstance.getManager().getPortals().remove(this);
    }

    public void delete()
    {
        try
        {
            File file = new File(pluginInstance.getDataFolder() + "/portals/" + getPortalId() + ".yml");
            file.delete();
        } catch (Exception ignored)
        {
        }
    }

    public void save()
    {
        try
        {
            File file = new File(pluginInstance.getDataFolder(), "/portals/" + getPortalId() + ".yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            // save region id
            yaml.set("portal-id", getPortalId());
            yaml.set("portal-server", getServerSwitchName());

            if (getRegion() != null)
            {
                // save point 1.
                Location point1 = getRegion().getPoint1();
                if (point1 != null)
                {
                    yaml.set("point-1.world", point1.getWorld().getName());
                    yaml.set("point-1.x", point1.getX());
                    yaml.set("point-1.y", point1.getY());
                    yaml.set("point-1.z", point1.getZ());
                }

                // save point 2.
                Location point2 = getRegion().getPoint2();
                if (point2 != null)
                {
                    yaml.set("point-2.world", point2.getWorld().getName());
                    yaml.set("point-2.x", point2.getX());
                    yaml.set("point-2.y", point2.getY());
                    yaml.set("point-2.z", point2.getZ());
                }
            }

            // save teleport location.
            Location teleportLocation = getTeleportLocation();
            if (teleportLocation != null)
            {
                yaml.set("teleport-location.world", teleportLocation.getWorld().getName());
                yaml.set("teleport-location.x", teleportLocation.getX());
                yaml.set("teleport-location.y", teleportLocation.getY());
                yaml.set("teleport-location.z", teleportLocation.getZ());
                yaml.set("teleport-location.yaw", teleportLocation.getYaw());
                yaml.set("teleport-location.pitch", teleportLocation.getPitch());
            }

            // save file.
            yaml.save(file);
        } catch (Exception ignored)
        {
            ignored.printStackTrace();
        }
    }

    public void performAction(Player player)
    {
        if (getServerSwitchName() == null || !getServerSwitchName().equalsIgnoreCase("none"))
            pluginInstance.getManager().teleportPlayerWithEntity(player, getTeleportLocation());
        else pluginInstance.getManager().switchServer(player, getServerSwitchName());

        try
        {
            String particleEffect = pluginInstance.getConfig().getString("teleport-visual-effect")
                    .toUpperCase().replace(" ", "_").replace("-", "_");
            player.getWorld().playSound(player.getLocation(), Sound.valueOf(pluginInstance.getConfig().getString("teleport-sound")
                    .toUpperCase().replace(" ", "_").replace("-", "_")), 1, 1);
            pluginInstance.getManager().getParticleHandler().broadcastParticle(player.getLocation(), 1, 2, 1, 0, particleEffect, 50);
        } catch (Exception ignored) {}
    }

    public void displayRegion(Player player)
    {
        String particleEffect = pluginInstance.getConfig().getString("region-visual-effect")
                .toUpperCase().replace(" ", "_").replace("-", "_");

        new BukkitRunnable()
        {
            Location point1 = getRegion().getPoint1().clone(), point2 = getRegion().getPoint2().clone();
            int duration = pluginInstance.getConfig().getInt("region-visual-duration");
            double lifetime = 0;

            @Override
            public void run()
            {
                if (lifetime >= duration)
                {
                    cancel();
                    return;
                }

                if (point1.getWorld().getName().equalsIgnoreCase(point2.getWorld().getName()))
                {
                    if (point1.getBlockX() <= point2.getBlockX())
                    {
                        for (int pos_x = point1.getBlockX() - 1; ++pos_x <= point2.getBlockX(); )
                        {
                            if (point1.getBlockZ() <= point2.getBlockZ())
                            {
                                for (int pos_z = point1.getBlockZ() - 1; ++pos_z <= point2.getBlockZ(); )
                                {
                                    if (point1.getBlockY() <= point2.getBlockY())
                                    {
                                        for (int pos_y = point1.getBlockY() - 1; ++pos_y <= point2.getBlockY(); )
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    } else
                                    {
                                        for (int pos_y = point2.getBlockY() - 1; ++pos_y <= point1.getBlockY(); )
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    }
                                }
                            } else
                            {
                                for (int pos_z = point2.getBlockZ() - 1; ++pos_z <= point1.getBlockZ(); )
                                {
                                    if (point1.getBlockY() <= point2.getBlockY())
                                    {
                                        for (int pos_y = point1.getBlockY() - 1; ++pos_y <= point2.getBlockY(); )
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    } else
                                    {
                                        for (int pos_y = point2.getBlockY() - 1; ++pos_y <= point1.getBlockY(); )
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    }
                                }
                            }
                        }
                    } else
                    {
                        for (int pos_x = point2.getBlockX(); pos_x <= point1.getBlockX(); pos_x++)
                        {
                            if (point1.getBlockZ() <= point2.getBlockZ())
                            {
                                for (int pos_z = point1.getBlockZ(); pos_z <= point2.getBlockZ(); pos_z++)
                                {
                                    if (point1.getBlockY() <= point2.getBlockY())
                                    {
                                        for (int pos_y = point1.getBlockY(); pos_y <= point2.getBlockY(); pos_y++)
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    } else
                                    {
                                        for (int pos_y = point2.getBlockY(); pos_y <= point1.getBlockY(); pos_y++)
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    }
                                }
                            } else
                            {
                                for (int pos_z = point2.getBlockZ(); pos_z <= point1.getBlockZ(); pos_z++)
                                {
                                    if (point1.getBlockY() <= point2.getBlockY())
                                    {
                                        for (int pos_y = point1.getBlockY(); pos_y <= point2.getBlockY(); pos_y++)
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    } else
                                    {
                                        for (int pos_y = point2.getBlockY(); pos_y <= point1.getBlockY(); pos_y++)
                                        {
                                            Location location = new Location(point1.getWorld(), pos_x, pos_y, pos_z);
                                            if (location.getX() == point1.getX() || location.getX() == point2.getX() ||
                                                    location.getY() == point1.getY() || location.getY() == point2.getY() ||
                                                    location.getZ() == point1.getZ() || location.getZ() == point2.getZ())
                                                pluginInstance.getManager().getParticleHandler().displayParticle(player, location.add(0.5, 0.5, 0.5),
                                                        0, 0, 0, 0, particleEffect, 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                lifetime += 0.25;
            }
        }.runTaskTimer(pluginInstance, 0, 5);
    }

    public Region getRegion()
    {
        return region;
    }

    public void setRegion(Region region)
    {
        this.region = region;
    }

    public String getPortalId()
    {
        return portalId;
    }

    public void setPortalId(String portalId)
    {
        this.portalId = portalId;
    }

    public Location getTeleportLocation()
    {
        return teleportLocation != null ? teleportLocation.asBukkitLocation() : null;
    }

    public void setTeleportLocation(Location teleportLocation)
    {
        this.teleportLocation = new SerializableLocation(pluginInstance, teleportLocation);
    }

    public String getServerSwitchName()
    {
        return serverSwitchName;
    }

    public void setServerSwitchName(String serverSwitchName)
    {
        this.serverSwitchName = serverSwitchName;
    }
}
