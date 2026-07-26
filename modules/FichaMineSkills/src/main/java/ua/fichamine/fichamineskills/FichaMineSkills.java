package ua.fichamine.fichamineskills;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichamineskills.bonus.BonusEffectsListener;
import ua.fichamine.fichamineskills.command.CommandManager;
import ua.fichamine.fichamineskills.command.CommandTabCompleter;
import ua.fichamine.fichamineskills.config.ConfigManager;
import ua.fichamine.fichamineskills.database.DatabaseManager;
import ua.fichamine.fichamineskills.event.BlockBreakListener;
import ua.fichamine.fichamineskills.hologram.HologramManager;
import ua.fichamine.fichamineskills.menu.MenuEventListener;
import ua.fichamine.fichamineskills.menu.MenuManager;
import ua.fichamine.fichamineskills.placeholder.PlaceholderManager;
import ua.fichamine.fichamineskills.util.Logger;

public class FichaMineSkills extends JavaPlugin {
   private static FichaMineSkills instance;
   private ConfigManager configManager;
   private DatabaseManager databaseManager;
   private MenuManager menuManager;
   private HologramManager hologramManager;
   private PlaceholderManager placeholderManager;
   private CommandManager commandManager;

   public FichaMineSkills() {
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

   public static FichaMineSkills getInstance() {
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
