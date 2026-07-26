package ru.kotikstasika.eventship.service;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventship.EventShip;

public class RegionBarrierCleaner {
   private final EventShip plugin;
   private static final int BATCH_SIZE = 5000;
   private static final int START_Y = 0;

   public RegionBarrierCleaner(EventShip plugin) {
      this.plugin = plugin;
   }

   public void asyncRemoveBarrier(Location center, final int radius) {
      if (center != null && center.getWorld() != null) {
         final World world = center.getWorld();
         final int centerX = center.getBlockX();
         final int centerZ = center.getBlockZ();
         final int maxY = world.getMaxHeight();
         final int radiusSquared = Math.max(radius, 100) * Math.max(radius, 100);
         (new BukkitRunnable() {
            private int minX = centerX - Math.max(radius, 100);
            private int maxX = centerX + Math.max(radius, 100);
            private int minZ = centerZ - Math.max(radius, 100);
            private int maxZ = centerZ + Math.max(radius, 100);
            private int currentX = this.minX;
            private int currentZ = this.minZ;
            private int currentY = 0;

            public void run() {
               int processed = 0;

               while (this.currentX <= this.maxX && processed < 5000) {
                  while (this.currentZ <= this.maxZ && processed < 5000) {
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

                     for (; this.currentY < maxY && processed < 5000; this.currentY++) {
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
