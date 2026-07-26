package org.dimasik.lootmanager.frontend.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.LootItem;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.EditItem;
import org.dimasik.lootmanager.frontend.menus.Main;

public class EditItemListener implements Listener {
   public EditItemListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof EditItem) {
         if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            if (event.getClick().isShiftClick()) {
               event.setCancelled(true);
            }

            return;
         }

         event.setCancelled(true);
         EditItem editItem = (EditItem)inventory.getHolder();
         LootItem lootItem = editItem.getLootItem();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         if (slot == 3) {
            if (event.getCursor() == null || event.getCursor().getType().isAir()) {
               double now = lootItem.getChance();
               if (event.isLeftClick()) {
                  if (event.isShiftClick()) {
                     now += 10.0;
                  } else {
                     now++;
                  }
               } else if (event.isRightClick()) {
                  if (event.isShiftClick()) {
                     now -= 10.0;
                  } else {
                     now--;
                  }
               }

               now = Math.max(now, 0.0);
               now = Math.min(now, 100.0);
               lootItem.setChance(now);
               player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
               new EditItem(lootItem, editItem.getBack()).setPlayer(player).compile().open();
            }
         } else if (slot == 4) {
            if (event.getCursor() == null || event.getCursor().getType().isAir()) {
               int now = lootItem.getMaxAmount();
               if (event.isLeftClick()) {
                  if (event.isShiftClick()) {
                     now += 10;
                  } else {
                     now++;
                  }
               } else if (event.isRightClick()) {
                  if (event.isShiftClick()) {
                     now -= 10;
                  } else {
                     now--;
                  }
               }

               now = Math.max(now, 1);
               now = Math.min(now, 64);
               lootItem.setMaxAmount(now);
               player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
               new EditItem(lootItem, editItem.getBack()).setPlayer(player).compile().open();
            }
         } else if (slot == 5) {
            if (event.getCursor() == null || event.getCursor().getType().isAir()) {
               int now = lootItem.getLevel();
               if (event.isLeftClick()) {
                  now++;
               } else if (event.isRightClick()) {
                  now--;
               }

               now = Math.max(now, 0);
               now = Math.min(now, 100);
               lootItem.setLevel(now);
               player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
               new EditItem(lootItem, editItem.getBack()).setPlayer(player).compile().open();
            }
         } else if (slot == 17) {
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
               lootItem.setItem(event.getCursor().asOne());
               event.setCursor(null);
               player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
               new EditItem(lootItem, editItem.getBack()).setPlayer(player).compile().open();
            }
         } else if (slot == 18) {
            LootManager.addItemInventory(player.getInventory(), editItem.getLootItem().getItem(), player.getLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
         } else if (slot == 21) {
            Main back = editItem.getBack();
            back.compile().open();
         } else if (slot == 23) {
            player.sendMessage(Parser.color("&#00D4FB▶ &fПредмет сохранен."));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            LootManager.getInstance().getItemManager().setItem(editItem.getLootItem());
         }
      }
   }
}
