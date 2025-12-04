/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.packets.titles.versions;

import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.config.LangConfig;
import xzot1k.plugins.sp.core.packets.titles.TitleHandler;

public class Titles_Latest implements TitleHandler {

    @Override
    public void sendTitle(Player player, String text, int fadeIn, int displayTime, int fadeOut) {
        player.sendTitle(text, "", fadeIn * 20, displayTime * 20, fadeOut * 20);
    }

    @Override
    public void sendSubTitle(Player player, String text, int fadeIn, int displayTime, int fadeOut) {
        player.sendTitle("", LangConfig.colorText(text), fadeIn * 20, displayTime * 20, fadeOut * 20);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int displayTime, int fadeOut) {
        player.sendTitle(LangConfig.colorText(title), LangConfig.colorText(subTitle),
                fadeIn * 20, displayTime * 20, fadeOut * 20);
    }

}
