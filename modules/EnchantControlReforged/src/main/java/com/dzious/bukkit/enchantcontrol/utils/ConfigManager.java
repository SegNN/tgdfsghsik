package com.dzious.bukkit.enchantcontrol.utils;

import com.dzious.bukkit.enchantcontrol.EnchantControl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
   private FileConfiguration configFile = null;
   private final EnchantControl plugin;

   public ConfigManager(EnchantControl plugin) {
      this.plugin = plugin;
      File file = new File(this.plugin.getDataFolder(), "config.yml");
      if (!file.exists()) {
         this.plugin.saveDefaultConfig();
      }

      this.configFile = this.plugin.getConfig();
   }

   public void reload() throws FileNotFoundException, IOException, InvalidConfigurationException {
      File file = new File(this.plugin.getDataFolder(), "config.yml");
      this.configFile.load(file);
   }

   public boolean doPathExist(String path) {
      return this.configFile.contains(path);
   }

   public String getStringFromPath(String path) {
      return this.configFile.getString(path);
   }

   public boolean getBooleanFromPath(String path) {
      return this.configFile.getBoolean(path);
   }

   public int getIntFromPath(String path) {
      return this.configFile.getInt(path);
   }

   public List<String> getListFromPath(String path) {
      return this.configFile.getStringList(path);
   }

   public Map<String, Integer> loadEnchantments(String path) {
      Map<String, Integer> map = new HashMap<>();

      for (String enchantment : this.configFile.getConfigurationSection("enchantments." + path).getKeys(false)) {
         Integer level = this.getIntFromPath("enchantments." + path + "." + enchantment);
         map.put(enchantment, level);
      }

      return map;
   }
}
