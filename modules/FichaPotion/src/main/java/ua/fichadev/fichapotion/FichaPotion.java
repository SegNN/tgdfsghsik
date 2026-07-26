package ua.fichadev.fichapotion;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichapotion.command.PotionCommand;
import ua.fichadev.fichapotion.listener.AnvilListener;
import ua.fichadev.fichapotion.util.PotionManager;

public final class FichaPotion extends JavaPlugin {
   private PotionManager potionManager;

   public FichaPotion() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.potionManager = new PotionManager(this);
      this.getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
      PluginCommand cmd = this.getCommand("fichapotion");
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
