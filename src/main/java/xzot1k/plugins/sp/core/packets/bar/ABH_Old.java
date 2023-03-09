/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.bar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.SimplePortals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ABH_Old implements BarHandler {
    private Class<?> icbClass, csClass, packetClass, packetChatClass, craftPlayerClass;

    public ABH_Old() {
        try {
            icbClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".IChatBaseComponent");

            csClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".IChatBaseComponent$ChatSerializer");

            packetClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".Packet");

            packetChatClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".PacketPlayOutChat");

            craftPlayerClass = Class.forName("org.bukkit.craftbukkit."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".entity.CraftPlayer");
        } catch (ClassNotFoundException e) {e.printStackTrace();}
    }

    @Override
    public void sendActionBar(@NotNull Player player, @NotNull String message) {
        try {
            final Method method = csClass.getDeclaredMethod("a", String.class);
            final Object icbc = method.invoke(csClass, ("{\"text\": \""
                    + SimplePortals.getPluginInstance().getManager().colorText(message) + "\"}"));

            Constructor<?> packetConstructor = null;
            for (Constructor<?> con : packetChatClass.getConstructors()) {

                if (con.getParameterTypes().length != 2 || con.getParameterTypes()[0] != icbClass
                        || con.getParameterTypes()[1] != byte.class) continue;

                packetConstructor = con;
                break;
            }

            if (packetConstructor == null) return;
            final Object packet = packetConstructor.newInstance(icbc, (byte) 2);

            final Object cPlayer = craftPlayerClass.cast(player);
            final Object getHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(cPlayer);
            final Object pConnection = getHandle.getClass().getDeclaredField("playerConnection").get(getHandle);
            final Method sendPacket = pConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacket.invoke(pConnection, packet);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                 | InstantiationException | NoSuchFieldException e) {e.printStackTrace();}
    }

}
