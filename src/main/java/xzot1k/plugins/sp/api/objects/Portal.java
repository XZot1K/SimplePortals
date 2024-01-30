/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.enums.Direction;
import xzot1k.plugins.sp.api.enums.PortalCommandType;
import xzot1k.plugins.sp.core.tasks.RegionTask;
import xzot1k.plugins.sp.core.tasks.TeleportTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Portal {

    private final SimplePortals pluginInstance;
    private Region region;
    private SerializableLocation teleportLocation, serverSwitchLocation;
    private String portalId, serverSwitchName, message, title, subTitle, barMessage;
    private boolean commandsOnly, disabled;
    private List<String> commands;
    private Material lastFillMaterial;
    private int cooldown, // in seconds - nether-portal-like cooldown
            delay; // in seconds - nether-portal-like delay

    public Portal(SimplePortals pluginInstance, String portalId, Region region) {
        this.pluginInstance = pluginInstance;
        this.serverSwitchLocation = null;

        setRegion(region);
        setPortalId(portalId.toLowerCase());
        setDisabled(false);
        setCommands(new ArrayList<>());
        setCommandsOnly(false);
        setCooldown(0);
        setDelay(0);

        setLastFillMaterial(Material.AIR);
        if (getRegion() != null && getRegion().getPoint1() != null)
            setTeleportLocation(getRegion().getPoint1().asBukkitLocation().clone().add(0, 2, 0));
        setMessage(getPluginInstance().getLangConfig().getString("portal-message"));
        setTitle(getPluginInstance().getLangConfig().getString("portal-title-message"));
        setSubTitle(getPluginInstance().getLangConfig().getString("portal-subtitle-message"));
        setBarMessage(getPluginInstance().getLangConfig().getString("portal-bar-message"));
        getPluginInstance().getManager().getPortalMap().put(getPortalId(), this);
    }

    /**
     * Attempts to delete the portal file.
     *
     * @return Whether the process was successful.
     */
    public boolean delete() {
        getPluginInstance().getManager().getPortalMap().remove(getPortalId());
        File file = new File(getPluginInstance().getDataFolder(), "/portals/" + getPortalId() + ".yml");
        if (file != null && file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public static void invokeCommands(@NotNull List<String> commands, @NotNull Player player, Location locationForCoords) {invokeCmds(commands, player, locationForCoords);}

    /**
     * @return Gets a safe proper location outside the portal on the shorter depth side.
     */
    public Location estimateNearbySafeLocation() {
        SerializableLocation location = new SerializableLocation(getPluginInstance(), getRegion().getPoint1().getWorldName(),
                ((getRegion().getPoint1().getX() + getRegion().getPoint2().getX()) / 2),
                (Math.min(getRegion().getPoint1().getY(), getRegion().getPoint2().getY())),
                ((getRegion().getPoint1().getZ() + getRegion().getPoint2().getZ()) / 2), 0f, 0f);

        final double xMax = Math.max(getRegion().getPoint1().getX(), getRegion().getPoint2().getX()),
                xMin = Math.min(getRegion().getPoint1().getX(), getRegion().getPoint2().getX()),
                zMax = Math.max(getRegion().getPoint1().getZ(), getRegion().getPoint2().getZ()),
                zMin = Math.min(getRegion().getPoint1().getZ(), getRegion().getPoint2().getZ());

        final double xDepth = (xMax - xMin), zDepth = (zMax - zMin);

        if (xDepth >= zDepth) {
            final boolean isDepthEven = (xDepth % 2 == 0);
            Location newLocation = location.asBukkitLocation().add((!isDepthEven ? 0.5 : 0), 0, (int) -(zDepth + 1) + 0.5);
            newLocation.setYaw(180);
            newLocation.setPitch(0);

            Block block = newLocation.getBlock();
            if (block.getType().name().contains("AIR")) {
                if (block.getRelative(BlockFace.UP).getType().name().contains("AIR"))
                    return newLocation.add(0, 1.2, 0);
            }

            newLocation = location.asBukkitLocation().add((!isDepthEven ? 0.5 : 0), 0, ((int) (zDepth + 1)) + 0.5);
            newLocation.setYaw(0);
            newLocation.setPitch(0);

            block = newLocation.getBlock();
            if (block.getType().name().contains("AIR"))
                return newLocation.add(0, 1.2, 0);

        } else {
            final boolean isDepthEven = (zDepth % 2 == 0);

            int firstXDepth = (int) -(xDepth + 1);

            Location newLocation = location.asBukkitLocation().add((firstXDepth < 0 ? (firstXDepth + 0.5) : (firstXDepth - 0.5)), 0, (isDepthEven ?
                    0.5 : 0));
            newLocation.setYaw(90);
            newLocation.setPitch(0);

            Block block = newLocation.getBlock();
            if (block.getType().name().contains("AIR")) {
                if (block.getRelative(BlockFace.UP).getType().name().contains("AIR"))
                    return newLocation.add(0, 1.2, 0);
            }

            newLocation = location.asBukkitLocation().add((int) (xDepth + 1) + 0.5, 0, (isDepthEven ? 0.5 : 0));
            newLocation.setYaw(-90);
            newLocation.setPitch(0);

            block = newLocation.getBlock();
            if (block.getType().name().contains("AIR"))
                return newLocation.add(0, 1.2, 0);
        }

        return null;
    }

    private static void invokeCmds(@NotNull List<String> commands, @NotNull Player player, Location locationForCoords) {
        SimplePortals.getPluginInstance().getServer().getScheduler().runTaskLater(SimplePortals.getPluginInstance(), () -> {
            for (String commandLine : commands) {
                PortalCommandType portalCommandType = PortalCommandType.CONSOLE;
                double percentage = 100;
                if (commandLine.contains(":")) {
                    String[] commandLineSplit = commandLine.split(":");
                    if (commandLineSplit.length >= 3) {

                        String foundPercentValue = commandLineSplit[(commandLineSplit.length - 1)];
                        if (SimplePortals.getPluginInstance().getManager().isNumeric(foundPercentValue)) {

                            percentage = Double.parseDouble(foundPercentValue);

                            PortalCommandType foundPortalCommandType = PortalCommandType.getType(commandLineSplit[commandLineSplit.length - 2]);
                            if (foundPortalCommandType != null) portalCommandType = foundPortalCommandType;

                        } else {
                            PortalCommandType foundPortalCommandType = PortalCommandType.getType(commandLineSplit[commandLineSplit.length - 1]);
                            if (foundPortalCommandType != null) portalCommandType = foundPortalCommandType;
                        }

                    } else if (commandLineSplit.length == 2) {
                        PortalCommandType foundPortalCommandType = PortalCommandType.getType(commandLineSplit[commandLineSplit.length - 1]);
                        if (foundPortalCommandType != null) portalCommandType = foundPortalCommandType;
                    }
                }


                double chance = (Math.random() * 100);
                if (chance < percentage) {
                    commandLine = commandLine.replaceAll("(?i):player", "").replaceAll("(?i):console", "")
                            .replaceAll("(?i):chat", "").replace((":" + percentage), "");

                    switch (portalCommandType) {

                        case PLAYER:
                            SimplePortals.getPluginInstance().getServer().dispatchCommand(player, commandLine.replace("{x}",
                                            String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        case CHAT:
                            player.chat(commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                    .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}", String.valueOf(locationForCoords.getZ()))
                                    .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                        default:
                            SimplePortals.getPluginInstance().getServer().dispatchCommand(SimplePortals.getPluginInstance().getServer().getConsoleSender(),
                                    commandLine.replace("{x}", String.valueOf(locationForCoords.getX()))
                                            .replace("{y}", String.valueOf(locationForCoords.getY())).replace("{z}",
                                                    String.valueOf(locationForCoords.getZ()))
                                            .replace("{world}", locationForCoords.getWorld().getName()).replace("{player}", player.getName()));
                            break;

                    }
                }
            }
        }, SimplePortals.getPluginInstance().getConfig().getInt("command-tick-delay"));
    }

    /**
     * Attempts to save the portal to its own file located in the portals folder.
     */
    public synchronized void save() {
        try {
            File file = new File(getPluginInstance().getDataFolder(), "/portals/" + getPortalId().toLowerCase() + ".yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            yaml.set("last-fill-material", getLastFillMaterial().name());
            yaml.set("portal-server", getServerSwitchName());

            SerializableLocation pointOne = getRegion().getPoint1();
            if (pointOne != null) yaml.set("point-one", pointOne.toString());

            SerializableLocation pointTwo = getRegion().getPoint2();
            if (pointTwo != null) yaml.set("point-two", pointTwo.toString());

            SerializableLocation teleportLocation = getTeleportLocation();
            if (teleportLocation != null) yaml.set("teleport-location", teleportLocation.toString());

            SerializableLocation serverSwitchLocation = getServerSwitchLocation();
            if (serverSwitchLocation != null) yaml.set("server-switch-location", serverSwitchLocation.toString());

            yaml.set("commands-only", isCommandsOnly());
            yaml.set("commands", getCommands());
            yaml.set("disabled", isDisabled());
            yaml.set("message", getMessage());
            yaml.set("title", getTitle());
            yaml.set("sub-title", getSubTitle());
            yaml.set("bar-message", getBarMessage());
            yaml.set("cooldown", getCooldown());
            yaml.set("delay", getDelay());

            yaml.save(file);
        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * Invokes all commands attached to the portal (includes percentage calculations).
     *
     * @param player            The player to send command based on.
     * @param locationForCoords The location where the player is or should be.
     */
    public void invokeCommands(@NotNull Player player, Location locationForCoords) {invokeCmds(getCommands(), player, locationForCoords);}

    /**
     * Performs the general action of the portal by teleporting the player and playing effects. (Handles server transfer)
     *
     * @param entity The entity to perform actions on.
     */
    public void performAction(@NotNull Entity entity) {
        final boolean isPlayer = (entity instanceof Player);

        if (isPlayer) {
            final Player player = (Player) entity;
            //Cancel if entity is already in any teleporting cooldown
            if (getPluginInstance().getManager().getEntitiesInTeleportationAndPortals().containsKey(player.getUniqueId())
                    && getPluginInstance().getManager().getTeleportTasks().containsKey(player.getUniqueId())) return;
        }

        if (getServerSwitchName() == null || getServerSwitchName().isEmpty() || getServerSwitchName().equalsIgnoreCase("none")) {
            Location location = getTeleportLocation().asBukkitLocation();
            if (location != null) {
                if (getPluginInstance().getConfig().getBoolean("keep-teleport-head-axis")) {
                    location.setYaw(entity.getLocation().getYaw());
                    location.setPitch(entity.getLocation().getPitch());
                }

                if (isPlayer) {
                    final Player player = (Player) entity;

                    if (getDelay() > 0) {
                        getPluginInstance().getManager().getEntitiesInTeleportationAndPortals().put(player.getUniqueId(), this);

                        TeleportTask teleportTask = new TeleportTask(player, this, location);
                        getPluginInstance().getManager().getTeleportTasks().put(player.getUniqueId(), teleportTask);
                        getPluginInstance().getServer().getScheduler().scheduleSyncDelayedTask(getPluginInstance(), () -> {
                            if (!teleportTask.isCancelled()) teleportTask.cancel();
                        }, (getDelay() * 20L + 1));
                    } else {

                        invokeCommands((Player) entity, location);
                        if (isCommandsOnly()) return;

                        getPluginInstance().getManager().playTeleportEffect(entity.getLocation());
                        getPluginInstance().getManager().teleportWithEntity(entity, location);
                        getPluginInstance().getManager().getPortalLinkMap().put(entity.getUniqueId(), getPortalId());
                        getPluginInstance().getManager().playTeleportEffect(location);

                        pluginInstance.getManager().getEntitiesInTeleportationAndPortals().remove(player.getUniqueId());
                    }
                } else {
                    getPluginInstance().getManager().playTeleportEffect(entity.getLocation());
                    getPluginInstance().getManager().teleportWithEntity(entity, location);
                    getPluginInstance().getManager().playTeleportEffect(location);
                }
            }
        } else if (isPlayer) {
            final Player player = (Player) entity;
            if (getServerSwitchLocation() != null) {
                CompletableFuture.runAsync(() -> getPluginInstance().sendTransferMessage(player, getServerSwitchName(), getServerSwitchLocation().toString(), getCommands()))
                        .thenRun(() -> pluginInstance.getServer().getScheduler().runTask(pluginInstance, () -> actionHelper(player)));
            } else actionHelper(player);
        }
    }

    private void actionHelper(@NotNull Player player) {
        if ((!getPluginInstance().getManager().getSmartTransferMap().isEmpty() && getPluginInstance().getManager().getSmartTransferMap().containsKey(player.getUniqueId()))) {
            SerializableLocation serializableLocation = getPluginInstance().getManager().getSmartTransferMap().get(player.getUniqueId());

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

            if (getDelay() != 0) {
                getPluginInstance().getManager().getEntitiesInTeleportationAndPortals().put(player.getUniqueId(), this);

                TeleportTask teleportTask = new TeleportTask(player, this, serializableLocation.asBukkitLocation());
                getPluginInstance().getServer().getScheduler().scheduleSyncDelayedTask(getPluginInstance(), () -> {
                    if (!teleportTask.isCancelled()) teleportTask.cancel();
                }, (getDelay() * 20L + 1));
            } else {
                final Location loc = serializableLocation.asBukkitLocation();

                invokeCommands(player, loc);
                if (isCommandsOnly()) return;

                getPluginInstance().getManager().playTeleportEffect(player.getLocation());
                getPluginInstance().getManager().teleportWithEntity(player, loc);
                getPluginInstance().getManager().getPortalLinkMap().put(player.getUniqueId(), getPortalId());
                getPluginInstance().getManager().playTeleportEffect(loc);
            }
        }

        final Location newSafeLocation = estimateNearbySafeLocation();
        if (newSafeLocation != null) getPluginInstance().getManager().teleportWithEntity(player, newSafeLocation);

        // transfer
        getPluginInstance().getManager().switchServer(player, getServerSwitchName());

        pluginInstance.getManager().getEntitiesInTeleportationAndPortals().remove(player.getUniqueId());
    }


    /**
     * Attempts to fill a portals region with the passed material and durability.
     *
     * @param player     The player whom filled the portal's region (Determines Block Face Direction).
     * @param material   The material that is used.
     * @param durability The durability to modify the material.
     */
    public void fillPortal(Player player, Material material, int durability) {
        final boolean isOldVersion = (getPluginInstance().getServerVersion().startsWith("v1_12")
                || getPluginInstance().getServerVersion().startsWith("v1_11")
                || getPluginInstance().getServerVersion().startsWith("v1_10")
                || getPluginInstance().getServerVersion().startsWith("v1_9")
                || getPluginInstance().getServerVersion().startsWith("v1_8")
                || getPluginInstance().getServerVersion().startsWith("v1_7"));

        if (isOldVersion && (material == Material.WATER || material == Material.LAVA))
            material = Material.valueOf("STATIONARY_" + material.name());

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
                        blockState.setType(Material.AIR);
                        blockState.update(true, false);

                        blockState.setType(material);
                        try {
                            if (!isOldVersion && (blockState instanceof org.bukkit.block.data.Directional)) {
                                blockState.setBlockData(getPluginInstance().getServer().createBlockData(material));
                                setBlock(block, material, BlockFace.valueOf(Direction.getYaw(player).name()));
                            } else if (durability > 0) {
                                Method method = Block.class.getMethod("setData", byte.class);
                                method.setAccessible(true);
                                method.invoke(block, (byte) durability);
                            }
                        } catch (NoClassDefFoundError | Exception e) {
                            e.printStackTrace();
                            getPluginInstance().log(Level.WARNING, "There was an issue filling the portal due to the material entry.");
                        }

                        blockState.update(true, false);
                    }
                }

        setLastFillMaterial(material);
    }

    public void cancelTask() {
        getPluginInstance().getServer().getOnlinePlayers().parallelStream().forEach(this::cancelTask);
    }

    public void cancelTask(Player player) {
        HashMap<String, BukkitTask> tasks = getPluginInstance().getManager().getTasks().getOrDefault(player.getUniqueId(), null);
        if (tasks != null)
            tasks.entrySet().parallelStream().filter(pair -> pair.getKey().equals(getTitle())).forEach(pair -> pair.getValue().cancel());
        tasks.remove(getTitle());
    }

    /**
     * Displays the region using particle packets.
     *
     * @param player The player to display to.
     */
    public void displayRegion(Player player) {
        String particleEffect = getPluginInstance().getConfig().getString("region-visual-effect");
        if (particleEffect == null || particleEffect.isEmpty()) return;

        HashMap<String, BukkitTask> tasks = getPluginInstance().getManager().getTasks()
                .computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());
        tasks.put(getTitle(), new RegionTask(getPluginInstance(), player, this)
                .runTaskTimerAsynchronously(getPluginInstance(), 0, 5));
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

    public Region getRegion() {return region;}

    public void setRegion(Region region) {this.region = region;}

    public String getPortalId() {return portalId.toLowerCase();}

    private void setPortalId(String portalId) {this.portalId = portalId.toLowerCase();}

    public SerializableLocation getTeleportLocation() {return teleportLocation;}

    public void setTeleportLocation(Location teleportLocation) {this.teleportLocation = (teleportLocation == null ? null : new SerializableLocation(getPluginInstance(), teleportLocation));}

    public void setTeleportLocation(SerializableLocation teleportLocation) {this.teleportLocation = teleportLocation;}

    public SerializableLocation getServerSwitchLocation() {return serverSwitchLocation;}

    public void setServerSwitchLocation(Location serverSwitchLocation) {
        this.serverSwitchLocation = (serverSwitchLocation == null ? null : new SerializableLocation(getPluginInstance(), serverSwitchLocation));
    }

    public void setServerSwitchLocation(SerializableLocation serverSwitchLocation) {this.serverSwitchLocation = serverSwitchLocation;}

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

    public void setSubTitle(String subTitle) {this.subTitle = subTitle;}

    public String getBarMessage() {return barMessage;}

    public void setBarMessage(String barMessage) {this.barMessage = barMessage;}

    public int getCooldown() {return cooldown;}

    public void setCooldown(int cooldown) {this.cooldown = cooldown;}

    private SimplePortals getPluginInstance() {return pluginInstance;}

    public int getDelay() {return delay;}

    public void setDelay(int delay) {this.delay = delay;}

}