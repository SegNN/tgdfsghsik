package ru.kotikstasika.eventdeathmine.task;

import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;

public class BossBarUpdaterTask extends BukkitRunnable {
   private final DeathMineManager manager;

   public BossBarUpdaterTask(DeathMineManager manager) {
      this.manager = manager;
   }

   public void run() {
      this.manager.onTickBossBarVisibility();
   }
}
