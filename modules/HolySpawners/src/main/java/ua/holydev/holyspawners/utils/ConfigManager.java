package ua.holydev.holyspawners.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ua.holydev.holyspawners.HolySpawners;

public class ConfigManager {
   private final HolySpawners plugin;
   private final Map<String, String> spawnerNames;
   private final Map<String, List<String>> spawnerLore;

   public ConfigManager(HolySpawners plugin) {
      this.plugin = plugin;
      this.spawnerNames = new HashMap<>();
      this.spawnerLore = new HashMap<>();
   }

   public void loadConfig() {
      this.plugin.saveDefaultConfig();
      this.plugin.reloadConfig();
      FileConfiguration config = this.plugin.getConfig();
      this.spawnerNames.clear();
      this.spawnerLore.clear();
      ConfigurationSection spawnersSection = config.getConfigurationSection("spawners");
      if (spawnersSection != null) {
         for (String key : spawnersSection.getKeys(false)) {
            String name = config.getString("spawners." + key + ".name");
            List<String> lore = config.getStringList("spawners." + key + ".lore");
            if (name != null) {
               this.spawnerNames.put(key, name);
            }

            if (lore != null && !lore.isEmpty()) {
               this.spawnerLore.put(key, lore);
            }
         }
      }
   }

   public String getSpawnerName(String entityType) {
      return this.spawnerNames.get(entityType);
   }

   public List<String> getSpawnerLore(String entityType) {
      return this.spawnerLore.get(entityType);
   }
}
