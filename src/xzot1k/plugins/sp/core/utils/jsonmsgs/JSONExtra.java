package xzot1k.plugins.sp.core.utils.jsonmsgs;

import org.json.simple.JSONObject;
import xzot1k.plugins.sp.SimplePortals;

public class JSONExtra
{
    private SimplePortals plugin;
    private JSONObject extraObject;

    @SuppressWarnings("unchecked")
    public JSONExtra(String text)
    {
        plugin = SimplePortals.getPluginInstance();
        extraObject = new JSONObject();

        if (text != null)
        {
            getExtraObject().put("text", plugin.getManager().colorText(text));
        }
    }

    public JSONObject getExtraObject()
    {
        return extraObject;
    }

    @SuppressWarnings("unchecked")
    public void setClickEvent(JSONClickAction action, String value)
    {
        JSONObject clickEvent = new JSONObject();
        clickEvent.put("action", action.name().toLowerCase());
        clickEvent.put("value", plugin.getManager().colorText(value));
        getExtraObject().put("clickEvent", clickEvent);
    }

    @SuppressWarnings("unchecked")
    public void setHoverEvent(JSONHoverAction action, String value)
    {
        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", action.name().toLowerCase());
        hoverEvent.put("value", plugin.getManager().colorText(value));
        getExtraObject().put("hoverEvent", hoverEvent);
    }

}
