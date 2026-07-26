package ru.kotikstasika.holyliteend.end.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.api.WorldEdit;
import ru.kotikstasika.holyliteend.api.WorldGuard;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;

public class EndEvent {
   private final HolyLiteEnd plugin;
   private static boolean isEventActive = false;
   private LocalDateTime eventStartTime;
   private LocalDateTime eventEndTime;
   private BossBar bossBar;
   private BukkitTask schedulerTask;
   private BukkitTask notificationTask;
   private final Set<Integer> notifiedIntervals = new HashSet<>();
   private final WorldEdit worldEdit;
   private final WorldGuard worldGuard;
   private static long lastNotificationSecond = Long.MIN_VALUE;
   private static long lastIntervalKey = Long.MIN_VALUE;

   public EndEvent(HolyLiteEnd plugin) {
      this.plugin = plugin;
      this.startScheduler();
      this.worldEdit = new WorldEdit();
      this.worldGuard = new WorldGuard();
   }

   private void startScheduler() {
      this.schedulerTask = (new BukkitRunnable() {
         public void run() {
            EndEvent.this.checkAndStartEvent();
         }
      }).runTaskTimer(this.plugin, 20L, 20L);
      this.startTimeNotifications();
   }

   private void startTimeNotifications() {
      this.notificationTask = (new BukkitRunnable() {
         public void run() {
            EndEvent.this.checkTimeNotifications();
         }
      }).runTaskTimer(this.plugin, 20L, 20L);
   }

