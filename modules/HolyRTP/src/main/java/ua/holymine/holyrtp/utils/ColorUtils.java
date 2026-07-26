package ua.holymine.holyrtp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Pattern NEX_PATTERN = Pattern.compile("&x&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])");
   private static final Pattern SHORT_HEX_PATTERN = Pattern.compile("&([A-Fa-f0-9]{6})");

   public ColorUtils() {
   }

   public static String colorize(String message) {
      if (message == null) {
         return "";
      } else {
         message = ChatColor.translateAlternateColorCodes('&', message);

         for (Matcher hexMatcher = HEX_PATTERN.matcher(message); hexMatcher.find(); hexMatcher = HEX_PATTERN.matcher(message)) {
            String color = hexMatcher.group(1);
            message = hexMatcher.replaceFirst(ChatColor.of("#" + color).toString());
         }

         for (Matcher nexMatcher = NEX_PATTERN.matcher(message); nexMatcher.find(); nexMatcher = NEX_PATTERN.matcher(message)) {
            String color = nexMatcher.group(1) + nexMatcher.group(2) + nexMatcher.group(3) + nexMatcher.group(4) + nexMatcher.group(5) + nexMatcher.group(6);
            message = nexMatcher.replaceFirst(ChatColor.of("#" + color).toString());
         }

         for (Matcher shortHexMatcher = SHORT_HEX_PATTERN.matcher(message); shortHexMatcher.find(); shortHexMatcher = SHORT_HEX_PATTERN.matcher(message)) {
            String color = shortHexMatcher.group(1);
            message = shortHexMatcher.replaceFirst(ChatColor.of("#" + color).toString());
         }

         return message;
      }
   }
}
