package org.nezxenka.litecontainer.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nezxenka.litecontainer.LiteContainer;

public class Utils {
   private Utils() {
   }

   public static void addItemInventory(Player player, ItemStack itemStack) {
      if (player != null && itemStack != null) {
         Inventory inventory = player.getInventory();
         Location location = player.getLocation();

         for (int id = 0; id < inventory.getStorageContents().length; id++) {
            ItemStack item = inventory.getItem(id);
            if (item == null || item.getType().isAir()) {
               inventory.addItem(new ItemStack[]{itemStack});
               return;
            }

            int count;
            if (item.isSimilar(itemStack) && (count = item.getMaxStackSize() - item.getAmount()) > 0) {
               if (itemStack.getAmount() <= count) {
                  inventory.addItem(new ItemStack[]{itemStack});
                  return;
               }

               ItemStack i = itemStack.clone();
               i.setAmount(count);
               inventory.addItem(new ItemStack[]{i});
               itemStack.setAmount(itemStack.getAmount() - count);
            }
         }

         Bukkit.getScheduler().runTask(LiteContainer.getInstance(), () -> location.getWorld().dropItemNaturally(location, itemStack));
      }
   }

   public static ItemRarity upgradeRarity(ItemRarity rarity) {
      if (rarity == ItemRarity.DEFAULT) {
         return ItemRarity.RARE;
      } else {
         return rarity == ItemRarity.RARE ? ItemRarity.MYTHIC : ItemRarity.LEGENDARY;
      }
   }

   public static Inventory updateTitle(@NotNull Inventory inventory, String title) {
      Inventory newInventory = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), title);
      newInventory.setContents(inventory.getContents());
      return newInventory;
   }
}
