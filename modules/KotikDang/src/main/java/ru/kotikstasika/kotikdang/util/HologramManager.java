package ru.kotikstasika.kotikdang.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.data.UniqueShulkerLocation;
import ru.kotikstasika.kotikdang.data.UniqueShulkerSession;

public class HologramManager {
   private final KotikDang plugin;
   private final Map<String, Hologram> holograms = new HashMap<>();

   public HologramManager(KotikDang plugin) {
      this.plugin = plugin;
   }

   public void createIdleHologram(UniqueShulkerLocation shulkerLocation) {
      String hologramId = "kotikdang_idle_" + shulkerLocation.getId().toString().substring(0, 8);
      this.removeHologram(hologramId);
      double heightOffset = 3.5;
      Location holoLoc = shulkerLocation.getLocation().clone().add(0.5, heightOffset, 0.5);
      List<String> lines = new ArrayList<>();
      lines.add(ColorUtil.colorize("Получайте редкие и ценные вещи,"));
      lines.add(ColorUtil.colorize("&#DE35FB▼    &fоткрывая этот шалкер    &#DE35FB▼"));
      lines.add(ColorUtil.colorize(""));
      lines.add(ColorUtil.colorize("&#DE35FBУникальный шалкер"));
      lines.add(ColorUtil.colorize(""));
      lines.add(ColorUtil.colorize("Для открытия воспользуйтесь"));
      lines.add(ColorUtil.colorize("   &#FB6335Универсальным ключом"));
      Hologram hologram = DHAPI.createHologram(hologramId, holoLoc, lines);
      this.holograms.put(hologramId, hologram);
      shulkerLocation.setHologramId(hologramId);
   }

   public void removeIdleHologram(UniqueShulkerLocation shulkerLocation) {
      String hologramId = shulkerLocation.getHologramId();
      if (hologramId != null) {
         this.removeHologram(hologramId);
         shulkerLocation.setHologramId(null);
      }
   }

   public void createOpeningHologram(UniqueShulkerSession session) {
      String hologramId = "kotikdang_opening_" + session.getSessionId().toString().substring(0, 8);
      this.removeHologram(hologramId);
      double heightOffset = 2.5;
      Location holoLoc = session.getShulkerLocation().getLocation().clone().add(0.5, heightOffset, 0.5);
      int remaining = session.getRemainingSeconds();
      int minutes = remaining / 60;
      int seconds = remaining % 60;
      List<String> lines = new ArrayList<>();
      lines.add(ColorUtil.colorize("Шалкер закроется через: &#FB6335" + minutes + " м., " + seconds + " сек."));
      lines.add(ColorUtil.colorize("Открывает: &#FB6335" + session.getPlayer().getName()));
      Hologram hologram = DHAPI.createHologram(hologramId, holoLoc, lines);
      this.holograms.put(hologramId, hologram);
      session.setOpeningHologramId(hologramId);
   }

   public void updateOpeningHologram(UniqueShulkerSession session) {
      String hologramId = session.getOpeningHologramId();
      if (hologramId != null) {
         int remaining = session.getRemainingSeconds();
         int minutes = remaining / 60;
         int seconds = remaining % 60;
         List<String> lines = new ArrayList<>();
         lines.add(ColorUtil.colorize("Шалкер закроется через: &#FB6335" + minutes + " м., " + seconds + " сек."));
         lines.add(ColorUtil.colorize("Открывает: &#FB6335" + session.getPlayer().getName()));
         Hologram hologram = DHAPI.getHologram(hologramId);
         if (hologram != null) {
            DHAPI.setHologramLines(hologram, lines);
         }
      }
   }

   public void removeOpeningHologram(UniqueShulkerSession session) {
      String hologramId = session.getOpeningHologramId();
      if (hologramId != null) {
         this.removeHologram(hologramId);
         session.setOpeningHologramId(null);
      }
   }

   public void resetToIdleHologram(UniqueShulkerLocation shulkerLocation) {
      this.createIdleHologram(shulkerLocation);
   }

   public void removeHologram(String hologramId) {
      if (hologramId != null && DHAPI.getHologram(hologramId) != null) {
         DHAPI.removeHologram(hologramId);
         this.holograms.remove(hologramId);
      }
   }

   public void removeAllHolograms() {
      for (String id : new ArrayList<>(this.holograms.keySet())) {
         this.removeHologram(id);
      }
   }
}
