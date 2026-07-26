package org.dimasik.playtimeapi.api;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.dimasik.playtimeapi.Main;
import org.dimasik.playtimeapi.models.PlayerData;

public class PlayTimeAPI {
   private final Main plugin = Main.getInstance();

   public PlayTimeAPI() {
   }

   public long getTotalPlayTime(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getTotalPlayTime() : 0L;
   }

   public long getTotalActiveTime(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getTotalActiveTime() : 0L;
   }

   public long getSessionPlayTime(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getSessionPlayTime() : 0L;
   }

   public long getSessionActiveTime(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getSessionActiveTime() : 0L;
   }

   public long getIdleTime(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getIdleTime() : 0L;
   }

   public long getLastActivityTime(UUID uuid) {
      return this.plugin.getLastActivity(uuid);
   }

   public String getCurrentServer(UUID uuid) {
      PlayerData data = this.plugin.getPlayerData(uuid);
      return data != null ? data.getCurrentServer() : "Оффлайн";
   }

   public void updatePlayerActivity(Player player) {
      UUID uuid = player.getUniqueId();
      PlayerData data = this.plugin.getPlayerData(uuid);
      if (data != null) {
         data.setLastActivity(System.currentTimeMillis());
         this.plugin.getDatabaseManager().savePlayerDataAsync(data);
      }
   }
}
