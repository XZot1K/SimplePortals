package xzot1k.plugins.sp.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.objects.TaskHolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portal {

    private SimplePortals pluginInstance;
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

    public void register() {
        if (!pluginInstance.getManager().getPortals().contains(this))
            pluginInstance.getManager().getPortals().add(this);
    }

    public void unregister() {
        pluginInstance.getManager().getPortals().remove(this);
    }

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

        try {
            String particleEffect = Objects
                    .requireNonNull(pluginInstance.getConfig().getString("teleport-visual-effect")).toUpperCase()
                    .replace(" ", "_").replace("-", "_");
            player.getWorld().playSound(player.getLocation(),
                    Sound.valueOf(Objects.requireNonNull(pluginInstance.getConfig().getString("teleport-sound"))
                            .toUpperCase().replace(" ", "_").replace("-", "_")),
                    1, 1);
            pluginInstance.getManager().getParticleHandler().broadcastParticle(player.getLocation(), 1, 2, 1, 0,
                    particleEffect, 50);
        } catch (Exception ignored) {
        }
    }

    public void fillPortal(Material material, int durability) {
        Location point1 = getRegion().getPoint1().asBukkitLocation(),
                point2 = getRegion().getPoint2().asBukkitLocation();
        if (Objects.requireNonNull(point1.getWorld()).getName()
                .equalsIgnoreCase(Objects.requireNonNull(point2.getWorld()).getName())) {
            if (point1.getBlockX() <= point2.getBlockX()) {
                for (int pos_x = point1.getBlockX() - 1; ++pos_x <= point2.getBlockX(); )
                    if (point1.getBlockZ() <= point2.getBlockZ())
                        fillHelper(material, (short) durability, point1, point2, pos_x, point1.getBlockY(), point2.getBlockY(), point1.getWorld());
                    else
                        fillHelper(material, (short) durability, point2, point1, pos_x, point1.getBlockY(), point2.getBlockY(), point1.getWorld());
            } else {
                for (int pos_x = point2.getBlockX(); pos_x <= point1.getBlockX(); pos_x++)
                    if (point1.getBlockZ() <= point2.getBlockZ())
                        fillHelperTwo(material, (short) durability, point1, point2, pos_x, point1.getBlockY(), point2.getBlockY(), point1.getWorld());
                    else
                        fillHelperTwo(material, (short) durability, point2, point1, pos_x, point1.getBlockY(), point2.getBlockY(), point1.getWorld());
            }
        }

        setLastFillMaterial(material);
    }

    private void fillHelperTwo(Material material, short durability, Location point1, Location point2, int pos_x, int blockY, int blockY2, World world) {
        for (int pos_z = point1.getBlockZ(); pos_z <= point2.getBlockZ(); pos_z++)
            if (blockY <= blockY2) fillHelperInner(material, durability, pos_x, blockY, blockY2, world, pos_z);
            else fillHelperInner(material, durability, pos_x, blockY2, blockY, world, pos_z);
    }

    private void fillHelperInner(Material material, short durability, int pos_x, int blockY, int blockY2, World world, int pos_z) {
        for (int pos_y = blockY; pos_y <= blockY2; pos_y++)
            fillHelperInnerTwo(material, durability, pos_x, world, pos_z, pos_y);
    }

    private void fillHelperInnerTwo(Material material, short durability, int pos_x, World world, int pos_z, int pos_y) {
        Location location = new Location(world, pos_x, pos_y, pos_z);
        if (location.getBlock().getType() == Material.AIR
                || location.getBlock().getType() == getLastFillMaterial()) {
            location.getBlock().setType(material);
            if ((pluginInstance.getServerVersion().toLowerCase().startsWith("v1_14") || pluginInstance.getServerVersion().toLowerCase().startsWith("v1_15"))
                    && !pluginInstance.getServerVersion().toLowerCase()
                    .startsWith("v1_13")) {
                try {
                    Method closeMethod = location.getBlock().getClass().getMethod("setData",
                            Short.class);
                    if (closeMethod != null)
                        closeMethod.invoke(location.getBlock().getClass(),
                                durability);
                } catch (NoSuchMethodException | IllegalAccessException
                        | InvocationTargetException ignored) {
                }
            }
        }
    }

    private void fillHelper(Material material, short durability, Location point1, Location point2, int pos_x, int blockY, int blockY2, World world) {
        for (int pos_z = point1.getBlockZ() - 1; ++pos_z <= point2.getBlockZ(); )
            if (blockY <= blockY2)
                for (int pos_y = blockY - 1; ++pos_y <= blockY2; ) {
                    fillHelperInnerTwo(material, durability, pos_x, world, pos_z, pos_y);
                }
            else
                for (int pos_y = blockY2 - 1; ++pos_y <= blockY; ) {
                    fillHelperInnerTwo(material, durability, pos_x, world, pos_z, pos_y);
                }
    }

    public void displayRegion(Player player) {
        String particleEffect = Objects.requireNonNull(pluginInstance.getConfig().getString("region-visual-effect"))
                .toUpperCase().replace(" ", "_").replace("-", "_");

        BukkitTask bukkitTask = new BukkitRunnable() {
            Location point1 = getRegion().getPoint1().asBukkitLocation(),
                    point2 = getRegion().getPoint2().asBukkitLocation();
            int duration = pluginInstance.getConfig().getInt("region-visual-duration");
            double lifetime = 0;

            @Override
            public void run() {
                if (lifetime >= duration) {
                    cancel();
                    return;
                }

                if (Objects.requireNonNull(point1.getWorld()).getName()
                        .equalsIgnoreCase(Objects.requireNonNull(point2.getWorld()).getName())) {
                    if (point1.getBlockX() <= point2.getBlockX()) {
                        for (int pos_x = point1.getBlockX() - 1; ++pos_x <= point2.getBlockX(); ) {
                            if (point1.getBlockZ() <= point2.getBlockZ()) {
                                displayHelperCaseOne(pos_x, point1, point2, player, particleEffect);
                            } else {
                                displayHelperCaseOne(pos_x, point2, point1, player, particleEffect);
                            }
                        }
                    } else {
                        for (int pos_x = point2.getBlockX(); pos_x <= point1.getBlockX(); pos_x++) {
                            if (point1.getBlockZ() <= point2.getBlockZ()) {
                                displayHelperCaseTwo(pos_x, point1, point2, player, particleEffect);
                            } else {
                                displayHelperCaseTwo(pos_x, point2, point1, player, particleEffect);
                            }
                        }
                    }
                }

                lifetime += 0.25;
            }

            private void displayHelperCaseTwo(int pos_x, Location point1, Location point2, Player player, String particleEffect) {
                for (int pos_z = point1.getBlockZ(); pos_z <= point2.getBlockZ(); pos_z++) {
                    if (point1.getBlockY() <= point2.getBlockY()) {
                        displayHelperCaseThree(pos_x, point1, point2, player, particleEffect, pos_z, point1.getWorld(), point1.getX(), point2.getX(), point1.getY(), point2.getY(), point1.getZ(), point2.getZ());
                    } else {
                        displayHelperCaseThree(pos_x, point2, point1, player, particleEffect, pos_z, point1.getWorld(), point1.getX(), point2.getX(), point1.getY(), point2.getY(), point1.getZ(), point2.getZ());
                    }
                }
            }

            private void displayHelperCaseOne(int pos_x, Location point1, Location point2, Player player, String particleEffect) {
                for (int pos_z = point1.getBlockZ() - 1; ++pos_z <= point2.getBlockZ(); ) {
                    if (point1.getBlockY() <= point2.getBlockY()) {
                        displayHelperCaseFour(pos_x, point1, point2, player, particleEffect, pos_z, point1.getWorld(), point1.getX(), point2.getX(), point1.getY(), point2.getY(), point1.getZ(), point2.getZ());
                    } else {
                        displayHelperCaseFour(pos_x, point2, point1, player, particleEffect, pos_z, point1.getWorld(), point1.getX(), point2.getX(), point1.getY(), point2.getY(), point1.getZ(), point2.getZ());
                    }
                }
            }
        }.runTaskTimer(pluginInstance, 0, 5);

        if (!pluginInstance.getManager().getVisualTasks().isEmpty()
                && pluginInstance.getManager().getVisualTasks().containsKey(player.getUniqueId())) {
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

    private void displayHelperCaseFour(int pos_x, Location point1, Location point2, Player player, String particleEffect, int pos_z, World world, double x, double x2, double y, double y2, double z, double z2) {
        for (int pos_y = point1.getBlockY() - 1; ++pos_y <= point2.getBlockY(); ) {
            displayHelperCaseFive(pos_x, player, particleEffect, pos_z, world, x, x2, y, y2, z, z2, pos_y);
        }
    }

    private void displayHelperCaseFive(int pos_x, Player player, String particleEffect, int pos_z, World world, double x, double x2, double y, double y2, double z, double z2, int pos_y) {
        Location location = new Location(world, pos_x, pos_y, pos_z);
        if (location.getX() == x || location.getX() == x2
                || location.getY() == y
                || location.getY() == y2
                || location.getZ() == z
                || location.getZ() == z2)
            pluginInstance.getManager().getParticleHandler().displayParticle(player,
                    location.add(0.5, 0.5, 0.5), 0, 0, 0, 0, particleEffect, 1);
    }

    private void displayHelperCaseThree(int pos_x, Location point1, Location point2, Player player, String particleEffect, int pos_z, World world, double x, double x2, double y, double y2, double z, double z2) {
        for (int pos_y = point1.getBlockY(); pos_y <= point2.getBlockY(); pos_y++) {
            displayHelperCaseFive(pos_x, player, particleEffect, pos_z, world, x, x2, y, y2, z, z2, pos_y);
        }
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
