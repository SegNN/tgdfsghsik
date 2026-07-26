package ua.fichamine.fichaautomine;

import org.bukkit.plugin.java.JavaPlugin;
import ua.fichamine.fichaautomine.commands.MineCommand;
import ua.fichamine.fichaautomine.expansions.MineExpansion;
import ua.fichamine.fichaautomine.listeners.BreakListener;
import ua.fichamine.fichaautomine.mines.EndNetherMine;
import ua.fichamine.fichaautomine.mines.OverworldMine;

public final class FichaAutoMine extends JavaPlugin {
   private OverworldMine overworldMine;
   private EndNetherMine endNetherMine;
   private MineExpansion expansion;

   public FichaAutoMine() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.overworldMine = new OverworldMine(this);
      this.endNetherMine = new EndNetherMine(this);

      try {
         this.overworldMine.initialize();
         this.endNetherMine.initialize();
      } catch (Exception var2) {
         this.getLogger().severe("Ошибка инициализации шахт: " + var2.getMessage());
         var2.printStackTrace();
      }

      this.overworldMine.startTimer();
      this.endNetherMine.startTimer();
      this.getCommand("fichaautomine").setExecutor(new MineCommand(this));
      this.getServer().getPluginManager().registerEvents(new BreakListener(), this);
      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         this.expansion = new MineExpansion(this);
      } else {
         this.getLogger().warning("PlaceholderAPI не найден! Плейсхолдеры не будут работать.");
      }
   }

   public void onDisable() {
      if (this.overworldMine != null) {
         this.overworldMine.cancelTimer();
      }

      if (this.endNetherMine != null) {
         this.endNetherMine.cancelTimer();
      }

      if (this.expansion != null) {
         this.expansion.unregisterExpansion();
         this.expansion = null;
      }
   }

   public OverworldMine getOverworldMine() {
      return this.overworldMine;
   }

   public EndNetherMine getEndNetherMine() {
      return this.endNetherMine;
   }
}
