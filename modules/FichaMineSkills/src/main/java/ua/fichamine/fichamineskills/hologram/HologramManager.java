package ua.fichamine.fichamineskills.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichamineskills.config.ConfigManager;
import ua.fichamine.fichamineskills.database.DatabaseManager;
import ua.fichamine.fichamineskills.util.Logger;

public class HologramManager {
   private final JavaPlugin plugin;
   private final DatabaseManager databaseManager;
   private final ConfigManager configManager;
   private final String hologramName = "points_top";
   private Hologram hologram;
   private int updateTaskId = -1;
   private int updateInterval = 60;

   public HologramManager(JavaPlugin plugin, DatabaseManager databaseManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
      this.configManager = configManager;
   }

   public void createHologram() {
      try {
         if (Bukkit.getPluginManager().getPlugin("DecentHolograms") == null) {
            Logger.warning("DecentHolograms не найден, голограмма не будет создана");
            return;
         }

         Location hologramLocation = this.createHologramLocation();
         if (hologramLocation == null) {
            Logger.severe("Не удалось создать локацию для голограммы");
            return;
         }

         this.deleteExistingHologram();
         this.hologram = DHAPI.createHologram("points_top", hologramLocation);
         if (this.hologram == null) {
            Logger.severe("Не удалось создать голограмму");
            return;
         }

         this.hologram.setDisplayRange(48);
         this.startUpdateTask();
      } catch (Exception var21) {
         Logger.severe("Ошибка при создании голограммы: " + var21.getMessage());
         var21.printStackTrace();
      }
   }

   private Location createHologramLocation() {
      try {
         double x = this.configManager.getHologramX();
         double y = this.configManager.getHologramY();
         double z = this.configManager.getHologramZ();
         World world = Bukkit.getWorld("spawn");
         if (world == null) {
            world = (World)Bukkit.getWorlds().get(0);
            if (world == null) {
               Logger.severe("Не найдено ни одного мира!");
               return null;
            }
         }

         return new Location(world, x, y, z);
      } catch (Exception var81) {
         Logger.severe("Ошибка при создании локации голограммы: " + var81.getMessage());
         return null;
      }
   }

   private void deleteExistingHologram() {
      try {
         Hologram existingHologram = DHAPI.getHologram("points_top");
         if (existingHologram != null) {
            existingHologram.delete();
         }
      } catch (Exception var21) {
         Logger.warning("Ошибка при удалении существующей голограммы: " + var21.getMessage());
      }
   }

   private void startUpdateTask() {
      try {
         if (this.updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
         }

         this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateHologram, 60L, this.updateInterval * 20L);
      } catch (Exception var2) {
         Logger.severe("Ошибка при запуске задачи обновления голограммы: " + var2.getMessage());
      }
   }

   private void updateHologram() {
      try {
         if (this.hologram == null) {
            return;
         }

         CompletableFuture<DatabaseManager.PlayerStats> topFuture = this.databaseManager.getTopPlayersAsync(10);
         topFuture.thenAccept(
               topStats -> {
                  try {
                     List<String> lines = new ArrayList<>();
                     lines.add("&fТоп по заработанным");
                     lines.add("&fочкам квестовика (Автошахта) - &x&0&0&f&1&f&d/mineskills");
                     int rank = 1;
                     if (topStats != null && !topStats.getPlayers().isEmpty()) {
                        for (DatabaseManager.PlayerData playerData : topStats.getPlayers()) {
                           if (rank > 10) {
                              break;
                           }

                           String line = String.format(
                              "&x&0&0&f&1&f&d#%d &7&l┆&x&8&a&f&7&f&d %s &f%s", rank, playerData.getUsername(), this.formatPoints(playerData.getPoints())
                           );
                           lines.add(line);
                           rank++;
                        }
                     }

                     while (rank <= 10) {
                        String linex = String.format("&x&0&0&f&1&f&d#%d &7&l┆&x&8&a&f&7&f&d --- &f---", rank);
                        lines.add(linex);
                        rank++;
                     }

                     lines.add("&fОбновление раз в &x&0&0&f&1&f&d~1 минуту");
                     Bukkit.getScheduler().runTask(this.plugin, () -> {
                        try {
                           if (this.hologram != null) {
                              DHAPI.setHologramLines(this.hologram, lines);
                           }
                        } catch (Exception var3x) {
                           Logger.severe("Ошибка при обновлении голограммы: " + var3x.getMessage());
                        }
                     });
                  } catch (Exception var71) {
                     Logger.severe("Ошибка при формировании данных голограммы: " + var71.getMessage());
                  }
               }
            )
            .exceptionally(e -> {
               Logger.severe("Ошибка при получении топ игроков для голограммы: " + e.getMessage());
               return null;
            });
      } catch (Exception var21) {
         Logger.severe("Ошибка при обновлении голограммы: " + var21.getMessage());
      }
   }

   public void disable() {
      try {
         if (this.updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
            this.updateTaskId = -1;
         }

         if (this.hologram != null) {
            try {
               this.hologram.delete();
            } catch (Exception var2) {
               Logger.warning("Ошибка при удалении голограммы: " + var2.getMessage());
            }

            this.hologram = null;
         }
      } catch (Exception var3) {
         Logger.severe("Ошибка при отключении HologramManager: " + var3.getMessage());
      }
   }

   public void setUpdateInterval(int seconds) {
      this.updateInterval = Math.max(60, seconds);
      if (this.updateTaskId != -1) {
         this.startUpdateTask();
      }
   }

   private String formatPoints(double points) {
      long rounded = (long)points;
      if (rounded < 1000L) {
         return rounded + " опыта";
      } else if (rounded < 1000000L) {
         double millions = points / 1000.0;
         return millions == (long)millions ? String.format("%dк опыта", (long)millions) : String.format("%.1fк опыта", millions).replace(",", ".");
      } else {
         double millions = points / 1000000.0;
         return millions == (long)millions ? String.format("%dкк опыта", (long)millions) : String.format("%.1fкк опыта", millions).replace(",", ".");
      }
   }
}
