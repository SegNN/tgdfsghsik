package org.dimasik.lootmanager.frontend.menus;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class Config implements InventoryHolder {
   private Player viewer;
   private Configs back;
   private Inventory inventory;
   private String configName;

   public Config(String configName, Configs back) {
      this.configName = configName;
      this.back = back;
   }

   public Config compile() {
      try {
         this.inventory = Bukkit.createInventory(this, InventoryType.DISPENSER, "Действия с конфигурацией");
         ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&2&2&F&F&2&2↑ &x&D&5&D&B&D&CПерезаписать конфигурацию"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(0, itemStack);
         itemStack = new ItemStack(Material.LIME_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F↓ &x&D&5&D&B&D&CЗагрузить конфигурацию"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(1, itemStack);
         itemStack = new ItemStack(Material.BROWN_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2✖ &x&D&5&D&B&D&CУдалить"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(2, itemStack);
         itemStack = new ItemStack(Material.ARROW);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(7, itemStack);
         this.viewer.openInventory(this.inventory);
         return this;
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public Config setPlayer(Player player) {
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
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public Configs getBack() {
      return this.back;
   }

   @Generated
   public String getConfigName() {
      return this.configName;
   }
}
