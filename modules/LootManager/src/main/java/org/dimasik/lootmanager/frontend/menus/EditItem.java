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

public class EditItem implements InventoryHolder {
   private LootItem lootItem;
   private Player viewer;
   private Main back;
   private Inventory inventory;

   public EditItem(LootItem sellItem, Main back) {
      this.lootItem = sellItem.clone();
      this.back = back;
   }

   public EditItem compile() {
      try {
         this.inventory = Bukkit.createInventory(this, 27, Parser.color("Редактирование предмета"));
         ItemStack itemStack = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color(" "));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(1, itemStack);
         this.inventory.setItem(7, itemStack);
         this.inventory.setItem(8, itemStack);
         this.inventory.setItem(9, itemStack);
         this.inventory.setItem(10, itemStack);
         this.inventory.setItem(16, itemStack);
         this.inventory.setItem(19, itemStack);
         this.inventory.setItem(25, itemStack);
         this.inventory.setItem(26, itemStack);
         itemStack = this.lootItem.getItem().clone();
         itemStack.setAmount(this.lootItem.getMaxAmount());
         this.inventory.setItem(0, itemStack);
         itemStack = new ItemStack(Material.SNOWBALL);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Шанс"));
         List<String> lore = new ArrayList<>();
         lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущий шанс: &x&D&5&D&B&D&C" + this.lootItem.getChance()));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить на 1"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить на 1"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ЛКМ, чтобы увеличить на 10"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ПКМ, чтобы уменьшить на 10"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(3, itemStack);
         itemStack = new ItemStack(Material.CLOCK);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Максимальное количество"));
         lore = new ArrayList<>();
         lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущее количество: &x&D&5&D&B&D&C" + this.lootItem.getMaxAmount()));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить на 1"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить на 1"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ЛКМ, чтобы увеличить на 10"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ПКМ, чтобы уменьшить на 10"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(4, itemStack);
         itemStack = new ItemStack(Material.PRISMARINE_SHARD);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Уровень"));
         lore = new ArrayList<>();
         lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущий уровень: &x&D&5&D&B&D&C" + this.lootItem.getLevel()));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить на 1"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить на 1"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(5, itemStack);
         itemStack = new ItemStack(Material.STRUCTURE_VOID);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color(" "));
         lore = new ArrayList<>();
         lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CПоложите сюда предмет,"));
         lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  чтобы его &x&0&0&D&8&F&Fзаменить&x&D&5&D&B&D&C."));
         lore.add(Parser.color(""));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(17, itemStack);
         itemStack = new ItemStack(Material.LIME_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CПолучить предмет"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(18, itemStack);
         itemStack = new ItemStack(Material.ARROW);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(21, itemStack);
         itemStack = new ItemStack(Material.EMERALD);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CСохранить предмет"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(23, itemStack);
         return this;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public EditItem setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   public void open() {
      this.viewer.openInventory(this.inventory);
   }

   @NotNull
   public Inventory getInventory() {
      return null;
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
