package ua.fichamine.fichamineskills.placeholder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ua.fichamine.fichamineskills.bonus.BonusSystem;
import ua.fichamine.fichamineskills.database.DatabaseManager;
import ua.fichamine.fichamineskills.util.Logger;

public class PlaceholderManager extends PlaceholderExpansion {
   private final DatabaseManager databaseManager;
   private final ConcurrentHashMap<String, PlaceholderManager.CachedValue> placeholderCache;
   private static final long CACHE_DURATION = 30000L;

   public PlaceholderManager(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
      this.placeholderCache = new ConcurrentHashMap<>();
   }

   public String getIdentifier() {
      return "fichamineskills";
   }

   public String getAuthor() {
      return "Smail";
   }

   public String getVersion() {
      return "1.0";
   }

   public boolean persist() {
      return true;
   }

   public boolean register() {
      try {
         return super.register();
      } catch (Exception var2) {
         Logger.severe("Ошибка при регистрации PlaceholderAPI расширения: " + var2.getMessage());
         return false;
      }
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      if (player == null) {
         return "";
      } else {
         try {
            String var3 = identifier.toLowerCase();
            switch (var3) {
               case "points":
                  return this.getPlayerPoints(player);
               case "points_formatted":
                  return this.getPlayerPointsFormatted(player);
               case "rank":
                  return this.getPlayerRank(player);
               case "total_blocks":
                  return this.getPlayerTotalBlocks(player);
               case "next_bonus":
                  return this.getNextBonusInfo(player);
               case "active_bonuses":
                  return this.getActiveBonusesCount(player);
               default:
                  return null;
            }
         } catch (Exception var51) {
            Logger.severe(
               "Ошибка при обработке плейсхолдера " + identifier + " для " + (player != null ? player.getName() : "null") + ": " + var51.getMessage()
            );
            return "Ошибка";
         }
      }
   }

   private String getPlayerPoints(Player player) {
      String cacheKey = player.getUniqueId() + "_points";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         CompletableFuture pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());

         try {
            Double points = (Double)pointsFuture.get(5L, TimeUnit.SECONDS);
            String result = String.format("%.0f", points != null ? points : 0.0);
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var71) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить очки для " + var10000 + ": " + var71.getMessage());
            return "0";
         }
      }
   }

   private String getPlayerPointsFormatted(Player player) {
      String cacheKey = player.getUniqueId() + "_points_formatted";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         CompletableFuture pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());

         try {
            Double points = (Double)pointsFuture.get(5L, TimeUnit.SECONDS);
            double pointsValue = points != null ? points : 0.0;
            String result = this.formatPoints(pointsValue);
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var91) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить форматированные очки для " + var10000 + ": " + var91.getMessage());
            return "0";
         }
      }
   }

   private String getPlayerRank(Player player) {
      String cacheKey = player.getUniqueId() + "_rank";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         try {
            CompletableFuture<DatabaseManager.PlayerStats> topFuture = this.databaseManager.getTopPlayersAsync(1000);
            DatabaseManager.PlayerStats stats = topFuture.get(10L, TimeUnit.SECONDS);
            int rank = 1;

            for (DatabaseManager.PlayerData playerData : stats.getPlayers()) {
               if (playerData.getUuid().equals(player.getUniqueId())) {
                  break;
               }

               rank++;
            }

            String result = String.valueOf(rank);
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var91) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить ранг для " + var10000 + ": " + var91.getMessage());
            return "?";
         }
      }
   }

   private String getPlayerTotalBlocks(Player player) {
      String cacheKey = player.getUniqueId() + "_total_blocks";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         CompletableFuture blocksFuture = this.databaseManager.getTotalBlocksMinedAsync(player.getUniqueId());

         try {
            Long blocks = (Long)blocksFuture.get(5L, TimeUnit.SECONDS);
            String result = String.valueOf(blocks != null ? blocks : 0L);
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var71) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить количество блоков для " + var10000 + ": " + var71.getMessage());
            return "0";
         }
      }
   }

   private String getNextBonusInfo(Player player) {
      String cacheKey = player.getUniqueId() + "_next_bonus";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         try {
            CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
            Double points = pointsFuture.get(5L, TimeUnit.SECONDS);
            double currentPoints = points != null ? points : 0.0;
            String result = "Следующий бонус: " + (int)(currentPoints + 100.0) + " очков";
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var91) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить информацию о следующем бонусе для " + var10000 + ": " + var91.getMessage());
            return "Информация недоступна";
         }
      }
   }

   private String getActiveBonusesCount(Player player) {
      String cacheKey = player.getUniqueId() + "_active_bonuses";
      PlaceholderManager.CachedValue cached = this.placeholderCache.get(cacheKey);
      if (cached != null && !cached.isExpired()) {
         return cached.getValue();
      } else {
         try {
            CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
            Double points = pointsFuture.get(5L, TimeUnit.SECONDS);
            double currentPoints = points != null ? points : 0.0;
            List<String> effects = BonusSystem.getActiveEffects(currentPoints);
            int activeCount = effects != null ? effects.size() : 0;
            String result = String.valueOf(activeCount);
            this.placeholderCache.put(cacheKey, new PlaceholderManager.CachedValue(result, System.currentTimeMillis()));
            return result;
         } catch (Exception var111) {
            String var10000 = player.getName();
            Logger.warning("Не удалось получить количество активных бонусов для " + var10000 + ": " + var111.getMessage());
            return "0";
         }
      }
   }

   private String formatPoints(double points) {
      long roundedPoints = (long)points;
      if (roundedPoints < 1000L) {
         return String.valueOf(roundedPoints);
      } else if (roundedPoints < 1000000L) {
         double millions = points / 1000.0;
         return millions == (long)millions ? String.format("%dк", (long)millions) : String.format("%.1fк", millions).replace(",", ".");
      } else {
         double millions = points / 1000000.0;
         return millions == (long)millions ? String.format("%dкк", (long)millions) : String.format("%.1fкк", millions).replace(",", ".");
      }
   }

   private static class CachedValue {
      private final String value;
      private final long timestamp;

      public CachedValue(String value, long timestamp) {
         this.value = value;
         this.timestamp = timestamp;
      }

      public String getValue() {
         return this.value;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.timestamp > 30000L;
      }
   }
}
