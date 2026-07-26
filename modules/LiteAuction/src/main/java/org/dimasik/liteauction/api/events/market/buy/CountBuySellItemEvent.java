package org.dimasik.liteauction.api.events.market.buy;

import lombok.Generated;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

public class CountBuySellItemEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   private final Player player;
   private final SellItem item;
   private final int amount;

   public CountBuySellItemEvent(Player player, SellItem item, int amount) {
      this.player = player;
      this.item = item;
      this.amount = amount;
   }

   @Generated
   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public SellItem getItem() {
      return this.item;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
