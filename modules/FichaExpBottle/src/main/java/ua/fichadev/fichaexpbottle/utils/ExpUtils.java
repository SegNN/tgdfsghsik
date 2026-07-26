package ua.fichadev.fichaexpbottle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExpUtils {
   public ExpUtils() {
   }

   public static List<Entry<String, Integer>> getTopPlayers(int limit) {
      Map<String, Integer> playerExp = new HashMap<>();

      for (Player player : Bukkit.getOnlinePlayers()) {
         playerExp.put(player.getName(), getTotalExperience(player));
      }

      List<Entry<String, Integer>> sortedList = new ArrayList<>(playerExp.entrySet());
      sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
      return sortedList.size() > limit ? sortedList.subList(0, limit) : sortedList;
   }

   public static int getTotalExperience(Player player) {
      int level = player.getLevel();
      return Math.round(getExpAtLevel(level) + player.getExp() * getExpToLevelUp(level));
   }

   public static void setTotalExperience(Player player, int exp) {
      player.setExp(0.0F);
      player.setLevel(0);
      player.setTotalExperience(0);
      if (exp > 0) {
         int level = getLevelAtExp(exp);
         player.setLevel(level);
         int remainingExp = exp - getExpAtLevel(level);
         float progress = (float)remainingExp / getExpToLevelUp(level);
         player.setExp(Math.max(0.0F, Math.min(progress, 0.99999F)));
      }
   }

   public static void addExperience(Player player, int exp) {
      int currentExp = getTotalExperience(player);
      setTotalExperience(player, currentExp + exp);
   }

   private static int getExpAtLevel(int level) {
      if (level <= 16) {
         return level * level + 6 * level;
      } else {
         return level <= 31 ? (int)(2.5 * level * level - 40.5 * level + 360.0) : (int)(4.5 * level * level - 162.5 * level + 2220.0);
      }
   }

   private static int getLevelAtExp(int exp) {
      if (exp <= 352) {
         return (int)Math.floor((-6.0 + Math.sqrt(36 + 4 * exp)) / 2.0);
      } else {
         return exp <= 1507
            ? (int)Math.floor(8.1 + Math.sqrt(0.4 * (exp - 360 + 164.025)))
            : (int)Math.floor(18.055555555555557 + Math.sqrt(0.2222222222222222 * (exp - 2220 + 1467.013888888889)));
      }
   }

   private static int getExpToLevelUp(int level) {
      if (level <= 15) {
         return 2 * level + 7;
      } else {
         return level <= 30 ? 5 * level - 38 : 9 * level - 158;
      }
   }
}
