package org.dimasik.lootmanager.frontend.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.LootItem;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.dimasik.lootmanager.frontend.menus.Configs;
import org.dimasik.lootmanager.frontend.menus.EditItem;
import org.dimasik.lootmanager.frontend.menus.Main;
import org.dimasik.lootmanager.frontend.menus.Properties;
import org.dimasik.lootmanager.frontend.menus.RemoveItem;

public class MainListener implements Listener {
   public MainListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      Inventory inventory = event.getView().getTopInventory();
      if (inventory.getHolder() instanceof Main) {
         event.setCancelled(true);
         if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
         }

         Main main = (Main)inventory.getHolder();
         Player player = (Player)event.getWhoClicked();
         int slot = event.getSlot();
         if (slot < 45) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
               return;
            }

            LootItem lootItem = main.getItems().get(slot);
            if (lootItem != null) {
               if (event.isLeftClick()) {
                  new EditItem(lootItem, main).setPlayer(player).compile().open();
               } else if (event.isRightClick()) {
                  new RemoveItem(lootItem, main).setPlayer(player).compile().open();
               }
            }
         } else if (slot == 45) {
            new Properties(5, 1, main).setPlayer(player).compile().open();
         } else if (slot == 46) {
            new Configs(1, main).setPlayer(player).compile().open();
         } else if (slot == 47) {
            player.sendMessage(Parser.color("&#00D4FB▶ &fПредметы обновлены."));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0F, 1.0F);
            int newPage = main.getPage();
            List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
            List<LootItem> items = allItems.stream()
               .filter(item -> main.getCurrentFilterLevel() == 0 || item.getLevel() == main.getCurrentFilterLevel())
               .filter(item -> main.getCurrentCategory().equals("Все подряд") || item.getCategory().equals(main.getCurrentCategory()))
               .toList();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            Main newMain = new Main(newPage);
            newMain.setCurrentFilterLevel(main.getCurrentFilterLevel());
            newMain.setCurrentCategory(main.getCurrentCategory());
            newMain.setPlayer(player).compile().open();
         } else if (slot == 48) {
            int newPage = main.getPage() - 1;
            List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
            List<LootItem> items = allItems.stream()
               .filter(item -> main.getCurrentFilterLevel() == 0 || item.getLevel() == main.getCurrentFilterLevel())
               .filter(item -> main.getCurrentCategory().equals("Все подряд") || item.getCategory().equals(main.getCurrentCategory()))
               .toList();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            if (newPage != main.getPage()) {
               player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
               Main newMain = new Main(newPage);
               newMain.setCurrentFilterLevel(main.getCurrentFilterLevel());
               newMain.setCurrentCategory(main.getCurrentCategory());
               newMain.setPlayer(player).compile().open();
            }
         } else if (slot == 50) {
            int newPage = main.getPage() + 1;
            List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
            List<LootItem> items = allItems.stream()
               .filter(item -> main.getCurrentFilterLevel() == 0 || item.getLevel() == main.getCurrentFilterLevel())
               .filter(item -> main.getCurrentCategory().equals("Все подряд") || item.getCategory().equals(main.getCurrentCategory()))
               .toList();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            if (newPage != main.getPage()) {
               player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
               Main newMain = new Main(newPage);
               newMain.setCurrentFilterLevel(main.getCurrentFilterLevel());
               newMain.setCurrentCategory(main.getCurrentCategory());
               newMain.setPlayer(player).compile().open();
            }
         } else if (slot == 52) {
            int newPage = main.getPage();
            List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
            List<LootItem> items = allItems.stream()
               .filter(item -> main.getCurrentFilterLevel() == 0 || item.getLevel() == main.getCurrentFilterLevel())
               .filter(item -> main.getCurrentCategory().equals("Все подряд") || item.getCategory().equals(main.getCurrentCategory()))
               .toList();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            Main newMain = new Main(newPage);
            newMain.setCurrentCategory(main.getCurrentCategory());
            if (event.isLeftClick()) {
               if (event.isShiftClick()) {
                  newMain.setCurrentFilterLevel(0);
                  newMain.setPlayer(player).compile().open();
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
               } else {
                  int newLevel = main.getCurrentFilterLevel() + 1;
                  if (newLevel > 100) {
                     newLevel = 1;
                  }

                  newMain.setCurrentFilterLevel(newLevel);
                  newMain.setPlayer(player).compile().open();
                  player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
               }
            } else if (event.isRightClick()) {
               int newLevel = main.getCurrentFilterLevel() - 1;
               if (newLevel < 0) {
                  newLevel = 100;
               }

               newMain.setCurrentFilterLevel(newLevel);
               newMain.setPlayer(player).compile().open();
               player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            }
         } else if (slot == 53) {
            int newPage = main.getPage();
            List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
            List<LootItem> items = allItems.stream()
               .filter(item -> main.getCurrentFilterLevel() == 0 || item.getLevel() == main.getCurrentFilterLevel())
               .filter(item -> main.getCurrentCategory().equals("Все подряд") || item.getCategory().equals(main.getCurrentCategory()))
               .toList();
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            newPage = Math.min(pages, newPage);
            newPage = Math.max(1, newPage);
            Main newMain = new Main(newPage);
            newMain.setCurrentFilterLevel(main.getCurrentFilterLevel());
            if (event.isLeftClick()) {
               newMain.setCurrentCategory(this.getNextCategory(main.getCurrentCategory()));
               newMain.setPlayer(player).compile().open();
               player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            } else if (event.isRightClick()) {
               newMain.setCurrentCategory(this.getPrevCategory(main.getCurrentCategory()));
               newMain.setPlayer(player).compile().open();
               player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            }
         }
      }
   }

   public String getNextCategory(String current) {
      List<String> categories = LootManager.getInstance().getItemManager().getAllCategories().stream().sorted().collect(Collectors.toList());
      categories.add(0, "Все подряд");
      int currentIndex = categories.indexOf(current);
      return currentIndex != -1 && currentIndex != categories.size() - 1 ? categories.get(currentIndex + 1) : categories.get(0);
   }

   public String getPrevCategory(String current) {
      List<String> categories = LootManager.getInstance().getItemManager().getAllCategories().stream().sorted().collect(Collectors.toList());
      categories.add(0, "Все подряд");
      int currentIndex = categories.indexOf(current);
      return currentIndex <= 0 ? categories.get(categories.size() - 1) : categories.get(currentIndex - 1);
   }
}
