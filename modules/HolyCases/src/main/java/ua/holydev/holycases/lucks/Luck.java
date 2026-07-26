package ua.holydev.holycases.lucks;

import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.database.dao.PlayerLuckDao;

public class Luck {
   private final HolyCases plugin;
   private final String name;
   private final String history;
   private final String prizeMessage;

   public Luck(HolyCases plugin, String name, String history, String prizeMessage) {
      this.plugin = plugin;
      this.name = name;
      this.history = history;
      this.prizeMessage = prizeMessage;
   }

   public CompletableFuture<Integer> getLuckFor(OfflinePlayer player) {
      CompletableFuture<Integer> future = new CompletableFuture<>();
      if (this.plugin.isSQL()) {
         this.plugin.getExecutor().useExtension(PlayerLuckDao.class, daoLuck -> {
            int lucks = daoLuck.getLuck(player.getUniqueId(), this.name);
            future.complete(lucks);
         });
      } else {
         future.complete(this.plugin.getLucks(player, this.name));
      }

      return future;
   }

   public String getName() {
      return this.name;
   }

   public String getHistory() {
      return this.history;
   }

   public String getPrizeMessage() {
      return this.prizeMessage;
   }
}
