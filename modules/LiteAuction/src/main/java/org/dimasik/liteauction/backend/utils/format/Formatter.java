package org.dimasik.liteauction.backend.utils.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Generated;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;

public final class Formatter {
   public static final String CURRENCY_SYMBOL = ConfigManager.getString("settings/settings.yml", "currency-symbol", "¤");

   public static String formatPrice(int price) {
      if (price == 0) {
         return "0" + CURRENCY_SYMBOL;
      } else {
         String formatted = String.format("%,d", price).replace(",", " ");
         return formatted + CURRENCY_SYMBOL;
      }
   }

   public static String getTimeUntilExpiration(BidItem bidItem) {
      long remainingTime = bidItem.getExpiryTime() - System.currentTimeMillis();
      return formatTime(remainingTime);
   }

   public static String getTimeUntilExpiration(SellItem sellItem) {
      long expirationTime = sellItem.getCreateTime() + ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200L) * 1000L;
      long remainingTime = expirationTime - System.currentTimeMillis();
      return formatTime(remainingTime);
   }

   public static String getTimeUntilDeletion(UnsoldItem unsoldItem) {
      long deletionTime = unsoldItem.getCreateTime() + ConfigManager.getLong("settings/settings.yml", "lifetime.unsold", 604800L) * 1000L;
      long remainingTime = deletionTime - System.currentTimeMillis();
      return formatTime(remainingTime);
   }

   private static String formatTime(long remainingTime) {
      if (remainingTime <= 0L) {
         return "0сек.";
      } else {
         long seconds = remainingTime / 1000L;
         long days = seconds / 86400L;
         seconds %= 86400L;
         long hours = seconds / 3600L;
         seconds %= 3600L;
         long minutes = seconds / 60L;
         seconds %= 60L;
         StringBuilder result = new StringBuilder();
         if (days > 0L) {
            result.append(days).append("д. ");
         }

         if (hours > 0L) {
            result.append(hours).append("ч. ");
         }

         if (minutes > 0L) {
            result.append(minutes).append("мин. ");
         }

         if (seconds > 0L || result.isEmpty()) {
            result.append(seconds).append("сек.");
         }

         return result.toString().trim();
      }
   }

   public static String formatDateTime(long milliseconds) {
      Date date = new Date(milliseconds);
      SimpleDateFormat formatter = new SimpleDateFormat("dd.MM HH:mm");
      return formatter.format(date);
   }

   @Generated
   private Formatter() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
