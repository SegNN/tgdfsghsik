package ua.holydev.holyautocraft.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   public ColorUtils() {
   }

   public static String apply(String text) {
      if (text == null) {
         return "";
      } else {
         Matcher matcher = HEX_PATTERN.matcher(text);
         StringBuilder builder = new StringBuilder();

         while (matcher.find()) {
            String hex = matcher.group(1);
            String color = ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(builder, color);
         }

         matcher.appendTail(builder);
         return ChatColor.translateAlternateColorCodes('&', builder.toString());
      }
   }
}
