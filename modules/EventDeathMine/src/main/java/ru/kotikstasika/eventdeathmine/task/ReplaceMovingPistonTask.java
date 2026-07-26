package ru.kotikstasika.eventdeathmine.task;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;

public class ReplaceMovingPistonTask extends BukkitRunnable {
   private final Location center;
   private final int radius;
   private final int operationsPerTick;
   private final DeathMineManager manager;
   private final int minY;
   private final int maxY;
   private int x;
   private int y;
   private int z;
   private final int minX;
   private final int maxX;
   private final int minZ;
   private final int maxZ;

   public ReplaceMovingPistonTask(DeathMineManager manager, Location center, int radius, int operationsPerTick) {
      this.manager = manager;
      this.center = center;
      this.radius = radius;
      this.operationsPerTick = Math.max(1, operationsPerTick);
      this.minX = center.getBlockX() - radius;
      this.maxX = center.getBlockX() + radius;
      this.minY = Math.max(0, center.getBlockY() - 20);
      this.maxY = Math.min(center.getWorld().getMaxHeight(), center.getBlockY() + 30);
      this.minZ = center.getBlockZ() - radius;
      this.maxZ = center.getBlockZ() + radius;
      this.x = this.minX;
      this.y = this.minY;
      this.z = this.minZ;
   }

   public void run() {
      World world = this.center.getWorld();
      if (world == null) {
         this.cancel();
      } else {
         int ops = 0;

         while (ops < this.operationsPerTick) {
            Block block = world.getBlockAt(this.x, this.y, this.z);
            if (block.getType() == Material.MOVING_PISTON) {
               Material replacement = this.rollReplacement();
               block.setType(replacement, false);
               if (this.manager != null) {
                  this.manager.registerResourceBlock(block.getLocation());
               }
            }

            ops++;
            if (++this.z > this.maxZ) {
               this.z = this.minZ;
               if (++this.y > this.maxY) {
                  this.y = this.minY;
                  if (++this.x > this.maxX) {
                     this.cancel();
                     return;
                  }
               }
            }
         }
      }
   }

   private Material rollReplacement() {
      int r = ThreadLocalRandom.current().nextInt(100);
      if (r < 80) {
         return Material.OBSIDIAN;
      } else {
         return r < 95 ? Material.CRYING_OBSIDIAN : Material.ANCIENT_DEBRIS;
      }
   }
}
