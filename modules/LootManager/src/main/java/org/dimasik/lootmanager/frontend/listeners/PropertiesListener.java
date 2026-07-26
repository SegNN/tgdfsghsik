package org.dimasik.lootmanager.frontend.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.lootmanager.LootAPI;
import org.dimasik.lootmanager.frontend.menus.Main;
import org.dimasik.lootmanager.frontend.menus.Properties;
import org.dimasik.lootmanager.frontend.menus.RandomItems;

public class PropertiesListener implements Listener {
   public PropertiesListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof Properties) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         Properties properties = (Properties)inventory.getHolder();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         if (slot == 0) {
            int now = properties.getItems();
            if (event.isLeftClick()) {
               if (event.isShiftClick()) {
                  now += 16;
               } else {
                  now++;
               }
            } else if (event.isRightClick()) {
               if (event.isShiftClick()) {
                  now -= 16;
               } else {
                  now--;
               }
            }

            now = Math.max(now, 1);
            now = Math.min(now, 300);
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            new Properties(now, properties.getLevel(), properties.getBack()).setPlayer(player).compile().open();
         } else if (slot == 1) {
            int now = properties.getLevel();
            if (event.isLeftClick()) {
               now++;
            } else if (event.isRightClick()) {
               now--;
            }

            now = Math.max(now, 1);
            now = Math.min(now, 100);
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            new Properties(properties.getItems(), now, properties.getBack()).setPlayer(player).compile().open();
         } else if (slot == 3) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            RandomItems randomItems = new RandomItems(1, new LootAPI().getRandomItems(properties.getItems(), properties.getLevel()), properties.getBack())
               .setPlayer(player)
               .compile();
            randomItems.setIitems(properties.getItems());
            randomItems.setLevel(properties.getLevel());
            randomItems.open();
         } else if (slot == 4) {
            Main back = properties.getBack();
            back.compile().open();
         }
      }
   }
}
