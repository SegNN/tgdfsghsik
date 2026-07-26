package org.dimasik.shame.modules.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.dimasik.shame.Shame;
import org.dimasik.shame.modules.Module;

public class RollbackModule extends Module {
   public RollbackModule() {
   }

   public static void startRollback(final Player player) {
      final Deque<Location> locations = new ArrayDeque<>();
      (new BukkitRunnable() {
         int ticks = 0;

         public void run() {
            if (this.ticks < 30 && player != null) {
               this.ticks++;
               locations.add(player.getLocation());
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(Shame.getInstance(), 0L, 2L);
      (new BukkitRunnable() {
         public void run() {
            if (!locations.isEmpty() && player != null) {
               player.teleport(locations.removeLast());
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(Shame.getInstance(), 60L, 1L);
   }
}
