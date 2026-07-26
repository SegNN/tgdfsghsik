package org.dimasik.lootmanager.frontend.menus;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.lootmanager.backend.models.LootItem;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class RemoveItem implements InventoryHolder {
   private LootItem lootItem;
   private Player viewer;
   private Main back;
   private Inventory inventory;

   public RemoveItem(LootItem sellItem, Main back) {
      this.lootItem = sellItem;
      this.back = back;
   }

   public RemoveItem compile() {
      try {
         this.inventory = Bukkit.createInventory(this, 27, "Удалить предмет");
         ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0▶ &x&D&5&D&B&D&CУдалить предмет"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(0, itemStack);
         this.inventory.setItem(1, itemStack);
         this.inventory.setItem(2, itemStack);
         this.inventory.setItem(9, itemStack);
         this.inventory.setItem(10, itemStack);
         this.inventory.setItem(11, itemStack);
         this.inventory.setItem(18, itemStack);
         this.inventory.setItem(19, itemStack);
         this.inventory.setItem(20, itemStack);
         itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color(" "));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(3, itemStack);
         this.inventory.setItem(4, itemStack);
         this.inventory.setItem(5, itemStack);
         this.inventory.setItem(12, itemStack);
         this.inventory.setItem(14, itemStack);
         this.inventory.setItem(21, itemStack);
         this.inventory.setItem(22, itemStack);
         this.inventory.setItem(23, itemStack);
         itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CОтменить"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(6, itemStack);
         this.inventory.setItem(7, itemStack);
         this.inventory.setItem(8, itemStack);
         this.inventory.setItem(15, itemStack);
         this.inventory.setItem(16, itemStack);
         this.inventory.setItem(17, itemStack);
         this.inventory.setItem(24, itemStack);
         this.inventory.setItem(25, itemStack);
         this.inventory.setItem(26, itemStack);
         itemStack = this.lootItem.getItem().clone();
         itemStack.setAmount(this.lootItem.getMaxAmount());
         itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         if (itemMeta != null && itemMeta.getLore() != null) {
            lore = itemMeta.getLore();
         }

         lore.add(Parser.color(""));
         lore.add(Parser.color("&x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы удалить"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(13, itemStack);
         return this;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public RemoveItem setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   public void open() {
      this.viewer.openInventory(this.inventory);
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   @Generated
   public LootItem getLootItem() {
      return this.lootItem;
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }
}
