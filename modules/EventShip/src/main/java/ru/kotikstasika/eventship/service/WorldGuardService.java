package ru.kotikstasika.eventship.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;

public class WorldGuardService {
   public WorldGuardService() {
   }

   public void createRegion(String regionId, Location center, int radius) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(center.getWorld()));
      if (regionManager != null) {
         BlockVector3 pos1 = BlockVector3.at(center.getBlockX() + radius, -1, center.getBlockZ() + radius);
         BlockVector3 pos2 = BlockVector3.at(center.getBlockX() - radius, 300, center.getBlockZ() - radius);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, pos1, pos2);
         StateFlag holyItemsFlag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         if (holyItemsFlag != null) {
            region.setFlag(holyItemsFlag, State.DENY);
         }

         region.setFlag(Flags.BUILD, State.DENY);
         region.setFlag(Flags.BLOCK_BREAK, State.DENY);
         region.setFlag(Flags.PVP, State.ALLOW);
         region.setFlag(Flags.BLOCK_PLACE, State.DENY);
         region.setFlag(Flags.INTERACT, State.ALLOW);
         region.setFlag(Flags.WITHER_DAMAGE, State.DENY);
         region.setFlag(Flags.CHEST_ACCESS, State.DENY);
         region.setFlag(Flags.GHAST_FIREBALL, State.DENY);
         region.setFlag(Flags.TNT, State.DENY);
         region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
         region.setFlag(Flags.CREEPER_EXPLOSION, State.DENY);
         regionManager.addRegion(region);
      }
   }

   public void removeRegion(String regionId, Location center) {
      if (center != null) {
         RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(center.getWorld()));
         if (regionManager != null) {
            regionManager.removeRegion(regionId);
         }
      }
   }
}
