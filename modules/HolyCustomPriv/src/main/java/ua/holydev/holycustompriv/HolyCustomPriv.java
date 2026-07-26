package ua.holydev.holycustompriv;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holycustompriv.cmd.PrivilegeAdminCMD;
import ua.holydev.holycustompriv.cmd.PrivilegeMainCMD;
import ua.holydev.holycustompriv.cmd.TitulAdminCMD;
import ua.holydev.holycustompriv.cmd.TitulMainCMD;
import ua.holydev.holycustompriv.cmd.TokenAdminCMD;
import ua.holydev.holycustompriv.functional.TokenManager;
import ua.holydev.holycustompriv.functional.custom.PrivilegeManager;
import ua.holydev.holycustompriv.functional.custom.TitulManager;
import ua.holydev.holycustompriv.functional.database.DataBaseManager;
import ua.holydev.holycustompriv.functional.database.DatabaseWrapper;
import ua.holydev.holycustompriv.functional.listeners.PlayerDataListener;
import ua.holydev.holycustompriv.functional.listeners.PrivilegeMenuListener;
import ua.holydev.holycustompriv.functional.listeners.TitulMenuListener;
import ua.holydev.holycustompriv.functional.menu.PrivilegeMenuManager;
import ua.holydev.holycustompriv.functional.menu.TitulMenuManager;
import ua.holydev.holycustompriv.utils.HexUtil;

public class HolyCustomPriv extends JavaPlugin implements Listener {
   private DatabaseWrapper databaseWrapper;
   private DataBaseManager databaseManager;
   private TokenManager tokenManager;
   private PrivilegeManager privilegeManager;
   private TitulManager titulManager;
   private PrivilegeMenuManager privilegeMenuManager;
   private TitulMenuManager titulMenuManager;

   public HolyCustomPriv() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.databaseWrapper = new DatabaseWrapper(this);
      this.databaseManager = this.databaseWrapper.getDatabase();
      this.privilegeMenuManager = new PrivilegeMenuManager(this);
      this.titulMenuManager = new TitulMenuManager(this);
      this.tokenManager = new TokenManager(this);
      this.privilegeManager = new PrivilegeManager(this);
      this.titulManager = new TitulManager(this);
      this.getServer().getPluginManager().registerEvents(new PrivilegeMenuListener(this), this);
      this.getServer().getPluginManager().registerEvents(new TitulMenuListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
      this.registerCommands();
   }

   private void registerCommands() {
      this.getCommand("custompriv").setExecutor(new PrivilegeMainCMD(this));
      this.getCommand("privadmin").setExecutor(new PrivilegeAdminCMD(this));
      TokenAdminCMD tokenAdminCMD = new TokenAdminCMD(this);
      this.getCommand("holycustompriv").setExecutor(tokenAdminCMD);
      this.getCommand("holycustompriv").setTabCompleter(tokenAdminCMD);
      this.getCommand("customtitul").setExecutor(new TitulMainCMD(this));
      this.getCommand("tituladmin").setExecutor(new TitulAdminCMD(this));
   }

   public void onDisable() {
      if (this.tokenManager != null) {
         this.tokenManager.saveAllTokens();
      }

      if (this.databaseManager != null) {
         this.databaseManager.disconnect();
      }
   }

   public DataBaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   public TokenManager getTokenManager() {
      return this.tokenManager;
   }

   public PrivilegeManager getPrivilegeManager() {
      return this.privilegeManager;
   }

   public TitulManager getTitulManager() {
      return this.titulManager;
   }

   public PrivilegeMenuManager getPrivilegeMenuManager() {
      return this.privilegeMenuManager;
   }

   public TitulMenuManager getTitulMenuManager() {
      return this.titulMenuManager;
   }

   public String getMessage(String path) {
      String message = this.getConfig().getString("messages." + path, "&cСообщение не найдено: " + path);
      return HexUtil.color(message);
   }
}
