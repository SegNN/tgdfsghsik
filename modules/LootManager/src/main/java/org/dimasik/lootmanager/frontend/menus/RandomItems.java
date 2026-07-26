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

public class RandomItems implements InventoryHolder {
   private LootItem lootItem;
   private Player viewer;
   private int page;
   private Main back;
   private Inventory inventory;
   private final List<ItemStack> items;
   private int iitems;
   private int level;

   public RandomItems(int page, List<ItemStack> items, Main back) {
      this.page = page;
      this.items = items;
      this.back = back;
   }

   public RandomItems compile() {
      int slot = 0;
      int startIndex = 45 * (this.page - 1);
      int pages = this.items.size() / 45 + (this.items.size() % 45 == 0 ? 0 : 1);
      this.inventory = Bukkit.createInventory(this, 54, Parser.color("&0Сгенерированные предметы (" + this.page + "/" + pages + ")"));

      for (int i = startIndex; i < this.items.size() && slot < 45; i++) {
         ItemStack itemStack = this.items.get(i);
         this.inventory.setItem(slot, itemStack);
         slot++;
      }

      ItemStack itemStack = new ItemStack(Material.ARROW);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(45, itemStack);
      itemStack = new ItemStack(Material.GRAY_DYE);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F◀ Предыдущая страница"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(48, itemStack);
      itemStack = new ItemStack(Material.NETHER_STAR);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по рандомным предметам"));
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &f&m                                      &f "));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&9&C&F&9&F&F       Рандомные предметы"));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&D&5&D&B&D&C Здесь находятся предметы,"));
      lore.add(Parser.color(" &x&D&5&D&B&D&C сгенерированные по вашем"));
      lore.add(Parser.color(" &x&D&5&D&B&D&C настройкам в прошлом меню."));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &f&m                                      &f "));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы вернуться к &x&0&0&D&8&F&Fнастройкам"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(49, itemStack);
      itemStack = new ItemStack(Material.LIME_DYE);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(50, itemStack);
      itemStack = new ItemStack(Material.EMERALD);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CСгенерировать еще раз"));
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(53, itemStack);
      return this;
   }

   public RandomItems setPlayer(Player player) {
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
   public int getPage() {
      return this.page;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }

   @Generated
   public Main getBack() {
      return this.back;
   }

   @Generated
   public List<ItemStack> getItems() {
      return this.items;
   }

   @Generated
   public void setIitems(int iitems) {
      this.iitems = iitems;
   }

   @Generated
   public int getIitems() {
      return this.iitems;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }
}
