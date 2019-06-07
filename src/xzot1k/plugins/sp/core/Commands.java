package xzot1k.plugins.sp.core;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xzot1k.plugins.sp.SimplePortals;
import xzot1k.plugins.sp.api.objects.Portal;
import xzot1k.plugins.sp.api.objects.Region;
import xzot1k.plugins.sp.core.utils.jsonmsgs.JSONClickAction;
import xzot1k.plugins.sp.core.utils.jsonmsgs.JSONExtra;
import xzot1k.plugins.sp.core.utils.jsonmsgs.JSONHoverAction;
import xzot1k.plugins.sp.core.utils.jsonmsgs.JSONMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor {

    private SimplePortals pluginInstance;
    private HashMap<Integer, List<String>> helpPageMap;

    public Commands(SimplePortals pluginInstance) {
        this.pluginInstance = pluginInstance;
        setHelpPageMap(new HashMap<>());
        setupHelpPageMap();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("simpleportals")) {

            if (args.length >= 3 && (args[0].equalsIgnoreCase("addcommand") || args[0].equalsIgnoreCase("addcmd"))) {
                addCommand(sender, args);
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
                    if (args[0].equalsIgnoreCase("create")) {
                        initiatePortalCreation(sender, args[1]);
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
                    }

                    break;
                default:
                    break;
            }

            sendHelpPage(sender, "1");
            return true;
        }

        return false;
    }

    private void sendPortalCommands(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.viewcommands")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal == null) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-commands-message"))
                .replace("{commands}", portal.getCommands().toString()).replace("{name}", portalName)));
    }

    private void initiateFill(CommandSender sender, String portalName, String materialString) {
        if (!sender.hasPermission("simpleportals.fill")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal == null) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
            return;
        }

        String materialName;
        int durability = 0;
        if (materialString.contains(":")) {
            String[] args = materialString.split(":");
            materialName = args[0];
            if (pluginInstance.getManager().isNumeric(args[1]))
                durability = Integer.parseInt(args[1]);
        } else materialName = materialString;

        if (materialName == null || materialName.equalsIgnoreCase("")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("invalid-material-message")));
            return;
        }

        Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_").replace("-", "_"));
        if (material == null) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("invalid-material-message")));
            return;
        }

        portal.fillPortal(material, durability);
        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-filled-message")).replace("{name}", portal.getPortalId()).replace("{material}", material.name())));
    }

    private void addCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.addcommand")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(args[1]);
            if (portal != null) {
                StringBuilder enteredCommand = new StringBuilder(args[2]);
                if (args.length > 3) for (int i = 2; ++i < args.length; ) enteredCommand.append(" ").append(args[i]);
                portal.getCommands().add(enteredCommand.toString());
                String fixedCommand = enteredCommand.toString().replaceAll("(?i):PLAYER", "").replaceAll("(?i):CONSOLE", "");
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-command-added-message"))
                        .replace("{command}", fixedCommand).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", args[1])));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void clearCommands(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.clearcommands")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null) {
                portal.getCommands().clear();
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-commands-cleared-message"))
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void toggleCommandOnly(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.togglecommandonly")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null) {
                portal.setCommandsOnly(!portal.isCommandsOnly());
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-command-only-toggle-message"))
                        .replace("{status}", portal.isCommandsOnly() ? "Enabled" : "Disabled")
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateRelocate(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.relocate") || !player.hasPermission("simpleportals.rl")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Region region = pluginInstance.getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("selected-region-invalid-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null) {
                portal.setRegion(region);
                pluginInstance.getManager().clearCurrentSelection(player);
                portal.displayRegion(player);
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("region-relocated-message")).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiatePortalRegion(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.showregion") || !player.hasPermission("simpleportals.sr")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null) {
                portal.displayRegion(player);
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("region-displayed-message")).replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiatePortalLocationSet(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simpleportals.setlocation") || !player.hasPermission("simpleportals.sl")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalById(portalName);
            if (portal != null) {
                portal.setTeleportLocation(player.getLocation());
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("location-set-message"))
                        .replace("{name}", portal.getPortalId())));
            } else
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateInfo(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.info")) {
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

    private void initiateReload(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.reload")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        pluginInstance.reloadConfig();
        pluginInstance.reloadPortalsConfig();
        pluginInstance.getManager().savePortals();
        pluginInstance.getManager().loadPortals();
        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("reload-message")));
    }

    private void initiateList(CommandSender sender) {
        if (!sender.hasPermission("simpleportals.list")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        List<String> portalNames = new ArrayList<>();
        for (int i = -1; ++i < pluginInstance.getManager().getPortals().size(); )
            portalNames.add(pluginInstance.getManager().getPortals().get(i).getPortalId());
        sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + Objects.requireNonNull(pluginInstance.getConfig().getString("list-message"))
                .replace("{list}", portalNames.toString())));
    }

    private void initiateSwitchServerSet(CommandSender sender, String portalName, String serverName) {
        if (!sender.hasPermission("simpleportals.switchserver") || !sender.hasPermission("simpleportals.ss")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal != null) {
            portal.setServerSwitchName(serverName);
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("switch-server-set-message"))
                    .replace("{name}", portal.getPortalId()).replace("{server}", serverName)));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
    }

    private void initiatePortalDeletion(CommandSender sender, String portalName) {
        if (!sender.hasPermission("simpleportals.delete")) {
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + pluginInstance.getConfig().getString("no-permission-message")));
            return;
        }

        Portal portal = pluginInstance.getManager().getPortalById(portalName);
        if (portal != null) {
            if (sender instanceof Player) pluginInstance.getManager().clearAllVisuals((Player) sender);
            portal.delete();
            portal.unregister();
            sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-deleted-message"))
                    .replace("{name}", portal.getPortalId())));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-invalid-message")).replace("{name}", portalName)));
    }

    private void initiatePortalCreation(CommandSender sender, String portalName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.create")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            Portal portal = pluginInstance.getManager().getPortalAtLocation(player.getLocation());
            if (portal != null) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-location-exists-message")).replace("{name}", portal.getPortalId())));
                return;
            }

            if (pluginInstance.getManager().doesPortalExist(portalName)) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-exists-message")).replace("{name}", portalName)));
                return;
            }

            Region region = pluginInstance.getManager().getCurrentSelection(player);
            if (region == null || region.getPoint1() == null || region.getPoint2() == null) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("selected-region-invalid-message")));
                return;
            }

            if (!region.getPoint1().getWorldName().equalsIgnoreCase(region.getPoint2().getWorldName())) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("not-same-world-message")));
                return;
            }

            Portal newPortal = new Portal(pluginInstance, portalName, region);
            newPortal.register();
            newPortal.displayRegion(player);
            pluginInstance.getManager().clearCurrentSelection(player);
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("portal-created-message"))
                    .replace("{name}", newPortal.getPortalId())));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void initiateSelectionMode(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("simpleportals.selectionmode") || !player.hasPermission("simpleportals.sm")) {
                player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                        + pluginInstance.getConfig().getString("no-permission-message")));
                return;
            }

            pluginInstance.getManager().setSelectionMode(player, !pluginInstance.getManager().isInSelectionMode(player));
            player.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("selection-mode-message"))
                    .replace("{status}", pluginInstance.getManager().isInSelectionMode(player) ? "Enabled" : "Disabled")));
        } else sender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                + pluginInstance.getConfig().getString("must-be-player-message")));
    }

    private void setupHelpPageMap() {
        if (!getHelpPageMap().isEmpty()) getHelpPageMap().clear();
        List<String> page1Lines = new ArrayList<>(), page2Lines = new ArrayList<>(), page3Lines = new ArrayList<>();

        page1Lines.add("&e/portals <selectionmode/sm> &7- toggles selection mode.");
        page1Lines.add("&e/portals reload &7- reloads the configuration files.");
        page1Lines.add("&e/portals <switchserver/ss> <name> <server> &7- sets the server for the portal.");
        page1Lines.add("&e/portals <showregion/sr> <name> &7- shows the portal's current region.");
        page1Lines.add("&e/portals <setlocation/sl> <name> &7- sets the portal's teleport location.");
        page1Lines.add("&e/portals info &7- shows plugin information.");
        page1Lines.add("&e/portals create <name> &7- creates a new portal.");
        getHelpPageMap().put(1, page1Lines);

        page2Lines.add("&e/portals delete <name> &7- deletes the given portal.");
        page2Lines.add("&e/portals list &7- shows all available portals.");
        page2Lines.add("&e/portals fill <name> <material:durability> &7- replaces air inside the portals region.");
        page2Lines.add("&e/portals relocate <name> &7- relocates the portal to a selected region.");
        page2Lines.add("&e/portals <addcommand/addcmd> <name> <command> &7- adds the entered command line to the portal's command list.");
        page2Lines.add("&e/portals <clearcommands/clearcmds> <name> &7- clears all commands from the specified portal.");
        page2Lines.add("&e/portals <togglecommandonly/tco> <name> &7- toggles command only mode for a portal.");
        getHelpPageMap().put(2, page2Lines);

        page3Lines.add("&e/portals <commands/cmds> <name> &7- provides a list of all commands on the defined warp in the order they were added.");
        getHelpPageMap().put(3, page3Lines);
    }

    private void sendHelpPage(CommandSender commandSender, String pageString) {
        int page;
        try {
            page = Integer.parseInt(pageString);
        } catch (Exception ignored) {
            commandSender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("invalid-page-message")).replace("{pages}", String.valueOf(getHelpPageMap().size()))));
            return;
        }

        if (getHelpPageMap().isEmpty() || !getHelpPageMap().containsKey(page)) {
            commandSender.sendMessage(pluginInstance.getManager().colorText(pluginInstance.getConfig().getString("prefix")
                    + Objects.requireNonNull(pluginInstance.getConfig().getString("invalid-page-message")).replace("{pages}", String.valueOf(getHelpPageMap().size()))));
            return;
        }

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> pageLines = getHelpPageMap().get(page);

            player.sendMessage(pluginInstance.getManager().colorText("\n&e&m---------------&d[ &bSP Help &e(&a" + page + "&e) &d]&e&m---------------"));
            for (int i = -1; ++i < pageLines.size(); )
                player.sendMessage(pluginInstance.getManager().colorText(pageLines.get(i)));

            if (page < getHelpPageMap().size() && page > 1) {
                // page is both below the max page and above 1
                JSONMessage footerMessage1 = new JSONMessage("&e&m-------&r&d[");
                JSONExtra footerExtra1 = new JSONExtra(" &b(Previous Page)"),
                        footerExtra2 = new JSONExtra(" &b(Next Page) "),
                        footerExtra3 = new JSONExtra("&d]&e&m--------\n");

                footerExtra1.setClickEvent(JSONClickAction.RUN_COMMAND, "/portals help " + (page - 1));
                footerExtra1.setHoverEvent(JSONHoverAction.SHOW_TEXT, "&aClicking this will open the help menu at page &e" + (page - 1) + "&a.");
                footerExtra2.setClickEvent(JSONClickAction.RUN_COMMAND, "/portals help " + (page + 1));
                footerExtra2.setHoverEvent(JSONHoverAction.SHOW_TEXT, "&aClicking this will open the help menu at page &e" + (page + 1) + "&a.");

                footerMessage1.addExtra(footerExtra1);
                footerMessage1.addExtra(footerExtra2);
                footerMessage1.addExtra(footerExtra3);

                footerMessage1.sendJSONToPlayer(player);
            } else if (page < getHelpPageMap().size() && page <= 1) {
                // page is less than or = to 1
                JSONMessage footerMessage1 = new JSONMessage("&e&m---------------&r&d[");
                JSONExtra footerExtra1 = new JSONExtra(" &b(Next Page) "),
                        footerExtra2 = new JSONExtra("&d]&e&m---------------\n");

                footerExtra1.setClickEvent(JSONClickAction.RUN_COMMAND, "/portals help " + (page + 1));
                footerExtra1.setHoverEvent(JSONHoverAction.SHOW_TEXT, "&aClicking this will open the help menu at page &e" + (page + 1) + "&a.");
                footerMessage1.addExtra(footerExtra1);
                footerMessage1.addExtra(footerExtra2);

                footerMessage1.sendJSONToPlayer(player);
            } else if (page >= getHelpPageMap().size() && page > 1) {
                // page at/above max page and greater that 1
                JSONMessage footerMessage1 = new JSONMessage("&d[&e&m------------&r&d]");
                JSONExtra footerExtra1 = new JSONExtra(" &b(Previous Page) "),
                        footerExtra2 = new JSONExtra("&d]&e&m-------------\n");

                footerExtra1.setClickEvent(JSONClickAction.RUN_COMMAND, "/portals help " + (page - 1));
                footerExtra1.setHoverEvent(JSONHoverAction.SHOW_TEXT, "&aClicking this will open the help menu at page &e" + (page - 1) + "&a.");
                footerMessage1.addExtra(footerExtra1);
                footerMessage1.addExtra(footerExtra2);

                footerMessage1.sendJSONToPlayer(player);
            } else
                player.sendMessage(pluginInstance.getManager().colorText("&d[&e&m---------------------------------------&r&d]\n"));
        } else {
            List<String> pageLines = getHelpPageMap().get(page);
            commandSender.sendMessage(pluginInstance.getManager().colorText("&d[&e&m-------------&r&d] &bSP Help &e(&a" + page + "&e) &d[&e&m-------------&r&d]"));
            for (int i = -1; ++i < pageLines.size(); )
                commandSender.sendMessage(pluginInstance.getManager().colorText(pageLines.get(i)));
            commandSender.sendMessage(pluginInstance.getManager().colorText("&d[&e&m---------------------------------------&r&d]\n"));
        }
    }

    private HashMap<Integer, List<String>> getHelpPageMap() {
        return helpPageMap;
    }

    private void setHelpPageMap(HashMap<Integer, List<String>> helpPageMap) {
        this.helpPageMap = helpPageMap;
    }
}
