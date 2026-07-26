package ru.kotikstasika.kotikdang.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class ColorUtil {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   private ColorUtil() {
   }

   public static String colorize(String message) {
      if (message == null) {
         return "";
      } else {
         Matcher matcher = HEX_PATTERN.matcher(message);
         StringBuilder builder = new StringBuilder(message.length() + 32);

         while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder, ChatColor.of("#" + group).toString());
         }

         matcher.appendTail(builder);
         return ChatColor.translateAlternateColorCodes('&', builder.toString());
      }
   }

   public static String stripColor(String message) {
      return ChatColor.stripColor(colorize(message));
   }
}
