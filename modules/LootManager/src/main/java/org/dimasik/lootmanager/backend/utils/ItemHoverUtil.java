package org.dimasik.lootmanager.backend.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemHoverUtil {
   public ItemHoverUtil() {
   }

   public static void sendHoverItemMessage(Player player, String message, ItemStack hoverItem) {
      player.sendMessage(message.replace("%item%", getItemDisplayName(hoverItem)));
   }

   public static String getHoverItemMessage(String message, ItemStack hoverItem) {
      return message.replace("%item%", getItemDisplayName(hoverItem));
   }

   private static String getItemDisplayName(ItemStack item) {
      return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
         ? item.getItemMeta().getDisplayName()
         : "[" + item.getType().toString().toUpperCase() + "]";
   }
}
