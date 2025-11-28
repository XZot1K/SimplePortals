/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import xzot1k.plugins.sp.SimplePortals;

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

    public boolean isInRegion(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        if (point1 == null || point2 == null) {
            return false;
        }

        if (!point1.getWorldName().equalsIgnoreCase(loc.getWorld().getName())) {
            return false;
        }

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        double x1 = point1.getX(), x2 = point2.getX();
        double y1 = point1.getY(), y2 = point2.getY();
        double z1 = point1.getZ(), z2 = point2.getZ();

        double minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

        return (x >= minX && x <= maxX) &&
                (y >= minY && y <= maxY) &&
                (z >= minZ && z <= maxZ);
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
