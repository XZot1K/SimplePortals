/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core.tasks;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.config.LangConfig;
import xzot1k.plugins.sp.config.LangKey;

import java.util.HashMap;

public class TeleportTask extends BukkitRunnable {

    private final SimplePortals INSTANCE;
    private final Entity entity;
    private final Portal portal;
    private final Location location;
    private long ticksPassed = 0;
    private boolean done = false;

    public TeleportTask(Entity entity, Portal portal, Location location) {
        this.INSTANCE = SimplePortals.getPluginInstance();
        this.entity = entity;
        this.portal = portal;
        this.location = location;

        HashMap<String, BukkitTask> ids = INSTANCE.getManager().getTasks().computeIfAbsent(entity.getUniqueId(), id -> new HashMap<>());
        if (!ids.isEmpty()) {
            final BukkitTask teleportTask = ids.getOrDefault("teleport", null);
            if (teleportTask != null) {
                teleportTask.cancel();
                ids.remove("teleport");
            }
        }

        ids.put("teleport", runTaskTimer(INSTANCE, 0, 2));
    }

    @Override
    public void run() {

        ticksPassed += 2;
        double rem = (portal.getDelay() - (ticksPassed / 20f));
        rem = (Math.floor(rem * 10) / 10);

        if (rem <= 0 && !done) {
            done = true;

            if (entity instanceof Player) {
                final Player player = ((Player) entity);

                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("rem", String.valueOf(rem));

                String title = LangConfig.get().get(LangKey.TELEPORT_DELAY_TITLE),
                        subTitle = LangConfig.get().get(LangKey.TELEPORT_DELAY_SUBTITLE, placeholders);
                if ((title != null && !title.isEmpty()) || (subTitle != null && !subTitle.isEmpty())) {
                    player.sendTitle(title, subTitle, 0, 40, 0);
                }
            }

            portal.invokeCommands((Player) entity, location);

            if (!portal.isCommandsOnly()) {
                INSTANCE.getManager().teleportWithEntity(entity, location);
                if (entity instanceof Player) {
                    final Player player = ((Player) entity);
                    INSTANCE.getManager().getPortalLinkMap().put(player.getUniqueId(), portal.getPortalId());
                    INSTANCE.getManager().getEntitiesInTeleportationAndPortals().remove(player.getUniqueId());
                }
            }

            cancel();
            return;
        }

        if (rem > 0 && entity instanceof Player) {
            final Player player = ((Player) entity);

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("rem", String.valueOf(rem));

            String title = LangConfig.get().get(LangKey.TELEPORT_DELAY_TITLE),
                    subTitle = LangConfig.get().get(LangKey.TELEPORT_DELAY_SUBTITLE, placeholders);
            if ((title != null && !title.isEmpty()) || (subTitle != null && !subTitle.isEmpty())) {
                player.sendTitle(title, subTitle, 0, 40, 0);
            }
        }
    }

}
