package ua.fichamine.fichamineskills.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichamineskills.util.Logger;

public class ConfigManager {
   private final JavaPlugin plugin;
   private FileConfiguration config;
   private final Map<Material, Double> blockPointsCache = new ConcurrentHashMap<>();
   private final Map<String, Object> configCache = new ConcurrentHashMap<>();

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean loadConfig() {
      try {
         this.plugin.saveDefaultConfig();
         this.plugin.reloadConfig();
         this.config = this.plugin.getConfig();
         if (!this.validateConfig()) {
            Logger.severe("Конфигурация содержит ошибки!");
            return false;
         } else {
            this.loadBlockPoints();
            return true;
         }
      } catch (Exception var2) {
         Logger.severe("Ошибка при загрузке конфигурации: " + var2.getMessage());
         var2.printStackTrace();
         return false;
      }
   }

   private boolean validateConfig() {
      try {
         if (!this.config.contains("database.host")
            || !this.config.contains("database.port")
            || !this.config.contains("database.database")
            || !this.config.contains("database.username")
            || !this.config.contains("database.password")) {
            Logger.severe("Отсутствуют обязательные настройки базы данных!");
            return false;
         } else if (!this.config.contains("hologram.points.x") || !this.config.contains("hologram.points.y") || !this.config.contains("hologram.points.z")) {
            Logger.severe("Отсутствуют координаты голограммы!");
            return false;
         } else if (this.config.contains("blocks") && this.config.getConfigurationSection("blocks") != null) {
            return true;
         } else {
            Logger.severe("Отсутствует секция блоков!");
            return false;
         }
      } catch (Exception var2) {
         Logger.severe("Ошибка валидации конфигурации: " + var2.getMessage());
         return false;
      }
   }

   private void loadBlockPoints() {
      this.blockPointsCache.clear();
      ConfigurationSection section = this.config.getConfigurationSection("blocks");
      if (section != null) {
         for (String blockName : section.getKeys(false)) {
            try {
               Material material = Material.matchMaterial(blockName);
               if (material != null) {
                  double points = section.getDouble(blockName, 0.0);
                  if (points > 0.0) {
                     this.blockPointsCache.put(material, points);
                  }
               } else {
                  Logger.warning("Неизвестный материал: " + blockName);
               }
            } catch (Exception var71) {
               Logger.warning("Ошибка при загрузке блока " + blockName + ": " + var71.getMessage());
            }
         }
      }
   }

   public String getDbHost() {
      return this.config.getString("database.host", "localhost");
   }

   public int getDbPort() {
      return this.config.getInt("database.port", 3306);
   }

   public String getDbName() {
      return this.config.getString("database.database", "minecraft");
   }

   public String getDbUser() {
      return this.config.getString("database.username", "root");
   }

   public String getDbPassword() {
      return this.config.getString("database.password", "password");
   }

   public double getHologramX() {
      return this.config.getDouble("hologram.points.x", 0.0);
   }

   public double getHologramY() {
      return this.config.getDouble("hologram.points.y", 64.0);
   }

   public double getHologramZ() {
      return this.config.getDouble("hologram.points.z", 0.0);
   }

   public double getPointsForBlock(Material material) {
      return this.blockPointsCache.getOrDefault(material, 0.0);
   }

   public boolean isDebugEnabled() {
      return this.config.getBoolean("debug", false);
   }

   public long getCacheDuration() {
      return this.config.getLong("performance.cache_duration", 300000L);
   }

   public int getHologramUpdateInterval() {
      return this.config.getInt("performance.hologram_update_interval", 60);
   }

   public long getPlaceholderCacheDuration() {
      return this.config.getLong("performance.placeholder_cache_duration", 30000L);
   }

   public boolean isBonusUnlockNotificationEnabled() {
      return this.config.getBoolean("notifications.bonus_unlock", true);
   }

   public boolean isPointsGainNotificationEnabled() {
      return this.config.getBoolean("notifications.points_gain", false);
   }

   public boolean isErrorLoggingEnabled() {
      return this.config.getBoolean("notifications.error_logging", true);
   }

   public boolean reloadConfig() {
      try {
         this.plugin.reloadConfig();
         this.config = this.plugin.getConfig();
         if (!this.validateConfig()) {
            Logger.severe("Конфигурация содержит ошибки после перезагрузки!");
            return false;
         } else {
            this.loadBlockPoints();
            return true;
         }
      } catch (Exception var2) {
         Logger.severe("Ошибка при перезагрузке конфигурации: " + var2.getMessage());
         return false;
      }
   }
}
