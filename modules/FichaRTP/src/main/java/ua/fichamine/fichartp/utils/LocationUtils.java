package ua.fichamine.fichartp.utils;

import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import ua.fichamine.fichartp.FichaRTP;

public class LocationUtils {
   private static final Random random = new Random();

   public LocationUtils() {
   }

   public static Location getRandomLocation(World world, int minRadius, int maxRadius) {
      int attempts = 0;
      int maxAttempts = 50;

      while (attempts < maxAttempts) {
         attempts++;
         int x = random.nextInt(maxRadius - minRadius) + minRadius;
         int z = random.nextInt(maxRadius - minRadius) + minRadius;
         if (random.nextBoolean()) {
            x = -x;
         }

         if (random.nextBoolean()) {
            z = -z;
         }

         if (isWithinWorldBorder(world, x, z)) {
            int y = world.getHighestBlockYAt(x, z);
            Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (isSafeLocation(location)) {
               return location;
            }
         }
      }

      return world.getSpawnLocation();
   }

   public static boolean isSafeLocation(Location location) {
      if (location != null && location.getWorld() != null) {
         Block block = location.getBlock();
         Block below = block.getRelative(0, -1, 0);
         Block above = block.getRelative(0, 1, 0);
         if (!block.getType().equals(Material.AIR) || !above.getType().equals(Material.AIR)) {
            return false;
         } else if (!below.getType().isSolid()) {
            return false;
         } else {
            List<String> unsafeBlocks = FichaRTP.getInstance().getConfigManager().getUnsafeBlocks();
            if (!unsafeBlocks.contains(below.getType().name())
               && !unsafeBlocks.contains(block.getType().name())
               && !unsafeBlocks.contains(above.getType().name())) {
               for (int x = -1; x <= 1; x++) {
                  for (int z = -1; z <= 1; z++) {
                     Block surrounding = block.getRelative(x, 0, z);
                     if (unsafeBlocks.contains(surrounding.getType().name())) {
                        return false;
                     }
                  }
               }

               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean isWithinWorldBorder(World world, int x, int z) {
      WorldBorder border = world.getWorldBorder();
      Location center = border.getCenter();
      double size = border.getSize() / 2.0;
      return Math.abs(x - center.getX()) <= size && Math.abs(z - center.getZ()) <= size;
   }

   public static Location getRandomLocationAroundPoint(Location center, int minRadius, int maxRadius) {
      int attempts = 0;
      int maxAttempts = 30;

      while (attempts < maxAttempts) {
         attempts++;
         double angle = random.nextDouble() * 2.0 * Math.PI;
         int radius = random.nextInt(maxRadius - minRadius) + minRadius;
         int x = (int)(center.getX() + radius * Math.cos(angle));
         int z = (int)(center.getZ() + radius * Math.sin(angle));
         if (isWithinWorldBorder(center.getWorld(), x, z)) {
            int y = center.getWorld().getHighestBlockYAt(x, z);
            Location location = new Location(center.getWorld(), x + 0.5, y + 1, z + 0.5);
            if (isSafeLocation(location)) {
               return location;
            }
         }
      }

      return center;
   }
}
