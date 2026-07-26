package ua.holydev.holyexpbottle;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holyexpbottle.command.ExpBottleCommand;
import ua.holydev.holyexpbottle.command.ExpCommand;
import ua.holydev.holyexpbottle.listeners.BottleUseListener;
import ua.holydev.holyexpbottle.menu.MenuListener;

public class HolyExpBottle extends JavaPlugin {
   private static HolyExpBottle instance;

   public HolyExpBottle() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.getCommand("exp").setExecutor(new ExpCommand());
      ExpBottleCommand expBottleCommand = new ExpBottleCommand();
      this.getCommand("expbottle").setExecutor(expBottleCommand);
      this.getCommand("expbottle").setTabCompleter(expBottleCommand);
      this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
      this.getServer().getPluginManager().registerEvents(new BottleUseListener(), this);
   }

   public void onDisable() {
   }

   public static HolyExpBottle getInstance() {
      return instance;
   }

   public FileConfiguration getPluginConfig() {
      return this.getConfig();
   }
}
