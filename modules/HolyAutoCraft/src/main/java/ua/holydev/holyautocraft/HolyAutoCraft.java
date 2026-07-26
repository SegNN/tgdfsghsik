package ua.holydev.holyautocraft;

import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holyautocraft.core.Config;
import ua.holydev.holyautocraft.handlers.AnvilHandler;
import ua.holydev.holyautocraft.handlers.BlockHandler;

public class HolyAutoCraft extends JavaPlugin {
   private static HolyAutoCraft instance;
   private Config configManager;

   public HolyAutoCraft() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.configManager = new Config(this);
      this.registerHandlers();
   }

   public void onDisable() {
      instance = null;
   }

   private void registerHandlers() {
      this.getServer().getPluginManager().registerEvents(new AnvilHandler(this), this);
      this.getServer().getPluginManager().registerEvents(new BlockHandler(this), this);
   }

   public Config getConfigManager() {
      return this.configManager;
   }

   public static HolyAutoCraft getInstance() {
      return instance;
   }
}
