package stickdev.siynes.stickhwstar;

import org.bukkit.plugin.java.JavaPlugin;
import stickdev.siynes.stickhwstar.command.StarCommand;
import stickdev.siynes.stickhwstar.manager.ConfigManager;
import stickdev.siynes.stickhwstar.manager.DatabaseManager;
import stickdev.siynes.stickhwstar.star.StarManager;
import stickdev.siynes.stickhwstar.star.StarPlaceholder;

public final class StickHWStar extends JavaPlugin {
   private static DatabaseManager databaseManager;
   private StarManager starManager;
   private ConfigManager configManager;

   public StickHWStar() {
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
