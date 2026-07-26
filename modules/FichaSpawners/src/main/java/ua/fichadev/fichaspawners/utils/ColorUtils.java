package ua.fichadev.fichaspawners.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

   public ColorUtils() {
   }

   public static String translate(String text) {
      Matcher matcher = HEX_PATTERN.matcher(text);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hexCode = matcher.group();
         ChatColor color = ChatColor.of(hexCode);
         matcher.appendReplacement(buffer, color.toString());
      }

      matcher.appendTail(buffer);
      return ChatColor.translateAlternateColorCodes('&', buffer.toString());
   }
}
