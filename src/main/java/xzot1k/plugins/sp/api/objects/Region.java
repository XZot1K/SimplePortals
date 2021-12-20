/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import xzot1k.plugins.sp.SimplePortals;

import java.util.Objects;

public class Region {

    private final SimplePortals pluginInstance;
    private SerializableLocation point1, point2;

    public Region(SimplePortals pluginInstance, Location point1, Location point2) {
        this.pluginInstance = pluginInstance;
        setPoint1(point1);
        setPoint2(point2);
    }

    public Region(SimplePortals pluginInstance, SerializableLocation point1, SerializableLocation point2) {
        this.pluginInstance = pluginInstance;
        setPoint1(point1);
        setPoint2(point2);
    }

    public boolean isInRegion(Location location) {
        if (getPoint1() == null || getPoint2() == null) return false;
        Location point1 = getPoint1().asBukkitLocation(), point2 = getPoint2().asBukkitLocation();
        if (Objects.requireNonNull(point1.getWorld()).getName().equalsIgnoreCase(Objects.requireNonNull(location.getWorld()).getName())
                && Objects.requireNonNull(point2.getWorld()).getName().equalsIgnoreCase(location.getWorld().getName())) {
            final double highestX = Math.max(point1.getX(), point2.getX()), highestY = Math.max(point1.getY(), point2.getY()), highestZ = Math.max(point1.getZ(), point2.getZ()),
                    lowestX = Math.min(point1.getX(), point2.getX()), lowestY = Math.min(point1.getY(), point2.getY()), lowestZ = Math.min(point1.getZ(), point2.getZ());
            return (highestX >= location.getBlockX() && lowestX <= location.getBlockX()) && (highestY >= location.getBlockY()
                    && lowestY <= location.getBlockY()) && (highestZ >= location.getBlockZ() && lowestZ <= location.getBlockZ());
        }

        return false;
    }

    public SerializableLocation getPoint1() {
        return point1;
    }

    public void setPoint1(Location point1) {
        this.point1 = new SerializableLocation(pluginInstance, point1);
    }

    private void setPoint1(SerializableLocation point1) {
        this.point1 = point1;
    }

    public SerializableLocation getPoint2() {
        return point2;
    }

    public void setPoint2(Location point2) {
        this.point2 = new SerializableLocation(pluginInstance, point2);
    }

    private void setPoint2(SerializableLocation point2) {
        this.point2 = point2;
    }
}
