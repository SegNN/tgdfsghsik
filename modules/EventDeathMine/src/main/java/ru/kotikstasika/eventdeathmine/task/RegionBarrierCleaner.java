package ru.kotikstasika.eventdeathmine.task;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RegionBarrierCleaner {
   private final JavaPlugin plugin;
   private static final int START_Y = 0;

   public RegionBarrierCleaner(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void asyncRemoveBarrier(Location center, final int radius) {
      if (center != null && center.getWorld() != null) {
         final World world = center.getWorld();
         final int centerX = center.getBlockX();
         final int centerZ = center.getBlockZ();
         final int maxY = world.getMaxHeight();
         final int radiusSquared = radius * radius;
         final int batchSize = Math.max(1, this.plugin.getConfig().getInt("settings.barrier.opsPerTick", 2500));
         (new BukkitRunnable() {
            private final int minX = centerX - radius;
            private final int maxX = centerX + radius;
            private final int minZ = centerZ - radius;
            private final int maxZ = centerZ + radius;
            private int currentX = this.minX;
            private int currentZ = this.minZ;
            private int currentY = 0;

            public void run() {
               int processed = 0;

               while (this.currentX <= this.maxX && processed < batchSize) {
                  while (this.currentZ <= this.maxZ && processed < batchSize) {
                     int dx = this.currentX - centerX;
                     int dz = this.currentZ - centerZ;
                     if (dx * dx + dz * dz > radiusSquared) {
                        this.currentZ++;
                        continue;
                     }

                     Chunk chunk = world.getChunkAt(this.currentX >> 4, this.currentZ >> 4);
                     if (!chunk.isLoaded()) {
                        chunk.load(true);
                     }

                     for (; this.currentY < maxY && processed < batchSize; this.currentY++) {
                        Block block = world.getBlockAt(this.currentX, this.currentY, this.currentZ);
                        if (block.getType() == Material.BARRIER) {
                           block.setType(Material.AIR, false);
                           processed++;
                        }
                     }

                     this.currentY = 0;
                     this.currentZ++;
                  }

                  if (this.currentZ > this.maxZ) {
                     this.currentZ = this.minZ;
                     this.currentX++;
                  }
               }

               if (this.currentX > this.maxX) {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }
}
