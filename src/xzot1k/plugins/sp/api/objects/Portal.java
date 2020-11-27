/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.Direction;
import xzot1k.plugins.sp.api.enums.PortalCommandType;
import xzot1k.plugins.sp.core.objects.TaskHolder;
import xzot1k.plugins.sp.core.tasks.RegionTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Portal {

    private final SimplePortals pluginInstance;
    private Region region;
    private SerializableLocation teleportLocation;
    private String portalId, serverSwitchName, message, title, subTitle, barMessage;
    private boolean commandsOnly, disabled;
    private List<String> commands;
    private Material lastFillMaterial;

    public Portal(SimplePortals pluginInstance, String portalId, Region region) {
        this.pluginInstance = pluginInstance;
        setRegion(region);
        setPortalId(portalId);
        setDisabled(false);
        setCommands(new ArrayList<>());
        setCommandsOnly(false);
        setLastFillMaterial(Material.AIR);
        if (getRegion() != null && getRegion().getPoint1() != null)
            setTeleportLocation(getRegion().getPoint1().asBukkitLocation().clone().add(0, 2, 0));
        setMessage(getPluginInstance().getLangConfig().getString("portal-message"));
        setTitle(getPluginInstance().getLangConfig().getString("portal-title-message"));
        setSubTitle(getPluginInstance().getLangConfig().getString("portal-subtitle-message"));
        setBarMessage(getPluginInstance().getLangConfig().getString("portal-bar-message"));
    }

    /**
     * Attempts to delete the portal file.
     *
     * @return Whether the process was successful.
     */
    public boolean delete() {
        File file = new File(getPluginInstance().getDataFolder(), "/portals/" + getPortalId() + ".yml");
        if (file.exists()) {
            file.delete();
            return true;
        }

        return false;
    }

    /**
     * Attempts to save the portal to its own file located in the portals folder.
     */
    public void save() {
        try {
            File file = new File(getPluginInstance().getDataFolder(), "/portals/" + getPortalId() + ".yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            yaml.set("last-fill-material", getLastFillMaterial().name());
            yaml.set("portal-server", getServerSwitchName());

            SerializableLocation pointOne = getRegion().getPoint1();
            if (pointOne != null) yaml.set("point-one", pointOne.toString());

            SerializableLocation pointTwo = getRegion().getPoint2();
            if (pointTwo != null) yaml.set("point-two", pointTwo.toString());

            SerializableLocation teleportLocation = getTeleportLocation();
            if (teleportLocation != null) yaml.set("teleport-location", teleportLocation.toString());

            yaml.set("commands-only", isCommandsOnly());
            yaml.set("commands", getCommands());
            yaml.set("disabled", isDisabled());
            yaml.set("message", getMessage());
            yaml.set("title", getTitle());
            yaml.set("sub-title", getSubTitle());
            yaml.set("bar-message", getBarMessage());

            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Invokes all commands attached to the portal (includes percentage calculations).
     *
     * @param player            The player to send command based on.
     * @param locationForCoords The location where the player is or should be.
     */
    public void invokeCommands(Player player, Location locationForCoords) {
        getPluginInstance().getServer().getScheduler().runTaskLater(getPluginInstance(), () -> {
            for (String commandLine : getCommands()) {
                PortalCommandType portalCommandType = PortalCommandType.CONSOLE;
                int percentage = 100;
                if (commandLine.contains(":")) {
                    String[] commandLineSplit = commandLine.split(":");
                    if (commandLineSplit.length == 2) {
                        PortalCommandType foundPortalCommandType = PortalCommandType.getType(commandLineSplit[1]);
                        if (foundPortalCommandType != null) portalCommandType = foundPortalCommandType;
                    } else if (commandLineSplit.length == 3) {
                        PortalCommandType foundPortalCommandType = PortalCommandType.getType(commandLineSplit[1]);
                        if (foundPortalCommandType != null) portalCommandType = foundPortalCommandType;

                        String foundPercentValue = commandLineSplit[2];
                        if (getPluginInstance().getManager().isNumeric(foundPercentValue))
                            percentage = Integer.parseInt(foundPercentValue);
                    }
                }


                int chance = getPluginInstance().getManager().getRandom(1, 100);
                if (chance < percentage) {
                    commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                            .replaceAll("(?i):chat", "").replaceAll("(?i):" + percentage, "");
                    switch (portalCommandType) {

                        case PLAYER:
                            getPluginInstance().getServer().dispatchCommand(player, commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        case CHAT:
                            player.chat(commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        default:
                            getPluginInstance().getServer().dispatchCommand(getPluginInstance().getServer().getConsoleSender(), commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                    }
                }
            }
        }, getPluginInstance().getConfig().getInt("command-tick-delay"));
    }

    /**
     * Performs the general action of the portal by teleporting the player and playing effects. (Handles server transfer)
     *
     * @param player The player to perform actions on.
     */
    public void performAction(Player player) {
        if (getServerSwitchName() == null || getServerSwitchName().equalsIgnoreCase("none")) {
            Location location = getTeleportLocation().asBukkitLocation();
            if (location != null) {
                if (getPluginInstance().getConfig().getBoolean("keep-teleport-head-axis")) {
                    location.setYaw(player.getLocation().getYaw());
                    location.setPitch(player.getLocation().getPitch());
                }

                getPluginInstance().getManager().teleportPlayerWithEntity(player, location);
                getPluginInstance().getManager().getPortalLinkMap().put(player.getUniqueId(), getPortalId());
            }
        } else {
            if ((!getPluginInstance().getManager().getSmartTransferMap().isEmpty()
                    && getPluginInstance().getManager().getSmartTransferMap().containsKey(player.getUniqueId()))) {
                SerializableLocation serializableLocation = getPluginInstance().getManager().getSmartTransferMap()
                        .get(player.getUniqueId());

                if (getPluginInstance().getManager().isFacingPortal(player, this, 5)) {
                    double currentYaw = serializableLocation.getYaw();
                    String direction = getPluginInstance().getManager().getDirection(currentYaw);

                    // Set YAW to opposite directions.
                    switch (direction.toUpperCase()) {
                        case "NORTH":
                            serializableLocation.setYaw(0);
                            break;

                        case "SOUTH":
                            serializableLocation.setYaw(180);
                            break;

                        case "EAST":
                            serializableLocation.setYaw(90);
                            break;

                        case "WEST":
                            serializableLocation.setYaw(-90);
                            break;
                        default:
                            break;
                    }

                }

                getPluginInstance().getManager().teleportPlayerWithEntity(player, serializableLocation.asBukkitLocation());
                getPluginInstance().getManager().getPortalLinkMap().put(player.getUniqueId(), getPortalId());
            }

            getPluginInstance().getManager().switchServer(player, getServerSwitchName());
        }

        String particleEffect = getPluginInstance().getConfig().getString("teleport-visual-effect");
        if (particleEffect != null && !particleEffect.isEmpty())
            getPluginInstance().getManager().getParticleHandler().broadcastParticle(player.getLocation(), 1, 2, 1, 0,
                    particleEffect.toUpperCase().replace(" ", "_").replace("-", "_"), 10);

        String soundName = getPluginInstance().getConfig().getString("teleport-sound");
        if (soundName != null && !soundName.isEmpty())
            player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase().replace(" ", "_")
                    .replace("-", "_")), 1, 1);
    }

    /**
     * Attempts to fill a portals region with the passed material and durability.
     *
     * @param player     The player whom filled the portal's region (Determines Block Face Direction).
     * @param material   The material that is used.
     * @param durability The durability to modify the material.
     */
    public void fillPortal(Player player, Material material, int durability) {
        if (!getRegion().getPoint1().getWorldName().equalsIgnoreCase(getRegion().getPoint2().getWorldName())) return;

        int lowestX = (int) Math.min(getRegion().getPoint1().getX(), getRegion().getPoint2().getX()),
                highestX = (int) Math.max(getRegion().getPoint1().getX(), getRegion().getPoint2().getX()),

                lowestY = (int) Math.min(getRegion().getPoint1().getY(), getRegion().getPoint2().getY()),
                highestY = (int) Math.max(getRegion().getPoint1().getY(), getRegion().getPoint2().getY()),

                lowestZ = (int) Math.min(getRegion().getPoint1().getZ(), getRegion().getPoint2().getZ()),
                highestZ = (int) Math.max(getRegion().getPoint1().getZ(), getRegion().getPoint2().getZ());

        final World world = getPluginInstance().getServer().getWorld(getRegion().getPoint1().getWorldName());
        for (int x = (lowestX - 1); ++x <= highestX; )
            for (int z = (lowestZ - 1); ++z <= highestZ; )
                for (int y = (lowestY - 1); ++y <= highestY; ) {
                    final Location location = new Location(world, x, y, z);
                    final Block block = location.getBlock();
                    final BlockState blockState = block.getState();
                    if (block.getType() == Material.AIR || block.getType() == getLastFillMaterial()) {
                        blockState.setType(material);

                        if (!getPluginInstance().getServerVersion().startsWith("v1_12") && !getPluginInstance().getServerVersion().startsWith("v1_11") && !getPluginInstance().getServerVersion().startsWith("v1_10")
                                && !getPluginInstance().getServerVersion().startsWith("v1_9") && !getPluginInstance().getServerVersion().startsWith("v1_8") && !getPluginInstance().getServerVersion().startsWith("v1_7"))
                            try {
                                Method method = block.getClass().getMethod("setData", Byte.class);
                                if (method != null)
                                    method.invoke(block, (byte) durability);
                            } catch (Exception ignored) {}

                        if (!getPluginInstance().getServerVersion().startsWith("v1_7") && !getPluginInstance().getServerVersion().startsWith("v1_8")
                                && !getPluginInstance().getServerVersion().startsWith("v1_9") && !getPluginInstance().getServerVersion().startsWith("v1_10")) {
                            blockState.update(true, false);
                            blockState.setBlockData(getPluginInstance().getServer().createBlockData(material));
                            setBlock(block, material, BlockFace.valueOf(Direction.getYaw(player).name()));
                        } else {
                            if (block instanceof Directional)
                                try {
                                    Method method = BlockState.class.getMethod("setData", Byte.class);
                                    method.setAccessible(true);
                                    method.invoke(block, oppositeDirectionByte(Direction.getYaw(player)));
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                }
                            else try {
                                Method method = Block.class.getMethod("setData", Byte.class);
                                method.setAccessible(true);
                                method.invoke(block, oppositeDirectionByte(Direction.getYaw(player)));
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                            }

                            blockState.update(true, false);
                        }
                    }
                }

        setLastFillMaterial(material);
    }

    /**
     * Displays the region using particle packets.
     *
     * @param player The player to display to.
     */
    public void displayRegion(Player player) {
        String particleEffect = getPluginInstance().getConfig().getString("region-visual-effect");
        if (particleEffect == null || particleEffect.isEmpty()) return;

        BukkitTask bukkitTask = new RegionTask(getPluginInstance(), player, this).runTaskTimerAsynchronously(getPluginInstance(), 0, 5);
        if (!getPluginInstance().getManager().getVisualTasks().isEmpty() && getPluginInstance().getManager().getVisualTasks().containsKey(player.getUniqueId())) {
            TaskHolder taskHolder = getPluginInstance().getManager().getVisualTasks().get(player.getUniqueId());
            if (taskHolder != null) {
                if (taskHolder.getSelectionPointOne() != null)
                    taskHolder.getSelectionPointOne().cancel();
                if (taskHolder.getSelectionPointTwo() != null)
                    taskHolder.getSelectionPointTwo().cancel();
                taskHolder.setRegionDisplay(bukkitTask);
                return;
            }
        }

        TaskHolder taskHolder = new TaskHolder();
        taskHolder.setRegionDisplay(bukkitTask);
        getPluginInstance().getManager().getVisualTasks().put(player.getUniqueId(), taskHolder);
    }

    private void setBlock(Block block, Material material, BlockFace blockFace) {
        final BlockState blockState = block.getState();
        blockState.setType(material);

        if (material.name().contains("PORTAL"))
            if (blockFace.name().startsWith("NORTH") || blockFace.name().startsWith("SOUTH"))
                blockFace = BlockFace.WEST;
            else if (blockFace.name().startsWith("EAST") || blockFace.name().startsWith("WEST"))
                blockFace = BlockFace.SOUTH;

        org.bukkit.block.data.BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(blockFace);
            blockState.setBlockData(blockData);
        }

        if (blockData instanceof org.bukkit.block.data.Orientable) {
            ((org.bukkit.block.data.Orientable) blockData).setAxis(org.bukkit.Axis.valueOf(convertBlockFaceToAxis(blockFace)));
            blockState.setBlockData(blockData);
        }

        if (blockData instanceof org.bukkit.block.data.Rotatable) {
            ((org.bukkit.block.data.Rotatable) blockData).setRotation(blockFace);
            blockState.setBlockData(blockData);
        }

        blockState.update(true, false);
    }

    private String convertBlockFaceToAxis(BlockFace face) {
        switch (face) {
            case NORTH:
            case SOUTH:
                return "Z";
            case UP:
            case DOWN:
                return "Y";
            case EAST:
            case WEST:
            default:
                return "X";
        }
    }

    private byte oppositeDirectionByte(Direction direction) {
        for (int i = -1; ++i < Direction.values().length; )
            if (direction == Direction.values()[i]) return (byte) i;
        return 4;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getPortalId() {
        return portalId;
    }

    private void setPortalId(String portalId) {
        this.portalId = portalId;
    }

    public SerializableLocation getTeleportLocation() {
        return teleportLocation;
    }

    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = new SerializableLocation(getPluginInstance(), teleportLocation);
    }

    public void setTeleportLocation(SerializableLocation teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public String getServerSwitchName() {
        return serverSwitchName;
    }

    public void setServerSwitchName(String serverSwitchName) {
        this.serverSwitchName = serverSwitchName;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public boolean isCommandsOnly() {
        return commandsOnly;
    }

    public void setCommandsOnly(boolean commandsOnly) {
        this.commandsOnly = commandsOnly;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Material getLastFillMaterial() {
        return lastFillMaterial;
    }

    public void setLastFillMaterial(Material lastFillMaterial) {
        this.lastFillMaterial = lastFillMaterial;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getBarMessage() {
        return barMessage;
    }

    public void setBarMessage(String barMessage) {
        this.barMessage = barMessage;
    }

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }
}
