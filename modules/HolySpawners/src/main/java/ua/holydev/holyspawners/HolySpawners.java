package ua.holydev.holyspawners;

import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holyspawners.listener.SpawnerListener;
import ua.holydev.holyspawners.utils.ConfigManager;

public class HolySpawners extends JavaPlugin {
   private static HolySpawners instance;
   private ConfigManager configManager;

   public HolySpawners() {
   }

   public void onEnable() {
      instance = this;
      this.configManager = new ConfigManager(this);
      this.configManager.loadConfig();
      this.getServer().getPluginManager().registerEvents(new SpawnerListener(this.configManager), this);
   }

   public void onDisable() {
      instance = null;
   }

   public static HolySpawners getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }
}
