/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.titles.versions;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.core.packets.titles.TitleHandler;

public class Titles1_8R3 implements TitleHandler {

    @Override
    public void sendTitle(Player player, String text, int fadeIn, int displayTime, int fadeout) {
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\""
                + SimplePortals.getPluginInstance().getManager().colorText(text) + "\"}"), fadeIn * 20, displayTime * 20, fadeout * 20);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    }

    @Override
    public void sendSubTitle(Player player, String text, int fadeIn, int displayTime, int fadeout) {
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\""
                + SimplePortals.getPluginInstance().getManager().colorText(text) + "\"}"), fadeIn * 20, displayTime * 20, fadeout * 20);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int displayTime, int fadeOut) {
        sendTitle(player, title, fadeIn * 20, displayTime * 20, fadeOut * 20);
        sendSubTitle(player, subTitle, fadeIn * 20, displayTime * 20, fadeOut * 20);
    }

}
