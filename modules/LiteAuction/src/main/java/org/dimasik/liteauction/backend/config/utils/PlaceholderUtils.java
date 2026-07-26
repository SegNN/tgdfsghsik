package org.dimasik.liteauction.backend.config.utils;

import lombok.Generated;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.utils.format.Parser;

public final class PlaceholderUtils {
   @SafeVarargs
   public static String replace(String origin, boolean applyParser, Pair<String, String>... replaces) {
      for (Pair<String, String> replace : replaces) {
         while (origin.contains(replace.getLeft())) {
            origin = origin.replace(replace.getLeft(), replace.getRight());
         }
      }

      if (applyParser) {
         origin = Parser.color(origin);
      }

      return origin;
   }

   @SafeVarargs
   public static String replace(Player source, String origin, boolean applyParser, Pair<String, String>... replaces) {
      for (Pair<String, String> replace : replaces) {
         while (origin.contains(replace.getLeft())) {
            origin = origin.replace(replace.getLeft(), replace.getRight());
         }
      }

      if (applyParser) {
         origin = Parser.color(origin);
      }

      return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders(source, origin) : origin;
   }

   @Generated
   private PlaceholderUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