   private void checkTimeNotifications() {
      if (isEventActive) {
         this.notifiedIntervals.clear();
      } else {
         DatabaseManager.EndSettings settings = this.plugin.getDatabaseManager().getEndSettings();
         String timezone = "Europe/Moscow";
         LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
         LocalDateTime nextEvent = this.getNextEventTimeFromSettings(settings);
         if (nextEvent != null) {
            long secondsUntilEvent = Duration.between(now, nextEvent).getSeconds();
            if (secondsUntilEvent <= 30L && secondsUntilEvent > 0L) {
               if ((secondsUntilEvent == 30L || secondsUntilEvent == 20L || secondsUntilEvent == 10L || secondsUntilEvent <= 9L)
                  && lastNotificationSecond != secondsUntilEvent) {
                  this.sendCountdownNotification(secondsUntilEvent);
                  lastNotificationSecond = secondsUntilEvent;
               }
            } else if (secondsUntilEvent == 0L) {
               this.notifiedIntervals.clear();
               this.startEvent();
            } else {
               for (Object intervalObj : this.plugin.getConfig().getList("end-event.notification-intervals")) {
                  double interval;
                  if (intervalObj instanceof Integer) {
                     interval = ((Integer)intervalObj).intValue() * 60.0;
                  } else {
                     if (!(intervalObj instanceof Double)) {
                        continue;
                     }

                     interval = (Double)intervalObj * 60.0;
                  }

                  int intervalMinutes = (int)Math.round(interval / 60.0);
                  if (Math.abs(secondsUntilEvent - interval) < 2.0) {
                     long intervalKey = (long)interval;
                     if (lastIntervalKey != intervalKey) {
                        this.sendTimeNotification(this.formatTimeInterval(intervalMinutes));
                        lastIntervalKey = intervalKey;
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   private String formatTimeInterval(int minutes) {
      if (minutes >= 60) {
         int hours = minutes / 60;
         if (hours == 1) {
            return "1 час";
         } else {
            return hours < 5 ? hours + " часа" : hours + " часов";
         }
      } else if (minutes == 1) {
         return "1 минута";
      } else {
         return minutes < 5 ? minutes + " минуты" : minutes + " минут";
      }
   }

   private void sendTimeNotification(String timeText) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
         String message = "§x§F§B§0§0§7§F▶ §x§F§F§F§F§F§FДо §x§F§B§0§0§7§F«§x§F§B§0§0§8§9З§x§F§B§0§0§9§4а§x§F§C§0§0§9§Eх§x§F§C§0§0§A§8в§x§F§C§0§0§B§2а§x§F§C§0§0§B§Dт§x§F§C§0§0§C§7а §x§F§C§0§0§D§1Э§x§F§D§0§0§D§Bн§x§F§D§0§0§E§6д§x§F§D§0§0§F§0а§x§F§D§0§0§F§A» §x§F§F§F§F§F§Fосталось §x§F§F§C§9§0§0"
            + timeText
            + "&f!";
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         message = "  &fРовно через &bстолько &fбудет открыт портал";
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         message = "  &fв Энд на спавне &b(/warp end) &fи начнется";
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         message = "  §x§0§0§0§0§0§0. §x§F§F§F§F§F§F §x§E§7§E§7§E§7большая битва с Эндер-драконом.";
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         message = "  §x§0§0§D§8§F§FСкорее приготовься!";
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
      }
   }

   private void sendCountdownNotification(long seconds) {
      for (Player player : this.plugin.getServer().getOnlinePlayers()) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&5&7&8&5&A&D▶ &fДо открытия энда осталось &x&5&7&8&5&A&D" + seconds + " секунд!"));
         if (seconds <= 10L) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
         }

         if (seconds == 1L) {
            player.sendMessage(
               ChatColor.translateAlternateColorCodes(
                  '&', "&x&0&0&D&5&F&x&f&b&0&0&0&0▶ &x&F&8&0&0&7&DЭндер-мир &fоткрыт! Телепорт: &x&0&0&D&5&F&x&f&b&0&0&0&0/warp end"
               )
            );
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0F, 1.0F);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holyliteend start");
         }
      }
   }

   private LocalDateTime getNextEventTimeFromSettings(DatabaseManager.EndSettings settings) {
      String timezone = "Europe/Moscow";
      LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
      LocalDateTime nextEvent = now;
      int targetDayOfWeek = settings.getDayOfWeek();
      int startHour = settings.getStartHour();
      int startMinute = settings.getStartMinute();
      DayOfWeek targetDay = DayOfWeek.of(targetDayOfWeek);
      if (now.getDayOfWeek() == targetDay) {
         LocalTime eventTime = LocalTime.of(startHour, startMinute);
         if (now.toLocalTime().isAfter(eventTime)) {
            nextEvent = now.plusWeeks(1L);
         }
      }

      while (nextEvent.getDayOfWeek() != targetDay) {
         nextEvent = nextEvent.plusDays(1L);
      }

      nextEvent = nextEvent.withHour(startHour).withMinute(startMinute).withSecond(0).withNano(0);
      if (nextEvent.isBefore(now)) {
         nextEvent = nextEvent.plusWeeks(1L);
      }

      return nextEvent;
   }

   private void checkAndStartEvent() {
      DatabaseManager.EndSettings settings = this.plugin.getDatabaseManager().getEndSettings();
      String timezone = "Europe/Moscow";
      LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
      int targetDayOfWeek = settings.getDayOfWeek();
      DayOfWeek targetDay = DayOfWeek.of(targetDayOfWeek);
      if (now.getDayOfWeek() == targetDay) {
         int startHour = settings.getStartHour();
         int startMinute = settings.getStartMinute();
         LocalTime eventTime = LocalTime.of(startHour, startMinute);
         if (now.toLocalTime().equals(eventTime) && !isEventActive) {
            if (startHour == 0 && startMinute == 0) {
               this.startEvent();
               return;
            }

            if (this.eventEndTime != null) {
               long minutesSinceLastEvent = Duration.between(this.eventEndTime, now).toMinutes();
               if (minutesSinceLastEvent < 5L) {
                  return;
               }
            }

            this.startEvent();
         }
      }

      if (isEventActive && this.eventEndTime != null && now.isAfter(this.eventEndTime)) {
         this.endEvent();
      }
   }

   public void startEvent() {
      if (!isEventActive) {
         this.worldEdit.loadAndPasteSchematic();
         this.worldGuard.createRegion(20);
         isEventActive = true;
         String timezone = "Europe/Moscow";
         this.eventStartTime = LocalDateTime.now(ZoneId.of(timezone));
         int duration = this.plugin.getDatabaseManager().getEndSettings().getDuration();
         this.eventEndTime = this.eventStartTime.plusMinutes(duration);
         this.notifiedIntervals.clear();
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            this.plugin.getEndBossBarManager().startEndBossBar();
            this.spawnNewDragon();
         });
      }
   }

