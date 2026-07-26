package ua.holydev.holycases.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
   public ColorUtil() {
   }

   public static String parse(String message) {
      if (message == null) {
         return null;
      } else {
         Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

         for (Matcher matcher = pattern.matcher(message); matcher.find(); matcher = pattern.matcher(message)) {
            String color = message.substring(matcher.start() + 1, matcher.end());
            message = message.replace("&" + color, ChatColor.of(color) + "");
         }

         return ChatColor.translateAlternateColorCodes('&', message);
      }
   }
}
