package xzot1k.plugins.sp.core.packets.particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ParticleHandler
{

    void displayParticle(Player player, Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount);

    void broadcastParticle(Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount);

}
