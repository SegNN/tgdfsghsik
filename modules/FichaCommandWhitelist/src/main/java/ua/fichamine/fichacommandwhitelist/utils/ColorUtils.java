package ua.fichamine.fichacommandwhitelist.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   public ColorUtils() {
   }

   public static String translateHexColor(String hexColor) {
      if (hexColor != null && hexColor.startsWith("#") && hexColor.length() == 7) {
         String red = hexColor.substring(1, 3);
         String green = hexColor.substring(3, 5);
         String blue = hexColor.substring(5, 7);
         return "§x§" + red.charAt(0) + "§" + red.charAt(1) + "§" + green.charAt(0) + "§" + green.charAt(1) + "§" + blue.charAt(0) + "§" + blue.charAt(1);
      } else {
         return "";
      }
   }

   public static String translateColors(String text) {
      if (text == null) {
         return "";
      } else {
         Matcher matcher = HEX_PATTERN.matcher(text);
         StringBuffer result = new StringBuffer();

         while (matcher.find()) {
            String hexColor = "#" + matcher.group(1);
            String minecraftColor = translateHexColor(hexColor);
            matcher.appendReplacement(result, minecraftColor);
         }

         matcher.appendTail(result);
         return ChatColor.translateAlternateColorCodes('&', result.toString());
      }
   }
}
