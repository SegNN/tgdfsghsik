package ua.fichadev.fichaautocraft;

import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichaautocraft.core.Config;
import ua.fichadev.fichaautocraft.handlers.AnvilHandler;
import ua.fichadev.fichaautocraft.handlers.BlockHandler;

public class FichaAutoCraft extends JavaPlugin {
   private static FichaAutoCraft instance;
   private Config configManager;

   public FichaAutoCraft() {
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

   public static FichaAutoCraft getInstance() {
      return instance;
   }
}
