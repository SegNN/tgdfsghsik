package ua.fichadev.fichaexpbottle;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichaexpbottle.command.ExpBottleCommand;
import ua.fichadev.fichaexpbottle.command.ExpCommand;
import ua.fichadev.fichaexpbottle.listeners.BottleUseListener;
import ua.fichadev.fichaexpbottle.menu.MenuListener;

public class FichaExpBottle extends JavaPlugin {
   private static FichaExpBottle instance;

   public FichaExpBottle() {
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

   public static FichaExpBottle getInstance() {
      return instance;
   }

   public FileConfiguration getPluginConfig() {
      return this.getConfig();
   }
}
