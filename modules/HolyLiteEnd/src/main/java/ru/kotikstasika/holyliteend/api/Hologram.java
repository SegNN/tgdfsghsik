package ru.kotikstasika.holyliteend.api;

import eu.decentsoftware.holograms.api.DHAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.utils.ColorUtils;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;

public class Hologram {
   private final HolyLiteEnd plugin;
   private final Map<String, eu.decentsoftware.holograms.api.holograms.Hologram> holograms = new HashMap<>();
   private BukkitTask updateTask;
   private BukkitTask retryTask;
   private Location timerLocation;
   private Location topLocation;
   private int pageIndex = 0;
   private boolean forward = true;
   private final int pageSize = 10;
   private String lastTimerContent = "";
   private String lastTopContent = "";
   private long lastTopUpdate = 0L;
   private static final long TOP_UPDATE_INTERVAL = 1000L;
   private boolean initialized = false;

   public Hologram(HolyLiteEnd plugin) {
      this.plugin = plugin;
      this.startInitRetry();
   }

   private void startInitRetry() {
      this.retryTask = (new BukkitRunnable() {
         private int attempts = 0;

         public void run() {
            this.attempts++;
            if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null && Bukkit.getPluginManager().getPlugin("DecentHolograms").isEnabled()) {
               if (!Hologram.this.loadLocations()) {
                  if (this.attempts > 60) {
                     Hologram.this.plugin.getLogger().severe("Миры для голограмм не загружены после 60 попыток");
                     this.cancel();
                  }
               } else {
                  this.cancel();
                  Hologram.this.retryTask = null;

                  try {
                     Hologram.this.removeExistingHolograms();
                  } catch (Exception var2) {
                  }

                  Hologram.this.initializeHolograms();
                  Hologram.this.startUpdateTask();
                  Hologram.this.initialized = true;
                  Hologram.this.plugin.getLogger().info("Голограммы успешно созданы (попытка " + this.attempts + ")");
               }
            } else {
               if (this.attempts > 60) {
                  Hologram.this.plugin.getLogger().severe("DecentHolograms не загрузился после 60 попыток, голограммы не будут созданы");
                  this.cancel();
               }
            }
         }
      }).runTaskTimer(this.plugin, 20L, 20L);
   }

   private boolean loadLocations() {
      if (this.plugin.getConfig().contains("holograms.timer-location")) {
         String worldName = this.plugin.getConfig().getString("holograms.timer-location.world", "world");
         World world = this.plugin.getServer().getWorld(worldName);
         if (world == null) {
            return false;
         }

         double x = this.plugin.getConfig().getDouble("holograms.timer-location.x", -76.5);
         double y = this.plugin.getConfig().getDouble("holograms.timer-location.y", 46.25);
         double z = this.plugin.getConfig().getDouble("holograms.timer-location.z", 102.5);
         this.timerLocation = new Location(world, x, y, z);
      }

      if (this.plugin.getConfig().contains("holograms.top-location")) {
         String worldName = this.plugin.getConfig().getString("holograms.top-location.world", "world");
         World world = this.plugin.getServer().getWorld(worldName);
         if (world == null) {
            return false;
         }

         double x = this.plugin.getConfig().getDouble("holograms.top-location.x", 11.5);
         double y = this.plugin.getConfig().getDouble("holograms.top-location.y", 51.3);
         double z = this.plugin.getConfig().getDouble("holograms.top-location.z", 142.5);
         this.topLocation = new Location(world, x, y, z);
      }

      return true;
   }

   private void removeExistingHolograms() {
      try {
         eu.decentsoftware.holograms.api.holograms.Hologram timerHologram = DHAPI.getHologram("timer");
         if (timerHologram != null) {
            timerHologram.delete();
         }
      } catch (Exception var3) {
      }

      try {
         eu.decentsoftware.holograms.api.holograms.Hologram topHologram = DHAPI.getHologram("top");
         if (topHologram != null) {
            topHologram.delete();
         }
      } catch (Exception var2) {
      }

      this.holograms.clear();
   }

   private void initializeHolograms() {
      try {
         if (this.timerLocation != null) {
            this.createHologram("timer", this.timerLocation, this.createTimerLines());
         } else {
            this.plugin.getLogger().warning("Локация таймера не найдена!");
         }

         if (this.topLocation != null) {
            this.createHologram("top", this.topLocation, this.createTopLines());
         } else {
            this.plugin.getLogger().warning("Локация топа не найдена!");
         }
      } catch (Exception var2) {
         this.plugin.getLogger().warning("Ошибка при инициализации голограмм: " + var2.getMessage());
         var2.printStackTrace();
      }
   }

   private void createHologram(String id, Location location, List<String> lines) {
      try {
         List<String> coloredLines = new ArrayList<>();

         for (String line : lines) {
            coloredLines.add(ColorUtils.translateColors(line));
         }

         eu.decentsoftware.holograms.api.holograms.Hologram hologram = DHAPI.createHologram(id, location, false, coloredLines);
         hologram.setDefaultVisibleState(true);
         hologram.enable();
         hologram.showAll();
         this.holograms.put(id, hologram);
      } catch (Exception var7) {
         this.plugin.getLogger().warning("Ошибка при создании голограммы " + id + ": " + var7.getMessage());
         var7.printStackTrace();
      }
   }

   private void startUpdateTask() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
      }

      this.updateTask = (new BukkitRunnable() {
         public void run() {
            Hologram.this.updateTimerHologram();
            Hologram.this.updateTopHologram();
         }
      }).runTaskTimer(this.plugin, 20L, 20L);
   }

   public void updateTimerHologram() {
      if (this.initialized && this.timerLocation != null) {
         List<String> lines = this.createTimerLines();
         String currentContent = String.join("", lines);
         if (!currentContent.equals(this.lastTimerContent)) {
            this.updateHologram("timer", this.timerLocation, lines);
            this.lastTimerContent = currentContent;
         }
      }
   }

   public void updateTopHologram() {
      if (this.initialized && this.topLocation != null) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastTopUpdate >= 1000L) {
            List<String> lines = this.createTopLines();
            String currentContent = String.join("", lines);
            if (!currentContent.equals(this.lastTopContent)) {
               this.updateHologram("top", this.topLocation, lines);
               this.lastTopContent = currentContent;
            }

            this.lastTopUpdate = currentTime;
         }
      }
   }

   private List<String> createTimerLines() {
      List<String> lines = new ArrayList<>();
      lines.add(
         "&fДо §x§F§B§0§0§7§F«§x§F§B§0§0§8§9З§x§F§B§0§0§9§4а§x§F§C§0§0§9§Eх§x§F§C§0§0§A§8в§x§F§C§0§0§B§2а§x§F§C§0§0§B§Dт§x§F§C§0§0§C§7а §x§F§C§0§0§D§1Э§x§F§D§0§0§D§Bн§x§F§D§0§0§E§6д§x§F§D§0§0§F§0а§x§F§D§0§0§F§A»"
      );
      lines.add("&fосталось:");
      lines.add("&x&D&F&B&0&0&0%holyliteend_time%");
      lines.add("");
      lines.add("&fОткрытие Энд-мира каждое");
      lines.add("&bвоскресенье в 16:00 &fпо МСК");
      lines.add("&fПодробнее об событие - &b/help");
      return lines;
   }

   private List<String> createTopLines() {
      List<String> lines = new ArrayList<>();
      String defaultServer = this.plugin.getConfig().getString("server-number", "server");
      String serverNumber0 = this.plugin.getConfig().getString("server-number0", "Сервер");
      lines.add("&fТоп по &#F62987Я&#F62690й&#F72398ц&#F720A1а&#F71CA9м &#F816BBД&#F813C3р&#F810CCа&#F90DD5к&#F909DDо&#F906E6н&#FA03EEа&#FA00F7:");
      List<DatabaseManager.DragonEggData> eggs = this.plugin.getDatabaseManager().getGroupedDragonEggs();
      int total = eggs.size();
      int windows = Math.max(1, (total + 10 - 1) / 10);
      if (this.pageIndex >= windows) {
         this.pageIndex = 0;
      }

      int effectiveWindow = this.forward ? this.pageIndex : windows - 1 - this.pageIndex;
      int start = effectiveWindow * 10;
      int endExclusive = Math.min(start + 10, total);
      if (this.forward) {
         for (int idx = start; idx < start + 10; idx++) {
            if (idx >= endExclusive) {
               lines.add(String.format("&#F62987#%d &7&l┆ &#F62987%s - - - (0) &7шт", idx + 1, serverNumber0));
            } else {
               DatabaseManager.DragonEggData egg = eggs.get(idx);
               String server = egg.getServer() != null && !egg.getServer().isEmpty() ? egg.getServer() : defaultServer;
               String line = String.format(
                  "&#F62987#%d &7&l┆ &#F62987%s x: %d, y: %d, z: %d (%d) &7шт", idx + 1, server, egg.getX(), egg.getY(), egg.getZ(), egg.getEggCount()
               );
               lines.add(line);
            }
         }
      } else {
         int displayCount = 10;
         int lastIdx = endExclusive - 1;

         for (int k = 0; k < displayCount; k++) {
            int idxx = lastIdx - k;
            if (idxx >= start && idxx >= 0 && idxx < total) {
               DatabaseManager.DragonEggData egg = eggs.get(idxx);
               String server = egg.getServer() != null && !egg.getServer().isEmpty() ? egg.getServer() : defaultServer;
               String line = String.format(
                  "&#F62987#%d &7&l┆ &#F62987%s x: %d, y: %d, z: %d (%d) &7шт", idxx + 1, server, egg.getX(), egg.getY(), egg.getZ(), egg.getEggCount()
               );
               lines.add(line);
            } else {
               lines.add(String.format("&#F62987#%d &7&l┆ &#F62987%s - - - (0) &7шт", idxx >= 0 ? idxx + 1 : 0, serverNumber0));
            }
         }
      }

      lines.add("&f*&fВ данном топе содержатся");
      lines.add("&fкоординаты сундуков с");
      lines.add("&#F62987Яйцами Дракона в обычном мире");
      return lines;
   }

   private void updateHologram(String id, Location location, List<String> lines) {
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
         try {
            List<String> coloredLines = new ArrayList<>();

            for (String line : lines) {
               coloredLines.add(ColorUtils.translateColors(line));
            }

            eu.decentsoftware.holograms.api.holograms.Hologram existingHologram = DHAPI.getHologram(id);
            if (existingHologram != null) {
               existingHologram.setLocation(location);
               DHAPI.setHologramLines(existingHologram, coloredLines);
               if (!existingHologram.isEnabled()) {
                  existingHologram.enable();
                  existingHologram.showAll();
               }

               this.holograms.put(id, existingHologram);
            } else {
               eu.decentsoftware.holograms.api.holograms.Hologram hologram = DHAPI.createHologram(id, location, false, coloredLines);
               hologram.setDefaultVisibleState(true);
               hologram.enable();
               hologram.showAll();
               this.holograms.put(id, hologram);
            }
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Ошибка при обновлении голограммы " + id + ": " + var7.getMessage());
            var7.printStackTrace();
         }
      });
   }

   public void shutdown() {
      if (this.retryTask != null) {
         this.retryTask.cancel();
         this.retryTask = null;
      }

      if (this.updateTask != null) {
         this.updateTask.cancel();
      }

      try {
         eu.decentsoftware.holograms.api.holograms.Hologram timerHologram = DHAPI.getHologram("timer");
         if (timerHologram != null) {
            timerHologram.delete();
         }

         eu.decentsoftware.holograms.api.holograms.Hologram topHologram = DHAPI.getHologram("top");
         if (topHologram != null) {
            topHologram.delete();
         }

         this.holograms.clear();
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Ошибка при удалении голограмм: " + var3.getMessage());
      }
   }
}
