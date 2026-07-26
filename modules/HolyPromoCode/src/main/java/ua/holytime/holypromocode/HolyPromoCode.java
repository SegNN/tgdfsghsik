package ua.holytime.holypromocode;

import org.bukkit.plugin.java.JavaPlugin;
import ua.holytime.holypromocode.command.CommandVisibilityHandler;
import ua.holytime.holypromocode.command.PromoCommandExecutor;
import ua.holytime.holypromocode.command.PromoStatsCommand;
import ua.holytime.holypromocode.manager.ConfigManager;
import ua.holytime.holypromocode.storage.Database;

public final class HolyPromoCode extends JavaPlugin {
   private Database database;
   private ConfigManager configManager;

   public HolyPromoCode() {
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
