package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import xzot1k.plugins.sp.SimplePortals;

public class Region
{

    private SimplePortals pluginInstance;
    private SerializableLocation point1, point2;
    private Material previousFilledMaterial;
    private int previousFilledDurability;

    public Region(SimplePortals pluginInstance, Location point1, Location point2)
    {
        this.pluginInstance = pluginInstance;
        previousFilledDurability = 0;
        setPoint1(point1);
        setPoint2(point2);
    }

    public void revertFilledBlocks()
    {
        if (previousFilledMaterial == null) return;
        Location point1 = getPoint1(), point2 = getPoint2();

        if (point1.getBlockY() >= point2.getBlockY())
        {
            for (int y = point1.getBlockY() + 1; --y >= point2.getBlockY(); )
                if (point1.getBlockX() >= point2.getBlockX())
                    for (int x = point1.getBlockX() + 1; --x >= point2.getBlockX(); )
                        if (point1.getBlockZ() >= point2.getBlockZ())
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                        else
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                else
                    for (int x = point2.getBlockX() + 1; --x >= point1.getBlockX(); )
                        if (point2.getBlockZ() >= point1.getBlockZ())
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                        else
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
        } else
        {
            for (int y = point2.getBlockY() + 1; --y >= point1.getBlockY(); )
            {
                if (point2.getBlockX() >= point1.getBlockX())
                {
                    for (int x = point2.getBlockX() + 1; --x >= point1.getBlockX(); )
                        if (point2.getBlockZ() >= point1.getBlockZ())
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                        else
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                } else
                {
                    for (int x = point1.getBlockX() + 1; --x >= point2.getBlockX(); )
                        if (point1.getBlockZ() >= point2.getBlockZ())
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                        else
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if ((location.getBlock().getType() == previousFilledMaterial)
                                        || (previousFilledMaterial == Material.WATER && location.getBlock().getType() == Material.STATIONARY_WATER)
                                        || (previousFilledMaterial == Material.LAVA && location.getBlock().getType() == Material.STATIONARY_LAVA)
                                        && location.getBlock().getData() == previousFilledDurability)
                                    location.getBlock().setType(Material.AIR);
                            }
                }
            }
        }
    }

    public void setAirBlocks(Material material, int durability)
    {
        previousFilledMaterial = material;
        previousFilledDurability = durability;
        Location point1 = getPoint1(), point2 = getPoint2();

        if (point1.getBlockY() >= point2.getBlockY())
        {
            for (int y = point1.getBlockY() + 1; --y >= point2.getBlockY(); )
                if (point1.getBlockX() >= point2.getBlockX())
                    for (int x = point1.getBlockX() + 1; --x >= point2.getBlockX(); )
                        if (point1.getBlockZ() >= point2.getBlockZ())
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                        else
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                else
                    for (int x = point2.getBlockX() + 1; --x >= point1.getBlockX(); )
                        if (point2.getBlockZ() >= point1.getBlockZ())
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                        else
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
        } else
        {
            for (int y = point2.getBlockY() + 1; --y >= point1.getBlockY(); )
            {
                if (point2.getBlockX() >= point1.getBlockX())
                {
                    for (int x = point2.getBlockX() + 1; --x >= point1.getBlockX(); )
                        if (point2.getBlockZ() >= point1.getBlockZ())
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                        else
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                } else
                {
                    for (int x = point1.getBlockX() + 1; --x >= point2.getBlockX(); )
                        if (point1.getBlockZ() >= point2.getBlockZ())
                            for (int z = point1.getBlockZ() + 1; --z >= point2.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                        else
                            for (int z = point2.getBlockZ() + 1; --z >= point1.getBlockZ(); )
                            {
                                Location location = new Location(point1.getWorld(), x, y, z);
                                if (location.getBlock().getType() == Material.AIR)
                                {
                                    location.getBlock().setType(material);
                                    location.getBlock().setData((byte) durability);
                                }
                            }
                }
            }
        }
    }

    public boolean isInRegion(Location location)
    {
        Location point1 = getPoint1(), point2 = getPoint2();

        return ((location.getBlockX() <= point1.getBlockX() && location.getBlockX() >= point2.getBlockX())
                || (location.getBlockX() <= point2.getBlockX() && location.getBlockX() >= point1.getBlockX()))
                && ((location.getBlockY() <= point1.getBlockY() && location.getY() >= point2.getBlockY())
                || (location.getBlockY() <= point2.getBlockY() && location.getBlockY() >= point1.getBlockY()))
                && ((location.getBlockZ() <= point1.getZ() && location.getBlockZ() >= point2.getBlockZ())
                || (location.getBlockZ() <= point2.getBlockZ() && location.getBlockZ() >= point1.getBlockZ()));

    }

    public Location getPoint1()
    {
        return point1.asBukkitLocation();
    }

    public void setPoint1(Location point1)
    {
        this.point1 = new SerializableLocation(pluginInstance, point1);
    }

    public Location getPoint2()
    {
        return point2.asBukkitLocation();
    }

    public void setPoint2(Location point2)
    {
        this.point2 = new SerializableLocation(pluginInstance, point2);
    }
}
