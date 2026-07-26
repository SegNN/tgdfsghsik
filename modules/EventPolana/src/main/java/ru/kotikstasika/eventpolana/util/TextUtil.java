package ru.kotikstasika.eventpolana.util;

import org.bukkit.ChatColor;

public class TextUtil {
   public TextUtil() {
   }

   public static String colorize(String message) {
      return ChatColor.translateAlternateColorCodes('&', message);
   }
}
