package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import xzot1k.plugins.sp.SimplePortals;

public class Region
{

    private SimplePortals pluginInstance;
    private SerializableLocation point1, point2;

    public Region(SimplePortals pluginInstance, Location point1, Location point2)
    {
        this.pluginInstance = pluginInstance;
        setPoint1(point1);
        setPoint2(point2);
    }

    public boolean isInRegion(Location location)
    {
        Location point1 = getPoint1().asBukkitLocation(), point2 = getPoint2().asBukkitLocation();

        return ((location.getBlockX() <= point1.getBlockX() && location.getBlockX() >= point2.getBlockX())
                || (location.getBlockX() <= point2.getBlockX() && location.getBlockX() >= point1.getBlockX()))
                && ((location.getBlockY() <= point1.getBlockY() && location.getY() >= point2.getBlockY())
                || (location.getBlockY() <= point2.getBlockY() && location.getBlockY() >= point1.getBlockY()))
                && ((location.getBlockZ() <= point1.getZ() && location.getBlockZ() >= point2.getBlockZ())
                || (location.getBlockZ() <= point2.getBlockZ() && location.getBlockZ() >= point1.getBlockZ()));

    }

    public SerializableLocation getPoint1()
    {
        return point1;
    }

    public void setPoint1(Location point1)
    {
        this.point1 = new SerializableLocation(pluginInstance, point1);
    }

    public SerializableLocation getPoint2()
    {
        return point2;
    }

    public void setPoint2(Location point2)
    {
        this.point2 = new SerializableLocation(pluginInstance, point2);
    }
}
