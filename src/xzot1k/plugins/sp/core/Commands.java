package xzot1k.plugins.sp.core;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor
{

    private SimplePortals pluginInstance;

    public Commands(SimplePortals pluginInstance)
    {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("simpleportals"))
        {

            switch (args.length)
            {
                case 1:
                    if (args[0].equalsIgnoreCase("selectionmode") || args[0].equalsIgnoreCase("sm"))
                    {
                        initiateSelectionMode(sender);
                        return true;
                    } else if (args[0].equalsIgnoreCase("list"))
                    {
                        initiateList(sender);
                        return true;
                    } else if (args[0].equalsIgnoreCase("reload"))
                    {
                        initiateReload(sender);
                        return true;
                    } else if (args[0].equalsIgnoreCase("info"))
                    {
                        initiateInfo(sender);
                        return true;
                    }

                    break;
                case 2:
                    if (args[0].equalsIgnoreCase("create"))
                    {
                        initiatePortalCreation(sender, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("delete"))
                    {
                        initiatePortalDeletion(sender, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("setlocation") || args[0].equalsIgnoreCase("sl"))
                    {
                        initiatePortalLocationSet(sender, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("showregion") || args[0].equalsIgnoreCase("sr"))
                    {
                        initiatePortalRegion(sender, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("relocate") || args[0].equalsIgnoreCase("rl"))
                    {
                        initiateRelocate(sender, args[1]);
                        return true;
                    }

                    break;
                case 3:
                    if (args[0].equalsIgnoreCase("switchserver") || args[0].equalsIgnoreCase("ss"))
                    {
                        initiateSwitchServerSet(sender, args[1], args[2]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("fill"))
                    {
                        initiateFill(sender, args[1], args[2]);
                        return true;
                    }

                    break;
                default:
                    break;
            }

            List<String> usageMessageList = pluginInstance.getConfig().getStringList("usage-message");
            for (int i = -1; ++i < usageMessageList.size(); )
                sender.sendMessage(pluginInstance.getManager().colorText(usageMessageList.get(i)));
            return true;
        }

        return false;
    }

    private void initiateFill(CommandSender sender, String portalName, String materialString)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.fill"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null)
            {
                Material material;
                int durability = 0;

                if (materialString.contains(":"))
                {
                    String[] materialStringArgs = materialString.split(":");

                    try
                    {
                        material = Material.getMaterial(materialStringArgs[0].toUpperCase().replace(" ", "_").replace("-", "_"));
                        durability = Integer.parseInt(materialStringArgs[1]);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        pluginInstance.getManager().sendConsoleMessage("&cThere seems to have been a issue with the material argument " +
                                "you entered. Be sure if a colon is used to follow it by a integer.");
                        return;
                    }
                } else
                {
                    try
                    {
                        material = Material.getMaterial(materialString.toUpperCase().replace(" ", "_").replace("-", "_"));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        pluginInstance.getManager().sendConsoleMessage("&cThere seems to have been a issue with the material argument you entered.");
                        return;
                    }
                }

                Region region = portal.getRegion();
                region.setAirBlocks(material, durability);
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("region-filled-message")
                        .replace("{material}", WordUtils.capitalize(material.name()) + ":" + String.valueOf(durability))
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateRelocate(CommandSender sender, String portalName)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.relocate") || !player.hasPermission("simpleportals.rl"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Region region = pluginInstance.getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null)
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("selected-region-invalid-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null)
            {
                portal.setRegion(region);
                pluginInstance.getManager().clearCurrentSelection(player);
                portal.displayRegion(player);
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("region-relocated-message").replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiatePortalRegion(CommandSender sender, String portalName)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.showregion") || !player.hasPermission("simpleportals.sr"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null)
            {
                portal.displayRegion(player);
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("region-displayed-message").replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null)
            {
                portal.setTeleportLocation(player.getLocation());
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("location-set-message")
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateInfo(CommandSender sender)
    {
        if (!sender.hasPermission("simpleportals.info"))
        {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        sender.sendMessage(pluginInstance.getManager().colorText("&d&m-----------------------------"));
        sender.sendMessage("");
        sender.sendMessage(pluginInstance.getManager().colorText(" &7Plugin Name:&r &bSimplePortals"));
        sender.sendMessage(pluginInstance.getManager().colorText(" &7Author(s):&r &cXZot1K"));
        sender.sendMessage(pluginInstance.getManager().colorText(" &7Plugin Version:&r &a" + pluginInstance.getDescription().getVersion()));
        sender.sendMessage("");
        sender.sendMessage(pluginInstance.getManager().colorText("&d&m-----------------------------"));
    }

    private void initiateReload(CommandSender sender)
    {
        if (!sender.hasPermission("simpleportals.reload"))
        {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        pluginInstance.reloadConfig();
        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("reload-message")));
    }

    private void initiateList(CommandSender sender)
    {
        if (!sender.hasPermission("simpleportals.list"))
        {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        List<String> portalNames = new ArrayList<>();
        for (int i = -1; ++i < pluginInstance.getManager().getPortals().size(); )
            portalNames.add(pluginInstance.getManager().getPortals().get(i).getPortalId());
        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("list-message")
                .replace("{list}", portalNames.toString())));
    }

    private void initiateSwitchServerSet(CommandSender sender, String portalName, String serverName)
    {
        if (!sender.hasPermission("simpleportals.switchserver") || !sender.hasPermission("simpleportals.ss"))
        {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal != null)
        {
            portal.setServerSwitchName(serverName);
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("switch-server-set-message")
                    .replace("{name}", portal.getPortalId()).replace("{server}", serverName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
    }

    private void initiatePortalDeletion(CommandSender sender, String portalName)
    {
        if (!sender.hasPermission("simpleportals.delete"))
        {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal != null)
        {
            if (sender instanceof Player) pluginInstance.getManager().clearAllVisuals((Player) sender);
            Region region = portal.getRegion();
            region.revertFilledBlocks();

            portal.delete();
            portal.unregister();
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("portal-deleted-message")
                    .replace("{name}", portal.getPortalId())));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("portal-invalid-message").replace("{name}", portalName)));
    }

    private void initiatePortalCreation(CommandSender sender, String portalName)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.create"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalAtLocation(player.getLocation());
            if (portal != null)
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-location-exists-message").replace("{name}", portal.getPortalId())));
                return;
            }

            if (pluginInstance.getManager().doesPortalExist(portalName))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("portal-exists-message").replace("{name}", portalName)));
                return;
            }

            Region region = pluginInstance.getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null)
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("selected-region-invalid-message")));
                return;
            }

            if (!region.getPoint1().getWorld().getName().equalsIgnoreCase(region.getPoint2().getWorld().getName()))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("not-same-world-message")));
                return;
            }

            Portal newPortal = new Portal(pluginInstance, portalName, region);
            newPortal.register();
            newPortal.displayRegion(player);
            pluginInstance.getManager().clearCurrentSelection(player);
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("portal-created-message")
                    .replace("{name}", newPortal.getPortalId())));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateSelectionMode(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.selectionmode") || !player.hasPermission("simpleportals.sm"))
            {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            pluginInstance.getManager().setSelectionMode(player, !pluginInstance.getManager().isInSelectionMode(player));
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("selection-mode-message")
                    .replace("{status}", pluginInstance.getManager().isInSelectionMode(player) ? "Enabled" : "Disabled")));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

}
