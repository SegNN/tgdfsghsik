package org.impldev.holyclans;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Проверка WorldGuard: можно ли ставить клановый хом в данной точке.
 * Разрешено в глобальном регионе и в своём, запрещено в чужих.
 */
public final class RegionCheck {

   private RegionCheck() {}

   /**
    * Возвращает {@code true}, если локация находится в чужом регионе
    * (в котором у игрока нет прав owner/member).
    */
   public static boolean isInForeignRegion(Player player, Location loc) {
      try {
         RegionManager rm = WorldGuard.getInstance().getPlatform()
               .getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
         if (rm == null) return false;

         ApplicableRegionSet set = rm.getApplicableRegions(
               BukkitAdapter.asBlockVector(loc));

         com.sk89q.worldguard.LocalPlayer localPlayer =
               WorldGuardPlugin.inst().wrapPlayer(player);

         for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase("__global__")) continue;
            if (!region.isOwner(localPlayer) && !region.isMember(localPlayer)) {
               return true;
            }
         }
         return false;
      } catch (Exception e) {
         return false;
      }
   }
}
