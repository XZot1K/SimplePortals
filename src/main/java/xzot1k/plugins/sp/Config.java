/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import xzot1k.plugins.sp.api.objects.SerializableLocation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config {

    private static Config instance;
    private static SimplePortals pluginInstance;

    public static Config get() {
        return instance;
    }

    public static void load(SimplePortals plugin) {
        pluginInstance = plugin;
        plugin.reloadConfig();
        instance = new Config(plugin.getConfig());
    }

    public final String mysqlServerName;
    public final boolean mysqlUseSSL;
    public final String mysqlDatabase;
    public final String mysqlTransferTable;
    public final String mysqlHost;
    public final String mysqlPort;
    public final String mysqlUsername;
    public final String mysqlPassword;

    public final boolean managementTask;
    public final boolean keepYawPitch;
    public final boolean maintainVehicleVelocity;
    public final boolean maintainEntityVelocity;

    public final boolean bypassPortalPermissions;
    public final boolean vehicleTeleportation;
    public final boolean blockCreativePortal;
    public final boolean forceJoin;
    public final String forceJoinWorld;

    public final Map<String, SerializableLocation> netherPortalLocations;
    public final Map<String, SerializableLocation> endPortalLocations;
    public final boolean endOverrideDeath;

    public final EnumSet<EntityType> creatureBlacklist;

    public final int titleFadeIn;
    public final int titleFadeOut;
    public final int titleDuration;

    public final boolean usePortalCooldown;
    public final boolean ptpProtection;
    public final int cooldownDuration;
    public final double throwVelocity;
    public final int commandTickDelay;

    public final boolean itemTransfer;
    public final int itemTeleportDelay;

    public final String teleportSound;
    public final String teleportEffect;
    public final String selectionEffect;
    public final int selectionDuration;
    public final String regionEffect;
    public final int regionDuration;

    private Config(FileConfiguration c) {

        mysqlServerName = c.getString("mysql.server-name");
        mysqlUseSSL = c.getBoolean("mysql.use-ssl");
        mysqlDatabase = c.getString("mysql.database");
        mysqlTransferTable = c.getString("mysql.transfer-table");
        mysqlHost = c.getString("mysql.host");
        mysqlPort = c.getString("mysql.port");
        mysqlUsername = c.getString("mysql.username");
        mysqlPassword = c.getString("mysql.password");

        managementTask = c.getBoolean("management-task");
        keepYawPitch = c.getBoolean("keep-teleport-head-axis");
        maintainVehicleVelocity = c.getBoolean("maintain-vehicle-velocity");
        maintainEntityVelocity = c.getBoolean("maintain-entity-velocity");

        bypassPortalPermissions = c.getBoolean("bypass-portal-permissions");
        vehicleTeleportation = c.getBoolean("vehicle-teleportation");
        blockCreativePortal = c.getBoolean("block-creative-portal-entrance");
        forceJoin = c.getBoolean("force-join");
        forceJoinWorld = c.getString("force-join-world");

        netherPortalLocations = loadPortalLocations(c.getStringList("nether-portal-locations"));
        endPortalLocations = loadPortalLocations(c.getStringList("end-portal-locations"));
        endOverrideDeath = c.getBoolean("end-portal-locations-handle-death");

        creatureBlacklist = loadEntityBlacklist(c.getStringList("creature-spawning-blacklist"));

        titleFadeIn = c.getInt("titles.fade-in");
        titleFadeOut = c.getInt("titles.fade-out");
        titleDuration = c.getInt("titles.display-time");

        usePortalCooldown = c.getBoolean("use-portal-cooldown");
        ptpProtection = c.getBoolean("portal-to-portal-protection");
        cooldownDuration = c.getInt("portal-cooldown-duration");
        throwVelocity = c.getDouble("throw-velocity");
        commandTickDelay = c.getInt("command-tick-delay");

        itemTransfer = c.getBoolean("item-transfer");
        itemTeleportDelay = c.getInt("item-teleport-delay");

        teleportSound = c.getString("teleport-sound");
        teleportEffect = c.getString("teleport-visual-effect");
        selectionEffect = c.getString("selection-visual-effect");
        selectionDuration = c.getInt("selection-visual-duration");
        regionEffect = c.getString("region-visual-effect");
        regionDuration = c.getInt("region-visual-duration");
    }

    private Map<String, SerializableLocation> loadPortalLocations(List<String> list) {
        Map<String, SerializableLocation> map = new HashMap<>();

        for (String line : list) {
            if (line == null || !line.contains(":") || !line.contains(",")) continue;

            String[] parts = line.split(":");
            String sourceWorld = parts[0];

            String[] coords = parts[1].split(",");
            if (coords.length < 6) continue;

            String destWorld = coords[0];
            double x = Double.parseDouble(coords[1]);
            double y = Double.parseDouble(coords[2]);
            double z = Double.parseDouble(coords[3]);
            float yaw = Float.parseFloat(coords[4]);
            float pitch = Float.parseFloat(coords[5]);

            map.put(sourceWorld.toLowerCase(), new SerializableLocation(pluginInstance, destWorld, x, y, z, yaw, pitch));
        }

        return map;
    }

    public EnumSet<EntityType> loadEntityBlacklist(List<String> list) {
        EnumSet<EntityType> set = EnumSet.noneOf(EntityType.class);

        if (list == null) return set;

        for (String raw : list) {
            if (raw == null || raw.isEmpty()) continue;

            // Normalize config entry
            String fixed = raw.toUpperCase().replace(" ", "_").replace("-", "_");

            try {
                set.add(EntityType.valueOf(fixed));
            } catch (IllegalArgumentException ex) {
                pluginInstance.getLogger().warning("Invalid entity type " + raw);
            }
        }

        return set;
    }

}
