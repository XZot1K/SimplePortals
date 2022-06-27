package xzot1k.plugins.sp.core.packets.particles.versions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.packets.particles.ParticleHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PH_Old implements ParticleHandler {
    private Class<?> particlePacketClass, enumParticleClass, cpClass, packetClass, nmClass;

    public PH_Old() {
        try {
            packetClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".Packet");

            particlePacketClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".PacketPlayOutWorldParticles");

            enumParticleClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".EnumParticle");

            cpClass = Class.forName("org.bukkit.craftbukkit."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".entity.CraftPlayer");

            nmClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".NetworkManager");
        } catch (NoClassDefFoundError | ClassNotFoundException e) {e.printStackTrace();}
    }

    @Override
    public void displayParticle(Player player, Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount) {
        try {
            final Method valueOf = enumParticleClass.getDeclaredMethod("valueOf", String.class);
            final Object particle = valueOf.invoke(enumParticleClass, enumParticle);

            for (Constructor<?> cons : particlePacketClass.getDeclaredConstructors()) {
                if (cons.getParameterCount() >= 9) {
                    final Object packet = cons.newInstance(particle, false, (float) location.getX(), (float) location.getY(),
                            (float) location.getZ(), offsetX, offsetY, offsetZ, (float) speed, amount, null);
                    send(player, packet);
                    break;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                 | InstantiationException e) {e.printStackTrace();}
    }

    @Override
    public void broadcastParticle(Location location, float offsetX, float offsetY, float offsetZ, int speed, String enumParticle, int amount) {
        try {
            final Method valueOf = enumParticleClass.getDeclaredMethod("valueOf", String.class);
            final Object particle = valueOf.invoke(enumParticleClass, enumParticle);

            for (Constructor<?> cons : particlePacketClass.getDeclaredConstructors()) {
                if (cons.getParameterCount() >= 9) {
                    final Object packet = cons.newInstance(particle, false, (float) location.getX(), (float) location.getY(),
                            (float) location.getZ(), offsetX, offsetY, offsetZ, (float) speed, amount, null);
                    for (Player player : SimplePortals.getPluginInstance().getServer().getOnlinePlayers()) send(player, packet);
                    break;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                 | InstantiationException e) {e.printStackTrace();}
    }

    private void send(@NotNull Player player, @NotNull Object packet) {
        try {
            final Object cPlayer = cpClass.cast(player);
            final Object getHandle = cpClass.getDeclaredMethod("getHandle").invoke(cPlayer);
            final Object pConnection = getHandle.getClass().getDeclaredField("playerConnection").get(getHandle);
            final Method sendPacket = pConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacket.invoke(pConnection, packet);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException
                 | NoSuchFieldException e) {e.printStackTrace();}
    }

}
