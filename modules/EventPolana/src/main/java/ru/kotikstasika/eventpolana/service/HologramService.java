package ru.kotikstasika.eventpolana.service;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.Arrays;
import org.bukkit.Location;
import ru.kotikstasika.eventpolana.util.TextUtil;

public class HologramService {
   private String hologramId;

   public HologramService() {
   }

   public void createOrUpdate(Location location, boolean shearsAvailable) {
      String title = TextUtil.colorize("&x&4&3&B&C&0&6С&x&3&F&C&7&0&5а&x&3&C&D&2&0&4д&x&3&8&D&E&0&3о&x&3&4&E&9&0&2в&x&3&1&F&4&0&1о&x&2&D&F&F&0&0д");
      String line = shearsAvailable ? TextUtil.colorize("－ Пора! Бери ножницы! &a[ПКМ]") : TextUtil.colorize("－ Жди, сейчас отрастут цветы.");
      if (this.hologramId == null) {
         this.hologramId = "gardener_hologram_" + System.currentTimeMillis();
         DHAPI.createHologram(this.hologramId, location, false, Arrays.asList(title, line));
      } else {
         Hologram hologram = Hologram.getCachedHologram(this.hologramId);
         if (hologram != null) {
            DHAPI.setHologramLines(hologram, Arrays.asList(title, line));
         }
      }
   }

   public void remove() {
      if (this.hologramId != null) {
         DHAPI.removeHologram(this.hologramId);
         this.hologramId = null;
      }
   }
}
