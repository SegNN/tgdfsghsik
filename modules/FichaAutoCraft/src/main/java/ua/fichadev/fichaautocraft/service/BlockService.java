package ua.fichadev.fichaautocraft.service;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataType;
import ua.fichadev.fichaautocraft.FichaAutoCraft;

public class BlockService {
   public BlockService() {
   }

   public static boolean isAutoCraftBlock(Block block) {
      if (block.getType() != Material.DISPENSER) {
         return false;
      } else if (!(block.getState() instanceof TileState)) {
         return false;
      } else {
         TileState state = (TileState)block.getState();
         NamespacedKey key = FichaAutoCraft.getInstance().getConfigManager().getCraftKey();
         return state.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
      }
   }

   public static void markAsAutoCraft(TileState state, NamespacedKey key) {
      state.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
      state.update();
   }
}
