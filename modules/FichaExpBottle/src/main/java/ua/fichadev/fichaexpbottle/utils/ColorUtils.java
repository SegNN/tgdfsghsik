package ua.fichadev.fichaexpbottle.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   public ColorUtils() {
   }

   public static String colorize(String message) {
      if (message == null) {
         return null;
      } else {
         Matcher matcher = HEX_PATTERN.matcher(message);
         StringBuffer buffer = new StringBuffer(message.length());

         while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + color).toString());
         }

         matcher.appendTail(buffer);
         return ChatColor.translateAlternateColorCodes('&', buffer.toString());
      }
   }
}
