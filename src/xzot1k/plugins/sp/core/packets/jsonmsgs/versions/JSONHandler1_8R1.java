package xzot1k.plugins.sp.core.packets.jsonmsgs.versions;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.core.packets.jsonmsgs.JSONHandler;

public class JSONHandler1_8R1 implements JSONHandler
{

    public void sendJSONMessage(Player player, String JSONString)
    {
        IChatBaseComponent comp = ChatSerializer.a(JSONString);
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(comp);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }

}
