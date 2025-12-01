/*
 * Copyright (c) XZot1K $year. All rights reserved.
 */

package xzot1k.plugins.sp.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xzot1k.plugins.sp.SimplePortals;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LangConfig {

    private static LangConfig instance;
    private static SimplePortals pluginInstance;

    public static void init(SimplePortals plugin) {
        pluginInstance = plugin;

        if (instance == null)
            instance = new LangConfig();
    }

    public static LangConfig get() {
        return instance;
    }

    private FileConfiguration config;
    private File file;

    // Cached messages
    private final Map<LangKey, String> messages = new EnumMap<>(LangKey.class);

    private LangConfig() { load(); }

    public void load() {
        try {
            if (file == null)
                file = new File(pluginInstance.getDataFolder(), "lang.yml");

            if (!file.exists())
                pluginInstance.saveResource("lang.yml", false);

            config = YamlConfiguration.loadConfiguration(file);

            messages.clear();
            for (LangKey key : LangKey.values()) {
                String raw = config.getString(key.getPath(), "");
                messages.put(key, colorText(raw));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(LangKey key) {
        return messages.getOrDefault(key, "");
    }

    public String get(LangKey key, Map<String, String> placeholders) {
        String msg = get(key);
        return parsePlaceholders(msg, placeholders);
    }

    public static String parsePlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public void reload() { load(); }

    /**
     * Colors the text passed.
     *
     * @param message The message to translate.
     * @return The colored text.
     */
    public static String colorText(String message) {
        String messageCopy = message;
        
        String version = pluginInstance.getServerVersion();
        
        if ((!version.startsWith("v1_15") && !version.startsWith("v1_14")
                && !version.startsWith("v1_13") && !version.startsWith("v1_12")
                && !version.startsWith("v1_11") && !version.startsWith("v1_10")
                && !version.startsWith("v1_9") && !version.startsWith("v1_8"))
                && messageCopy.contains("#")) {
            try {
                final Pattern hexPattern = Pattern.compile("\\{#([A-Fa-f\\d]){6}}");
                Matcher matcher = hexPattern.matcher(message);
                while (matcher.find()) {
                    final net.md_5.bungee.api.ChatColor hex = net.md_5.bungee.api.ChatColor.of(matcher.group().substring(1,
                            matcher.group().length() - 1));
                    final String pre = message.substring(0, matcher.start()), post = message.substring(matcher.end());
                    matcher = hexPattern.matcher(message = (pre + hex + post));
                }
            } catch (IllegalArgumentException ignored) {}
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
        }

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', messageCopy);
    }

    public FileConfiguration getLangConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }
}
