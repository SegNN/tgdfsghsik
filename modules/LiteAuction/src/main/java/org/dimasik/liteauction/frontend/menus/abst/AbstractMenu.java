package org.dimasik.liteauction.frontend.menus.abst;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.dimasik.liteauction.LiteAuction;
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
      if (this.viewer != null && this.inventory != null) {
         Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> this.viewer.openInventory(this.inventory));
      }
   }

   public abstract AbstractMenu compile();

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   public void close() {
      if (this.viewer != null) {
         Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> this.viewer.closeInventory());
      }
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }
}
