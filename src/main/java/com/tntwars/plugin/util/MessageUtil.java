package com.tntwars.plugin.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static String prefix = "&8[&cTNT&fWars&8] &r";

    private MessageUtil() {
    }

    public static void setPrefix(String rawPrefix) {
        prefix = rawPrefix;
    }

    public static String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(prefix + message));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }
}
