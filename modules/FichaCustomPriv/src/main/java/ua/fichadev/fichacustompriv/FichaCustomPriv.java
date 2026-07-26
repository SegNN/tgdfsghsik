package ua.fichadev.fichacustompriv;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichacustompriv.cmd.PrivilegeAdminCMD;
import ua.fichadev.fichacustompriv.cmd.PrivilegeMainCMD;
import ua.fichadev.fichacustompriv.cmd.TitulAdminCMD;
import ua.fichadev.fichacustompriv.cmd.TitulMainCMD;
import ua.fichadev.fichacustompriv.cmd.TokenAdminCMD;
import ua.fichadev.fichacustompriv.functional.TokenManager;
import ua.fichadev.fichacustompriv.functional.custom.PrivilegeManager;
import ua.fichadev.fichacustompriv.functional.custom.TitulManager;
import ua.fichadev.fichacustompriv.functional.database.DataBaseManager;
import ua.fichadev.fichacustompriv.functional.database.DatabaseWrapper;
import ua.fichadev.fichacustompriv.functional.listeners.PlayerDataListener;
import ua.fichadev.fichacustompriv.functional.listeners.PrivilegeMenuListener;
import ua.fichadev.fichacustompriv.functional.listeners.TitulMenuListener;
import ua.fichadev.fichacustompriv.functional.menu.PrivilegeMenuManager;
import ua.fichadev.fichacustompriv.functional.menu.TitulMenuManager;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class FichaCustomPriv extends JavaPlugin implements Listener {
   private DatabaseWrapper databaseWrapper;
   private DataBaseManager databaseManager;
   private TokenManager tokenManager;
   private PrivilegeManager privilegeManager;
   private TitulManager titulManager;
   private PrivilegeMenuManager privilegeMenuManager;
   private TitulMenuManager titulMenuManager;

   public FichaCustomPriv() {
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
      this.getCommand("fichacustompriv").setExecutor(tokenAdminCMD);
      this.getCommand("fichacustompriv").setTabCompleter(tokenAdminCMD);
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
