package ru.kotikstasika.eventship.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.plugin.IllegalPluginAccessException;
import ru.kotikstasika.eventship.EventShip;

public class HologramManager {
   private final EventShip plugin;
   private final Map<String, String> hologramIds = new HashMap<>();
   private final List<String> allHologramIds = new ArrayList<>();

   public HologramManager(EventShip plugin) {
      this.plugin = plugin;
   }

   private void runSync(Runnable task) {
      try {
         if (this.plugin.isEnabled()) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, task);
         } else {
            task.run();
         }
      } catch (IllegalPluginAccessException var3) {
         task.run();
      }
   }

   public String createHologram(Location location, List<String> lines) {
      if (!this.isDecentHologramsAvailable()) {
         this.plugin.getLogger().warning("DecentHolograms не найден! Голограммы не будут работать.");
         return null;
      } else {
         String hologramId = UUID.randomUUID().toString();
         this.runSync(() -> {
            try {
               DHAPI.createHologram(hologramId, location, lines);
               this.hologramIds.put(hologramId, location.toString());
               this.allHologramIds.add(hologramId);
            } catch (Exception var5) {
               this.plugin.getLogger().warning("Ошибка при создании голограммы: " + var5.getMessage());
            }
         });
         return hologramId;
      }
   }

   public void updateHologram(String hologramId, List<String> lines) {
      if (hologramId != null && this.hologramIds.containsKey(hologramId)) {
         this.runSync(() -> {
            try {
               Hologram hologram = Hologram.getCachedHologram(hologramId);
               if (hologram != null) {
                  DHAPI.setHologramLines(hologram, lines);
               }
            } catch (Exception var4) {
               this.plugin.getLogger().warning("Ошибка при обновлении голограммы: " + var4.getMessage());
            }
         });
      }
   }

   public void removeHologram(String hologramId) {
      if (hologramId != null) {
         this.runSync(() -> {
            try {
               DHAPI.removeHologram(hologramId);
            } catch (Exception var3) {
               this.plugin.getLogger().warning("Ошибка при удалении голограммы: " + var3.getMessage());
            }
         });
         this.hologramIds.remove(hologramId);
         this.allHologramIds.remove(hologramId);
      }
   }

   public void removeAllHolograms() {
      for (String id : new ArrayList<>(this.allHologramIds)) {
         try {
            DHAPI.removeHologram(id);
         } catch (Exception var5) {
            this.plugin.getLogger().warning("Ошибка при удалении голограммы " + id + ": " + var5.getMessage());
         }
      }

      this.hologramIds.clear();
      this.allHologramIds.clear();
   }

   public String createHologramWithDelay(Location location, List<String> lines, long delayMs) {
      String hologramId = UUID.randomUUID().toString();
      if (!this.plugin.isEnabled()) {
         return hologramId;
      } else {
         long ticks = Math.max(1L, delayMs / 50L);

         try {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
               try {
                  DHAPI.createHologram(hologramId, location, lines);
                  this.hologramIds.put(hologramId, location.toString());
                  this.allHologramIds.add(hologramId);
               } catch (Exception var5x) {
                  this.plugin.getLogger().warning("Ошибка при создании голограммы с задержкой: " + var5x.getMessage());
               }
            }, ticks);
         } catch (IllegalPluginAccessException var9) {
         }

         return hologramId;
      }
   }

   private boolean isDecentHologramsAvailable() {
      return this.plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;
   }
}
