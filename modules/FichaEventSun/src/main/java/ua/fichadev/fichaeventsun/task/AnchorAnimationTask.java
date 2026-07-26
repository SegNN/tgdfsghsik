package ua.fichadev.fichaeventsun.task;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichaeventsun.FichaEventSun;

public class AnchorAnimationTask {
   private final FichaEventSun plugin;
   private int tickCounter;

   public AnchorAnimationTask(FichaEventSun plugin) {
      this.plugin = plugin;
      this.tickCounter = 0;
   }

   public void start() {
      final int lootInterval = this.plugin.getCfg().getLootDropIntervalTicks();
      final int stageInterval = lootInterval / 4;
      (new BukkitRunnable() {
         public void run() {
            AnchorAnimationTask.this.tickCounter += 20;
            Location coreLoc = AnchorAnimationTask.this.plugin.getCfg().getCoreLocation();
            if (coreLoc != null && coreLoc.getWorld() != null) {
               Block block = coreLoc.getBlock();
               if (block.getType() == Material.RESPAWN_ANCHOR) {
                  RespawnAnchor anchor = (RespawnAnchor)block.getBlockData();
                  int stage = Math.min(AnchorAnimationTask.this.tickCounter / stageInterval, 4);
                  anchor.setCharges(stage);
                  block.setBlockData(anchor, false);
                  int remaining = (lootInterval - AnchorAnimationTask.this.tickCounter) / 20;
                  if (remaining < 0) {
                     remaining = 0;
                  }

                  AnchorAnimationTask.this.plugin.getHologramManager().setLootCountdown(remaining);
                  if (AnchorAnimationTask.this.tickCounter >= lootInterval) {
                     AnchorAnimationTask.this.tickCounter = 0;
                     anchor.setCharges(0);
                     block.setBlockData(anchor, false);
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 20L, 20L);
   }
}
