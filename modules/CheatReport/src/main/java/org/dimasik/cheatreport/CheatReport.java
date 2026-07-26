package org.dimasik.cheatreport;

import org.bukkit.plugin.java.JavaPlugin;

public class CheatReport extends JavaPlugin {
   private DatabaseManager databaseManager;
   private ReportManager reportManager;
   private ReputationManager reputationManager;
   public OnlinePlayerManager onlinePlayerManager;

   public CheatReport() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.databaseManager = new DatabaseManager(this);
      this.reputationManager = new ReputationManager(this.databaseManager, this);
      this.onlinePlayerManager = new OnlinePlayerManager(this.databaseManager, this);
      this.reportManager = new ReportManager(this, this.databaseManager, this.reputationManager);
      this.getCommand("cheatreport").setExecutor(this.reportManager);
      this.getServer().getPluginManager().registerEvents(this.reportManager.getReportGUI(), this);
   }

   public void onDisable() {
      if (this.databaseManager != null) {
         this.databaseManager.close();
      }
   }

   public ReportManager getReportManager() {
      return this.reportManager;
   }
}
