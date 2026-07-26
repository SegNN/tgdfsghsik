package ru.holydev.holycustom.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class HexUtil {
   public HexUtil() {
   }

   public static String translate(String message) {
      Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
      Matcher matcher = pattern.matcher(message);

      while (matcher.find()) {
         String color = matcher.group(1);
         String replacement = ChatColor.of("#" + color).toString();
         message = message.replace("&#" + color, replacement);
      }

      message = message.replace("\n", "\n");
      return ChatColor.translateAlternateColorCodes('&', message);
   }
}
