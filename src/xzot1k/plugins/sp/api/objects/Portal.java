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
    private String portalId, serverSwitchName;
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
    }

    /**
     * Adds the portal to the virtual storage.
     */
    public void register() {
        if (!pluginInstance.getManager().getPortals().contains(this))
            pluginInstance.getManager().getPortals().add(this);
    }

    /**
     * Removes the portal from virtual storage.
     */
    public void unregister() {
        pluginInstance.getManager().getPortals().remove(this);
    }

    /**
     * Deletes the portal from file and virtual storage.
     */
    public void delete() {
        try {
            File file = new File(pluginInstance.getDataFolder(), "/portals.yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            yaml.set(getPortalId(), null);
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the portal to file.
     */
    public void save() {
        try {
            File file = new File(pluginInstance.getDataFolder(), "/portals.yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            yaml.set(getPortalId() + ".last-fill-material", getLastFillMaterial().name());
            yaml.set(getPortalId() + ".portal-server", getServerSwitchName());

            // save point 1.
            SerializableLocation point1 = getRegion().getPoint1();
            if (point1 != null) {
                yaml.set(getPortalId() + ".point-1.world", point1.getWorldName());
                yaml.set(getPortalId() + ".point-1.x", point1.getX());
                yaml.set(getPortalId() + ".point-1.y", point1.getY());
                yaml.set(getPortalId() + ".point-1.z", point1.getZ());
            }

            // save point 2.
            SerializableLocation point2 = getRegion().getPoint2();
            if (point2 != null) {
                yaml.set(getPortalId() + ".point-2.world", point2.getWorldName());
                yaml.set(getPortalId() + ".point-2.x", point2.getX());
                yaml.set(getPortalId() + ".point-2.y", point2.getY());
                yaml.set(getPortalId() + ".point-2.z", point2.getZ());
            }

            // save teleport location.
            SerializableLocation teleportLocation = getTeleportLocation();
            if (teleportLocation != null) {
                yaml.set(getPortalId() + ".teleport-location.world",
                        teleportLocation.getWorldName());
                yaml.set(getPortalId() + ".teleport-location.x", teleportLocation.getX());
                yaml.set(getPortalId() + ".teleport-location.y", teleportLocation.getY());
                yaml.set(getPortalId() + ".teleport-location.z", teleportLocation.getZ());
                yaml.set(getPortalId() + ".teleport-location.yaw",
                        teleportLocation.getYaw());
                yaml.set(getPortalId() + ".teleport-location.pitch",
                        teleportLocation.getPitch());
            }

            yaml.set(getPortalId() + ".commands-only", isCommandsOnly());
            yaml.set(getPortalId() + ".commands", getCommands());
            yaml.set(getPortalId() + ".disabled", isDisabled());

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
        pluginInstance.getServer().getScheduler().runTaskLater(pluginInstance, () -> {
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
                        if (pluginInstance.getManager().isNumeric(foundPercentValue))
                            percentage = Integer.parseInt(foundPercentValue);
                    }
                }


                int chance = pluginInstance.getManager().getRandom(1, 100);
                if (chance < percentage) {
                    commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                            .replaceAll("(?i):chat", "").replaceAll("(?i):" + percentage, "");
                    switch (portalCommandType) {

                        case PLAYER:
                            pluginInstance.getServer().dispatchCommand(player, commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        case CHAT:
                            player.chat(commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        default:
                            pluginInstance.getServer().dispatchCommand(pluginInstance.getServer().getConsoleSender(), commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                    }
                }
            }
        }, pluginInstance.getConfig().getInt("command-tick-delay"));
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
                if (pluginInstance.getConfig().getBoolean("keep-teleport-head-axis")) {
                    location.setYaw(player.getLocation().getYaw());
                    location.setPitch(player.getLocation().getPitch());
                }

                pluginInstance.getManager().teleportPlayerWithEntity(player, location);
            }
        } else {
            if ((!pluginInstance.getManager().getSmartTransferMap().isEmpty()
                    && pluginInstance.getManager().getSmartTransferMap().containsKey(player.getUniqueId()))) {
                SerializableLocation serializableLocation = pluginInstance.getManager().getSmartTransferMap()
                        .get(player.getUniqueId());

                if (pluginInstance.getManager().isFacingPortal(player, this, 5)) {
                    double currentYaw = serializableLocation.getYaw();
                    String direction = pluginInstance.getManager().getDirection(currentYaw);

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

                pluginInstance.getManager().teleportPlayerWithEntity(player, serializableLocation.asBukkitLocation());
            }

            pluginInstance.getManager().switchServer(player, getServerSwitchName());
        }

        String particleEffect = pluginInstance.getConfig().getString("teleport-visual-effect");
        if (particleEffect != null && !particleEffect.isEmpty())
            pluginInstance.getManager().getParticleHandler().broadcastParticle(player.getLocation(), 1, 2, 1, 0,
                    particleEffect.toUpperCase().replace(" ", "_").replace("-", "_"), 10);

        String soundName = pluginInstance.getConfig().getString("teleport-sound");
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

        final World world = pluginInstance.getServer().getWorld(getRegion().getPoint1().getWorldName());
        for (int y = lowestY - 1; ++y <= highestY; )
            for (int x = lowestX - 1; ++x <= highestX; )
                for (int z = lowestZ - 1; ++z <= highestZ; ) {
                    Location location = new Location(world, x, y, z);
                    if (location.getBlock().getType() == Material.AIR || location.getBlock().getType() == getLastFillMaterial()) {
                        location.getBlock().setType(material);
                        if ((pluginInstance.getServerVersion().toLowerCase().startsWith("v1_14") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_15"))
                                && !pluginInstance.getServerVersion().toLowerCase().startsWith("v1_13")) {
                            final Block block = location.getBlock();
                            block.setType(Material.AIR);
                            block.setType(material);

                            if ((pluginInstance.getServerVersion().startsWith("v1_12") || pluginInstance.getServerVersion().startsWith("v1_11")
                                    || pluginInstance.getServerVersion().startsWith("v1_10") || pluginInstance.getServerVersion().startsWith("v1_9")))
                                try {
                                    Method method = block.getClass().getMethod("setData", Byte.class);
                                    if (method != null)
                                        method.invoke(block, (byte) durability);
                                } catch (Exception ignored) {}

                            if (pluginInstance.getServerVersion().startsWith("v1_11") || pluginInstance.getServerVersion().startsWith("v1_12")
                                    || pluginInstance.getServerVersion().startsWith("v1_13") || pluginInstance.getServerVersion().startsWith("v1_14")
                                    || pluginInstance.getServerVersion().startsWith("v1_15") || pluginInstance.getServerVersion().startsWith("v1_16")) {
                                block.setBlockData(pluginInstance.getServer().createBlockData(material));
                                setBlock(block, material, BlockFace.valueOf(Direction.getYaw(player).name()));
                            } else {
                                if (block instanceof Directional)
                                    try {
                                        Method method = Block.class.getMethod("setData", Byte.class, Boolean.class);
                                        method.setAccessible(true);
                                        method.invoke(block, oppositeDirectionByte(Direction.getYaw(player)), true);
                                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                    }
                                else try {
                                    Method method = Block.class.getMethod("setData", Byte.class, Boolean.class);
                                    method.setAccessible(true);
                                    method.invoke(block, block.getData(), true);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                }
                            }
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
        String particleEffect = pluginInstance.getConfig().getString("region-visual-effect");
        if (particleEffect == null || particleEffect.isEmpty()) return;

        BukkitTask bukkitTask = new RegionTask(pluginInstance, player, this).runTaskTimerAsynchronously(pluginInstance, 0, 5);
        if (!pluginInstance.getManager().getVisualTasks().isEmpty() && pluginInstance.getManager().getVisualTasks().containsKey(player.getUniqueId())) {
            TaskHolder taskHolder = pluginInstance.getManager().getVisualTasks().get(player.getUniqueId());
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
        pluginInstance.getManager().getVisualTasks().put(player.getUniqueId(), taskHolder);
    }

    private void setBlock(Block block, Material material, BlockFace blockFace) {
        block.setType(material);

        org.bukkit.block.data.BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(blockFace);
            block.setBlockData(blockData);
        }

        if (blockData instanceof org.bukkit.block.data.Orientable) {
            ((org.bukkit.block.data.Orientable) blockData).setAxis(org.bukkit.Axis.valueOf(convertBlockFaceToAxis(blockFace)));
            block.setBlockData(blockData);
        }

        if (blockData instanceof org.bukkit.block.data.Rotatable) {
            ((org.bukkit.block.data.Rotatable) blockData).setRotation(blockFace);
            block.setBlockData(blockData);
        }
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
        this.teleportLocation = new SerializableLocation(pluginInstance, teleportLocation);
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
}
