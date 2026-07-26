package ua.fichadev.fichaeventsun.task;

import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichaeventsun.FichaEventSun;

public class RewardTask {
   private final FichaEventSun plugin;

   public RewardTask(FichaEventSun plugin) {
      this.plugin = plugin;
   }

   public void start() {
      int interval = this.plugin.getCfg().getRewardIntervalTicks();
      (new BukkitRunnable() {
         public void run() {
            RewardTask.this.plugin.getRewardManager().distributeRewards();
         }
      }).runTaskTimer(this.plugin, interval, interval);
   }
}
