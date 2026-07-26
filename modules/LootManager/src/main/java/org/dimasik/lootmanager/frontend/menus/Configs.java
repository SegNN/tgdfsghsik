package org.dimasik.lootmanager.frontend.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class Configs implements InventoryHolder {
   private Player viewer;
   private Main back;
   private Inventory inventory;
   private int page;
   private final HashMap<Integer, String> configs = new HashMap<>();

   public Configs(int page, Main back) {
      this.page = page;
      this.back = back;
   }

   public Configs compile() {
      try {
         this.configs.clear();
         int slot = 0;
         int startIndex = 45 * (this.page - 1);
         List<String> configNames = LootManager.getInstance().getDatabaseManager().getConfigNames().get();
         int pages = configNames.size() / 45 + (configNames.size() % 45 == 0 ? 0 : 1);
         this.inventory = Bukkit.createInventory(this, 54, "Клауд конфиги (" + this.page + "/" + pages + ")");

         for (int i = startIndex; i < configNames.size() && slot < 45; i++) {
            String name = configNames.get(i);
            this.configs.put(slot, name);
            ItemStack itemStack = new ItemStack(Material.CHEST_MINECART);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Parser.color("&x&D&5&D&B&D&C● &x&0&0&D&8&F&F" + name));
            List<String> lore = new ArrayList<>();
            lore.add(Parser.color(""));
            lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы открыть"));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(i + 1);
            this.inventory.setItem(slot, itemStack);
            slot++;
         }

         ItemStack itemStack = new ItemStack(Material.ARROW);
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(45, itemStack);
         itemStack = new ItemStack(Material.EMERALD);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить предметы"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(47, itemStack);
         itemStack = new ItemStack(Material.GRAY_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F◀ Предыдущая страница"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(48, itemStack);
         itemStack = new ItemStack(Material.NETHER_STAR);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по клауд конфигам"));
         List<String> lore = new ArrayList<>();
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &f&m                                      &f "));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&9&C&F&9&F&F       Клауд-конфиги"));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&D&5&D&B&D&C В этом разделе можно добавить,"));
         lore.add(Parser.color(" &x&D&5&D&B&D&C сохранить, удалить и загрузить"));
         lore.add(Parser.color(" &x&D&5&D&B&D&C конфигурации в базе данных."));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &f&m                                      &f "));
         lore.add(Parser.color(""));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(49, itemStack);
         itemStack = new ItemStack(Material.LIME_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(50, itemStack);
         itemStack = new ItemStack(Material.EGG);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CСохранить конфигурацию"));
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(53, itemStack);
         return this;
      } catch (Exception var10) {
         throw new RuntimeException(var10);
      }
   }

   public Configs setPlayer(Player player) {
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
   public Main getBack() {
      return this.back;
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
   public HashMap<Integer, String> getConfigs() {
      return this.configs;
   }
}
