package org.dimasik.lootmanager.backend.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PacketSender {
   public PacketSender() {
   }

   public static void sendAnvilSlotUpdate(Player player, int slot, ItemStack itemStack) {
      if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {
         player.getOpenInventory().getTopInventory().setItem(slot, itemStack);
         player.updateInventory();
      }
   }
}
