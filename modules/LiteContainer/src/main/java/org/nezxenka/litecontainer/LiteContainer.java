package org.nezxenka.litecontainer;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.litecontainer.commands.ContainerCommand;
import org.nezxenka.litecontainer.config.DatabaseManager;
import org.nezxenka.litecontainer.config.LootManager;
import org.nezxenka.litecontainer.config.PluginConfigManager;
import org.nezxenka.litecontainer.listeners.MenuListener;
import org.nezxenka.litecontainer.manager.MenuManager;
import org.nezxenka.litecontainer.placeholders.ContainerExpansion;

public final class LiteContainer extends JavaPlugin {
   private static LiteContainer instance;
   private DatabaseManager databaseManager;
   private LootManager lootManager;
   private PluginConfigManager configManager;
   private ContainerExpansion expansion;
   private MenuManager menuManager;

   public LiteContainer() {
   }

   public void onEnable() {
      instance = this;
      this.configManager = new PluginConfigManager(this);
      this.saveDefaultConfig();
      this.databaseManager = new DatabaseManager(this, this.getConfig());
      this.databaseManager.connect();
      this.lootManager = new LootManager(this);
      PluginCommand command = this.getCommand("container");
      if (command != null) {
         command.setExecutor(new ContainerCommand(this));
      }

      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         this.expansion = new ContainerExpansion(this);
         this.expansion.register();
         this.getLogger().info("PlaceholderAPI expansion registered successfully!");
      } else {
         this.getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
      }

      this.menuManager = new MenuManager();
      this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
   }

   public void onDisable() {
      if (this.expansion != null) {
         this.expansion.unregister();
      }

      if (this.databaseManager != null) {
         this.databaseManager.disconnect();
      }
   }

   public static LiteContainer getInstance() {
      return instance;
   }

   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   public LootManager getLootManager() {
      return this.lootManager;
   }

   public PluginConfigManager getConfigManager() {
      return this.configManager;
   }

   public MenuManager getMenuManager() {
      return this.menuManager;
   }
}
