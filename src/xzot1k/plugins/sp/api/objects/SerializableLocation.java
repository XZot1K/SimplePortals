package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.World;
import xzot1k.plugins.sp.SimplePortals;

import java.util.Objects;

public class SerializableLocation
{

    private SimplePortals pluginInstance;
    private double x, y, z;
    private float yaw, pitch;
    private String worldName;

    public SerializableLocation(SimplePortals pluginInstance, Location location)
    {
        this.pluginInstance = pluginInstance;
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setYaw(location.getYaw());
        setPitch(location.getPitch());
        setWorldName(Objects.requireNonNull(location.getWorld()).getName());
    }

    public SerializableLocation(SimplePortals pluginInstance, String worldName, double x, double y, double z, double yaw, double pitch)
    {
        this.pluginInstance = pluginInstance;
        setX(x);
        setY(y);
        setZ(z);
        setYaw((float) yaw);
        setPitch((float) pitch);
        setWorldName(worldName);
    }

    public Location asBukkitLocation()
    {
        World world = pluginInstance.getServer().getWorld(getWorldName());
        if (getYaw() == 0 && getPitch() == 0) return new Location(world, getX(), getY(), getZ());
        else return new Location(world, getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public String getWorldName()
    {
        return worldName;
    }

    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public float getYaw()
    {
        return yaw;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

}
