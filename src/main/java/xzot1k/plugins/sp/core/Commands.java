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
import xzot1k.plugins.sp.config.Config;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;
import xzot1k.plugins.sp.api.objects.SerializableLocation;
import xzot1k.plugins.sp.config.LangConfig;
import xzot1k.plugins.sp.config.LangKey;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            if (sender.hasPermission("simpleportals.use") || sender.hasPermission("simpleportals.use")) {

                if (args.length >= 3 && (args[0].equalsIgnoreCase("setswitchlocation") || args[0].equalsIgnoreCase("ssl"))) {
                    initiatePortalSwitchLocationSet(sender, args);
                    return true;
                } else if (args.length >= 3 && (args[0].equalsIgnoreCase("addcommand") || args[0].equalsIgnoreCase("addcmd"))) {
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
                        } else if (args[0].equalsIgnoreCase("cooldown") || args[0].equalsIgnoreCase("cd")) {
                            initiatePortalCooldown(sender, args[1], args[2]);
                            return true;
                        }

                        break;
                    default:
                        break;
                }

                sendHelpPage(sender, "1");
                return true;
            } else {
                sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return false;
            }

        }

        return false;
    }


    private void initiateDisableMessages(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.dm") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
            return;
        }

        portal.setMessage(null);
        portal.setTitle(null);
        portal.setSubTitle(null);
        portal.setBarMessage(null);
        portal.save();
        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.DM_DISABLED, placeholders));
    }

    private void initiateDisable(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.toggle") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
            return;
        }

        if (portal.isDisabled()) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.ALREADY_DISABLED, placeholders));
            return;
        }

        portal.setDisabled(true);
        portal.save();
        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.DM_DISABLED, placeholders));
    }

    private void initiateEnable(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.toggle") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL));
            return;
        }

        if (!portal.isDisabled()) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.ALREADY_DISABLED, placeholders));
            return;
        }

        portal.setDisabled(false);
        portal.save();
        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_ENABLED, placeholders));
    }

    private void sendPortalCommands(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.viewcommands") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
            return;
        }

        placeholders.put("commands", portal.getCommands().toString());

        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_COMMANDS, placeholders));
    }

    private void initiateFill(CommandSender sender, String portalName, String materialString) {
        if (!sender.hasPermission("simpleportals.fill") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Player player = (Player) sender;
        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
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
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_MATERIAL));
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_").replace("-", "_"));
        if (material == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_MATERIAL));
            return;
        }
        placeholders.put("material", material.toString());

        portal.fillPortal(player, material, durability);
        portal.save();
        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_FILLED, placeholders));
    }

    private void setMessage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("simpleportals.message") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", args[1]);

        Portal portal = getPluginInstance().getManager().getPortal(args[1]);
        if (portal == null) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
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
        placeholders.put("message", fixedMessage);
        placeholders.put("type", foundType);

        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MESSAGE_SET, placeholders));
    }

    private void addCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.addcommand") && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", args[1]);

            Portal portal = getPluginInstance().getManager().getPortal(args[1]);
            if (portal != null) {
                StringBuilder enteredCommand = new StringBuilder(args[2]);
                if (args.length > 3) for (int i = 2; ++i < args.length; ) enteredCommand.append(" ").append(args[i]);
                portal.getCommands().add(enteredCommand.toString());
                portal.save();

                String fixedCommand = enteredCommand.toString().replaceAll("(?i):CHAT", "")
                        .replaceAll("(?i):PLAYER", "").replaceAll("(?i):CONSOLE", "");
                placeholders.put("command", fixedCommand);
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.COMMAND_ADDED, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void clearCommands(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.clearcommands") && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.getCommands().clear();
                portal.save();
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.COMMANDS_CLEARED, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void toggleCommandOnly(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.togglecommandonly") && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setCommandsOnly(!portal.isCommandsOnly());
                portal.save();
                placeholders.put("status", portal.isCommandsOnly() ? "Enabled" : "Disabled");
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.COMMAND_ONLY_TOGGLED, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiatePortalSwitchLocationSet(CommandSender sender, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
            return;
        }

        Player player = (Player) sender;
        if ((!player.hasPermission("simpleportals.setswitchlocation") || !player.hasPermission("simpleportals.ssl")) && !sender.hasPermission("simpleportals.admin")) {
            player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", args[1]);

        Portal portal = getPluginInstance().getManager().getPortal(args[1]);
        if (portal == null) player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));

        final String worldName = args[2];
        if (getPluginInstance().getServer().getWorlds().parallelStream().noneMatch(world -> world.getName().equalsIgnoreCase(worldName))) {
            placeholders.put("world", worldName);
            player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_WORLD, placeholders));
            return;
        }

        String invalidCoordMessage = LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_COORDINATE);

        for (int i = 2; ++i < Math.min(args.length, 8); ) {
            if (getPluginInstance().getManager().isNumeric(args[i])) {
                player.sendMessage(invalidCoordMessage.replace("{value}", args[i]));
                return;
            }
        }

        SerializableLocation location = ((args.length >= 8) ? new SerializableLocation(getPluginInstance(), worldName, Double.parseDouble(args[3]),
                Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7]))
                : new SerializableLocation(getPluginInstance(), worldName, Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), 0, 0));

        portal.setServerSwitchLocation(location);
        portal.save();
        player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.SWITCH_LOCATION_SET, placeholders));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ((!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl")) && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setTeleportLocation(player.getLocation());
                portal.save();
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.LOCATION_SET, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName, String otherPortalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ((!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl")) && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", otherPortalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {

                Portal foundPortal = getPluginInstance().getManager().getPortal(otherPortalName);
                if (foundPortal == null) {
                    player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
                    return;
                }

                SerializableLocation foundPointOne = foundPortal.getRegion().getPoint1(), foundPointTwo = foundPortal.getRegion().getPoint2();
                final int x = (int) ((foundPointOne.getX() + foundPointTwo.getX()) / 2), y = (int) ((foundPointOne.getY() + foundPointTwo.getY()) / 2), z =
                        (int) ((foundPointOne.getZ() + foundPointTwo.getZ()) / 2);

                World world = getPluginInstance().getServer().getWorld(foundPointOne.getWorldName());
                if (world == null) {
                    placeholders.put("world", foundPointOne.getWorldName());
                    player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_WORLD, placeholders));
                    return;
                }

                portal.setTeleportLocation(new Location(world, x + 0.5, y + 0.5, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch()));
                portal.save();
                placeholders.put("name", portal.getPortalId());
                placeholders.put("other", foundPortal.getPortalId());
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_LINKED, placeholders));
            } else {
                placeholders.put("name", portalName);
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
            }
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiatePortalCooldown(CommandSender sender, String portalName, String cooldownInSeconds) {
        if (!sender.hasPermission("simpleportals.changecooldown") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal != null) {
            int cooldownInSecondsInt;
            try {
                cooldownInSecondsInt = Integer.parseInt(cooldownInSeconds);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid cooldown entered! Please enter just a number."); // TODO: Hardcoded
                return;
            }

            portal.setCooldown(cooldownInSecondsInt);
            portal.save();

            sender.sendMessage("§aCooldown of portal §b" + portal.getPortalId() + " §asuccessfully set to §e" + cooldownInSecondsInt + " seconds§a.");

        } else {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        }

    }


    private void initiateRelocate(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ((!player.hasPermission("simpleportals.relocate") || !player.hasPermission("simpleportals.rl")) && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            Region region = getPluginInstance().getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_REGION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.setRegion(region);
                portal.save();
                getPluginInstance().getManager().clearCurrentSelection(player);
                portal.displayRegion(player);
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.REGION_RELOCATED, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiatePortalRegion(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ((!player.hasPermission("simpleportals.showregion") || !player.hasPermission("simpleportals.sr")) && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortal(portalName);
            if (portal != null) {
                portal.displayRegion(player);
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.REGION_DISPLAYED, placeholders));
            } else
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiateInfo(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.info") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        sender.sendMessage(LangConfig.colorText("&d&m-----------------------------"));
        sender.sendMessage("");
        sender.sendMessage(LangConfig.colorText(" &7Plugin Name:&r &bSimplePortals"));
        sender.sendMessage(LangConfig.colorText(" &7Author(s):&r &cXZot1K"));
        sender.sendMessage(LangConfig.colorText(" &7Plugin Version:&r &a" + getPluginInstance().getDescription().getVersion()));
        sender.sendMessage("");
        sender.sendMessage(LangConfig.colorText("&d&m-----------------------------"));
    }

    private void initiateReload(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.reload") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        getPluginInstance().reloadConfigs();
        if (Config.get().managementTask) {
            getPluginInstance().getManager().loadPortals();
        }

        sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.RELOADED));
    }

    private void initiateList(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.list") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        //Old version without clickable text

        /*List<String> portalNames = getPluginInstance().getManager().getPortalNames(true);
        StringBuilder stringBuilder = new StringBuilder();
        //Info message before portals
        stringBuilder.append(LangConfig.get().get(LangKey.PREFIX)).append(getPluginInstance().getLangConfig().getString("portal-list-message"));
        //Actual portals
        for (final String portalName : portalNames){
            stringBuilder.append("\n").append(portalName);
        }
        sender.sendMessage(getPluginInstance().getManager().colorText(stringBuilder.toString()));*/

        //Old version with clickable text which teleports you
        final TextComponent message =
                new TextComponent(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_LIST));
        for (final String portalName : getPluginInstance().getManager().getPortalMap().keySet()) {
            final Portal portal = getPluginInstance().getManager().getPortalMap().get(portalName);
            final int x = (int) ((portal.getRegion().getPoint1().getX() + portal.getRegion().getPoint2().getX()) / 2),
                    y = (int) ((portal.getRegion().getPoint1().getY() + portal.getRegion().getPoint2().getY()) / 2),
                    z = (int) ((portal.getRegion().getPoint1().getZ() + portal.getRegion().getPoint2().getZ()) / 2);

            final TextComponent portalText = new TextComponent("\n" + getPluginInstance().getManager().getPortalName(portal, true));
            portalText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(LangConfig.colorText("&bClick to teleport to the portal &a" + portalName))})); // TODO: hardcoded
            portalText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + x + " " + y + " " + z + " 0 0 " + portal.getRegion().getPoint1().getWorldName()));
            message.addExtra(portalText);
        }


        sender.spigot().sendMessage(message);
    }

    private void initiateSwitchServerSet(CommandSender sender, String portalName, String serverName) {
        if ((!sender.hasPermission("simpleportals.switchserver") || !sender.hasPermission("simpleportals.ss")) && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);
        placeholders.put("server", serverName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal != null) {
            portal.setServerSwitchName(serverName);
            portal.save();
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.SWITCH_SERVER_SET, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
    }

    private void initiatePortalDeletion(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.delete") && !sender.hasPermission("simpleportals.admin")) {
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("name", portalName);

        Portal portal = getPluginInstance().getManager().getPortal(portalName);
        if (portal != null) {
            if (sender instanceof Player) getPluginInstance().getManager().clearAllVisuals((Player) sender);
            if (portal.delete()) {
                sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_DELETED, placeholders));
            } else
                sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PORTAL, placeholders));
    }

    private void initiatePortalCreation(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.create") && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("name", portalName);

            Portal portal = getPluginInstance().getManager().getPortalAtLocation(player.getLocation());
            if (portal != null) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_EXISTS_AT_LOCATION, placeholders));
                return;
            }

            if (getPluginInstance().getManager().doesPortalExist(portalName)) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_EXISTS, placeholders));
                return;
            }

            Region region = getPluginInstance().getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_REGION));
                return;
            }

            if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NOT_SAME_WORLD));
                return;
            }

            Portal newPortal = new Portal(getPluginInstance(), portalName, region);
            newPortal.setTeleportLocation(player.getLocation().clone());
            newPortal.save();
            getPluginInstance().getManager().loadPortal(newPortal);
            placeholders.put("name", newPortal.getPortalId());

            newPortal.displayRegion(player);
            getPluginInstance().getManager().clearCurrentSelection(player);
            player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.PORTAL_CREATED, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void initiateSelectionMode(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if ((!player.hasPermission("simpleportals.selectionmode") && !player.hasPermission("simpleportals.sm")) && !sender.hasPermission("simpleportals.admin")) {
                player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.NO_PERMISSION));
                return;
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("status", getPluginInstance().getManager().isInSelectionMode(player) ? "Disabled" : "Enabled");

            getPluginInstance().getManager().setSelectionMode(player, !getPluginInstance().getManager().isInSelectionMode(player));
            player.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.SELECTION_MODE, placeholders));
        } else
            sender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.MUST_BE_PLAYER));
    }

    private void setupHelpPageMap() {
        if (!getHelpPageMap().isEmpty()) getHelpPageMap().clear();
        List<String> page1Lines = new ArrayList<>(), page2Lines = new ArrayList<>(), page3Lines = new ArrayList<>();

        page1Lines.add("&e/portals <selectionmode/sm> &7- toggles selection mode.");
        page1Lines.add("&e/portals create <name> &7- creates a new portal.");
        page1Lines.add("&e/portals delete <name> &7- deletes the given portal.");
        page1Lines.add("&e/portals list &7- shows all available portals.");
        page1Lines.add("&e/portals <showregion/sr> <name> &7- shows the portal's current region.");
        page1Lines.add("&e/portals <setlocation/sl> <name> &7- sets the portal's teleport location.");
        page1Lines.add("&e/portals <setlocation/sl> <name> <name> &7- sets the portal's teleport location to the center of the entered portal.");
        getHelpPageMap().put(1, page1Lines);

        page2Lines.add("&e/portals <addcommand/addcmd> <name> <command> &7- adds the entered command line to the portal's command list.");
        page2Lines.add("&e/portals <clearcommands/clearcmds> <name> &7- clears all commands from the specified portal.");
        page2Lines.add("&e/portals reload &7- reloads the configuration files.");
        page2Lines.add("&e/portals info &7- shows plugin information.");
        page2Lines.add("&e/portals <switchserver/ss> <name> <server> &7- sets the server for the portal.");
        page2Lines.add("&e/portals fill <name> <material:durability> &7- replaces air inside the portals region.");
        page2Lines.add("&e/portals relocate <name> &7- relocates the portal to a selected region.");
        getHelpPageMap().put(2, page2Lines);

        page3Lines.add("&e/portals <togglecommandsonly/tco> <name> &7- toggles command only mode for a portal.");
        page3Lines.add("&e/portals <commands/cmds> <name> &7- provides a list of all commands on the defined warp in the order they were added.");
        page3Lines.add("&e/portals <enable/disable> <name> &7- enables/disabled the portal entirely untiled toggled again.");
        page3Lines.add("&e/portals message <name> <text> &7- sets the message of the portal to the entered text. Refer to documentation for message types.");
        page3Lines.add("&e/portals <cooldown/cd> <name> <seconds> &7- sets a cooldown until the teleportation happens after you entered the portal");
        getHelpPageMap().put(3, page3Lines);
    }

    private void sendHelpPage(CommandSender commandSender, String pageString) {
        int page;

        HashMap<String, String> placeholders = new HashMap<>();

        try {
            page = Integer.parseInt(pageString);
        } catch (Exception ignored) {
            placeholders.put("pages", String.valueOf(getHelpPageMap().size()));
            commandSender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PAGE, placeholders));
            return;
        }

        if (getHelpPageMap().isEmpty() || !getHelpPageMap().containsKey(page)) {
            placeholders.put("pages", String.valueOf(getHelpPageMap().size()));
            commandSender.sendMessage(LangConfig.get().get(LangKey.PREFIX) + LangConfig.get().get(LangKey.INVALID_PAGE, placeholders));
            return;
        }

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> pageLines = getHelpPageMap().get(page);

            player.sendMessage(LangConfig.colorText("\n&e&m---------------&d[ &bSP Help &e(&a" + page + "&e) &d]&e&m---------------"));
            for (int i = -1; ++i < pageLines.size(); )
                player.sendMessage(LangConfig.colorText(pageLines.get(i)));

            if (page < getHelpPageMap().size() && page > 1) {
                // page is both below the max page and above 1
                TextComponent footerMessage1 = new TextComponent(LangConfig.colorText("&e&m-------&r&d[")),
                        footerExtra1 = new TextComponent(LangConfig.colorText(" &b(Previous Page)")),
                        footerExtra2 = new TextComponent(LangConfig.colorText(" &b(Next Page) ")),
                        footerEnd = new TextComponent(LangConfig.colorText("&d]&e&m--------\n"));

                footerExtra1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page - 1)));
                footerExtra1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent(LangConfig.colorText("&aClicking this will open the help menu at page &e" + (page - 1) + "&a."))}));
                footerExtra2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page + 1)));
                footerExtra2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent(LangConfig.colorText("&aClicking this will open the help menu at page &e" + (page + 1) + "&a."))}));

                footerMessage1.addExtra(footerExtra1);
                footerMessage1.addExtra(footerExtra2);
                footerMessage1.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage1);
            } else if (page < getHelpPageMap().size() && page <= 1) {
                // page is less than or = to 1
                TextComponent footerMessage = new TextComponent(LangConfig.colorText("&e&m---------------&r&d[")),
                        footerExtra = new TextComponent(LangConfig.colorText(" &b(Next Page) ")),
                        footerEnd = new TextComponent(LangConfig.colorText("&d]&e&m---------------\n"));

                footerExtra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page + 1)));
                footerExtra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(LangConfig.colorText("&aClicking this will open the" +
                        " help menu at page &e" + (page + 1) + "&a."))}));
                footerMessage.addExtra(footerExtra);
                footerMessage.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage);
            } else if (page >= getHelpPageMap().size() && page > 1) {
                // page at/above max page and greater that 1
                TextComponent footerMessage = new TextComponent(LangConfig.colorText("&d[&e&m------------&r&d]")),
                        footerExtra = new TextComponent(LangConfig.colorText(" &b(Previous Page) ")),
                        footerEnd = new TextComponent(LangConfig.colorText("&d]&e&m-------------\n"));

                footerExtra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/portals help " + (page - 1)));
                footerExtra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(LangConfig.colorText("&aClicking this will open the" +
                        " help menu at page &e" + (page - 1) + "&a."))}));
                footerMessage.addExtra(footerExtra);
                footerMessage.addExtra(footerEnd);

                player.spigot().sendMessage(footerMessage);
            } else
                player.sendMessage(LangConfig.colorText("&d[&e&m---------------------------------------&r&d]\n"));
        } else {
            List<String> pageLines = getHelpPageMap().get(page);
            commandSender.sendMessage(LangConfig.colorText("&d[&e&m-------------&r&d] &bSP Help &e(&a" + page + "&e) &d[&e&m-------------&r&d]"));
            for (int i = -1; ++i < pageLines.size(); )
                commandSender.sendMessage(LangConfig.colorText(pageLines.get(i)));
            commandSender.sendMessage(LangConfig.colorText("&d[&e&m---------------------------------------&r&d]\n"));
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