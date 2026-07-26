package ru.kotikstasika.eventdeathmine.config;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.eventdeathmine.util.ColorUtil;

public class ConfigManager {
   private final JavaPlugin plugin;
   private FileConfiguration cfg;

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      plugin.saveDefaultConfig();
      this.cfg = plugin.getConfig();
   }

   public String worldName() {
      return this.cfg.getString("settings.world", "world");
   }

   public int scanRadius() {
      return this.cfg.getInt("settings.scanRadius", 40);
   }

   public int bossbarRadius() {
      return this.cfg.getInt("settings.bossbarRadius", 100);
   }

   public int maxSpawnAttempts() {
      return this.cfg.getInt("settings.maxSpawnAttempts", 50);
   }

   public int defaultDurationSeconds() {
      return this.cfg.getInt("settings.defaultDurationSeconds", 1200);
   }

   public String schematicName() {
      return this.cfg.getString("settings.schematic", "event_death_mine.schem");
   }

   public int cubeSize() {
      return this.cfg.getInt("settings.cube.size", 50);
   }

   public long cubeDurationSeconds() {
      return this.cfg.getLong("settings.cube.durationSeconds", 600L);
   }

   public int barrierRadius() {
      return this.cfg.getInt("settings.barrier.radius", 135);
   }

   public String displayName() {
      return this.cfg.getString("messages.displayName", "&4&lСмертельная шахта");
   }

   public String accentColor() {
      return this.cfg.getString("messages.color", "&4");
   }

   public String format(String template, Map<String, String> placeholders) {
      if (template == null) {
         return "";
      } else {
         String out = template;
         if (placeholders != null) {
            for (Entry<String, String> e : placeholders.entrySet()) {
               out = out.replace("%" + e.getKey() + "%", e.getValue());
            }
         }

         return ColorUtil.colorize(out);
      }
   }
}
