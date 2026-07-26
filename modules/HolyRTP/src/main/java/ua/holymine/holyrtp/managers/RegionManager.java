package ua.holymine.holyrtp.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ua.holymine.holyrtp.HolyRTP;
import ua.holymine.holyrtp.utils.LocationUtils;

public class RegionManager {
   private final HolyRTP plugin;
   private final Random random = new Random();
   private boolean worldGuardEnabled = false;
   private boolean protectionStonesEnabled = false;

   public RegionManager(HolyRTP plugin) {
      this.plugin = plugin;
      this.checkPlugins();
   }

   private void checkPlugins() {
      this.worldGuardEnabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
      this.protectionStonesEnabled = Bukkit.getPluginManager().getPlugin("ProtectionStones") != null;
   }

   public Location getRandomRegionLocation(Player player) {
      List<Location> regionCenters = new ArrayList<>();
      if (this.worldGuardEnabled && this.plugin.getConfigManager().isUseWorldGuard()) {
         regionCenters.addAll(this.getWorldGuardRegions(player));
      }

      if (this.protectionStonesEnabled && this.plugin.getConfigManager().isUseProtectionStones()) {
         regionCenters.addAll(this.getProtectionStonesRegions(player));
      }

      if (regionCenters.isEmpty()) {
         return null;
      } else {
         Location regionCenter = regionCenters.get(this.random.nextInt(regionCenters.size()));
         int minRadius = this.plugin.getConfigManager().getBaseMinRadius();
         int maxRadius = this.plugin.getConfigManager().getBaseMaxRadius();
         return LocationUtils.getRandomLocationAroundPoint(regionCenter, minRadius, maxRadius);
      }
   }

   private List<Location> getWorldGuardRegions(Player player) {
      List<Location> centers = new ArrayList<>();
      if (!this.worldGuardEnabled) {
         return centers;
      } else {
         try {
            List<String> excludedWorlds = this.plugin.getConfigManager().getBaseExcludedWorlds();

            for (World world : Bukkit.getWorlds()) {
               if (!excludedWorlds.contains(world.getName())) {
                  com.sk89q.worldguard.protection.managers.RegionManager wgRegionManager = WorldGuard.getInstance()
                     .getPlatform()
                     .getRegionContainer()
                     .get(BukkitAdapter.adapt(world));
                  if (wgRegionManager != null) {
                     for (ProtectedRegion region : wgRegionManager.getRegions().values()) {
                        if (region.getOwners().contains(player.getUniqueId()) || region.getMembers().contains(player.getUniqueId())) {
                           BlockVector3 min = region.getMinimumPoint();
                           BlockVector3 max = region.getMaximumPoint();
                           BlockVector3 center = min.add(max).divide(2);
                           Location location = new Location(world, center.getX(), world.getHighestBlockYAt(center.getX(), center.getZ()), center.getZ());
                           centers.add(location);
                        }
                     }
                  }
               }
            }
         } catch (Exception var13) {
            this.plugin.getLogger().warning("Error getting WorldGuard regions: " + var13.getMessage());
         }

         return centers;
      }
   }

   private List<Location> getProtectionStonesRegions(Player player) {
      List<Location> centers = new ArrayList<>();
      if (!this.protectionStonesEnabled) {
         return centers;
      } else {
         try {
            List<String> excludedWorlds = this.plugin.getConfigManager().getBaseExcludedWorlds();

            for (World world : Bukkit.getWorlds()) {
               if (!excludedWorlds.contains(world.getName())) {
                  com.sk89q.worldguard.protection.managers.RegionManager wgRegionManager = WorldGuard.getInstance()
                     .getPlatform()
                     .getRegionContainer()
                     .get(BukkitAdapter.adapt(world));
                  if (wgRegionManager != null) {
                     for (ProtectedRegion region : wgRegionManager.getRegions().values()) {
                        if (region.getId().startsWith("ps")
                           && (region.getOwners().contains(player.getUniqueId()) || region.getMembers().contains(player.getUniqueId()))) {
                           BlockVector3 min = region.getMinimumPoint();
                           BlockVector3 max = region.getMaximumPoint();
                           BlockVector3 center = min.add(max).divide(2);
                           Location location = new Location(world, center.getX(), world.getHighestBlockYAt(center.getX(), center.getZ()), center.getZ());
                           centers.add(location);
                        }
                     }
                  }
               }
            }
         } catch (Exception var13) {
            this.plugin.getLogger().warning("Error getting ProtectionStones regions via WorldGuard: " + var13.getMessage());
         }

         return centers;
      }
   }
}
