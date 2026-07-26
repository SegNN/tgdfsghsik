package ru.kotikstasika.eventdeathmine.util;

import lombok.Generated;
import org.bukkit.ChatColor;

public final class ColorUtil {
   public static String colorize(String input) {
      return input == null ? "" : ChatColor.translateAlternateColorCodes('&', input);
   }

   @Generated
   private ColorUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
