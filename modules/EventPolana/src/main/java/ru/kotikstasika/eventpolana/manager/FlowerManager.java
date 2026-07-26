package ru.kotikstasika.eventpolana.manager;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.kotikstasika.eventpolana.EventPolana;
import ru.kotikstasika.eventpolana.config.PluginConfig;

public class FlowerManager {
   private static final Material[] FLOWERS = new Material[]{
      Material.POPPY,
      Material.DANDELION,
      Material.BLUE_ORCHID,
      Material.ALLIUM,
      Material.AZURE_BLUET,
      Material.RED_TULIP,
      Material.ORANGE_TULIP,
      Material.WHITE_TULIP,
      Material.PINK_TULIP,
      Material.CORNFLOWER
   };
   private final Random random = new Random();
   private final EventPolana plugin;
   private final PluginConfig cfg;
   private BukkitTask spawnTask;
   private final AtomicInteger currentFlowerCount = new AtomicInteger(0);

   public FlowerManager(EventPolana plugin) {
      this.plugin = plugin;
      this.cfg = new PluginConfig(plugin);
   }

   public void start(final Location center, final int radius, long periodTicks) {
      this.stop();
      this.currentFlowerCount.set(0);
      this.spawnTask = (new BukkitRunnable() {
         public void run() {
            FlowerManager.this.spawnRandomFlowers(center, radius);
         }
      }).runTaskTimer(this.plugin, 0L, periodTicks);
   }

   public void stop() {
      if (this.spawnTask != null && !this.spawnTask.isCancelled()) {
         this.spawnTask.cancel();
      }

      this.currentFlowerCount.set(0);
   }

   private void spawnRandomFlowers(Location center, int radius) {
      if (center != null) {
         World world = center.getWorld();
         if (world != null) {
            int cx = center.getBlockX();
            int cz = center.getBlockZ();
            int maxConcurrent = Math.max(1, this.cfg.getFlowersMaxConcurrent());
            int existing = this.currentFlowerCount.get();
            if (existing < maxConcurrent) {
               int randomBatch = 1 + this.random.nextInt(50);
               int toSpawn = Math.min(randomBatch, maxConcurrent - existing);
               if (toSpawn > 0) {
                  int placed = 0;
                  int attempts = 0;
                  int maxAttempts = toSpawn * 6;

                  while (placed < toSpawn && attempts < maxAttempts) {
                     attempts++;
                     int x = cx + this.random.nextInt(radius * 2) - radius;
                     int z = cz + this.random.nextInt(radius * 2) - radius;
                     if (this.tryPlaceFlower(world, x, z)) {
                        placed++;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean tryPlaceFlower(World world, int x, int z) {
      try {
         int y = world.getHighestBlockYAt(x, z);
         Block ground = world.getBlockAt(x, y, z);
         Material groundType = ground.getType();
         if (this.isValidGround(groundType)) {
            Block above = world.getBlockAt(x, y + 1, z);
            if (above.getType().isAir()) {
               Material flower = FLOWERS[this.random.nextInt(FLOWERS.length)];
               above.setType(flower, false);
               this.currentFlowerCount.incrementAndGet();
               return true;
            }
         }
      } catch (Exception var9) {
      }

      return false;
   }

   private boolean isValidGround(Material groundType) {
      return groundType == Material.GRASS_BLOCK || groundType == Material.DIRT;
   }

   public void decreaseFlowerCount() {
      int after = this.currentFlowerCount.decrementAndGet();
      if (after < 0) {
         this.currentFlowerCount.set(0);
      }
   }

   public int getCurrentFlowerCount() {
      return this.currentFlowerCount.get();
   }

   public static boolean isFlower(Material type) {
      return Arrays.asList(FLOWERS).contains(type);
   }
}
