package org.dimasik.playtimeapi.utils;

public class TimeFormatter {
   public TimeFormatter() {
   }

   public static String formatTime(long millis) {
      long seconds = millis / 1000L;
      long days = seconds / 86400L;
      seconds %= 86400L;
      long hours = seconds / 3600L;
      seconds %= 3600L;
      long minutes = seconds / 60L;
      seconds %= 60L;
      StringBuilder builder = new StringBuilder();
      if (days > 0L) {
         builder.append(days).append("д., ");
      }

      builder.append(hours).append("ч., ");
      builder.append(minutes).append("м., ");
      builder.append(seconds).append("с.");
      return builder.toString();
   }

   public static String formatTimeAgo(long timestamp) {
      return formatTime(System.currentTimeMillis() - timestamp);
   }
}
