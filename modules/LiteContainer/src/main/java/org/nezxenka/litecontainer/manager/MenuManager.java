package org.nezxenka.litecontainer.manager;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.inventory.Inventory;
import org.nezxenka.litecontainer.menu.Menu;

public class MenuManager {
   private final Set<Menu> menus = new HashSet<>();

   public MenuManager() {
   }

   public void registerMenu(Menu menu) {
      this.menus.add(menu);
   }

   public void unregisterMenu(Menu menu) {
      this.menus.remove(menu);
   }

   public boolean isMenuInventory(Inventory inventory) {
      return inventory.getHolder() instanceof Menu && this.menus.contains(inventory.getHolder());
   }
}
