package org.nezxenka.litecontainer.menu;

import java.util.Arrays;
import java.util.List;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Button {
   private final ItemStack item;
   private final List<Integer> slots;
   private final ButtonClickHandler handler;

   public Button(ItemStack item, Integer[] slots, ButtonClickHandler handler) {
      this.item = item;
      this.slots = Arrays.asList(slots);
      this.handler = handler;
   }

   public Button(ItemStack item, Integer[] slots) {
      this(item, slots, null);
   }

   public void onClick(InventoryClickEvent event) {
      if (this.handler != null) {
         this.handler.onClick(event);
      }
   }

   public ItemStack getItem() {
      return this.item;
   }

   public List<Integer> getSlots() {
      return this.slots;
   }
}
