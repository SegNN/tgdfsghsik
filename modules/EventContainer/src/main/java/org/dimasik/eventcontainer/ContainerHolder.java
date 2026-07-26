package org.dimasik.eventcontainer;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ContainerHolder implements InventoryHolder {
   public ContainerHolder() {
   }

   @NotNull
   public Inventory getInventory() {
      return null;
   }
}
