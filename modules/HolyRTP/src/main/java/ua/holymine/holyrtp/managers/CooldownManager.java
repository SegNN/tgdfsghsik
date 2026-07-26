package ua.holymine.holyrtp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CooldownManager {
   private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

   public CooldownManager() {
   }

   public void setCooldown(Player player, String type, int seconds) {
      UUID playerId = player.getUniqueId();
      this.cooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
      this.cooldowns.get(playerId).put(type, System.currentTimeMillis() + seconds * 1000L);
   }

   public boolean hasCooldown(Player player, String type) {
      UUID playerId = player.getUniqueId();
      if (this.cooldowns.containsKey(playerId) && this.cooldowns.get(playerId).containsKey(type)) {
         long expireTime = this.cooldowns.get(playerId).get(type);
         if (System.currentTimeMillis() >= expireTime) {
            this.cooldowns.get(playerId).remove(type);
            if (this.cooldowns.get(playerId).isEmpty()) {
               this.cooldowns.remove(playerId);
            }

            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public long getRemainingCooldown(Player player, String type) {
      UUID playerId = player.getUniqueId();
      if (this.cooldowns.containsKey(playerId) && this.cooldowns.get(playerId).containsKey(type)) {
         long expireTime = this.cooldowns.get(playerId).get(type);
         long remaining = expireTime - System.currentTimeMillis();
         return Math.max(0L, remaining / 1000L);
      } else {
         return 0L;
      }
   }

   public void removeCooldown(Player player, String type) {
      UUID playerId = player.getUniqueId();
      if (this.cooldowns.containsKey(playerId)) {
         this.cooldowns.get(playerId).remove(type);
         if (this.cooldowns.get(playerId).isEmpty()) {
            this.cooldowns.remove(playerId);
         }
      }
   }
}
