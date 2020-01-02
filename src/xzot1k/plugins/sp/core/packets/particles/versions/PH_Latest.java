package xzot1k.plugins.sp.core.packets.particles.versions;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;

public class PH_Latest implements ParticleHandler {
    @Override
    public void displayParticle(Player player, Location location, float offsetX, float offsetY, float offsetZ,
                                int speed, String enumParticle, int amount) {
        Particle particle = Particle.valueOf(enumParticle);
        if (particle == Particle.REDSTONE)
            player.spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ,
                    new Particle.DustOptions(Color.fromBGR(255, 0, 0), 1));
        else
            player.spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ, 0);
    }

    @Override
    public void broadcastParticle(Location location, float offsetX, float offsetY, float offsetZ, int speed,
                                  String enumParticle, int amount) {
        Particle particle = Particle.valueOf(enumParticle);
        if (particle == Particle.REDSTONE)
            location.getWorld().spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ,
                    new Particle.DustOptions(Color.fromBGR(255, 0, 0), 1));
        else
            location.getWorld().spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ, 0);
    }

}
