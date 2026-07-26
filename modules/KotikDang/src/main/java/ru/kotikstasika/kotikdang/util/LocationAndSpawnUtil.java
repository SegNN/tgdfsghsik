package ru.kotikstasika.kotikdang.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.dang.DangType;

public class LocationAndSpawnUtil {
   private static final Set<Location> spawnedDangs = new HashSet<>();
   private static final int MAX_REGION_ATTEMPTS = 10;
   private static final int MAX_BIOME_SEARCH_ATTEMPTS = 500;
   private static final int SEARCH_RADIUS = 500;

   public LocationAndSpawnUtil() {
   }

   public static CompletableFuture<Location> findSuitableLocation(final World world, final DangType type, final int attempts) {
      final CompletableFuture<Location> future = new CompletableFuture<>();
      (new BukkitRunnable() {
         int tries = 0;

         public void run() {
            if (this.tries++ >= attempts) {
               future.complete(null);
               this.cancel();
            } else {
               Location base = LocationAndSpawnUtil.generateRandomLocation(world, type);
               if (base != null) {
                  Location found = LocationAndSpawnUtil.findLocationOutsideRegions(base, type, 10);
                  if (found != null) {
                     LocationAndSpawnUtil.spawnedDangs.add(found);
                     future.complete(found);
                     this.cancel();
                  }
               }
            }
         }
      }).runTaskTimer(KotikDang.plugin, 1L, 1L);
      return future;
   }

   private static Location findLocationOutsideRegions(Location initialLocation, DangType type, int attempts) {
      Location currentLocation = initialLocation.clone();

      for (int i = 0; i < attempts; i++) {
         if (isLocationValid(currentLocation, type)) {
            return currentLocation;
         }

         currentLocation = getRandomShiftedLocation(initialLocation, 500);
      }

      return null;
   }

   private static Location getRandomShiftedLocation(Location base, int radius) {
      World world = base.getWorld();
      int offsetX = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
      int offsetZ = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
      return new Location(world, base.getX() + offsetX, base.getY(), base.getZ() + offsetZ);
   }

   private static Location generateRandomLocation(World world, DangType type) {
      WorldBorder border = world.getWorldBorder();
      int size = (int)border.getSize() / 2;
      Location center = border.getCenter();
      int x = ThreadLocalRandom.current().nextInt((int)center.getX() - size, (int)center.getX() + size);
      int z = ThreadLocalRandom.current().nextInt((int)center.getZ() - size, (int)center.getZ() + size);
      if (type.getDangType() == DangType.Type.NO_RG) {
         return findBiomeLocation(world, x, z, type);
      } else {
         Location loc = new Location(world, x, 0.0, z);
         loc.setY(world.getHighestBlockYAt(x, z) + 1);
         return loc;
      }
   }

   private static Location findBiomeLocation(World world, int x, int z, DangType type) {
      ConfigurationSection noRgSection = KotikDang.plugin.getConfig().getConfigurationSection("dangs.no_rg");
      if (noRgSection == null) {
         return null;
      } else {
         Set<Biome> targetBiomes = new HashSet<>();

         for (String key : noRgSection.getKeys(false)) {
            ConfigurationSection config = noRgSection.getConfigurationSection(key);
            if (config != null && config.contains("biomes")) {
               for (String biomeName : config.getStringList("biomes")) {
                  if ("ALL".equalsIgnoreCase(biomeName)) {
                     return findAnyLocation(world, x, z);
                  }

                  try {
                     targetBiomes.add(Biome.valueOf(biomeName.toUpperCase()));
                  } catch (IllegalArgumentException var13) {
                  }
               }
            }
         }

         if (targetBiomes.isEmpty()) {
            return findAnyLocation(world, x, z);
         } else {
            for (int i = 0; i < 500; i++) {
               int offsetX = x + ThreadLocalRandom.current().nextInt(-500, 501);
               int offsetZ = z + ThreadLocalRandom.current().nextInt(-500, 501);
               if (world.isChunkLoaded(offsetX >> 4, offsetZ >> 4)) {
                  Location loc = new Location(world, offsetX, 0.0, offsetZ);
                  loc.setY(world.getHighestBlockYAt(loc) + 1);
                  if (isValidLocation(loc)) {
                     Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                     if (targetBiomes.contains(biome)) {
                        return loc;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   private static Location findAnyLocation(World world, int x, int z) {
      for (int i = 0; i < 500; i++) {
         int offsetX = x + ThreadLocalRandom.current().nextInt(-500, 501);
         int offsetZ = z + ThreadLocalRandom.current().nextInt(-500, 501);
         if (world.isChunkLoaded(offsetX >> 4, offsetZ >> 4)) {
            Location loc = new Location(world, offsetX, 0.0, offsetZ);
            loc.setY(world.getHighestBlockYAt(loc) + 1);
            if (isValidLocation(loc)) {
               return loc;
            }
         }
      }

      return null;
   }

   private static boolean isValidLocation(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         Block feet = loc.getBlock();
         Block head = feet.getRelative(BlockFace.UP);
         Block groundBlock = feet.getRelative(BlockFace.DOWN);
         Material groundType = groundBlock.getType();
         if (!groundType.isSolid()) {
            return false;
         } else if (isUnsafeGround(groundType)) {
            return false;
         } else if (feet.getType().isAir() && head.getType().isAir()) {
            if (!feet.isLiquid() && !head.isLiquid()) {
               for (Location spawned : spawnedDangs) {
                  if (spawned.getWorld().equals(loc.getWorld()) && spawned.distanceSquared(loc) < 4096.0) {
                     return false;
                  }
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isLocationValid(Location location, DangType type) {
      return !isValidLocation(location) ? false : !isInProtectedRegion(location);
   }

   private static boolean isUnsafeGround(Material groundType) {
      return switch (groundType) {
         case WATER, LAVA, AIR, CACTUS, MAGMA_BLOCK, CAMPFIRE, FIRE, SOUL_FIRE, COBWEB, SAND, RED_SAND, GRAVEL -> true;
         default -> false;
      };
   }

   private static boolean isInProtectedRegion(Location location) {
      try {
         RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
         return regionManager == null
            ? false
            : !regionManager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint()).getRegions().isEmpty();
      } catch (Exception var2) {
         KotikDang.plugin.getLogger().warning("Ошибка при проверке регионов WorldGuard: " + var2.getMessage());
         return false;
      }
   }
}
