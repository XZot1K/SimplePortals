/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.titles.versions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.packets.titles.TitleHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Titles_Old implements TitleHandler {

    private Class<?> csClass, titlePacketClass, etaClass, cpClass, packetClass;

    public Titles_Old() {
        try {
            csClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".IChatBaseComponent$ChatSerializer");

            packetClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".Packet");

            titlePacketClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".PacketPlayOutTitle");

            etaClass = Class.forName("net.minecraft.server."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".PacketPlayOutTitle$EnumTitleAction");

            cpClass = Class.forName("org.bukkit.craftbukkit."
                    + SimplePortals.getPluginInstance().getServerVersion() + ".entity.CraftPlayer");
        } catch (NoClassDefFoundError | ClassNotFoundException e) {e.printStackTrace();}
    }

    @Override
    public void sendTitle(Player player, String text, int fadeIn, int displayTime, int fadeout) {
        send(player, "TITLE", text, fadeIn, displayTime, fadeout);
    }

    @Override
    public void sendSubTitle(Player player, String text, int fadeIn, int displayTime, int fadeout) {
        send(player, "SUBTITLE", text, fadeIn, displayTime, fadeout);
    }

    private void send(@NotNull Player player, @NotNull String action, @NotNull String text, int fadeIn, int displayTime, int fadeOut) {
        try {
            final Object titleAction = etaClass.getDeclaredField(action);

            final Method aMethod = csClass.getDeclaredMethod("a", String.class);
            final String textField = (String) aMethod.invoke(csClass, "{\"text\":\""
                    + SimplePortals.getPluginInstance().getManager().colorText(text) + "\"}");

            final Constructor<?> pConst = titlePacketClass.getConstructor(etaClass, String.class,
                    Integer.class, Integer.class, Integer.class);

            final Object packet = pConst.newInstance(titleAction, textField, (fadeIn * 20), (displayTime * 20), (fadeOut * 20));

            final Object cPlayer = cpClass.cast(player);
            final Object getHandle = cpClass.getDeclaredMethod("getHandle").invoke(cPlayer);
            final Object pConnection = getHandle.getClass().getDeclaredField("playerConnection").get(getHandle);
            final Method sendPacket = pConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacket.invoke(pConnection, packet);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | InstantiationException e) {e.printStackTrace();}
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int displayTime, int fadeOut) {
        sendTitle(player, title, fadeIn * 20, displayTime * 20, fadeOut * 20);
        sendSubTitle(player, subTitle, fadeIn * 20, displayTime * 20, fadeOut * 20);
    }

}
