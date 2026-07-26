package ua.fichadev.fichasalary;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichasalary.command.SalaryCommand;
import ua.fichadev.fichasalary.handler.SalaryHandler;
import ua.fichadev.fichasalary.listener.PlayerListener;
import ua.fichadev.fichasalary.managers.ConfigManager;
import ua.fichadev.fichasalary.managers.DataManager;

public final class FichaSalary extends JavaPlugin {
   private ConfigManager configManager;
   private DataManager dataManager;
   private SalaryHandler salaryHandler;
   private LuckPerms luckPerms;

   public FichaSalary() {
   }

   public void onEnable() {
      this.configManager = new ConfigManager(this);
      this.dataManager = new DataManager(this);
      this.setupLuckPerms();
      this.salaryHandler = new SalaryHandler(this, this.configManager, this.dataManager, this.luckPerms);
      this.getServer().getPluginManager().registerEvents(new PlayerListener(this.salaryHandler, this.dataManager), this);
      this.getCommand("salary").setExecutor(new SalaryCommand(this.salaryHandler, this.configManager, this.dataManager));
   }

   private void setupLuckPerms() {
      Bukkit.getScheduler().runTaskLater(this, () -> {
         RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
         if (provider != null) {
            this.luckPerms = (LuckPerms)provider.getProvider();
         }
      }, 40L);
   }

   public void onDisable() {
      this.dataManager.saveData();
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public SalaryHandler getSalaryHandler() {
      return this.salaryHandler;
   }

   public LuckPerms getLuckPerms() {
      return this.luckPerms;
   }
}
