package org.nezxenka.litecontainer.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.nezxenka.litecontainer.LiteContainer;
import org.nezxenka.litecontainer.menu.ContainerAnimationMenu;
import org.nezxenka.litecontainer.menu.Menu;

public class MenuListener implements Listener {
   public MenuListener() {
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() instanceof Menu) {
         Menu menu = (Menu)event.getInventory().getHolder();
         menu.handleClick(event);
      }

      if (LiteContainer.getInstance().getMenuManager().isMenuInventory(event.getInventory())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder() instanceof Menu) {
         Menu menu = (Menu)event.getInventory().getHolder();
         LiteContainer.getInstance().getMenuManager().unregisterMenu(menu);
         if (menu instanceof ContainerAnimationMenu) {
            ((ContainerAnimationMenu)menu).onClose(event);
         }
      }
   }
}
