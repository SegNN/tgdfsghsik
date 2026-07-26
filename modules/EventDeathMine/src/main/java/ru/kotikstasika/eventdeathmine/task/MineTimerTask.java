package ru.kotikstasika.eventdeathmine.task;

import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;

public class MineTimerTask extends BukkitRunnable {
   private final DeathMineManager manager;

   public MineTimerTask(DeathMineManager manager) {
      this.manager = manager;
   }

   public void run() {
      this.manager.onTimerTick();
   }
}
