/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.SerializableLocation;

public class HighlightTask extends BukkitRunnable {
    private SimplePortals pluginInstance;
    private double lifeTime;
    private int duration;
    private SerializableLocation blockLocation;
    private Player player;
    private World world;
    private String particleEffect;

    public HighlightTask(SimplePortals pluginInstance, Player player, Location blockLocation, String particleEffect) {
        setPluginInstance(pluginInstance);
        setBlockLocation(new SerializableLocation(pluginInstance, blockLocation));
        setDuration(getPluginInstance().getConfig().getInt("selection-visual-duration"));
        setPlayer(player);
        setWorld(blockLocation.getWorld());
        setParticleEffect(particleEffect);
        setLifeTime(0);
    }

    @Override
    public void run() {
        if (getLifeTime() >= getDuration() || getPlayer() == null || getWorld() == null || getBlockLocation() == null
                || getParticleEffect() == null || getParticleEffect().isEmpty()) {
            cancel();
            return;
        }

        for (double y = getBlockLocation().getY() - 0.2; (y += 0.2) < (getBlockLocation().getY() + 1.1); )
            for (double x = getBlockLocation().getX() - 0.2; (x += 0.2) < (getBlockLocation().getX() + 1.1); )
                for (double z = getBlockLocation().getZ() - 0.2; (z += 0.2) < (getBlockLocation().getZ() + 1.1); ) {
                    final Location location = new Location(world, x, y, z);

                    if ((y < (blockLocation.getY() + 0.2) || y > (blockLocation.getY() + 0.9))
                            && (z < (blockLocation.getZ() + 0.2) || z > (blockLocation.getZ() + 0.9)))
                        getPluginInstance().getManager().getParticleHandler().displayParticle(getPlayer(), location,
                                0, 0, 0, 0, getParticleEffect(), 1);

                    if ((x < (blockLocation.getX() + 0.2) || x > (blockLocation.getX() + 0.9))
                            && (z < (blockLocation.getZ() + 0.2) || z > (blockLocation.getZ() + 0.9)))
                        getPluginInstance().getManager().getParticleHandler().displayParticle(getPlayer(), location,
                                0, 0, 0, 0, getParticleEffect(), 1);

                    if ((y < (blockLocation.getY() + 0.2) || y > (blockLocation.getY() + 0.9))
                            && (x < (blockLocation.getX() + 0.2) || x > (blockLocation.getX() + 0.9)))
                        getPluginInstance().getManager().getParticleHandler().displayParticle(getPlayer(), location,
                                0, 0, 0, 0, getParticleEffect(), 1);
                }

        setLifeTime(getLifeTime() + 0.25);
    }

    private double getLifeTime() {
        return lifeTime;
    }

    private void setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
    }

    private SimplePortals getPluginInstance() {
        return this.pluginInstance;
    }

    private void setPluginInstance(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    private SerializableLocation getBlockLocation() {
        return this.blockLocation;
    }

    private void setBlockLocation(SerializableLocation blockLocation) {
        this.blockLocation = blockLocation;
    }

    private int getDuration() {
        return duration;
    }

    private void setDuration(int duration) {
        this.duration = duration;
    }

    private Player getPlayer() {
        return player;
    }

    private void setPlayer(Player player) {
        this.player = player;
    }

    private World getWorld() {
        return world;
    }

    private void setWorld(World world) {
        this.world = world;
    }

    private String getParticleEffect() {
        return particleEffect;
    }

    private void setParticleEffect(String particleEffect) {
        this.particleEffect = particleEffect;
    }
}
