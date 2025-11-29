/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xzot1k.plugins.sp.Config;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;

public class RegionTask extends BukkitRunnable {
    private SimplePortals pluginInstance;
    private double lifeTime;
    private int duration;
    private Player player;
    private World world;
    private String particleEffect;
    private Portal portal;

    public RegionTask(SimplePortals pluginInstance, Player player, Portal portal) {
        setPluginInstance(pluginInstance);
        setDuration(Config.get().regionDuration);
        setPlayer(player);
        setWorld(getPluginInstance().getServer().getWorld(portal.getRegion().getPoint1().getWorldName()));
        setParticleEffect(Config.get().regionEffect);
        setLifeTime(0);
        setPortal(portal);
    }

    @Override
    public void run() {
        if (getLifeTime() >= getDuration() || getPlayer() == null || getWorld() == null || getPortal() == null
                || getPortal().getRegion() == null || getParticleEffect() == null || getParticleEffect().isEmpty()) {
            cancel();
            return;
        }

        int lowestX = (int) Math.min(getPortal().getRegion().getPoint1().getX(), getPortal().getRegion().getPoint2().getX()),
                highestX = (int) Math.max(getPortal().getRegion().getPoint1().getX(), getPortal().getRegion().getPoint2().getX()),

                lowestY = (int) Math.min(getPortal().getRegion().getPoint1().getY(), getPortal().getRegion().getPoint2().getY()),
                highestY = (int) Math.max(getPortal().getRegion().getPoint1().getY(), getPortal().getRegion().getPoint2().getY()),

                lowestZ = (int) Math.min(getPortal().getRegion().getPoint1().getZ(), getPortal().getRegion().getPoint2().getZ()),
                highestZ = (int) Math.max(getPortal().getRegion().getPoint1().getZ(), getPortal().getRegion().getPoint2().getZ());

        for (int y = (lowestY - 1); ++y <= highestY; )
            for (int x = (lowestX - 1); ++x <= highestX; )
                for (int z = (lowestZ - 1); ++z <= highestZ; )
                    if (x == lowestX || x == highestZ || y == lowestY || y == highestY || z == lowestZ || z == highestZ) {
                        final Location location = new Location(getWorld(), x, y, z);
                        getPluginInstance().getManager().getParticleHandler().displayParticle(getPlayer(), location.add(0.5, 0.5, 0.5),
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

    private Portal getPortal() {
        return portal;
    }

    private void setPortal(Portal portal) {
        this.portal = portal;
    }
}
