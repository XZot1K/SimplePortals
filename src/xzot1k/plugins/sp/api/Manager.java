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
    private final List<Portal> portals;
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
        portals = new ArrayList<>();
        smartTransferMap = new HashMap<>();
        portalLinkMap = new HashMap<>();

        setRandom(new Random());
        setupPackets();
    }

    private void setupPackets() {
        try {
            switch (pluginInstance.getServerVersion()) {
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
                    particleHandler = new PH1_8R3(pluginInstance);
                    barHandler = new ABH1_8R3();
                    break;
                case "v1_8_R2":
                    titleHandler = new Titles1_8R2();
                    particleHandler = new PH1_8R2(pluginInstance);
                    barHandler = new ABH1_8R2();
                    break;
                case "v1_8_R1":
                    titleHandler = new Titles1_8R1();
                    particleHandler = new PH1_8R1(pluginInstance);
                    barHandler = new ABH1_8R1();
                    break;
                default:
                    break;
            }

            if (getParticleHandler() == null) particleHandler = new PH_Latest();
            if (getBarHandler() == null) barHandler = new ABH_Latest();
            if (getTitleHandler() == null) titleHandler = new Titles_Latest();

            pluginInstance.log(Level.INFO, "Packets have been setup for " + pluginInstance.getServerVersion() + "!");
        } catch (Exception e) {
            pluginInstance.log(Level.INFO, "There was an issue obtaining proper packets for " + pluginInstance.getServerVersion()
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
        if ((!pluginInstance.getServerVersion().startsWith("v1_15") && !pluginInstance.getServerVersion().startsWith("v1_14")
                && !pluginInstance.getServerVersion().startsWith("v1_13") && !pluginInstance.getServerVersion().startsWith("v1_12")
                && !pluginInstance.getServerVersion().startsWith("v1_11") && !pluginInstance.getServerVersion().startsWith("v1_10")
                && !pluginInstance.getServerVersion().startsWith("v1_9") && !pluginInstance.getServerVersion().startsWith("v1_8"))
                && messageCopy.contains("#")) {
            if (pluginInstance.isPrismaInstalled()) messageCopy = ColorProvider.translatePrisma(messageCopy);
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
            getTitleHandler().sendTitle(player, title, subTitle, pluginInstance.getConfig().getInt("titles.fade-in"),
                    pluginInstance.getConfig().getInt("titles.display-time"), pluginInstance.getConfig().getInt("titles.fade-out"));
        else if (title != null && !title.isEmpty())
            getTitleHandler().sendTitle(player, title, pluginInstance.getConfig().getInt("titles.fade-in"),
                    pluginInstance.getConfig().getInt("titles.display-time"), pluginInstance.getConfig().getInt("titles.fade-out"));
        else if (subTitle != null && !subTitle.isEmpty())
            getTitleHandler().sendSubTitle(player, subTitle, pluginInstance.getConfig().getInt("titles.fade-in"),
                    pluginInstance.getConfig().getInt("titles.display-time"), pluginInstance.getConfig().getInt("titles.fade-out"));
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
                region = new Region(pluginInstance, location, location);
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
     * Obtains a portal object from a location.
     *
     * @param location The location to check.
     * @return The portal object
     */
    public Portal getPortalAtLocation(Location location) {
        for (int i = -1; ++i < getPortals().size(); ) {
            Portal portal = getPortals().get(i);
            if (portal == null || portal.getRegion() == null) continue;

            if (portal.getRegion().isInRegion(location))
                return portal;
        }

        return null;
    }

    /**
     * Obtains a portal object by name.
     *
     * @param portalName The portal name.
     * @return The portal object.
     */
    public Portal getPortalById(String portalName) {
        for (int i = -1; ++i < getPortals().size(); ) {
            Portal portal = getPortals().get(i);
            if (portal.getPortalId().equalsIgnoreCase(portalName))
                return portal;
        }

        return null;
    }

    /**
     * Checks if a portal with a particular name exists.
     *
     * @param portalName The portal name to check for.
     * @return Whether the portal object exists.
     */
    public boolean doesPortalExist(String portalName) {
        for (int i = -1; ++i < getPortals().size(); ) {
            Portal portal = getPortals().get(i);
            if (portal.getPortalId().equalsIgnoreCase(portalName))
                return true;
        }

        return false;
    }

    /**
     * Handles general teleportation of a player to a location. (Handles the player's vehicle, if possible)
     *
     * @param player   The player to teleport.
     * @param location The destination.
     */
    @SuppressWarnings("deprecation")
    public void teleportPlayerWithEntity(Player player, Location location) {
        if (player.getVehicle() != null && pluginInstance.getConfig().getBoolean("vehicle-teleportation")) {
            Entity entity = player.getVehicle();
            if (pluginInstance.getServerVersion().startsWith("v1_11") || pluginInstance.getServerVersion().startsWith("v1_12")
                    || pluginInstance.getServerVersion().startsWith("v1_13") || pluginInstance.getServerVersion().startsWith("v1_14")
                    || pluginInstance.getServerVersion().startsWith("v1_15") || pluginInstance.getServerVersion().startsWith("v1_16"))
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
            }.runTaskLater(pluginInstance, 1);
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

        String particleEffect = pluginInstance.getConfig().getString("selection-visual-effect").toUpperCase()
                .replace(" ", "_").replace("-", "_");
        if (particleEffect == null || particleEffect.isEmpty()) return;

        BukkitTask bukkitTask = new HighlightTask(pluginInstance, player, block.getLocation(), particleEffect)
                .runTaskTimerAsynchronously(pluginInstance, 0, 5);

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
     * Loads portals from file and adds them to virtual storage.
     */
    public void loadPortals() {
        if (!getPortals().isEmpty()) getPortals().clear();
        File portalFile = new File(pluginInstance.getDataFolder(), "/portals.yml");
        if (!portalFile.exists()) return;
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(portalFile);

        final ConfigurationSection cs = yaml.getConfigurationSection("");
        if (cs == null) return;

        final List<String> portalIds = new ArrayList<>(cs.getKeys(false));
        if (portalIds.isEmpty()) return;

        for (int i = -1; ++i < portalIds.size(); ) {
            final String portalId = portalIds.get(i);

            if (doesPortalExist(portalId)) {
                pluginInstance.log(Level.WARNING, "The portal '" + portalId + "' was unable to be loaded due to a " +
                        "portal with a similar ID/Name already loaded.");
                return;
            }

            try {
                String pointOneWorld = yaml.getString(portalId + ".point-1.world"),
                        pointTwoWorld = yaml.getString(portalId + ".point-2.world"),
                        teleportWorld = yaml.getString(portalId + ".teleport-location.world");

                if (pointOneWorld == null || pointTwoWorld == null || teleportWorld == null
                        || pluginInstance.getServer().getWorld(pointOneWorld) == null
                        || pluginInstance.getServer().getWorld(pointTwoWorld) == null
                        || pluginInstance.getServer().getWorld(teleportWorld) == null) {
                    pluginInstance.log(Level.WARNING, "The portal '" + portalId
                            + "' was skipped and not loaded due to a invalid or missing world.");
                    continue;
                }

                SerializableLocation teleportPoint1 = new SerializableLocation(pluginInstance, pointOneWorld,
                        yaml.getDouble(portalId + ".point-1.x"), yaml.getDouble(portalId + ".point-1.y"),
                        yaml.getDouble(portalId + ".point-1.z"), yaml.getDouble(portalId + ".point-1.yaw"),
                        yaml.getDouble(portalId + ".point-1.pitch")),

                        teleportPoint2 = new SerializableLocation(pluginInstance, pointTwoWorld,
                                yaml.getDouble(portalId + ".point-2.x"), yaml.getDouble(portalId + ".point-2.y"),
                                yaml.getDouble(portalId + ".point-2.z"), yaml.getDouble(portalId + ".point-2.yaw"),
                                yaml.getDouble(portalId + ".point-2.pitch"));
                Region region = new Region(pluginInstance, teleportPoint1, teleportPoint2);
                Portal portal = new Portal(pluginInstance, portalId, region);

                SerializableLocation tpLocation = new SerializableLocation(pluginInstance, teleportWorld,
                        yaml.getDouble(portalId + ".teleport-location.x"), yaml.getDouble(portalId + ".teleport-location.y"),
                        yaml.getDouble(portalId + ".teleport-location.z"), yaml.getDouble(portalId + ".teleport-location.yaw"),
                        yaml.getDouble(portalId + ".teleport-location.pitch"));
                portal.setTeleportLocation(tpLocation);
                portal.setServerSwitchName(yaml.getString(portalId + ".portal-server"));
                portal.setCommandsOnly(yaml.getBoolean(portalId + ".commands-only"));
                portal.setCommands(yaml.getStringList(portalId + ".commands"));

                String materialName = yaml.getString(portalId + ".last-fill-material");
                portalMaterialCheckHelper(portal, materialName);

                if (yaml.contains(portalId + ".disabled")) portal.setDisabled(yaml.getBoolean(portalId + ".disabled"));
                if (yaml.contains(portalId + ".message")) portal.setMessage(yaml.getString(portalId + ".message"));
                if (yaml.contains(portalId + ".title")) portal.setTitle(yaml.getString(portalId + ".title"));
                if (yaml.contains(portalId + ".sub-title")) portal.setSubTitle(yaml.getString(portalId + ".sub-title"));
                if (yaml.contains(portalId + ".bar-message"))
                    portal.setBarMessage(yaml.getString(portalId + ".bar-message"));
            } catch (Exception ignored) {
                pluginInstance.log(Level.WARNING,
                        "The portal " + portalId + " was unable to be loaded. Please check its information in the portals.yml. "
                                + "This could be something as simple as a missing or invalid world.");
            }
        }
    }

    /**
     * Saves all portals to file.
     */
    public void savePortals() {
        for (int i = -1; ++i < getPortals().size(); ) {
            Portal portal = getPortals().get(i);
            portal.save();
        }
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
            Bukkit.getMessenger().registerOutgoingPluginChannel(pluginInstance, "BungeeCord");
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(pluginInstance, "BungeeCord", byteArray.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            pluginInstance.log(Level.WARNING,
                    "There seems to have been a issue when switching the player to the " + serverName + " server.");
        }
    }

    private void portalMaterialCheckHelper(Portal portal, String materialName) {
        if (materialName != null && !materialName.equalsIgnoreCase("")) {
            Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_")
                    .replace("-", "_"));
            portal.setLastFillMaterial(material == null ? Material.AIR : material);
        } else portal.setLastFillMaterial(Material.AIR);

        portal.register();
    }

    private boolean selectionWorldCheck(Player player, Region region) {
        if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getLangConfig().getString("prefix")
                    + pluginInstance.getLangConfig().getString("not-same-world-message")));
            return false;
        }

        getCurrentSelections().put(player.getUniqueId(), region);
        return true;
    }

    // getters & setters
    private HashMap<UUID, Region> getCurrentSelections() {
        return currentSelections;
    }

    public List<Portal> getPortals() {
        return portals;
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
}