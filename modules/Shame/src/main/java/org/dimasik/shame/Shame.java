package org.dimasik.shame;

import lombok.Generated;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.shame.command.CommandExecutor;
import org.dimasik.shame.command.impl.Add;
import org.dimasik.shame.command.impl.Ban;
import org.dimasik.shame.command.impl.Crash;
import org.dimasik.shame.command.impl.Damage;
import org.dimasik.shame.command.impl.Get;
import org.dimasik.shame.command.impl.Rollback;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.database.DatabaseManager;
import org.dimasik.shame.modules.impl.AddModule;
import org.dimasik.shame.modules.impl.BanModule;
import org.dimasik.shame.modules.impl.DamageModule;

public final class Shame extends JavaPlugin {
   private CommandExecutor commandExecutor;
   private static Shame instance;
   private DatabaseManager databaseManager;

   public Shame() {
   }

   public void onEnable() {
      instance = this;
      this.setupConfig();
      this.setupCommand();
      this.setupModules();
      this.setupDatabase();
   }

   private void setupConfig() {
      this.saveDefaultConfig();
      new Config(this.getConfig());
   }

   private void setupCommand() {
      this.commandExecutor = new CommandExecutor();
      PluginCommand command = this.getCommand("shame");
      command.setExecutor(this.commandExecutor);
      command.setTabCompleter(this.commandExecutor);
      new Ban("ban").register();
      new Damage("damage").register();
      new Crash("crash").register();
      new Rollback("rollback").register();
      new Add("add").register();
      new Get("get").register();
   }

   private void setupModules() {
      new AddModule();
      new BanModule();
      new DamageModule();
   }

   private void setupDatabase() {
      this.databaseManager = new DatabaseManager(
         Config.getString("mysql.host", "localhost"),
         Config.getString("mysql.database", "lite_shame"),
         Config.getString("mysql.user", "root"),
         Config.getString("mysql.password", "сайнес гпт кодер"),
         Config.getInteger("mysql.port", 3306)
      );
   }

   @Generated
   public CommandExecutor getCommandExecutor() {
      return this.commandExecutor;
   }

   @Generated
   public static Shame getInstance() {
      return instance;
   }

   @Generated
   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }
}
