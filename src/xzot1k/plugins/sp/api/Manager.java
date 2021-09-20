/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import us.eunoians.prisma.ColorProvider;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.PointType;
import xzot1k.plugins.sp.api.exceptions.PortalFormException;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;
import xzot1k.plugins.sp.api.objects.SerializableLocation;
import xzot1k.plugins.sp.core.objects.TaskHolder;
import xzot1k.plugins.sp.core.packets.bar.*;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;
import xzot1k.plugins.sp.core.packets.particles.versions.PH1_8R1;
import xzot1k.plugins.sp.core.packets.particles.versions.PH1_8R2;
import xzot1k.plugins.sp.core.packets.particles.versions.PH1_8R3;
import xzot1k.plugins.sp.core.packets.particles.versions.PH_Latest;
import xzot1k.plugins.sp.core.packets.titles.TitleHandler;
import xzot1k.plugins.sp.core.packets.titles.versions.*;
import xzot1k.plugins.sp.core.tasks.HighlightTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {
    private final SimplePortals pluginInstance;
    private final HashMap<UUID, Region> currentSelections;
    private final HashMap<UUID, Boolean> selectionMode;
    private final HashMap<UUID, HashMap<String, Long>> playerPortalCooldowns;
    private final HashMap<UUID, TaskHolder> visualTasks;
    private final HashMap<UUID, SerializableLocation> smartTransferMap;
    private final HashMap<UUID, String> portalLinkMap;
    private final HashMap<String, Portal> portalMap;

    private final HashMap<UUID, Portal> entitiesInTeleportationAndPortals;

    private Random random;
    private ParticleHandler particleHandler;
    private TitleHandler titleHandler;
    private BarHandler barHandler;

    private Class<?> mountPacketClass, packetClass, craftPlayerClass, entityClass;

    public Manager(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
        portalMap = new HashMap<>();
        currentSelections = new HashMap<>();
        selectionMode = new HashMap<>();
        playerPortalCooldowns = new HashMap<>();
        visualTasks = new HashMap<>();
        smartTransferMap = new HashMap<>();
        portalLinkMap = new HashMap<>();

        entitiesInTeleportationAndPortals = new HashMap<>();

        setRandom(new Random());

        try {
            packetClass = Class.forName("net.minecraft.server." + getPluginInstance().getServerVersion() + ".Packet");
            mountPacketClass = Class.forName("net.minecraft.server." + getPluginInstance().getServerVersion() + ".PacketPlayOutMount");
            craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getPluginInstance().getServerVersion() + ".entity.CraftPlayer");
            entityClass = Class.forName("net.minecraft.server." + getPluginInstance().getServerVersion() + ".Entity");
        } catch (ClassNotFoundException ignored) {
        }

        setupPackets();
    }

    private void setupPackets() {
        try {
            switch (getPluginInstance().getServerVersion()) {
                case "v1_12_R1":
                    titleHandler = new Titles1_12R1();
                    break;
                case "v1_11_R1":
                    titleHandler = new Titles1_11R1();
                    break;
                case "v1_10_R1":
                    titleHandler = new Titles1_10R1();
                    break;
                case "v1_9_R2":
                    titleHandler = new Titles1_9R2();
                    break;
                case "v1_9_R1":
                    titleHandler = new Titles1_9R1();
                    barHandler = new ABH1_9R1();
                    break;
                case "v1_8_R3":
                    titleHandler = new Titles1_8R3();
                    particleHandler = new PH1_8R3(getPluginInstance());
                    barHandler = new ABH1_8R3();
                    break;
                case "v1_8_R2":
                    titleHandler = new Titles1_8R2();
                    particleHandler = new PH1_8R2(getPluginInstance());
                    barHandler = new ABH1_8R2();
                    break;
                case "v1_8_R1":
                    titleHandler = new Titles1_8R1();
                    particleHandler = new PH1_8R1(getPluginInstance());
                    barHandler = new ABH1_8R1();
                    break;
                default:
                    break;
            }

            if (getParticleHandler() == null) particleHandler = new PH_Latest();
            if (getBarHandler() == null) barHandler = new ABH_Latest();
            if (getTitleHandler() == null) titleHandler = new Titles_Latest();

            getPluginInstance().log(Level.INFO, "Packets have been setup for " + getPluginInstance().getServerVersion() + "!");
        } catch (Exception e) {
            getPluginInstance().log(Level.INFO, "There was an issue obtaining proper packets for " + getPluginInstance().getServerVersion()
                    + ". (Error: " + e.getMessage() + ")");
        }
    }

    public void loadPortals() {
        final File portalDirectory = new File(getPluginInstance().getDataFolder(), "/portals");
        File[] listFiles = portalDirectory.listFiles();

        if (listFiles != null && listFiles.length > 0)
            for (File file : listFiles) {
                if (file == null || !file.getName().toLowerCase().endsWith(".yml")) continue;
                file.renameTo(new File(getPluginInstance().getDataFolder(), "/portals/" + file.getName().toLowerCase()));

                try {
                    Portal portal = getPortalFromFile(file.getName().toLowerCase().replace(".yml", ""));
                    getPortalMap().put(portal.getPortalId(), portal);
                } catch (PortalFormException e) {
                    getPluginInstance().log(Level.WARNING, e.getMessage());
                }
            }
    }

    /**
     * Obtains a random value in a range.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The generated value.
     */
    public int getRandom(int min, int max) {
        return getRandom().nextInt((max - min) + 1) + min;
    }

    /**
     * Checks if a string is considered numerical. (Accepts negatives)
     *
     * @param string The text to check.
     * @return Whether it is numerical.
     */
    public boolean isNumeric(String string) {
        return string.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Colors the text passed.
     *
     * @param message The message to translate.
     * @return The colored text.
     */
    public String colorText(String message) {
        String messageCopy = message;
        if ((!getPluginInstance().getServerVersion().startsWith("v1_15") && !getPluginInstance().getServerVersion().startsWith("v1_14")
                && !getPluginInstance().getServerVersion().startsWith("v1_13") && !getPluginInstance().getServerVersion().startsWith("v1_12")
                && !getPluginInstance().getServerVersion().startsWith("v1_11") && !getPluginInstance().getServerVersion().startsWith("v1_10")
                && !getPluginInstance().getServerVersion().startsWith("v1_9") && !getPluginInstance().getServerVersion().startsWith("v1_8"))
                && messageCopy.contains("#")) {
            if (getPluginInstance().isPrismaInstalled()) messageCopy = ColorProvider.translatePrisma(messageCopy);
            else {
                try {
                    final Pattern hexPattern = Pattern.compile("\\{#([A-Fa-f0-9]){6}}");
                    Matcher matcher = hexPattern.matcher(message);
                    while (matcher.find()) {
                        final net.md_5.bungee.api.ChatColor hex = net.md_5.bungee.api.ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
                        final String pre = message.substring(0, matcher.start()), post = message.substring(matcher.end());
                        matcher = hexPattern.matcher(message = (pre + hex + post));
                    }
                } catch (IllegalArgumentException ignored) {
                }
                return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
            }
        }

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', messageCopy);
    }

    public void sendBarMessage(Player player, String message) {
        if (getBarHandler() == null || message == null || message.isEmpty()) return;
        getBarHandler().sendActionBar(player, message);
    }

    public void sendTitle(Player player, String title, String subTitle) {
        if (getTitleHandler() == null || ((title == null || title.isEmpty()) && (subTitle == null || subTitle.isEmpty())))
            return;

        if (title != null && !title.isEmpty() && subTitle != null && !subTitle.isEmpty())
            getTitleHandler().sendTitle(player, title, subTitle, getPluginInstance().getConfig().getInt("titles.fade-in"),
                    getPluginInstance().getConfig().getInt("titles.display-time"), getPluginInstance().getConfig().getInt("titles.fade-out"));
        else if (title != null && !title.isEmpty())
            getTitleHandler().sendTitle(player, title, getPluginInstance().getConfig().getInt("titles.fade-in"),
                    getPluginInstance().getConfig().getInt("titles.display-time"), getPluginInstance().getConfig().getInt("titles.fade-out"));
        else if (subTitle != null && !subTitle.isEmpty())
            getTitleHandler().sendSubTitle(player, subTitle, getPluginInstance().getConfig().getInt("titles.fade-in"),
                    getPluginInstance().getConfig().getInt("titles.display-time"), getPluginInstance().getConfig().getInt("titles.fade-out"));
    }

    /**
     * Updates a player's selection region.
     *
     * @param player    The player selecting.
     * @param location  The point location.
     * @param pointType The point type. (lowest or highest)
     * @return Whether the process was successful.
     */
    public boolean updateCurrentSelection(Player player, Location location, PointType pointType) {
        if (!getCurrentSelections().isEmpty() && getCurrentSelections().containsKey(player.getUniqueId())) {
            Region region = getCurrentSelections().get(player.getUniqueId());
            if (region != null) {
                switch (pointType) {
                    case POINT_ONE:
                        region.setPoint1(location);
                        break;
                    case POINT_TWO:
                        region.setPoint2(location);
                        break;
                    default:
                        break;
                }

                return selectionWorldCheck(player, region);
            }
        }

        Region region = null;
        switch (pointType) {
            case POINT_ONE:
            case POINT_TWO:
                region = new Region(getPluginInstance(), location, location);
                break;
            default:
                break;
        }

        return selectionWorldCheck(player, region);
    }

    /**
     * Obtains a current selection region from the passed player if possible.
     *
     * @param player The player to obtain a region from.
     * @return the region object.
     */
    public Region getCurrentSelection(Player player) {
        if (!getCurrentSelections().isEmpty() && getCurrentSelections().containsKey(player.getUniqueId()))
            return getCurrentSelections().get(player.getUniqueId());
        return null;
    }

    /**
     * Clears a player's region selection.
     *
     * @param player The player to clear for.
     */
    public void clearCurrentSelection(Player player) {
        getCurrentSelections().remove(player.getUniqueId());
        getSelectionMode().remove(player.getUniqueId());
    }

    /**
     * Sets the player's selection mode status.
     *
     * @param player        The player to set for.
     * @param selectionMode Whether they need to be in or out of selection mode.
     */
    public void setSelectionMode(Player player, boolean selectionMode) {
        getSelectionMode().put(player.getUniqueId(), selectionMode);
    }

    /**
     * Checks if player is in selection mode.
     *
     * @param player The player to check.
     * @return Whether thee player is in selection mode.
     */
    public boolean isInSelectionMode(Player player) {
        if (!getSelectionMode().isEmpty() && getSelectionMode().containsKey(player.getUniqueId()))
            return getSelectionMode().get(player.getUniqueId());
        return false;
    }

    /**
     * Updates a cooldown with an id.
     *
     * @param player     The player to update for.
     * @param cooldownId The id of the cooldown.
     */
    public void updatePlayerPortalCooldown(Player player, String cooldownId) {
        if (getPlayerPortalCooldowns().containsKey(player.getUniqueId())) {
            HashMap<String, Long> cooldownIds = getPlayerPortalCooldowns().get(player.getUniqueId());
            if (cooldownIds != null) {
                cooldownIds.put(cooldownId, System.currentTimeMillis());
                return;
            }
        }

        HashMap<String, Long> cooldownIds = new HashMap<>();
        cooldownIds.put(cooldownId, System.currentTimeMillis());
        getPlayerPortalCooldowns().put(player.getUniqueId(), cooldownIds);
    }

    /**
     * Checks if player is on cooldown for a particular id.
     *
     * @param player     The player to check.
     * @param cooldownId The cooldown id.
     * @param cooldown   The duration of the cooldown being checked.
     * @return Whether the player is on cooldown.
     */
    public boolean isPlayerOnCooldown(Player player, String cooldownId, int cooldown) {
        if (!getPlayerPortalCooldowns().isEmpty() && getPlayerPortalCooldowns().containsKey(player.getUniqueId()))
            return getCooldownTimeLeft(player, cooldownId, cooldown) > 0;
        return false;
    }

    /**
     * Obtains the remaining cooldown duration.
     *
     * @param player     The player to obtain the cooldown from.
     * @param cooldownId The cooldown id.
     * @param cooldown   The duration of the cooldown being checked.
     * @return The obtained remaining duration.
     */
    public long getCooldownTimeLeft(Player player, String cooldownId, int cooldown) {
        long cd = 0;
        if (cd >= 0)
            if (!getPlayerPortalCooldowns().isEmpty() && getPlayerPortalCooldowns().containsKey(player.getUniqueId())) {
                HashMap<String, Long> cooldownIds = getPlayerPortalCooldowns().get(player.getUniqueId());
                if (cooldownIds != null && cooldownIds.containsKey(cooldownId))
                    cd = cooldownIds.get(cooldownId);
            }

        final long calculated = ((cd / 1000) + cooldown) - (System.currentTimeMillis() / 1000);
        return (cd > 0) ? Math.max(calculated, 0) : 0;
    }

    /**
     * Attempts to obtain a portal from the passed location.
     *
     * @param location The location to check for.
     * @return The portal found (Can return NULL).
     */
    public Portal getPortalAtLocation(Location location) {
        for (Portal portal : getPortalMap().values())
            if (portal != null && getPortalMap().containsKey(portal.getPortalId().toLowerCase())
                    && portal.getRegion().isInRegion(location)) return portal;
        return null;
    }

    /**
     * Gets portal from the instance.
     *
     * @param portalId The portal Id.
     * @return The portal object instance.
     */
    public Portal getPortal(String portalId) {
        return ((!getPortalMap().isEmpty() && getPortalMap().containsKey(portalId.toLowerCase())) ? getPortalMap().get(portalId.toLowerCase()) : null);
    }

    /**
     * Obtains portal from file.
     *
     * @param portalId The portal id.
     * @return The portal object
     * @throws PortalFormException Failed to form the portal.
     */
    public Portal getPortalFromFile(String portalId) throws PortalFormException {
        File file = new File(getPluginInstance().getDataFolder(), "/portals/" + portalId.toLowerCase() + ".yml");
        if (file == null || !file.exists()) return null;
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        SerializableLocation pointOne = new SerializableLocation(getPluginInstance(), yaml.getString("point-one")),
                pointTwo = new SerializableLocation(getPluginInstance(), yaml.getString("point-two")),
                teleportLocation = new SerializableLocation(getPluginInstance(), yaml.getString("teleport-location"));
        if (!pointOne.getWorldName().equalsIgnoreCase(pointTwo.getWorldName()))
            throw new PortalFormException("The portal's point one and point two have mismatching world names ('" + pointTwo.getWorldName()
                    + "' does NOT equal '" + pointTwo.getWorldName() + "').");

        World world = getPluginInstance().getServer().getWorld(pointOne.getWorldName());
        if (world == null)
            throw new PortalFormException("The portal \"" + portalId + "\" has a world assigned to it that no longer exists.");

        final Region region = new Region(getPluginInstance(), pointOne, pointTwo);
        final Portal portal = new Portal(getPluginInstance(), file.getName().toLowerCase().replace(".yml", ""), region);

        portal.setTeleportLocation(teleportLocation);
        portal.setServerSwitchName(yaml.getString("portal-server"));
        portal.setCommandsOnly(yaml.getBoolean("commands-only"));
        portal.setCommands(yaml.getStringList("commands"));
        portal.setCooldown(yaml.getInt("cooldown", 0));

        String materialName = yaml.getString("last-fill-material");
        if (materialName != null && !materialName.equalsIgnoreCase("")) {
            Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_").replace("-", "_"));
            portal.setLastFillMaterial(material == null ? Material.AIR : material);
        } else portal.setLastFillMaterial(Material.AIR);

        if (yaml.contains("disabled")) portal.setDisabled(yaml.getBoolean("disabled"));
        if (yaml.contains("message")) portal.setMessage(yaml.getString("message"));
        if (yaml.contains("title")) portal.setTitle(yaml.getString("title"));
        if (yaml.contains("sub-title")) portal.setSubTitle(yaml.getString("sub-title"));
        if (yaml.contains("bar-message")) portal.setBarMessage(yaml.getString("bar-message"));

        return portal;
    }

    /**
     * Checks if a portal with a particular name exists.
     *
     * @param portalName The portal name to check for.
     * @return Whether the portal object exists.
     */
    public boolean doesPortalExist(String portalName) {
        return (!getPortalMap().isEmpty() && getPortalMap().containsKey(portalName.toLowerCase()));
    }

    /**
     * Handles general teleportation of a player to a location. (Handles the player's vehicle, if possible)
     *
     * @param entity   The entity to teleport.
     * @param location The destination.
     */
    @SuppressWarnings("deprecation")
    public void teleportWithEntity(Entity entity, Location location) {
        final boolean vehicleTeleportation = getPluginInstance().getConfig().getBoolean("vehicle-teleportation"),
                entityVelocity = getPluginInstance().getConfig().getBoolean("maintain-entity-velocity"),
                vehicleVelocity = getPluginInstance().getConfig().getBoolean("maintain-vehicle-velocity");
        boolean isNew = (!getPluginInstance().getServerVersion().startsWith("v1_7") && !getPluginInstance().getServerVersion().startsWith("v1_8")
                && !getPluginInstance().getServerVersion().startsWith("v1_9") && !getPluginInstance().getServerVersion().startsWith("v1_10"));

        if (entity instanceof Vehicle && vehicleTeleportation) {
            final Vehicle vehicle = (Vehicle) entity;
            Vector newVehicleDirection = vehicle.getVelocity().clone();

            List<Entity> passengersList = (!vehicle.isEmpty() ? (isNew ? new ArrayList<>(vehicle.getPassengers())
                    : Collections.singletonList(vehicle.getPassenger())) : Collections.emptyList());
            vehicle.eject();

            entity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            if (!entityVelocity) entity.setVelocity(new Vector(0, 0, 0));

            for (Entity e : passengersList) e.teleport(location);

            vehicle.teleport(location);
            if (!vehicleVelocity) vehicle.setVelocity(new Vector(0, 0, 0));

            getPluginInstance().getServer().getScheduler().runTaskLater(getPluginInstance(), () -> {
                for (Entity e : passengersList) {
                    if (isNew) vehicle.addPassenger(e);
                    else vehicle.setPassenger(e);
                    if (e instanceof Player) sendMountPacket((Player) e);
                }

                if (!vehicleVelocity) vehicle.setVelocity(newVehicleDirection);
                else vehicle.setVelocity(new Vector(0, 0, 0));
            }, 5);
            return;
        }

        if (vehicleTeleportation && entity.getVehicle() != null) {
            Vehicle vehicle = (Vehicle) entity.getVehicle();
            Vector newVehicleDirection = vehicle.getVelocity().clone();

            List<Entity> passengersList = (!vehicle.isEmpty() ? (isNew ? new ArrayList<>(vehicle.getPassengers())
                    : Collections.singletonList(vehicle.getPassenger())) : Collections.emptyList());
            vehicle.eject();

            for (Entity e : passengersList) {
                e.teleport(location);
                if (!entityVelocity) e.setVelocity(new Vector(0, 0, 0));
            }

            vehicle.teleport(location);
            if (!vehicleVelocity) vehicle.setVelocity(new Vector(0, 0, 0));

            getPluginInstance().getServer().getScheduler().runTaskLater(getPluginInstance(), () -> {
                for (Entity e : passengersList) {
                    if (isNew) vehicle.addPassenger(e);
                    else vehicle.setPassenger(e);
                    if (e instanceof Player) sendMountPacket((Player) e);
                }

                if (vehicleVelocity) vehicle.setVelocity(newVehicleDirection);
            }, 5);
        } else {
            Vector newDirection = entity.getVelocity().clone();
            entity.teleport(location);
            if (entityVelocity) entity.setVelocity(newDirection);
            else entity.setVelocity(new Vector(0, 0, 0));
        }
    }

    public void sendMountPacket(Player player) {
        if (getMountPacketClass() == null) return;
        try {
            Constructor<?> constructor = getMountPacketClass().getDeclaredConstructor(getEntityClass());

            Object castedPlayer = getCraftPlayerClass().cast(player);
            Method handleMethod = castedPlayer.getClass().getMethod("getHandle");
            Object entityPlayer = handleMethod.invoke(castedPlayer);
            Object packet = constructor.newInstance(entityPlayer);

            for (Player foundPlayer : player.getWorld().getPlayers()) {
                Object foundCastedPlayer = getCraftPlayerClass().cast(foundPlayer);
                Method foundHandleMethod = foundCastedPlayer.getClass().getMethod("getHandle");
                Object foundEntityPlayer = foundHandleMethod.invoke(foundCastedPlayer);
                Field foundConnection = foundEntityPlayer.getClass().getDeclaredField("playerConnection");
                foundConnection.setAccessible(true);
                Object connection = foundConnection.get(foundEntityPlayer);
                Method sendPacket = connection.getClass().getMethod("sendPacket", getPacketClass());
                sendPacket.invoke(connection, packet);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
            getPluginInstance().log(Level.WARNING, "There was an issue passing a packet to the playing involving mount teleportation.");
        }
    }

    /**
     * Checks if a player is facing a portal.
     *
     * @param player The player to check.
     * @param portal The portal to check for.
     * @param range  The range to check.
     * @return Whether they are facing the portal.
     */
    public boolean isFacingPortal(Player player, Portal portal, int range) {
        BlockIterator blockIterator = new BlockIterator(player, range);
        Block lastBlock;

        boolean foundPortal = false;
        while (blockIterator.hasNext()) {
            lastBlock = blockIterator.next();
            if (!portal.getRegion().isInRegion(lastBlock.getLocation()))
                continue;

            foundPortal = true;
            break;
        }

        return foundPortal;
    }

    /**
     * Obtains a direction from a raw yaw value.
     *
     * @param yaw The yaw value of a location.
     * @return The direction as a string. (Returns NORTH, SOUTH, etc.)
     */
    public String getDirection(double yaw) {
        if (yaw < 0)
            yaw += 360;
        if (yaw >= 315 || yaw < 45)
            return "SOUTH";
        else if (yaw < 135)
            return "WEST";
        else if (yaw < 225)
            return "NORTH";
        else if (yaw < 315)
            return "EAST";
        return "NORTH";
    }

    /**
     * Highlights the passed block for the player (packet) based on a region point (A or B).
     *
     * @param block     The block to highlight.
     * @param player    The player to send the packet to.
     * @param pointType The region point type. (Allows the plugin to keep track of two highlights for one user)
     */
    public void highlightBlock(Block block, Player player, PointType pointType) {
        if (getParticleHandler() == null) return;

        String particleEffect = getPluginInstance().getConfig().getString("selection-visual-effect").toUpperCase()
                .replace(" ", "_").replace("-", "_");
        if (particleEffect == null || particleEffect.isEmpty()) return;

        BukkitTask bukkitTask = new HighlightTask(getPluginInstance(), player, block.getLocation(), particleEffect)
                .runTaskTimerAsynchronously(getPluginInstance(), 0, 5);

        if (!getVisualTasks().isEmpty() && getVisualTasks().containsKey(player.getUniqueId())) {
            TaskHolder taskHolder = getVisualTasks().get(player.getUniqueId());
            if (taskHolder != null) {
                if (taskHolder.getRegionDisplay() != null) taskHolder.getRegionDisplay().cancel();
                if (pointType == PointType.POINT_ONE) taskHolder.setSelectionPointOne(bukkitTask);
                else taskHolder.setSelectionPointTwo(bukkitTask);
                return;
            }
        }

        TaskHolder taskHolder = new TaskHolder();
        if (pointType == PointType.POINT_ONE) taskHolder.setSelectionPointOne(bukkitTask);
        else taskHolder.setSelectionPointTwo(bukkitTask);
        getVisualTasks().put(player.getUniqueId(), taskHolder);
    }

    /**
     * Attempts to read a portals.yml located in the plugin's folder to convert it to the new file structure.
     */
    public void convertFromPortalsFile() {
        File portalFile = new File(getPluginInstance().getDataFolder(), "/portals.yml");
        if (!portalFile.exists()) return;

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(portalFile);
        final ConfigurationSection cs = yaml.getConfigurationSection("");
        if (cs == null) return;

        final List<String> portalIds = new ArrayList<>(cs.getKeys(false));
        if (portalIds.isEmpty()) return;

        for (String portalId : portalIds) {

            if (doesPortalExist(portalId)) {
                getPluginInstance().log(Level.WARNING, "The portal '" + portalId + "' already exists in the 'portals' folder. Skipping conversion...");
                continue;
            }

            SerializableLocation pointOne = new SerializableLocation(getPluginInstance(), yaml.getString(portalId + ".point-1.world"),
                    yaml.getDouble(portalId + ".point-1.x"), yaml.getDouble(portalId + ".point-1.y"),
                    yaml.getDouble(portalId + ".point-1.z"), yaml.getDouble(portalId + ".point-1.yaw"),
                    yaml.getDouble(portalId + ".point-1.pitch")), pointTwo = new SerializableLocation(getPluginInstance(), yaml.getString(portalId + ".point-2.world"),
                    yaml.getDouble(portalId + ".point-2.x"), yaml.getDouble(portalId + ".point-2.y"),
                    yaml.getDouble(portalId + ".point-2.z"), yaml.getDouble(portalId + ".point-2.yaw"),
                    yaml.getDouble(portalId + ".point-2.pitch")), teleportLocation = new SerializableLocation(getPluginInstance(), yaml.getString(portalId + ".teleport-location.world"),
                    yaml.getDouble(portalId + ".teleport-location.x"), yaml.getDouble(portalId + ".teleport-location.y"),
                    yaml.getDouble(portalId + ".teleport-location.z"), yaml.getDouble(portalId + ".teleport-location.yaw"),
                    yaml.getDouble(portalId + ".teleport-location.pitch"));

            if (!pointOne.getWorldName().equalsIgnoreCase(pointTwo.getWorldName())) {
                getPluginInstance().log(Level.WARNING, "The portal '" + portalId + "' has mismatching point one and point two world names. Skipping conversion...");
                return;
            }

            final Region region = new Region(getPluginInstance(), pointOne, pointTwo);
            final Portal portal = new Portal(getPluginInstance(), portalId.toLowerCase(), region);

            portal.setTeleportLocation(teleportLocation);
            portal.setServerSwitchName(yaml.getString(portalId + ".portal-server"));
            portal.setCommandsOnly(yaml.getBoolean(portalId + ".commands-only"));
            portal.setCommands(yaml.getStringList(portalId + ".commands"));
            portal.setCooldown(yaml.getInt("cooldown", 0));

            String materialName = yaml.getString(portalId + ".last-fill-material");
            if (materialName != null && !materialName.equalsIgnoreCase("")) {
                Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_").replace("-", "_"));
                portal.setLastFillMaterial(material == null ? Material.AIR : material);
            } else portal.setLastFillMaterial(Material.AIR);

            if (yaml.contains(portalId + ".disabled")) portal.setDisabled(yaml.getBoolean(portalId + ".disabled"));
            if (yaml.contains(portalId + ".message")) portal.setMessage(yaml.getString(portalId + ".message"));
            if (yaml.contains(portalId + ".title")) portal.setTitle(yaml.getString(portalId + ".title"));
            if (yaml.contains(portalId + ".sub-title")) portal.setSubTitle(yaml.getString(portalId + ".sub-title"));
            if (yaml.contains(portalId + ".bar-message"))
                portal.setBarMessage(yaml.getString(portalId + ".bar-message"));


            portal.save();
            getPluginInstance().log(Level.INFO, "The portal '" + portalId + "' was converted over to the new file data structure!");
        }

        portalFile.renameTo(new File(getPluginInstance().getDataFolder(), "/portals-backup.yml"));
        getPluginInstance().log(Level.INFO, "The portal file data structure conversion process has completed. "
                + "The file has been renamed to \"portals-backup.yml\".");
    }

    /**
     * Clears visual effects from the player's view and internal storage.
     *
     * @param player The player to clear for.
     */
    public void clearAllVisuals(Player player) {
        if (!getVisualTasks().isEmpty() && getVisualTasks().containsKey(player.getUniqueId())) {
            TaskHolder taskHolder = getVisualTasks().get(player.getUniqueId());
            if (taskHolder.getRegionDisplay() != null)
                taskHolder.getRegionDisplay().cancel();
            if (taskHolder.getSelectionPointOne() != null)
                taskHolder.getSelectionPointOne().cancel();
            if (taskHolder.getSelectionPointTwo() != null)
                taskHolder.getSelectionPointTwo().cancel();
        }
    }

    /**
     * Contacts the proxy to move the player to another attached server.
     *
     * @param player     The player to move.
     * @param serverName The server to move the player to. (Exact same from configuration or from the /server command)
     */
    public void switchServer(Player player, String serverName) {
        try {
            Bukkit.getMessenger().registerOutgoingPluginChannel(getPluginInstance(), "BungeeCord");
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(getPluginInstance(), "BungeeCord", byteArray.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            getPluginInstance().log(Level.WARNING,
                    "There seems to have been a issue when switching the player to the " + serverName + " server.");
        }
    }

    /**
     * Gets a list of all found portal names.
     *
     * @param withCoordinates adds coordinates to the end of names.
     * @return The list of portal name/ids.
     */
    public List<String> getPortalNames(boolean withCoordinates) {
        return new ArrayList<String>() {{
            for (Portal portal : getPortalMap().values()) {
                if (!withCoordinates) {
                    add(portal.getPortalId().toLowerCase());
                    continue;
                }

                final int x = (int) ((portal.getRegion().getPoint1().getX() + portal.getRegion().getPoint2().getX()) / 2),
                        y = (int) ((portal.getRegion().getPoint1().getY() + portal.getRegion().getPoint2().getY()) / 2),
                        z = (int) ((portal.getRegion().getPoint1().getZ() + portal.getRegion().getPoint2().getZ()) / 2);
                add((portal.isDisabled() ? ChatColor.RED : ChatColor.GREEN) + portal.getPortalId().toLowerCase() + " (World: "
                        + portal.getRegion().getPoint1().getWorldName() + " X: " + x + " Y: " + y + " Z: " + z + ")");
            }
        }};
    }

    /**
     * Gets the name of the specified portal
     *
     * @param withCoordinates adds coordinates to the end of names.
     * @return The portal name/id.
     */
    public String getPortalName(Portal portal, boolean withCoordinates) {
        final int x = (int) ((portal.getRegion().getPoint1().getX() + portal.getRegion().getPoint2().getX()) / 2),
                y = (int) ((portal.getRegion().getPoint1().getY() + portal.getRegion().getPoint2().getY()) / 2),
                z = (int) ((portal.getRegion().getPoint1().getZ() + portal.getRegion().getPoint2().getZ()) / 2);
        if (!withCoordinates) {
            return portal.getPortalId().toLowerCase();
        }else{

            return ((portal.isDisabled() ? ChatColor.RED : ChatColor.GREEN) + portal.getPortalId().toLowerCase() + ChatColor.GRAY + " (World: "
                    + ChatColor.DARK_GRAY + portal.getRegion().getPoint1().getWorldName() + ChatColor.GRAY + " X: " + ChatColor.DARK_GRAY + x + ChatColor.GRAY + " Y: " + ChatColor.DARK_GRAY + y + ChatColor.GRAY + " Z: " + ChatColor.DARK_GRAY + z + ChatColor.GRAY + ")");
        }

    }

    /**
     * Handles the vanilla portal teleport location replacements.
     *
     * @param player     The player to handle the teleportation for.
     * @param world      The world where the nether/end portal is located.
     * @param portalType The type of vanilla portal.
     * @return Whether actions succeeded.
     */
    public boolean handleVanillaPortalReplacements(Player player, World world, PortalType portalType) {
        for (String line : getPluginInstance().getConfig().getStringList((portalType == PortalType.NETHER ? "nether" : "end") + "-portal-locations")) {
            if (line == null || line.isEmpty() || !line.contains(":") || !line.contains(",")) continue;
            String[] mainSplit = line.split(":");
            if (!mainSplit[0].equalsIgnoreCase(world.getName())) continue;
            String[] subSplit = mainSplit[1].split(",");

            World newWorld = getPluginInstance().getServer().getWorld(subSplit[0]);
            if (newWorld == null) continue;

            final Location location = new Location(newWorld, Double.parseDouble(subSplit[1]), Double.parseDouble(subSplit[2]), Double.parseDouble(subSplit[3]),
                    Float.parseFloat(subSplit[4]), Float.parseFloat(subSplit[5]));
            player.teleport(location);
            return true;
        }

        return false;
    }

    /**
     * Gets the vanilla portal teleport location replacement.
     *
     * @param world      The world where the nether/end portal is located.
     * @param portalType The type of vanilla portal.
     * @return Whether actions succeeded.
     */
    public Location getVanillaPortalReplacement(World world, PortalType portalType) {
        for (String line : getPluginInstance().getConfig().getStringList((portalType == PortalType.NETHER ? "nether" : "end") + "-portal-locations")) {
            if (line == null || line.isEmpty() || !line.contains(":") || !line.contains(",")) continue;
            String[] mainSplit = line.split(":");
            if (!mainSplit[0].equalsIgnoreCase(world.getName())) continue;
            String[] subSplit = mainSplit[1].split(",");

            World newWorld = getPluginInstance().getServer().getWorld(subSplit[0]);
            if (newWorld == null) continue;

            return new Location(newWorld, Double.parseDouble(subSplit[1]), Double.parseDouble(subSplit[2]), Double.parseDouble(subSplit[3]),
                    Float.parseFloat(subSplit[4]), Float.parseFloat(subSplit[5]));
        }

        return null;
    }

    private boolean selectionWorldCheck(Player player, Region region) {
        if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
            player.sendMessage(colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("not-same-world-message")));
            return false;
        }

        getCurrentSelections().put(player.getUniqueId(), region);
        return true;
    }

    // getters & setters
    private HashMap<UUID, Region> getCurrentSelections() {
        return currentSelections;
    }

    private HashMap<UUID, Boolean> getSelectionMode() {
        return selectionMode;
    }

    private HashMap<UUID, HashMap<String, Long>> getPlayerPortalCooldowns() {
        return playerPortalCooldowns;
    }

    public ParticleHandler getParticleHandler() {
        return particleHandler;
    }

    public HashMap<UUID, TaskHolder> getVisualTasks() {
        return visualTasks;
    }

    public HashMap<UUID, SerializableLocation> getSmartTransferMap() {
        return smartTransferMap;
    }

    public Random getRandom() {
        return random;
    }

    private void setRandom(Random random) {
        this.random = random;
    }

    public TitleHandler getTitleHandler() {
        return titleHandler;
    }

    public BarHandler getBarHandler() {
        return barHandler;
    }

    public HashMap<UUID, String> getPortalLinkMap() {
        return portalLinkMap;
    }

    public HashMap<UUID, Portal> getEntitiesInTeleportationAndPortals(){
        return entitiesInTeleportationAndPortals;
    }

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }

    public HashMap<String, Portal> getPortalMap() {
        return portalMap;
    }

    public Class<?> getMountPacketClass() {
        return mountPacketClass;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Class<?> getPacketClass() {
        return packetClass;
    }

    public Class<?> getCraftPlayerClass() {
        return craftPlayerClass;
    }
}