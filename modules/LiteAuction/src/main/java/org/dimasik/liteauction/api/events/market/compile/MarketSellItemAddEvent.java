package org.dimasik.liteauction.api.events.market.compile;

import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

public class MarketSellItemAddEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   private final SellItem sellItem;
   private final Inventory inventory;
   private final int slot;

   public MarketSellItemAddEvent(SellItem sellItem, Inventory inventory, int slot) {
      this.sellItem = sellItem;
      this.inventory = inventory;
      this.slot = slot;
   }

   @Generated
   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public SellItem getSellItem() {
      return this.sellItem;
   }

   @Generated
   public Inventory getInventory() {
      return this.inventory;
   }

   @Generated
   public int getSlot() {
      return this.slot;
   }

   @Generated
   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
