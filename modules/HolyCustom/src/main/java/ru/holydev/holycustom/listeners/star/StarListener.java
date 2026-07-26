package ru.holydev.holycustom.listeners.star;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.holydev.holycustom.holders.StarMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.menus.MainMenu;

public class StarListener implements Listener {
   public StarListener() {
   }

   @EventHandler
   public void on(InventoryClickEvent e) {
      Player player = (Player)e.getWhoClicked();
      if (e.getClickedInventory() != null
         && e.getClickedInventory().getHolder() != null
         && e.getWhoClicked() instanceof Player
         && e.getClickedInventory().getHolder() instanceof StarMenuHolder) {
         e.setCancelled(true);
         if (e.getSlot() == 53) {
            MenuItems menuItems = new MenuItems();
            MainMenu mainMenu = new MainMenu(menuItems);
            mainMenu.open(player);
         }
      }
   }
}
