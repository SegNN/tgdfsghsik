package org.impldev.holycore.extensions;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.impldev.holycore.checker.SpawnerChunkChecker;

public class SpawnerLimitExtension implements Listener {
   public SpawnerLimitExtension() {
   }

   @EventHandler
   public void onSpawnerPlace(BlockPlaceEvent event) {
      if (event.getBlockPlaced().getType() == Material.SPAWNER) {
         Chunk chunk = event.getBlock().getChunk();
         if (!SpawnerChunkChecker.canPlaceSpawner(chunk)) {
            event.setCancelled(true);
            event.getPlayer()
               .sendMessage(
                  ChatColor.translateAlternateColorCodes('&', "&c&l▶ &fВы не можете установить спавнер в этом чанке, достигнут лимит в 16 спавнеров.")
               );
         }
      }
   }
}
