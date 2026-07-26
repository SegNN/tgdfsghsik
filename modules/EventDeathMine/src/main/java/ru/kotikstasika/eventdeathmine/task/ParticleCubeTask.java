package ru.kotikstasika.eventdeathmine.task;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;

public class ParticleCubeTask extends BukkitRunnable {
   private final DeathMineManager manager;
   private final Location center;
   private final int size;
   private final long durationSeconds;
   private final double step = 0.75;
   private final DustOptions dustOptions = new DustOptions(Color.fromBGR(16759867), 1.0F);
   private final long startTime = System.currentTimeMillis();

   public ParticleCubeTask(DeathMineManager manager, Location center, int size, long durationSeconds) {
      this.manager = manager;
      this.center = center;
      this.size = size;
      this.durationSeconds = durationSeconds;
   }

   public void run() {
      if (System.currentTimeMillis() - this.startTime >= this.durationSeconds * 10000L) {
         this.manager.getActiveCubes().remove(this.center);
         this.cancel();
      } else {
         this.drawCubeParticles(this.center.getWorld(), this.center, this.size, 0.75, this.dustOptions);
      }
   }

   private void drawCubeParticles(World world, Location center, int size, double step, DustOptions dustOptions) {
      double half = size / 2.0;
      double minX = center.getX() - half;
      double minY = center.getY() - half;
      double minZ = center.getZ() - half;
      double maxX = center.getX() + half;
      double maxY = center.getY() + half;
      double maxZ = center.getZ() + half;
      double x = minX;

      while (x <= maxX) {
         world.spawnParticle(Particle.REDSTONE, x, minY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, x, minY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, x, maxY, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, x, maxY, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         x += step;
      }

      for (double y = minY; y <= maxY; y += step) {
         world.spawnParticle(Particle.REDSTONE, minX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, minX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, maxX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, maxX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
      }

      for (double z = minZ; z <= maxZ; z += step) {
         world.spawnParticle(Particle.REDSTONE, minX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, minX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, maxX, minY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         world.spawnParticle(Particle.REDSTONE, maxX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
      }
   }
}
