package org.dimasik.litebinds.menu.abst;

import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu implements InventoryHolder {
   protected Player viewer;
   protected Inventory inventory;

   public AbstractMenu() {
   }

   public AbstractMenu setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   public void open() {
      this.viewer.openInventory(this.inventory);
   }

   public abstract AbstractMenu compile();

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }
}
