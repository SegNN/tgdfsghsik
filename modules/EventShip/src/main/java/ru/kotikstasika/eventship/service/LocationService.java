package ru.kotikstasika.eventship.service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventship.EventShip;

public class LocationService {
   private final EventShip plugin;
   private final Random random = new Random();

   public LocationService(EventShip plugin) {
      this.plugin = plugin;
   }

   public CompletableFuture<Location> findWaterLocationAsync(final World world, final int maxAttempts) {
      final CompletableFuture<Location> future = new CompletableFuture<>();
      (new BukkitRunnable() {
         public void run() {
            try {
               Location location = LocationService.this.findWaterLocationOptimized(world, maxAttempts);
               if (location != null) {
                  future.complete(location);
               } else {
                  future.complete(null);
               }
            } catch (Exception var2) {
               future.complete(null);
            }
         }
      }).runTaskAsynchronously(this.plugin);
      return future;
   }

   public Location findWaterLocation(World world, int maxAttempts) {
      return this.findWaterLocationOptimized(world, maxAttempts);
   }

   private Location findWaterLocationOptimized(World world, int maxAttempts) {
      int worldBorder = (int)(world.getWorldBorder().getSize() / 2.0);
      if (worldBorder <= 0) {
         worldBorder = 10000;
      }

      int batchSize = 10;
      int attemptsPerBatch = maxAttempts / batchSize;

      for (int batch = 0; batch < batchSize; batch++) {
         for (int attempt = 0; attempt < attemptsPerBatch; attempt++) {
            int x = this.random.nextInt(worldBorder * 2) - worldBorder;
            int z = this.random.nextInt(worldBorder * 2) - worldBorder;
            Location location = new Location(world, x, 64.0, z);
            if (this.isOptimizedWaterLocation(location)) {
               return location;
            }
         }

         if (batch < batchSize - 1) {
            try {
               Thread.sleep(5L);
            } catch (InterruptedException var11) {
               Thread.currentThread().interrupt();
               return null;
            }
         }
      }

      return null;
   }

   private boolean isOptimizedWaterLocation(Location location) {
      try {
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            int x = location.getBlockX();
            int z = location.getBlockZ();

            for (int y = 70; y >= 20; y -= 2) {
               Location checkLocation = new Location(world, x, y, z);
               Block block = checkLocation.getBlock();
               if (block.getType() == Material.WATER) {
                  location.setY(y);
                  return true;
               }
            }

            return false;
         }
      } catch (Exception var8) {
         return false;
      }
   }

   public Location findSafeWaterLocation(World world, int maxAttempts) {
      return this.findWaterLocationOptimized(world, maxAttempts);
   }
}
