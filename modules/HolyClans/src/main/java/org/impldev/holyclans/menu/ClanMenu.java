package org.impldev.holyclans.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Маркер-холдер для GUI-меню кланов. Все клановые меню определяются по этому типу.
 */
public abstract class ClanMenu implements InventoryHolder {
   protected Inventory inventory;

   @NotNull
   @Override
   public Inventory getInventory() {
      return this.inventory;
   }
}
