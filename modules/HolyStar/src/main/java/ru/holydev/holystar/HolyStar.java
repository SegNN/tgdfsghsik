package ru.holydev.holystar;

import org.bukkit.plugin.java.JavaPlugin;
import ru.holydev.holystar.command.StarCommand;
import ru.holydev.holystar.manager.ConfigManager;
import ru.holydev.holystar.manager.DatabaseManager;
import ru.holydev.holystar.star.StarManager;
import ru.holydev.holystar.star.StarPlaceholder;

public final class HolyStar extends JavaPlugin {
   private static DatabaseManager databaseManager;
   private StarManager starManager;
   private ConfigManager configManager;

   public HolyStar() {
   }

   public void onEnable() {
      this.configManager = new ConfigManager(this);
      databaseManager = new DatabaseManager(this);
      databaseManager.connect();
      this.starManager = new StarManager(databaseManager);
      StarPlaceholder starPlaceholder = new StarPlaceholder(this.starManager, this.configManager);
      starPlaceholder.register();
      this.getCommand("star").setExecutor(new StarCommand(this.starManager));
      this.getCommand("star").setTabCompleter(new StarCommand(this.starManager));
   }

   public void onDisable() {
      if (databaseManager != null) {
         databaseManager.disconnect();
      }
   }

   public static DatabaseManager getDatabase() {
      return databaseManager;
   }
}
