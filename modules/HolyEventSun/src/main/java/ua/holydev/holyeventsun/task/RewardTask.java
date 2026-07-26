package ua.holydev.holyeventsun.task;

import org.bukkit.scheduler.BukkitRunnable;
import ua.holydev.holyeventsun.HolyEventSun;

public class RewardTask {
   private final HolyEventSun plugin;

   public RewardTask(HolyEventSun plugin) {
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