   private void spawnNewDragon() {
      World endWorld = Bukkit.getWorld("world_the_end");

      for (EnderDragon dragon : new ArrayList<EnderDragon>(endWorld.getEntitiesByClass(EnderDragon.class))) {
         if (dragon != null && !dragon.isDead()) {
            dragon.remove();
         }
      }

      Location spawnLocation = new Location(endWorld, 0.0, 64.0, 0.0);
      EnderDragon newDragon = (EnderDragon)endWorld.spawnEntity(spawnLocation, EntityType.ENDER_DRAGON);

      try {
         newDragon.setPhase(Phase.LAND_ON_PORTAL);
      } catch (Throwable var6) {
      }

      this.plugin.getDragonManager().onDragonSpawn(newDragon);
   }

   public void endEvent() {
      if (isEventActive) {
         isEventActive = false;
         this.eventEndTime = null;
         this.eventStartTime = null;
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            World endWorld = Bukkit.getWorld("world_the_end");
            if (endWorld != null) {
               for (Player player : endWorld.getPlayers()) {
                  World spawnWorld = Bukkit.getWorld("spawn");
                  if (spawnWorld != null) {
                     player.teleport(new Location(spawnWorld, 0.0, 80.0, 0.0));
                  } else {
                     World defaultWorld = (World)Bukkit.getWorlds().get(0);
                     if (defaultWorld != null) {
                        player.teleport(defaultWorld.getSpawnLocation());
                     }
                  }
               }
            }

            try {
               this.worldGuard.removeRegion();
            } catch (Exception var7) {
               this.plugin.getLogger().warning("Ошибка при удалении региона WorldGuard: " + var7.getMessage());
            }
         });
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            this.plugin.getEndBossBarManager().stopEndBossBar();
            this.plugin.getDragonManager().onEventEnd();
         });
      }
   }

   public long getRemainingTime() {
      if (isEventActive && this.eventEndTime != null) {
         String timezone = "Europe/Moscow";
         LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
         long remainingSeconds = Duration.between(now, this.eventEndTime).getSeconds();
         return Math.max(0L, remainingSeconds);
      } else {
         return 0L;
      }
   }

   private void removeBossBar() {
      if (this.bossBar != null) {
         this.bossBar.removeAll();
         this.bossBar = null;
      }
   }

   private void deleteDirectory(File directory) {
      if (directory.exists()) {
         File[] files = directory.listFiles();
         if (files != null) {
            for (File file : files) {
               if (file.isDirectory()) {
                  this.deleteDirectory(file);
               } else {
                  file.delete();
               }
            }
         }

         directory.delete();
      }
   }

   public void extractEndWorldArchive() {
      try {
         Random random = new Random();
         List<String> archiveNames = this.plugin.getConfig().getStringList("world-restore.archive-name");
         if (archiveNames == null || archiveNames.isEmpty()) {
            this.plugin.getLogger().warning("Список архивов world-restore.archive-name пуст или не найден!");
            return;
         }

         int randomIndex = random.nextInt(archiveNames.size());
         String selectedArchiveName = archiveNames.get(randomIndex);
         String archivePath = this.plugin.getConfig().getString("world-restore.archive-path", ".");
         File archiveFile = new File(archivePath, selectedArchiveName + ".zip");
         if (!archiveFile.exists()) {
            this.plugin.getLogger().warning("Архив " + selectedArchiveName + ".zip не найден в папке " + archivePath + "!");
            return;
         }

         if (archiveFile.getName().endsWith(".zip")) {
            this.extractZipArchive(archiveFile);
         }
      } catch (Exception var7) {
         this.plugin.getLogger().severe("Ошибка при распаковке архива: " + var7.getMessage());
         var7.printStackTrace();
      }
   }

   private void extractZipArchive(File archiveFile) throws Exception {
      ZipFile zipFile = new ZipFile(archiveFile);

      try {
         Enumeration<? extends ZipEntry> entries = zipFile.entries();

         while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(entry.getName());
            if (entry.isDirectory()) {
               file.mkdirs();
            } else {
               File parent = file.getParentFile();
               if (parent != null) {
                  parent.mkdirs();
               }

               InputStream inputStream = zipFile.getInputStream(entry);

               try {
                  FileOutputStream outputStream = new FileOutputStream(file);

                  try {
                     byte[] buffer = new byte[1024];

                     int length;
                     while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                     }
                  } catch (Throwable var14) {
                     try {
                        outputStream.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }

                     throw var14;
                  }

                  outputStream.close();
               } catch (Throwable var15) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var12) {
                        var15.addSuppressed(var12);
                     }
                  }

                  throw var15;
               }

               if (inputStream != null) {
                  inputStream.close();
               }
            }
         }
      } catch (Throwable var16) {
         try {
            zipFile.close();
         } catch (Throwable var11) {
            var16.addSuppressed(var11);
         }

         throw var16;
      }

      zipFile.close();
   }

   public void shutdown() {
      if (this.schedulerTask != null) {
         this.schedulerTask.cancel();
         this.schedulerTask = null;
      }

      if (this.notificationTask != null) {
         this.notificationTask.cancel();
         this.notificationTask = null;
      }

      this.removeBossBar();
      isEventActive = false;
      this.eventStartTime = null;
      this.eventEndTime = null;
      this.notifiedIntervals.clear();
   }

   public void shutdownEvent() {
      if (isEventActive) {
         isEventActive = false;
         World endWorld = Bukkit.getWorld("world_the_end");
         if (endWorld != null) {
            for (Player player : endWorld.getPlayers()) {
               World spawnWorld = Bukkit.getWorld("spawn");
               if (spawnWorld != null) {
                  player.teleport(new Location(spawnWorld, 0.0, 80.0, 0.0));
               } else {
                  World defaultWorld = (World)Bukkit.getWorlds().get(0);
                  if (defaultWorld != null) {
                     player.teleport(defaultWorld.getSpawnLocation());
                  }
               }
            }
         }

         try {
            this.worldGuard.removeRegion();
         } catch (Exception var9) {
            this.plugin.getLogger().warning("Ошибка при удалении региона WorldGuard: " + var9.getMessage());
         }

         this.plugin.getEndBossBarManager().stopEndBossBar();
         this.plugin.getDragonManager().onEventEnd();
         if (endWorld != null) {
            try {
               Bukkit.unloadWorld(endWorld, false);
               this.plugin.getLogger().info("Мир world_the_end выгружен");
            } catch (Exception var8) {
               this.plugin.getLogger().warning("Ошибка при выгрузке мира Энда: " + var8.getMessage());
            }
         }

         try {
            File endWorldFolder = new File("world_the_end");
            if (endWorldFolder.exists()) {
               this.deleteDirectory(endWorldFolder);
               this.plugin.getLogger().info("Папка мира world_the_end удалена");
            }
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Ошибка при удалении папки мира Энда: " + var7.getMessage());
         }
      }
   }

   @Generated
   public static boolean isEventActive() {
      return isEventActive;
   }

   @Generated
   public LocalDateTime getEventStartTime() {
      return this.eventStartTime;
   }
}
