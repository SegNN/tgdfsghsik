package ru.nezxenka.holykits;

import org.bukkit.plugin.java.JavaPlugin;
import ru.nezxenka.holykits.commands.KitAdminCommand;
import ru.nezxenka.holykits.commands.KitCommand;
import ru.nezxenka.holykits.commands.KitTabCompleter;
import ru.nezxenka.holykits.config.MessagesConfig;
import ru.nezxenka.holykits.database.Database;
import ru.nezxenka.holykits.listener.JoinKitListener;
import ru.nezxenka.holykits.managers.KitManager;

public final class HolyKits extends JavaPlugin {
   private Database database;
   private KitManager kitManager;
   private MessagesConfig messagesConfig;
   private static HolyKits instance;

   public HolyKits() {
   }

   public void onEnable() {
      instance = this;
      if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
         this.getLogger().severe("Не удалось создать папку плагина!");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.database = new Database(this);
         if (!this.database.connect()) {
            this.getLogger().severe("Не удалось подключиться к базе данных! Плагин будет отключен.");
            this.getServer().getPluginManager().disablePlugin(this);
         } else {
            this.messagesConfig = new MessagesConfig(this);
            this.kitManager = new KitManager(this);
            this.getServer().getPluginManager().registerEvents(new JoinKitListener(this), this);
            KitAdminCommand kitAdminCommand = new KitAdminCommand(this);
            this.getCommand("kit").setExecutor(new KitCommand(this));
            this.getCommand("kit").setTabCompleter(new KitTabCompleter(this));
            this.getCommand("kitadmin").setExecutor(kitAdminCommand);
            this.getCommand("kitadmin").setTabCompleter(kitAdminCommand);
         }
      }
   }

   public void onDisable() {
      if (this.database != null) {
         this.database.disconnect();
      }
   }

   public Database getDatabase() {
      return this.database;
   }

   public KitManager getKitManager() {
      return this.kitManager;
   }

   public MessagesConfig getMessagesConfig() {
      return this.messagesConfig;
   }

   public static HolyKits getInstance() {
      return instance;
   }
}
