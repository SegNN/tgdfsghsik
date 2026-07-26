package ua.holydev.holyeventsun.manager;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ua.holydev.holyeventsun.HolyEventSun;
import ua.holydev.holyeventsun.model.Phase;
import ua.holydev.holyeventsun.util.ColorUtils;

public class HologramManager {
   private final HolyEventSun plugin;
   private final List<ArmorStand> lines = new ArrayList<>();
   private int lootCountdown;

   public HologramManager(HolyEventSun plugin) {
      this.plugin = plugin;
      this.lootCountdown = plugin.getCfg().getLootDropIntervalTicks() / 20;
   }

   public void spawn() {
      this.removeAll();
      this.cleanupOrphaned();
      Location base = this.plugin.getCfg().getCoreLocation();
      if (base != null && base.getWorld() != null) {
         double yOffset = this.plugin.getCfg().getHologramOffset();
         Location loc = base.clone().add(0.5, yOffset, 0.5);
         this.lines.add(this.createLine(loc.clone().add(0.0, 0.75, 0.0), this.formatTitle()));
         this.lines.add(this.createLine(loc.clone().add(0.0, 0.25, 0.0), this.formatPhaseLine()));
         this.lines.add(this.createLine(loc, this.formatTimerLine()));
      }
   }

   public void cleanupOrphaned() {
      Location base = this.plugin.getCfg().getCoreLocation();
      if (base != null && base.getWorld() != null) {
         for (Entity entity : base.getWorld().getNearbyEntities(base, 3.0, 5.0, 3.0)) {
            if (entity.getType() == EntityType.ARMOR_STAND) {
               ArmorStand stand = (ArmorStand)entity;
               if (!stand.isVisible() && stand.isMarker() && stand.isCustomNameVisible()) {
                  stand.remove();
               }
            }
         }
      }
   }

   private ArmorStand createLine(Location location, String text) {
      ArmorStand stand = (ArmorStand)location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
      stand.setVisible(false);
      stand.setGravity(false);
      stand.setCustomName(text);
      stand.setCustomNameVisible(true);
      stand.setMarker(true);
      stand.setSmall(true);
      stand.setInvulnerable(true);
      return stand;
   }

   public void update() {
      if (this.lines.size() >= 3) {
         this.lines.get(0).setCustomName(this.formatTitle());
         this.lines.get(1).setCustomName(this.formatPhaseLine());
         this.lines.get(2).setCustomName(this.formatTimerLine());
      }
   }

   public void setLootCountdown(int seconds) {
      this.lootCountdown = seconds;
      if (this.lines.size() >= 3) {
         this.lines.get(2).setCustomName(this.formatTimerLine());
      }
   }

   public int getLootCountdown() {
      return this.lootCountdown;
   }

   public void removeAll() {
      for (ArmorStand stand : this.lines) {
         if (stand != null && !stand.isDead()) {
            stand.remove();
         }
      }

      this.lines.clear();
   }

   private String formatTitle() {
      return ColorUtils.colorize(this.plugin.getCfg().getHologramTitle());
   }

   private String formatPhaseLine() {
      Phase phase = this.plugin.getPhaseManager().getCurrentPhase();
      String phaseColor = ColorUtils.colorize(phase.getDisplayName());
      return ColorUtils.colorize(this.plugin.getCfg().getHologramPhaseLine().replace("%phase%", phaseColor));
   }

   private String formatTimerLine() {
      return ColorUtils.colorize(this.plugin.getCfg().getHologramTimerLine().replace("%time%", String.valueOf(this.lootCountdown)));
   }
}
