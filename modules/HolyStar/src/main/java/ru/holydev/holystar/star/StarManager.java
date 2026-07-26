package ru.holydev.holystar.star;

import org.bukkit.entity.Player;
import ru.holydev.holystar.manager.DatabaseManager;

public class StarManager {
   private final DatabaseManager databaseManager;

   public StarManager(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
   }

   public void add(Player player, int count) {
      if (count > 0) {
         int current = this.get(player);
         this.databaseManager.setValue(player.getName(), current + count);
      }
   }

   public void remove(Player player, int count) {
      int current = this.get(player);
      if (current >= count) {
         this.databaseManager.setValue(player.getName(), current - count);
      }
   }

   public int get(Player player) {
      try {
         return this.databaseManager.getValue(player.getName()).get();
      } catch (Exception var3) {
         return 0;
      }
   }

   public int get(String playerName) {
      try {
         return this.databaseManager.getValue(playerName).get();
      } catch (Exception var3) {
         return 0;
      }
   }
}
