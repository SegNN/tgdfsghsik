package org.dimasik.liteauction.api.events.market.buy;

import lombok.Generated;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.api.events.Cancellable;
import org.dimasik.liteauction.api.events.Event;
import org.dimasik.liteauction.backend.storage.models.SellItem;

public class BuySellItemEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   private final Player player;
   private final SellItem item;

   public BuySellItemEvent(Player player, SellItem item) {
      this.player = player;
      this.item = item;
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
   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
