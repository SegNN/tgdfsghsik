package ua.fichatime.fichapromocode;

import org.bukkit.plugin.java.JavaPlugin;
import ua.fichatime.fichapromocode.command.CommandVisibilityHandler;
import ua.fichatime.fichapromocode.command.PromoCommandExecutor;
import ua.fichatime.fichapromocode.command.PromoStatsCommand;
import ua.fichatime.fichapromocode.manager.ConfigManager;
import ua.fichatime.fichapromocode.storage.Database;

public final class FichaPromoCode extends JavaPlugin {
   private Database database;
   private ConfigManager configManager;

   public FichaPromoCode() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.configManager = new ConfigManager(this);
      this.database = new Database(this, this.configManager);
      this.registerPromoCodeCommands();
      this.registerStatsCommand();
   }

   private void registerStatsCommand() {
      this.getCommand("promostats").setExecutor(new PromoStatsCommand(this.database, this.configManager));
   }

   private void registerPromoCodeCommands() {
      if (this.configManager.getConfig().getConfigurationSection("promocode") != null) {
         this.getServer().getPluginManager().registerEvents(new PromoCommandExecutor(this, this.database, this.configManager), this);
         this.getServer().getPluginManager().registerEvents(new CommandVisibilityHandler(this.configManager), this);
      }
   }

   public void onDisable() {
      if (this.database != null) {
         this.database.closeConnection();
      }
   }
}
