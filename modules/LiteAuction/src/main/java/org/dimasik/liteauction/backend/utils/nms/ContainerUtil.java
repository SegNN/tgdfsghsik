package org.dimasik.liteauction.backend.utils.nms;

import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class ContainerUtil {
   public static Inventory getActiveContainer(Player player) {
      return player == null ? null : player.getOpenInventory().getTopInventory();
   }

   @Generated
   private ContainerUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
