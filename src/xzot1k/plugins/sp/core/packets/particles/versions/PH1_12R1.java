package xzot1k.plugins.sp.core.packets.particles.versions;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;

import java.util.ArrayList;
import java.util.List;

public class PH1_12R1 implements ParticleHandler
{

    private SimplePortals pluginInstance;

    public PH1_12R1(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public void displayParticle(Player player, Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount)
    {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.valueOf(enumParticle), false,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, amount);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void broadcastParticle(Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount)
    {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.valueOf(enumParticle), false,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, amount);

        List<Player> players = new ArrayList<>(pluginInstance.getServer().getOnlinePlayers());
        for (int i = -1; ++i < players.size(); )
            ((CraftPlayer) players.get(i)).getHandle().playerConnection.sendPacket(packet);
    }

}
