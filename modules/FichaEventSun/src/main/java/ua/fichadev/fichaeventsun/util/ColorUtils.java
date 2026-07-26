package ua.fichadev.fichaeventsun.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   private ColorUtils() {
   }

   public static String colorize(String text) {
      if (text == null) {
         return "";
      } else {
         text = applyHex(text);
         return ChatColor.translateAlternateColorCodes('&', text);
      }
   }

   private static String applyHex(String text) {
      Matcher matcher = HEX_PATTERN.matcher(text);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hex = matcher.group(1);
         StringBuilder replacement = new StringBuilder("§x");

         for (char c : hex.toCharArray()) {
            replacement.append('§').append(c);
         }

         matcher.appendReplacement(buffer, replacement.toString());
      }

      matcher.appendTail(buffer);
      return buffer.toString();
   }
}
