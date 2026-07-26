package ua.fichadev.fichaspawners;

import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichaspawners.listener.SpawnerListener;
import ua.fichadev.fichaspawners.utils.ConfigManager;

public class FichaSpawners extends JavaPlugin {
   private static FichaSpawners instance;
   private ConfigManager configManager;

   public FichaSpawners() {
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

   public static FichaSpawners getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }
}
