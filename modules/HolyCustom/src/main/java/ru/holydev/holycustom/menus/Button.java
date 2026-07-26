package ru.holydev.holycustom.menus;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Button {
   private final int slot;
   private final ItemStack item;
   private final Consumer<InventoryClickEvent> action;

   public Button(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
      this.slot = slot;
      this.item = item;
      this.action = action;
   }

   public void onClick(InventoryClickEvent e) {
      if (e.getSlot() == this.slot) {
         this.action.accept(e);
      }
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemStack getItem() {
      return this.item;
   }
}
