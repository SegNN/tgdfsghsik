package org.dimasik.liteexchange.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.Native;

@Native
public class PacketSender {
   public PacketSender() {
   }

   public static void sendAnvilSlotUpdate(Player player, int slot, ItemStack itemStack) {
      Inventory top = player.getOpenInventory().getTopInventory();
      if (top instanceof AnvilInventory && slot >= 0 && slot < top.getSize()) {
         top.setItem(slot, itemStack);
         player.updateInventory();
      }
   }
}
