package org.dimasik.lootmanager.frontend.menus;

import java.util.ArrayList;
import java.util.List;
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

public class Properties implements InventoryHolder {
   private Inventory inventory;
   private Player viewer;
   private int items;
   private int level;
   private Main back;

   public Properties(int items, int level, Main back) {
      this.items = items;
      this.level = level;
      this.back = back;
   }

   public Properties compile() {
      this.inventory = Bukkit.createInventory(this, InventoryType.HOPPER, Parser.color("&0Выбор настроек"));
      ItemStack itemStack = new ItemStack(Material.CLOCK);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Количество предметов"));
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущее количество: &x&D&5&D&B&D&C" + this.items));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить на 1"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить на 1"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ЛКМ, чтобы увеличить на 16"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ПКМ, чтобы уменьшить на 16"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(0, itemStack);
      itemStack = new ItemStack(Material.PRISMARINE_SHARD);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Уровень"));
      lore = new ArrayList<>();
      lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущий уровень: &x&D&5&D&B&D&C" + this.level));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить на 1"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить на 1"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(1, itemStack);
      itemStack = new ItemStack(Material.EMERALD);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CСгенерировать предметы"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(3, itemStack);
      itemStack = new ItemStack(Material.ARROW);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(4, itemStack);
      return this;
   }

   public Properties setPlayer(Player player) {
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
   public void setItems(int items) {
      this.items = items;
   }

   @Generated
   public int getItems() {
      return this.items;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }
}
