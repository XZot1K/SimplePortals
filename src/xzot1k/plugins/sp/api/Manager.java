/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
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

    private Random random;
    private ParticleHandler particleHandler;
    private TitleHandler titleHandler;
    private BarHandler barHandler;

    public Manager(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
        currentSelections = new HashMap<>();
        selectionMode = new HashMap<>();
        playerPortalCooldowns = new HashMap<>();
        visualTasks = new HashMap<>();
        smartTransferMap = new HashMap<>();
        portalLinkMap = new HashMap<>();

        setRandom(new Random());
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
                } catch (IllegalArgumentException ignored) {}
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
        if (!getCurrentSelections().isEmpty())
            getCurrentSelections().remove(player.getUniqueId());
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
        final File portalDirectory = new File(getPluginInstance().getDataFolder(), "/portals");
        File[] listFiles = portalDirectory.listFiles();

        if (listFiles != null && listFiles.length > 0)
            for (int i = -1; ++i < listFiles.length; ) {
                File file = listFiles[i];
                if (file == null || !file.getName().toLowerCase().endsWith(".yml")) continue;

                try {
                    Portal portal = getPortal(file.getName().replaceAll("(?i)\\.yml", ""));
                    if (portal.getRegion().isInRegion(location)) return portal;
                } catch (PortalFormException e) {
                    e.printStackTrace();
                    getPluginInstance().log(Level.WARNING, e.getMessage());
                }
            }

        return null;
    }

    public Portal getPortal(String portalId) throws PortalFormException {
        File file = new File(getPluginInstance().getDataFolder(), "/portals/" + portalId + ".yml");
        if (file == null || !file.exists()) return null;
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        SerializableLocation pointOne = new SerializableLocation(getPluginInstance(), yaml.getString("point-one")),
                pointTwo = new SerializableLocation(getPluginInstance(), yaml.getString("point-two")),
                teleportLocation = new SerializableLocation(getPluginInstance(), yaml.getString("teleport-location"));
        if (!pointOne.getWorldName().equalsIgnoreCase(pointTwo.getWorldName()))
            throw new PortalFormException("The portal's point one and point two have mismatching world names ('" + pointTwo.getWorldName()
                    + "' does NOT equal '" + pointTwo.getWorldName() + "').");

        final Region region = new Region(getPluginInstance(), pointOne, pointTwo);
        final Portal portal = new Portal(getPluginInstance(), file.getName().replaceAll("(?i)\\.yml", ""), region);
        portal.setTeleportLocation(teleportLocation);
        portal.setServerSwitchName(yaml.getString("portal-server"));
        portal.setCommandsOnly(yaml.getBoolean("commands-only"));
        portal.setCommands(yaml.getStringList("commands"));

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
        return new File(getPluginInstance().getDataFolder(), "/portals/" + portalName + ".yml").exists();
    }

    /**
     * Handles general teleportation of a player to a location. (Handles the player's vehicle, if possible)
     *
     * @param player   The player to teleport.
     * @param location The destination.
     */
    @SuppressWarnings("deprecation")
    public void teleportPlayerWithEntity(Player player, Location location) {
        if (player.getVehicle() != null && getPluginInstance().getConfig().getBoolean("vehicle-teleportation")) {
            Entity entity = player.getVehicle();
            if (getPluginInstance().getServerVersion().startsWith("v1_11") || getPluginInstance().getServerVersion().startsWith("v1_12")
                    || getPluginInstance().getServerVersion().startsWith("v1_13") || getPluginInstance().getServerVersion().startsWith("v1_14")
                    || getPluginInstance().getServerVersion().startsWith("v1_15") || getPluginInstance().getServerVersion().startsWith("v1_16"))
                entity.removePassenger(player);
            else entity.setPassenger(null);

            if (entity.getPassengers().contains(player))
                entity.eject();

            player.teleport(location);
            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.teleport(player.getLocation());
                    entity.addPassenger(player);
                }
            }.runTaskLater(getPluginInstance(), 1);
        } else
            player.teleport(location);
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
            final Portal portal = new Portal(getPluginInstance(), portalId, region);

            portal.setTeleportLocation(teleportLocation);
            portal.setServerSwitchName(yaml.getString(portalId + ".portal-server"));
            portal.setCommandsOnly(yaml.getBoolean(portalId + ".commands-only"));
            portal.setCommands(yaml.getStringList(portalId + ".commands"));

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

        getPluginInstance().log(Level.INFO, "The portal file data structure conversion process has completed. "
                + "Don't forget to remove or move your 'portals.yml' to prevent another conversion process!");
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
     * @return The list of portal name/ids.
     */
    public List<String> getPortalNames() {
        return new ArrayList<String>() {{
            File file = new File(getPluginInstance().getDataFolder(), "/portals");
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0)
                for (int i = -1; ++i < listFiles.length; ) {
                    File foundFile = listFiles[i];
                    if (foundFile != null && foundFile.getName().toLowerCase().endsWith(".yml"))
                        add(foundFile.getName().replaceAll("(?i)\\.yml", ""));
                }
        }};
    }

    private boolean selectionWorldCheck(Player player, Region region) {
        if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
            player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
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

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }

}