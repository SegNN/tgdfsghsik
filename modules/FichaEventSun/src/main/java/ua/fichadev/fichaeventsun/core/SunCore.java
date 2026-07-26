package ua.fichadev.fichaeventsun.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ua.fichadev.fichaeventsun.FichaEventSun;

public class SunCore {
   private final FichaEventSun plugin;

   public SunCore(FichaEventSun plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
      Location loc = this.plugin.getCfg().getCoreLocation();
      if (loc != null && loc.getWorld() != null) {
         Block block = loc.getBlock();
         if (block.getType() != Material.RESPAWN_ANCHOR) {
            block.setType(Material.RESPAWN_ANCHOR);
         }

         this.plugin.getHologramManager().spawn();
         this.plugin.getPhaseManager().startCycle();
      } else {
         this.plugin.getLogger().warning("Некорректная локация ядра");
      }
   }

   public void shutdown() {
      this.plugin.getPhaseManager().stop();
      this.plugin.getHologramManager().removeAll();
      this.plugin.getHologramManager().cleanupOrphaned();
   }
}
