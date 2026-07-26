package ua.holydev.holyeventsun.manager;

import org.bukkit.scheduler.BukkitRunnable;
import ua.holydev.holyeventsun.HolyEventSun;
import ua.holydev.holyeventsun.model.Phase;

public class PhaseManager {
   private final HolyEventSun plugin;
   private Phase currentPhase;
   private BukkitRunnable phaseTask;

   public PhaseManager(HolyEventSun plugin) {
      this.plugin = plugin;
      this.currentPhase = Phase.COINS;
   }

   public void startCycle() {
      int interval = this.plugin.getCfg().getPhaseChangeTicks();
      this.phaseTask = new BukkitRunnable() {
         public void run() {
            PhaseManager.this.currentPhase = PhaseManager.this.currentPhase.next();
            PhaseManager.this.plugin.getHologramManager().update();
         }
      };
      this.phaseTask.runTaskTimer(this.plugin, interval, interval);
   }

   public void stop() {
      if (this.phaseTask != null) {
         this.phaseTask.cancel();
      }
   }

   public Phase getCurrentPhase() {
      return this.currentPhase;
   }
}
