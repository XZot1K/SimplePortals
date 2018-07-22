package xzot1k.plugins.sp.core.utils.jsonmsgs;

import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xzot1k.plugins.sp.SimplePortals;

public class JSONMessage
{

    private SimplePortals plugin;
    private JSONObject chatObject;

    @SuppressWarnings("unchecked")
    public JSONMessage(String text)
    {
        plugin = SimplePortals.getPluginInstance();
        chatObject = new JSONObject();

        if (text != null)
        {
            getChatObject().put("text", plugin.getManager().colorText(text));
        }
    }

    public JSONObject getChatObject()
    {
        return chatObject;
    }

    @SuppressWarnings("unchecked")
    public void addExtra(JSONExtra extraObject)
    {
        if (!chatObject.containsKey("extra"))
        {
            chatObject.put("extra", new JSONArray());
        }
        JSONArray extra = (JSONArray) chatObject.get("extra");
        extra.add(extraObject.getExtraObject());
        getChatObject().put("extra", extra);
    }

    public void sendJSONToPlayer(Player player)
    {
        plugin.getManager().getJSONHandler().sendJSONMessage(player, getChatObject().toJSONString());
    }

    @SuppressWarnings("unchecked")
    public void setClickEvent(JSONClickAction action, String value)
    {
        JSONObject clickEvent = new JSONObject();
        clickEvent.put("action", action.name().toLowerCase());
        clickEvent.put("value", plugin.getManager().colorText(value));
        getChatObject().put("clickEvent", clickEvent);
    }

    @SuppressWarnings("unchecked")
    public void setHoverEvent(JSONHoverAction action, String value)
    {
        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", action.name().toLowerCase());
        hoverEvent.put("value", plugin.getManager().colorText(value));
        getChatObject().put("hoverEvent", hoverEvent);
    }

}
