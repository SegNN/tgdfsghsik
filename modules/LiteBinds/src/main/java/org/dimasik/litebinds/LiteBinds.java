package org.dimasik.litebinds;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.litebinds.command.CommandExecutor;
import org.dimasik.litebinds.database.DatabaseManager;
import org.dimasik.litebinds.listeners.EventListener;
import org.dimasik.litebinds.menu.listeners.MenuListener;
import org.dimasik.litebinds.menu.menus.Menu;

public final class LiteBinds extends JavaPlugin {
   private static LiteBinds instance;
   private DatabaseManager databaseManager;

   public LiteBinds() {
   }

   public void onEnable() {
      instance = this;
      this.setupDatabase();
      this.setupCommands();
      this.setupListeners();
   }

   private void setupDatabase() {
      super.saveDefaultConfig();
      FileConfiguration config = super.getConfig();
      this.databaseManager = new DatabaseManager(
         config.getString("database.type", "sqlite"),
         config.getString("mysql.host", "localhost"),
         config.getInt("mysql.port", 3306),
         config.getString("mysql.user", "root"),
         config.getString("mysql.password", ""),
         config.getString("mysql.database", "lite_binds"),
         config.getString("sqlite.file", "plugins/LiteBinds/binds.db")
      );
   }

   private void setupCommands() {
      PluginCommand cmd = this.getCommand("binds");
      CommandExecutor cmdExec = new CommandExecutor();
      cmd.setExecutor(cmdExec);
      cmd.setTabCompleter(cmdExec);
   }

   private void setupListeners() {
      new MenuListener().register();
      PluginManager pluginManager = super.getServer().getPluginManager();
      pluginManager.registerEvents(new EventListener(), this);
   }

   public void onDisable() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) {
            player.closeInventory();
         }
      }

      this.databaseManager.close();
   }

   @Generated
   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   @Generated
   public static LiteBinds getInstance() {
      return instance;
   }
}
