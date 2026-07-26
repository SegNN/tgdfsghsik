package ru.lolodimo4ka.holygoldspawner.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuardManager {
   private final JavaPlugin plugin;
   private boolean enabled;

   public WorldGuardManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.enabled = plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean canPlaceSpawner(Player player, Location location) {
      if (!this.enabled) {
         return true;
      } else {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
               return true;
            } else {
               BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
               ApplicableRegionSet regions = regionManager.getApplicableRegions(vector);
               if (regions.size() == 0) {
                  return true;
               } else {
                  LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                  return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(player.getWorld()))
                     ? true
                     : regions.testState(localPlayer, new StateFlag[]{Flags.BUILD});
               }
            }
         } catch (Exception var8) {
            return false;
         }
      }
   }

   private boolean hasRegionRights(Player player, ProtectedRegion region) {
      try {
         String playerName = player.getName();
         String playerUUID = player.getUniqueId().toString();
         Set<String> owners = region.getOwners().getPlayers();
         if (!owners.contains(playerName) && !owners.contains(playerUUID)) {
            Set<String> members = region.getMembers().getPlayers();
            return members.contains(playerName) || members.contains(playerUUID);
         } else {
            return true;
         }
      } catch (Exception var7) {
         return false;
      }
   }

   public boolean isInAnyRegion(Location location) {
      if (!this.enabled) {
         return false;
      } else {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
               return false;
            } else {
               BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
               ApplicableRegionSet regions = regionManager.getApplicableRegions(vector);
               return regions.size() > 0;
            }
         } catch (Exception var6) {
            return false;
         }
      }
   }

   public String getRegionNamesAtLocation(Location location) {
      if (!this.enabled) {
         return "";
      } else {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
               return "";
            } else {
               BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
               ApplicableRegionSet regions = regionManager.getApplicableRegions(vector);
               StringBuilder regionNames = new StringBuilder();

               for (ProtectedRegion region : regions) {
                  if (regionNames.length() > 0) {
                     regionNames.append(", ");
                  }

                  regionNames.append(region.getId());
               }

               return regionNames.toString();
            }
         } catch (Exception var9) {
            return "";
         }
      }
   }

   public boolean isStrictlyForbidden(Location location) {
      return !this.enabled ? false : this.isInAnyRegion(location);
   }

   public void reload() {
      this.enabled = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
      if (this.enabled) {
         this.plugin.getLogger().info("WorldGuard integration reloaded");
      }
   }
}
