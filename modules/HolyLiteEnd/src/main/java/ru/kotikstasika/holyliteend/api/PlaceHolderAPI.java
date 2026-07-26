package ru.kotikstasika.holyliteend.api;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.managers.EndEvent;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;

public class PlaceHolderAPI extends PlaceholderExpansion {
   private final HolyLiteEnd plugin;

   public PlaceHolderAPI(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public String getIdentifier() {
      return "holyliteend";
   }

   public String getAuthor() {
      return "kotikStasika";
   }

   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      return identifier.equals("time") ? this.getTimeUntilEvent() : null;
   }

   private String getTimeUntilEvent() {
      if (EndEvent.isEventActive()) {
         return ChatColor.translateAlternateColorCodes('&', "&x&F&F&C&4&0&0✓ Событие уже идет!");
      } else {
         String timezone = "Europe/Moscow";
         LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
         LocalDateTime nextEvent = this.getNextEventTimeFromDb();
         if (nextEvent == null) {
            return "неизвестно";
         } else {
            long secondsUntilEvent = Duration.between(now, nextEvent).getSeconds();
            if (secondsUntilEvent <= 0L) {
               return ChatColor.translateAlternateColorCodes('&', "&x&F&F&C&4&0&0✓ Событие уже идет!");
            } else {
               long days = secondsUntilEvent / 86400L;
               long hours = secondsUntilEvent % 86400L / 3600L;
               long minutes = secondsUntilEvent % 3600L / 60L;
               long seconds = secondsUntilEvent % 60L;
               StringBuilder result = new StringBuilder();
               result.append(days).append(" д. ");
               result.append(hours).append(" ч. ");
               result.append(minutes).append(" м. ");
               result.append(seconds).append(" с.");
               return result.toString().trim();
            }
         }
      }
   }

   private LocalDateTime getNextEventTimeFromDb() {
      String timezone = "Europe/Moscow";
      LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
      LocalDateTime nextEvent = now;
      DatabaseManager.EndSettings settings = this.plugin.getDatabaseManager().getEndSettings();
      int targetDayOfWeek = settings.getDayOfWeek();
      int startHour = settings.getStartHour();
      int startMinute = settings.getStartMinute();
      DayOfWeek targetDay = DayOfWeek.of(targetDayOfWeek);

      while (nextEvent.getDayOfWeek() != targetDay) {
         nextEvent = nextEvent.plusDays(1L);
      }

      nextEvent = nextEvent.withHour(startHour).withMinute(startMinute).withSecond(0).withNano(0);
      if (nextEvent.isBefore(now)) {
         nextEvent = nextEvent.plusWeeks(1L);
      }

      return nextEvent;
   }
}
