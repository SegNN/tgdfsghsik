package ru.kotikstasika.eventdeathmine.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;

public class RegionService {
   public RegionService() {
   }

   public void createRegion(Location center, int halfSize) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(center.getWorld()));
      if (regionManager != null) {
         BlockVector3 pos1 = BlockVector3.at(center.getBlockX() + halfSize, -1, center.getBlockZ() + halfSize);
         BlockVector3 pos2 = BlockVector3.at(center.getBlockX() - halfSize, 300, center.getBlockZ() - halfSize);
         ProtectedCuboidRegion region = new ProtectedCuboidRegion("death_mine_event", pos1, pos2);
         StateFlag holyItemsFlag = (StateFlag)WorldGuard.getInstance().getFlagRegistry().get("holyliteitems");
         if (holyItemsFlag != null) {
            region.setFlag(holyItemsFlag, State.DENY);
         }

         region.setFlag(Flags.BUILD, State.DENY);
         region.setFlag(Flags.WITHER_DAMAGE, State.DENY);
         region.setFlag(Flags.BLOCK_PLACE, State.DENY);
         region.setFlag(Flags.CHEST_ACCESS, State.DENY);
         region.setFlag(Flags.GHAST_FIREBALL, State.DENY);
         region.setFlag(Flags.INTERACT, State.ALLOW);
         region.setFlag(Flags.TNT, State.DENY);
         region.setFlag(Flags.PVP, State.ALLOW);
         region.setFlag(Flags.OTHER_EXPLOSION, State.DENY);
         region.setFlag(Flags.CREEPER_EXPLOSION, State.DENY);
         regionManager.addRegion(region);
      }
   }

   public void removeRegion(Location center) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(center.getWorld()));
      if (regionManager != null) {
         regionManager.removeRegion("death_mine_event");
      }
   }
}
