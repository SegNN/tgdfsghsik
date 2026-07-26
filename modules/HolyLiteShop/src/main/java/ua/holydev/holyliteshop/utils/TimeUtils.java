package ua.holydev.holyliteshop.utils;

public class TimeUtils {
   public TimeUtils() {
   }

   public static String formatTime(long milliseconds) {
      long seconds = milliseconds / 1000L;
      long minutes = seconds / 60L;
      long hours = minutes / 60L;
      seconds %= 60L;
      minutes %= 60L;
      if (hours > 0L) {
         return hours + " ч. " + minutes + " мин. " + seconds + " сек.";
      } else {
         return minutes > 0L ? minutes + " мин. " + seconds + " сек." : seconds + " сек.";
      }
   }

   public static String formatNumber(int number) {
      return String.format("%,d", number).replace(',', ' ');
   }
}
