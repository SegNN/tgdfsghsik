package ua.fichadev.fichasalary.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColorUtils {
   public ColorUtils() {
   }

   public static void sendMessage(Player player, String message) {
      if (message != null && !message.isEmpty()) {
         player.sendMessage(hexColor(message));
      }
   }

   public static String hexColor(String message) {
      Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
      Matcher matcher = pattern.matcher(message);
      StringBuffer buffer = new StringBuffer();

      while (matcher.find()) {
         String hex = matcher.group(1);
         StringBuilder magic = new StringBuilder("§x");

         for (char c : hex.toCharArray()) {
            magic.append('§').append(c);
         }

         matcher.appendReplacement(buffer, magic.toString());
      }

      matcher.appendTail(buffer);
      return ChatColor.translateAlternateColorCodes('&', buffer.toString());
   }

   public static String formatNumber(int number) {
      return String.format("%,d", number).replace(',', ' ');
   }

   public static String formatTime(long seconds) {
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long secs = seconds % 60L;
      if (hours > 0L) {
         return hours + "ч " + minutes + "м";
      } else {
         return minutes > 0L ? minutes + "м " + secs + "с" : secs + "с";
      }
   }
}
