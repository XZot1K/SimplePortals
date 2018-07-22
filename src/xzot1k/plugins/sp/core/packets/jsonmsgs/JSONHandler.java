package xzot1k.plugins.sp.core.packets.jsonmsgs;

import org.bukkit.entity.Player;

public interface JSONHandler
{
    void sendJSONMessage(Player player, String JSONString);
}
