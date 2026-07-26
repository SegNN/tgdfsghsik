package org.nezxenka.litecontainer.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.nezxenka.litecontainer.LiteContainer;

public abstract class Menu implements InventoryHolder {
   protected List<Button> buttons = new ArrayList<>();
   protected Inventory inventory;
   protected Player opener;

   public Menu() {
   }

   public abstract String getTitle();

   public abstract int getSize();

   public abstract void getItemsAndButtons();

   public void show(Player player) {
      this.opener = player;
      this.inventory = Bukkit.createInventory(this, this.getSize(), this.getTitle());
      this.buttons.clear();
      this.getItemsAndButtons();

      for (Button button : this.buttons) {
         for (Integer slot : button.getSlots()) {
            if (slot < this.inventory.getSize()) {
               this.inventory.setItem(slot, button.getItem());
            }
         }
      }

      player.openInventory(this.inventory);
      LiteContainer.getInstance().getMenuManager().registerMenu(this);
   }

   public void handleClick(InventoryClickEvent event) {
      event.setCancelled(true);
      if (event.getClickedInventory() != null && event.getClickedInventory().equals(this.inventory)) {
         int slot = event.getSlot();
         ItemStack clickedItem = event.getCurrentItem();
         if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            for (Button button : this.buttons) {
               if (button.getSlots().contains(slot)) {
                  button.onClick(event);
                  return;
               }
            }
         }
      }
   }

   protected void addButton(Button button) {
      this.buttons.add(button);
   }

   protected void addDecorItems(Material material, List<Integer> slots) {
      ItemStack item = new ItemStack(material);

      for (Integer slot : slots) {
         if (slot < this.inventory.getSize()) {
            this.inventory.setItem(slot, item);
         }
      }
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public Player getOpener() {
      return this.opener;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }
}
