package ua.holymine.holymineskills.util;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
   private static final String PREFIX = "&x&0&0&F&1&F&D[&x&0&0&F&1&F&DHolyMineSkills&x&0&0&F&1&F&D] &r";

   public Logger() {
   }

   public static void warning(String message) {
      log(Level.WARNING, message);
   }

   public static void severe(String message) {
      log(Level.SEVERE, message);
   }

   private static void log(Level level, String message) {
      String formattedMessage = ChatColor.translateAlternateColorCodes('&', "&x&0&0&F&1&F&D[&x&0&0&F&1&F&DHolyMineSkills&x&0&0&F&1&F&D] &r" + message);
      if (level == Level.SEVERE) {
         Bukkit.getLogger().severe(ChatColor.stripColor(formattedMessage));
      } else if (level == Level.WARNING) {
         Bukkit.getLogger().warning(ChatColor.stripColor(formattedMessage));
      } else {
         Bukkit.getLogger().info(ChatColor.stripColor(formattedMessage));
      }
   }
}
