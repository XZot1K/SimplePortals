/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.bar;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;

public class ABH1_8R1 implements BarHandler {
    @Override

    public void sendActionBar(Player player, String message) {
        IChatBaseComponent iChatBaseComponent = ChatSerializer.a("{\"text\": \""
                + SimplePortals.getPluginInstance().getManager().colorText(message) + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(iChatBaseComponent, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(bar);
    }

}
