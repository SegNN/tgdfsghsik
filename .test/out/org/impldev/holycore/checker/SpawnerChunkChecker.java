package org.impldev.holycore.checker;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class SpawnerChunkChecker {
   private static final int MAX_SPAWNERS_PER_CHUNK = 16;

   public SpawnerChunkChecker() {
   }

   public static boolean canPlaceSpawner(Chunk chunk) {
      int count = 0;

      for (BlockState state : chunk.getTileEntities()) {
         if (state.getType() == Material.SPAWNER) {
            if (++count >= 16) {
               return false;
            }
         }
      }

      return true;
   }
}
