package ru.nezxenka.holypvp;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
   private final JavaPlugin plugin;
   private FileConfiguration config;
   private File configFile;
   private static final String DEFAULT_SERVER_NAME = "Лайт #1";

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.setupConfig();
   }

   private void setupConfig() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdir();
      }

      this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
      if (!this.configFile.exists()) {
         this.plugin.saveResource("config.yml", false);
      }

      this.reloadConfig();
   }

   public void reloadConfig() {
      this.config = YamlConfiguration.loadConfiguration(this.configFile);
   }

   public void saveConfig() {
      try {
         this.config.save(this.configFile);
      } catch (IOException var2) {
         this.plugin.getLogger().severe("Ошибка сохранения конфига: " + var2.getMessage());
      }
   }

   public String getServerName() {
      return this.config.getString("server", "Анархия #1");
   }

   public void setServerName(String name) {
      this.config.set("server", name);
      this.saveConfig();
   }
}
