/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.World;
import xzot1k.plugins.sp.SimplePortals;

import java.util.Objects;

public class SerializableLocation {

    private final SimplePortals pluginInstance;
    private double x, y, z;
    private float yaw, pitch;
    private String worldName;

    public SerializableLocation(SimplePortals pluginInstance, Location location) {
        this.pluginInstance = pluginInstance;
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setYaw(location.getYaw());
        setPitch(location.getPitch());
        setWorldName(Objects.requireNonNull(location.getWorld()).getName());
    }

    public SerializableLocation(SimplePortals pluginInstance, String worldName, double x, double y, double z, double yaw, double pitch) {
        this.pluginInstance = pluginInstance;
        setX(x);
        setY(y);
        setZ(z);
        setYaw((float) yaw);
        setPitch((float) pitch);
        setWorldName(worldName);
    }

    public SerializableLocation(SimplePortals pluginInstance, String locationString) {
        this.pluginInstance = pluginInstance;
        load(locationString);
    }

    /**
     * Checks if location is equal to another (Exact).
     *
     * @param serializableLocation The other location.
     * @return Whether the locations are equal.
     */
    public boolean equals(SerializableLocation serializableLocation) {
        return (serializableLocation.getWorldName().equalsIgnoreCase(getWorldName()) && getX() == serializableLocation.getX()
                && getY() == serializableLocation.getY() && getZ() == serializableLocation.getZ() && getYaw() == serializableLocation.getYaw()
                && getPitch() == serializableLocation.getPitch());
    }

    /**
     * Checks if location is equal to another (Exact).
     *
     * @param location The other location.
     * @return Whether the locations are equal.
     */
    public boolean equals(Location location) {
        return (location.getWorld().getName().equalsIgnoreCase(getWorldName()) && getX() == location.getX()
                && getY() == location.getY() && getZ() == location.getZ() && getYaw() == location.getYaw()
                && getPitch() == location.getPitch());
    }

    @Override
    public String toString() {
        return getWorldName() + "," + getX() + "," + getY() + "," + getZ() + "," + getYaw() + "," + getPitch();
    }

    /**
     * Loads a location from the passed string.
     *
     * @param locationString The string to read.
     */
    public void load(String locationString) {
        if (locationString == null || !locationString.contains(",")) return;
        String[] args = locationString.split(",");
        setWorldName(args[0]);
        setX(Double.parseDouble(args[1]));
        setY(Double.parseDouble(args[2]));
        setZ(Double.parseDouble(args[3]));
        setYaw(Float.parseFloat(args[4]));
        setPitch(Float.parseFloat(args[5]));
    }

    /**
     * Obtains the distance between the location clone and the Bukkit location.
     *
     * @param location   The Bukkit location.
     * @param checkYAxis Whether or not to calculate utilizing the Y-axis.
     * @return The distance.
     */
    public double distance(Location location, boolean checkYAxis) {
        return Math.sqrt(Math.pow((getX() - location.getX()), 2)
                + (checkYAxis ? Math.pow((getY() - location.getY()), 2) : 0) + Math.pow((getZ() - location.getZ()), 2));
    }

    /**
     * Obtains the distance between the location clone and another location clone.
     *
     * @param location   The location clone.
     * @param checkYAxis Whether or not to calculate utilizing the Y-axis.
     * @return The distance.
     */
    public double distance(SerializableLocation location, boolean checkYAxis) {
        return Math.sqrt(Math.pow((getX() - location.getX()), 2)
                + (checkYAxis ? Math.pow((getY() - location.getY()), 2) : 0) + Math.pow((getZ() - location.getZ()), 2));
    }

    public Location asBukkitLocation() {
        World world = getPluginInstance().getServer().getWorld(getWorldName());
        if (world == null) return null;

        if (getYaw() == 0 && getPitch() == 0) return new Location(world, getX(), getY(), getZ());
        else return new Location(world, getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }
}
