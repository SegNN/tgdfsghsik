package org.dimasik.lootmanager.frontend.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.lootmanager.LootManager;
import org.dimasik.lootmanager.backend.models.LootItem;
import org.dimasik.lootmanager.backend.utils.Parser;
import org.jetbrains.annotations.NotNull;

public class Main implements InventoryHolder {
   private Inventory inventory;
   private Player viewer;
   private int currentFilterLevel;
   private String currentCategory;
   private int page;
   private final HashMap<Integer, LootItem> items = new HashMap<>();

   public Main(int page) {
      this.page = page;
   }

   public Main compile() {
      this.items.clear();
      int slot = 0;
      List<LootItem> allItems = new ArrayList<>(LootManager.getInstance().getItemManager().getAllItems());
      List<LootItem> filteredItems = allItems.stream()
         .filter(item -> this.currentFilterLevel == 0 || item.getLevel() == this.currentFilterLevel)
         .filter(item -> this.currentCategory.equals("Все подряд") || item.getCategory().equals(this.currentCategory))
         .toList();
      int startIndex = 45 * (this.page - 1);
      int pages = filteredItems.size() / 45 + (filteredItems.size() % 45 == 0 ? 0 : 1);
      this.inventory = Bukkit.createInventory(this, 54, Parser.color("&0Менеджер предметов (" + this.page + "/" + pages + ")"));

      for (int i = startIndex; i < filteredItems.size() && slot < 45; i++) {
         LootItem lootItem = filteredItems.get(i);
         this.items.put(slot, lootItem);
         ItemStack itemStack = lootItem.getItem().clone();
         ItemMeta itemMeta = itemStack.getItemMeta();
         List<String> lore = new ArrayList<>();
         if (itemMeta != null && itemMeta.getLore() != null) {
            lore = itemMeta.getLore();
         }

         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C ID:&x&0&0&D&8&F&F " + lootItem.getId()));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F " + lootItem.getCategory()));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Шанс:&x&0&0&D&8&F&F " + lootItem.getChance() + "%"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Уровень:&x&0&0&D&8&F&F " + lootItem.getLevel()));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Макс. кол-во:&x&0&0&D&8&F&F " + lootItem.getMaxAmount()));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы редактировать"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы удалить"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         itemStack.setAmount(lootItem.getMaxAmount());
         this.inventory.setItem(slot, itemStack);
         slot++;
      }

      ItemStack itemStack = new ItemStack(Material.ENDER_CHEST);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F ❏ Проверка предметов ❏"));
      List<String> lore = new ArrayList<>();
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C В этом разделе можно сгенерировать"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C предметы, с &x&0&0&D&8&F&Fуказанными параметрами&f."));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы открыть"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(45, itemStack);

      try {
         itemStack = new ItemStack(Material.CHEST);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F ❏ Клауд-конфиги ❏"));
         lore = new ArrayList<>();
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C В этом разделе можно добавить,"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C сохранить, удалить и загрузить"));
         lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&0&0&D&8&F&F конфигурации &x&D&5&D&B&D&Cв базе данных."));
         lore.add(Parser.color(""));
         lore.add(Parser.color("   &6Конфигураций:&x&0&0&D&8&F&F " + LootManager.getInstance().getDatabaseManager().getConfigNames().get().size()));
         lore.add(Parser.color(""));
         lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы открыть"));
         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         this.inventory.setItem(46, itemStack);
      } catch (Exception var12) {
         throw new RuntimeException(var12);
      }

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
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по менеджеру предметов"));
      lore = new ArrayList<>();
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &f&m                                      &f "));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&9&C&F&9&F&F       Менеджер предметов"));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&D&5&D&B&D&C Здесь можно настроить предметы,"));
      lore.add(Parser.color(" &x&D&5&D&B&D&C которые выпадают с ивентов, их"));
      lore.add(Parser.color(" &x&D&5&D&B&D&C шансы и другие параметры."));
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
      itemStack = new ItemStack(Material.HOPPER);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Уровень"));
      lore = new ArrayList<>();
      lore.add(Parser.color("&o&x&9&C&F&9&F&F ●&f Текущий уровень: &x&D&5&D&B&D&C" + (this.currentFilterLevel == 0 ? "Любой" : this.currentFilterLevel)));
      lore.add(Parser.color(""));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы увеличить уровень"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ПКМ, чтобы уменьшить уровень"));
      lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ШИФТ + ПКМ, чтобы сбросить уровень"));
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(52, itemStack);
      itemStack = new ItemStack(Material.CHEST_MINECART);
      itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Категории предметов"));
      lore = new ArrayList<>();
      List<String> categories = LootManager.getInstance().getItemManager().getAllCategories().stream().sorted().collect(Collectors.toList());
      categories.add(0, "Все подряд");

      for (String category : categories) {
         if (category.equals(this.currentCategory)) {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&o&6&6✔&6 &6" + category));
         } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&o&x&9&C&F&9&F&F● &f" + category));
         }
      }

      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      this.inventory.setItem(53, itemStack);
      return this;
   }

   public void open() {
      this.viewer.openInventory(this.inventory);
   }

   public Main setPlayer(Player player) {
      this.viewer = player;
      return this;
   }

   public Main setCurrentFilterLevel(int currentFilterLevel) {
      this.currentFilterLevel = currentFilterLevel;
      return this;
   }

   public Main setCurrentCategory(String currentCategory) {
      this.currentCategory = currentCategory;
      return this;
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
   public int getCurrentFilterLevel() {
      return this.currentFilterLevel;
   }

   @Generated
   public String getCurrentCategory() {
      return this.currentCategory;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }

   @Generated
   public int getPage() {
      return this.page;
   }

   @Generated
   public HashMap<Integer, LootItem> getItems() {
      return this.items;
   }
}
