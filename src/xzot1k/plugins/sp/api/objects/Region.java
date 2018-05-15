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
        Location point1 = getPoint1(), point2 = getPoint2();

        return ((location.getX() <= point1.getX() && location.getX() >= point2.getX())
                || (location.getX() <= point2.getX() && location.getX() >= point1.getX()))
                && ((location.getY() <= point1.getY() && location.getY() >= point2.getY())
                || (location.getY() <= point2.getY() && location.getY() >= point1.getY()))
                && ((location.getZ() <= point1.getZ() && location.getZ() >= point2.getZ())
                || (location.getZ() <= point2.getZ() && location.getZ() >= point1.getZ()));

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
