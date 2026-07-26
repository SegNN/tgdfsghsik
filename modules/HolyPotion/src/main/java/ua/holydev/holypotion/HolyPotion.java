package ua.holydev.holypotion;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holydev.holypotion.command.PotionCommand;
import ua.holydev.holypotion.listener.AnvilListener;
import ua.holydev.holypotion.util.PotionManager;

public final class HolyPotion extends JavaPlugin {
   private PotionManager potionManager;

   public HolyPotion() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.potionManager = new PotionManager(this);
      this.getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
      PluginCommand cmd = this.getCommand("holypotion");
      if (cmd != null) {
         PotionCommand executor = new PotionCommand(this);
         cmd.setExecutor(executor);
         cmd.setTabCompleter(executor);
      }
   }

   public void onDisable() {
   }

   public PotionManager getPotionManager() {
      return this.potionManager;
   }
}
