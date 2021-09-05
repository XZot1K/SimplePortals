/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.core;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;
import xzot1k.plugins.sp.api.objects.SerializableLocation;
import xzot1k.plugins.sp.core.tasks.ManagementTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor {

    private final SimplePortals pluginInstance;
    private HashMap<Integer, List<String>> helpPageMap;

    public Commands(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
        setHelpPageMap(new HashMap<>());
        setupHelpPageMap();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simpleportals")) {
            if(sender.hasPermission("simpleportals.use")){
                if (args.length >= 3 && (args[0].equalsIgnoreCase("addcommand") || args[0].equalsIgnoreCase("addcmd"))) {
                    addCommand(sender, args);
                    return true;
                } else if (args.length >= 3 && (args[0].equalsIgnoreCase("message"))) {
                    setMessage(sender, args);
                    return true;
                }

                switch (args.length) {
                    case 1:
                        if (args[0].equalsIgnoreCase("selectionmode") || args[0].equalsIgnoreCase("sm")) {
                            initiateSelectionMode(sender);
                            return true;
                        } else if (args[0].equalsIgnoreCase("list")) {
                            initiateList(sender);
                            return true;
                        } else if (args[0].equalsIgnoreCase("reload")) {
                            initiateReload(sender);
                            return true;
                        } else if (args[0].equalsIgnoreCase("info")) {
                            initiateInfo(sender);
                            return true;
                        }

                        break;
                    case 2:
                        if (args[0].equalsIgnoreCase("disablemessages") || args[0].equalsIgnoreCase("dm")) {
                            initiateDisableMessages(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("create")) {
                            initiatePortalCreation(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("enable")) {
                            initiateEnable(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("disable")) {
                            initiateDisable(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("cmds")) {
                            sendPortalCommands(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("delete")) {
                            initiatePortalDeletion(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("setlocation") || args[0].equalsIgnoreCase("sl")) {
                            initiatePortalLocationSet(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("showregion") || args[0].equalsIgnoreCase("sr")) {
                            initiatePortalRegion(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("relocate") || args[0].equalsIgnoreCase("rl")) {
                            initiateRelocate(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("clearcommands") || args[0].equalsIgnoreCase("clearcmds")) {
                            clearCommands(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("togglecommandsonly") || args[0].equalsIgnoreCase("tco")) {
                            toggleCommandOnly(sender, args[1]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("help")) {
                            sendHelpPage(sender, args[1]);
                            return true;
                        }

                        break;
                    case 3:
                        if (args[0].equalsIgnoreCase("switchserver") || args[0].equalsIgnoreCase("ss")) {
                            initiateSwitchServerSet(sender, args[1], args[2]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("fill")) {
                            initiateFill(sender, args[1], args[2]);
                            return true;
                        } else if (args[0].equalsIgnoreCase("setlocation") || args[0].equalsIgnoreCase("sl")) {
                            initiatePortalLocationSet(sender, args[1], args[2]);
                            return true;
                        }

                        break;
                    default:
                        break;
                }

                sendHelpPage(sender, "1");
                return true;
            }else{
                sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return false;
            }

        }

        return false;
    }

    private void initiateDisableMessages(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.dm")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        portal.setMessage(null);
        portal.setTitle(null);
        portal.setSubTitle(null);
        portal.setBarMessage(null);
        portal.save();
        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-dm-message"))
                .replace("{name}", portalName)));
    }

    private void initiateDisable(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.toggle")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        if (portal.isDisabled()) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("already-disabled-message"))
                    .replace("{name}", portalName)));
            return;
        }

        portal.setDisabled(true);
        portal.save();
        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-disabled-message"))
                .replace("{name}", portalName)));
    }

    private void initiateEnable(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.toggle")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        if (!portal.isDisabled()) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("already-enabled-message"))
                    .replace("{name}", portalName)));
            return;
        }

        portal.setDisabled(false);
        portal.save();
        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-enabled-message"))
                .replace("{name}", portalName)));
    }

    private void sendPortalCommands(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.viewcommands")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-commands-message"))
                .replace("{commands}", portal.getCommands().toString()).replace("{name}", portalName)));
    }

    private void initiateFill(CommandSender sender, String portalName, String materialString) {
        if (!sender.hasPermission("simpleportals.fill")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
            return;
        }

        Player player = (Player) sender;
        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        String materialName;
        int durability = 0;
        if (materialString.contains(":")) {
            String[] args = materialString.split(":");
            materialName = args[0];
            if (getPluginInstance().getManager().isNumeric(args[1]))
                durability = Integer.parseInt(args[1]);
        } else materialName = materialString;

        if (materialName == null || materialName.equalsIgnoreCase("")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("invalid-material-message")));
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_").replace("-", "_"));
        if (material == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("invalid-material-message")));
            return;
        }

        portal.fillPortal(player, material, durability);
        portal.save();
        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-filled-message")).replace("{name}", portal.getPortalId()).replace("{material}", material.name())));
    }

    private void setMessage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("simpleportals.message")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(args[1]);
        if (portal == null) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", args[1])));
            return;
        }

        StringBuilder enteredMessage = new StringBuilder(args[2]);
        if (args.length > 3) for (int i = 2; ++i < args.length; ) enteredMessage.append(" ").append(args[i]);

        String foundType = "Normal";
        final String tempMessage = enteredMessage.toString().toUpperCase(), fixedMessage = enteredMessage.toString().replaceAll("(?i):NORMAL", "")
                .replaceAll("(?i):BAR", "").replaceAll("(?i):SUBTITLE", "").replaceAll("(?i):TITLE", "");
        if (tempMessage.endsWith(":BAR")) {
            portal.setBarMessage(fixedMessage);
            foundType = "Bar";
        } else if (tempMessage.endsWith(":TITLE")) {
            portal.setTitle(fixedMessage);
            foundType = "Title";
        } else if (tempMessage.endsWith(":SUBTITLE")) {
            portal.setSubTitle(fixedMessage);
            foundType = "Sub-Title";
        } else portal.setMessage(fixedMessage);
        portal.save();

        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-message-set"))
                .replace("{message}", fixedMessage).replace("{type}", foundType).replace("{name}", portal.getPortalId())));
    }

    private void addCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.addcommand")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(args[1]);
            if (portal != null) {
                StringBuilder enteredCommand = new StringBuilder(args[2]);
                if (args.length > 3) for (int i = 2; ++i < args.length; ) enteredCommand.append(" ").append(args[i]);
                portal.getCommands().add(enteredCommand.toString());
                portal.save();

                String fixedCommand = enteredCommand.toString().replaceAll("(?i):CHAT", "")
                        .replaceAll("(?i):PLAYER", "").replaceAll("(?i):CONSOLE", "");
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-command-added-message"))
                        .replace("{command}", fixedCommand).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", args[1])));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void clearCommands(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.clearcommands")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.getCommands().clear();
                portal.save();
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-commands-cleared-message"))
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void toggleCommandOnly(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.togglecommandonly")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setCommandsOnly(!portal.isCommandsOnly());
                portal.save();
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-command-only-toggle-message"))
                        .replace("{status}", portal.isCommandsOnly() ? "Enabled" : "Disabled")
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setTeleportLocation(player.getLocation());
                portal.save();
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("location-set-message"))
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName, String otherPortalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {

                Portal foundPortal = getPluginInstance().getManager().getPortal(otherPortalName);
                if (foundPortal == null) {
                    player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                            + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", otherPortalName)));
                    return;
                }

                SerializableLocation foundPointOne = foundPortal.getRegion().getPoint1(), foundPointTwo = foundPortal.getRegion().getPoint2();
                final int x = (int) ((foundPointOne.getX() + foundPointTwo.getX()) / 2), y = (int) ((foundPointOne.getY() + foundPointTwo.getY()) / 2), z = (int) ((foundPointOne.getZ() + foundPointTwo.getZ()) / 2);

                World world = getPluginInstance().getServer().getWorld(foundPointOne.getWorldName());
                if (world == null) {
                    player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                            + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("world-invalid-message")).replace("{name}", foundPointOne.getWorldName())));
                    return;
                }

                portal.setTeleportLocation(new Location(world, x + 0.5, y + 0.5, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch()));
                portal.save();
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-link-message"))
                        .replace("{name}", portal.getPortalId()).replace("{other}", foundPortal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiateRelocate(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.relocate") || !player.hasPermission("simpleportals.rl")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Region region = getPluginInstance().getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("selected-region-invalid-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setRegion(region);
                portal.save();
                getPluginInstance().getManager().clearCurrentSelection(player);
                portal.displayRegion(player);
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("region-relocated-message")).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiatePortalRegion(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.showregion") || !player.hasPermission("simpleportals.sr")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.displayRegion(player);
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("region-displayed-message")).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiateInfo(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.info")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        sender.sendMessage(getPluginInstance().getManager().colorText("&d&m-----------------------------"));
        sender.sendMessage("");
        sender.sendMessage(getPluginInstance().getManager().colorText(" &7Plugin Name:&r &bSimplePortals"));
        sender.sendMessage(getPluginInstance().getManager().colorText(" &7Author(s):&r &cXZot1K"));
        sender.sendMessage(getPluginInstance().getManager().colorText(" &7Plugin Version:&r &a" + getPluginInstance().getDescription().getVersion()));
        sender.sendMessage("");
        sender.sendMessage(getPluginInstance().getManager().colorText("&d&m-----------------------------"));
    }

    private void initiateReload(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.reload")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        if (getPluginInstance().getConfig().getBoolean("management-task")) {
            getPluginInstance().getManagementTask().cancel();
            getPluginInstance().reloadConfigs();
            getPluginInstance().setManagementTask(new ManagementTask(getPluginInstance()));
            getPluginInstance().getManagementTask().runTaskTimerAsynchronously(getPluginInstance(), 0, 200);
        } else getPluginInstance().reloadConfigs();

        sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                + getPluginInstance().getLangConfig().getString("reload-message")));
    }

    private void initiateList(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.list")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        List<String> portalNames = getPluginInstance().getManager().getPortalNames(true);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPluginInstance().getLangConfig().getString("prefix")).append(getPluginInstance().getLangConfig().getString("portal-list-message"));
        for (String portalName : portalNames) stringBuilder.append("\n").append(portalName);
        sender.sendMessage(getPluginInstance().getManager().colorText(stringBuilder.toString()));
    }

    private void initiateSwitchServerSet(CommandSender sender, String portalName, String serverName) {
        if (!sender.hasPermission("simpleportals.switchserver") || !sender.hasPermission("simpleportals.ss")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal != null) {
            portal.setServerSwitchName(serverName);
            portal.save();
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("switch-server-set-message"))
                    .replace("{name}", portal.getPortalId()).replace("{server}", serverName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
    }

    private void initiatePortalDeletion(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.delete")) {
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal != null) {
            if (sender instanceof Player) getPluginInstance().getManager().clearAllVisuals((Player) sender);
            if (portal.delete()) {
                sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-deleted-message"))
                        .replace("{name}", portal.getPortalId())));
            } else
                sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
    }

    private void initiatePortalCreation(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.create")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = getPluginInstance().getManager().getPortalAtLocation(player.getLocation());
            if (portal != null) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-location-exists-message")).replace("{name}", portal.getPortalId())));
                return;
            }

            if (getPluginInstance().getManager().doesPortalExist(portalName)) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-exists-message")).replace("{name}", portalName)));
                return;
            }

            Region region = getPluginInstance().getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("selected-region-invalid-message")));
                return;
            }

            if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("not-same-world-message")));
                return;
            }

            Portal newPortal = new Portal(getPluginInstance(), portalName, region);
            newPortal.setTeleportLocation(player.getLocation().clone());
            newPortal.save();

            newPortal.displayRegion(player);
            getPluginInstance().getManager().clearCurrentSelection(player);
            player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("portal-created-message"))
                    .replace("{name}", newPortal.getPortalId())));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void initiateSelectionMode(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.selectionmode") && !player.hasPermission("simpleportals.sm")) {
                player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                        + getPluginInstance().getLangConfig().getString("no-permission-message")));
                return;
            }

            getPluginInstance().getManager().setSelectionMode(player, !getPluginInstance().getManager().isInSelectionMode(player));
            player.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("selection-mode-message"))
                    .replace("{status}", getPluginInstance().getManager().isInSelectionMode(player) ? "Enabled" : "Disabled")));
        } else
            sender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + getPluginInstance().getLangConfig().getString("must-be-player-message")));
    }

    private void setupHelpPageMap() {
        if (!getHelpPageMap().isEmpty()) getHelpPageMap().clear();
        List<String> page1Lines = new ArrayList<>(), page2Lines = new ArrayList<>(), page3Lines = new ArrayList<>();

        page1Lines.add("&e/portals <selectionmode/sm> &7- toggles selection mode.");
        page1Lines.add("&e/portals reload &7- reloads the configuration files.");
        page1Lines.add("&e/portals <switchserver/ss> <name> <server> &7- sets the server for the portal.");
        page1Lines.add("&e/portals <showregion/sr> <name> &7- shows the portal's current region.");
        page1Lines.add("&e/portals <setlocation/sl> <name> &7- sets the portal's teleport location.");
        page1Lines.add("&e/portals <setlocation/sl> <name> <name> &7- sets the portal's teleport location to the center of the entered portal.");
        page1Lines.add("&e/portals info &7- shows plugin information.");
        getHelpPageMap().put(1, page1Lines);

        page2Lines.add("&e/portals create <name> &7- creates a new portal.");
        page2Lines.add("&e/portals delete <name> &7- deletes the given portal.");
        page2Lines.add("&e/portals list &7- shows all available portals.");
        page2Lines.add("&e/portals fill <name> <material:durability> &7- replaces air inside the portals region.");
        page2Lines.add("&e/portals relocate <name> &7- relocates the portal to a selected region.");
        page2Lines.add("&e/portals <addcommand/addcmd> <name> <command> &7- adds the entered command line to the portal's command list.");
        page2Lines.add("&e/portals <clearcommands/clearcmds> <name> &7- clears all commands from the specified portal.");
        getHelpPageMap().put(2, page2Lines);

        page3Lines.add("&e/portals <togglecommandonly/tco> <name> &7- toggles command only mode for a portal.");
        page3Lines.add("&e/portals <commands/cmds> <name> &7- provides a list of all commands on the defined warp in the order they were added.");
        page3Lines.add("&e/portals <enable/disable> <name> &7- enables/disabled the portal entirely untiled toggled again.");
        page3Lines.add("&e/portals message <name> <text> &7- sets the message of the portal to the entered text. Refer to documentation for message types.");
        getHelpPageMap().put(3, page3Lines);
    }

    private void sendHelpPage(CommandSender commandSender, String pageString) {
        int page;
        try {
            page = Integer.parseInt(pageString);
        } catch (Exception ignored) {
            commandSender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("invalid-page-message")).replace("{pages}", String.valueOf(getHelpPageMap().size()))));
            return;
        }

        if (getHelpPageMap().isEmpty() || !getHelpPageMap().containsKey(page)) {
            commandSender.sendMessage(getPluginInstance().getManager().colorText(getPluginInstance().getLangConfig().getString("prefix")
                    + Objects.requireNonNull(getPluginInstance().getLangConfig().getString("invalid-page-message")).replace("{pages}", String.valueOf(getHelpPageMap().size()))));
            return;
        }

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> pageLines = getHelpPageMap().get(page);

            player.sendMessage(getPluginInstance().getManager().colorText("\n&e&m---------------&d[ &bSP Help &e(&a" + page + "&e) &d]&e&m---------------"));
            for (int i = -1; ++i < pageLines.size(); )
                player.sendMessage(getPluginInstance().getManager().colorText(pageLines.get(i)));

            if (page < getHelpPageMap().size() && page > 1) {
                // page is both below the max page and above 1
                TextComponent footerMessage1 = new TextComponent(getPluginInstance().getManager().colorText("&e&m-------&r&d[")),
                        footerExtra1 = new TextComponent(getPluginInstance().getManager().colorText(" &b(Previous Page)")),
                        footerExtra2 = new TextComponent(getPluginInstance().getManager().colorText(" &b(Next Page) ")),
                        footerEnd = new TextComponent(getPluginInstance().getManager().colorText("&d]&e&m--------\n"));

                footerExtra1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page - 1)));
                footerExtra1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent(getPluginInstance().getManager().colorText("&aClicking this will open the help menu at page &e" + (page - 1) + "&a."))}));
                footerExtra2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page + 1)));
                footerExtra2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent(getPluginInstance().getManager().colorText("&aClicking this will open the help menu at page &e" + (page + 1) + "&a."))}));

                footerMessage1.addExtra(footerExtra1);
                footerMessage1.addExtra(footerExtra2);
                footerMessage1.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage1);
            } else if (page < getHelpPageMap().size() && page <= 1) {
                // page is less than or = to 1
                TextComponent footerMessage = new TextComponent(getPluginInstance().getManager().colorText("&e&m---------------&r&d[")),
                        footerExtra = new TextComponent(getPluginInstance().getManager().colorText(" &b(Next Page) ")),
                        footerEnd = new TextComponent(getPluginInstance().getManager().colorText("&d]&e&m---------------\n"));

                footerExtra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page + 1)));
                footerExtra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(getPluginInstance().getManager().colorText("&aClicking this will open the help menu at page &e" + (page + 1) + "&a."))}));
                footerMessage.addExtra(footerExtra);
                footerMessage.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage);
            } else if (page >= getHelpPageMap().size() && page > 1) {
                // page at/above max page and greater that 1
                TextComponent footerMessage = new TextComponent(getPluginInstance().getManager().colorText("&d[&e&m------------&r&d]")),
                        footerExtra = new TextComponent(getPluginInstance().getManager().colorText(" &b(Previous Page) ")),
                        footerEnd = new TextComponent(getPluginInstance().getManager().colorText("&d]&e&m-------------\n"));

                footerExtra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page - 1)));
                footerExtra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(getPluginInstance().getManager().colorText("&aClicking this will open the help menu at page &e" + (page - 1) + "&a."))}));
                footerMessage.addExtra(footerExtra);
                footerMessage.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage);
            } else
                player.sendMessage(getPluginInstance().getManager().colorText("&d[&e&m---------------------------------------&r&d]\n"));
        } else {
            List<String> pageLines = getHelpPageMap().get(page);
            commandSender.sendMessage(getPluginInstance().getManager().colorText("&d[&e&m-------------&r&d] &bSP Help &e(&a" + page + "&e) &d[&e&m-------------&r&d]"));
            for (int i = -1; ++i < pageLines.size(); )
                commandSender.sendMessage(getPluginInstance().getManager().colorText(pageLines.get(i)));
            commandSender.sendMessage(getPluginInstance().getManager().colorText("&d[&e&m---------------------------------------&r&d]\n"));
        }
    }

    private HashMap<Integer, List<String>> getHelpPageMap() {
        return helpPageMap;
    }

    private void setHelpPageMap(HashMap<Integer, List<String>> helpPageMap) {
        this.helpPageMap = helpPageMap;
    }

    private SimplePortals getPluginInstance() {
        return pluginInstance;
    }
}
