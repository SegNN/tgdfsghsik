package ua.holymine.holymineskills;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holymine.holymineskills.bonus.BonusEffectsListener;
import ua.holymine.holymineskills.command.CommandManager;
import ua.holymine.holymineskills.command.CommandTabCompleter;
import ua.holymine.holymineskills.config.ConfigManager;
import ua.holymine.holymineskills.database.DatabaseManager;
import ua.holymine.holymineskills.event.BlockBreakListener;
import ua.holymine.holymineskills.hologram.HologramManager;
import ua.holymine.holymineskills.menu.MenuEventListener;
import ua.holymine.holymineskills.menu.MenuManager;
import ua.holymine.holymineskills.placeholder.PlaceholderManager;
import ua.holymine.holymineskills.util.Logger;

public class HolyMineSkills extends JavaPlugin {
   private static HolyMineSkills instance;
   private ConfigManager configManager;
   private DatabaseManager databaseManager;
   private MenuManager menuManager;
   private HologramManager hologramManager;
   private PlaceholderManager placeholderManager;
   private CommandManager commandManager;

   public HolyMineSkills() {
   }

   public void onEnable() {
      instance = this;

      try {
         this.configManager = new ConfigManager(this);
         if (!this.configManager.loadConfig()) {
            Logger.severe("Не удалось загрузить конфигурацию! Плагин будет отключен.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.databaseManager = new DatabaseManager(this, this.configManager);
         if (!this.databaseManager.initialize()) {
            Logger.severe("Не удалось подключиться к базе данных! Плагин будет отключен.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.menuManager = new MenuManager(this, this.databaseManager);
         this.hologramManager = new HologramManager(this, this.databaseManager, this.configManager);
         this.placeholderManager = new PlaceholderManager(this.databaseManager);
         this.commandManager = new CommandManager(this, this.menuManager, this.databaseManager);
         this.registerEvents();
         this.registerCommands();
         if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderManager.register();
         } else {
            Logger.warning("PlaceholderAPI не найден, плейсхолдеры не будут работать");
         }

         this.hologramManager.createHologram();
      } catch (Exception var2) {
         Logger.severe("Критическая ошибка при запуске плагина: " + var2.getMessage());
         var2.printStackTrace();
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void onDisable() {
      try {
         if (this.hologramManager != null) {
            this.hologramManager.disable();
         }

         if (this.databaseManager != null) {
            this.databaseManager.shutdown();
         }
      } catch (Exception var2) {
         Logger.severe("Ошибка при отключении плагина: " + var2.getMessage());
         var2.printStackTrace();
      }
   }

   private void registerEvents() {
      try {
         Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this, this.databaseManager, this.configManager), this);
         Bukkit.getPluginManager().registerEvents(new BonusEffectsListener(this.databaseManager), this);
         Bukkit.getPluginManager().registerEvents(new MenuEventListener(this.databaseManager), this);
      } catch (Exception var2) {
         Logger.severe("Ошибка при регистрации событий: " + var2.getMessage());
      }
   }

   private void registerCommands() {
      try {
         this.getCommand("mineskills").setExecutor(this.commandManager);
         this.getCommand("mineskills").setTabCompleter(new CommandTabCompleter());
      } catch (Exception var2) {
         Logger.severe("Ошибка при регистрации команд: " + var2.getMessage());
      }
   }

   public static HolyMineSkills getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   public MenuManager getMenuManager() {
      return this.menuManager;
   }

   public HologramManager getHologramManager() {
      return this.hologramManager;
   }
}
