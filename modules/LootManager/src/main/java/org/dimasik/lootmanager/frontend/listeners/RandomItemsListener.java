package org.dimasik.lootmanager.frontend.listeners;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.lootmanager.LootAPI;
import org.dimasik.lootmanager.frontend.menus.Main;
import org.dimasik.lootmanager.frontend.menus.Properties;
import org.dimasik.lootmanager.frontend.menus.RandomItems;

public class RandomItemsListener implements Listener {
   public RandomItemsListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof RandomItems) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         RandomItems randomItems = (RandomItems)inventory.getHolder();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         if (slot == 45) {
            Main main = randomItems.getBack();
            main.compile().open();
         } else if (slot == 48) {
            int newPage = randomItems.getPage() - 1;
            List<ItemStack> items = randomItems.getItems();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            if (newPage != randomItems.getPage()) {
               player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
               RandomItems newRandomItems = new RandomItems(newPage, randomItems.getItems(), randomItems.getBack()).setPlayer(player).compile();
               newRandomItems.setIitems(randomItems.getIitems());
               newRandomItems.setLevel(randomItems.getLevel());
               newRandomItems.open();
            }
         } else if (slot == 49) {
            new Properties(randomItems.getIitems(), randomItems.getLevel(), randomItems.getBack()).setPlayer(player).compile().open();
         } else if (slot == 50) {
            int newPage = randomItems.getPage() + 1;
            List<ItemStack> items = randomItems.getItems();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            if (newPage != randomItems.getPage()) {
               player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
               RandomItems newRandomItems = new RandomItems(newPage, randomItems.getItems(), randomItems.getBack()).setPlayer(player).compile();
               newRandomItems.setIitems(randomItems.getIitems());
               newRandomItems.setLevel(randomItems.getLevel());
               newRandomItems.open();
            }
         } else if (slot == 53) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            RandomItems newRandomItems = new RandomItems(
                  1, new LootAPI().getRandomItems(randomItems.getIitems(), randomItems.getLevel()), randomItems.getBack()
               )
               .setPlayer(player)
               .compile();
            newRandomItems.setIitems(randomItems.getIitems());
            newRandomItems.setLevel(randomItems.getLevel());
            newRandomItems.open();
         }
      }
   }
}
