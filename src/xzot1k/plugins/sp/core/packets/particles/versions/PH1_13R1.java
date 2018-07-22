package xzot1k.plugins.sp.core.packets.particles.versions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;

import java.util.ArrayList;
import java.util.List;

public class PH1_13R1 implements ParticleHandler
{

    private SimplePortals pluginInstance;

    public PH1_13R1(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public void displayParticle(Player player, Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount)
    {
        player.spawnParticle(Particle.valueOf(enumParticle), location, amount, offsetX, offsetY, offsetZ, 0);
    }

    @Override
    public void broadcastParticle(Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount)
    {
        location.getWorld().spawnParticle(Particle.valueOf(enumParticle), location, amount, offsetX, offsetY, offsetZ, 0);
    }

}
