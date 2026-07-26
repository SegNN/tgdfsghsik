package org.dimasik.buyer;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BuyerAutoMenu implements Listener {
   private final Buyer plugin;
   private final String TITLE = "Фильтр авто-продаж                                                                                                                                                                                                                                                                                                                                                [mantibuyer:auto]";

   public BuyerAutoMenu(Buyer plugin) {
      this.plugin = plugin;
   }

   public void open(Player player) {
      this.plugin.autoBuyerTypes.put(player, "mine");
      Inventory inventory = Bukkit.createInventory(
         player,
         54,
         "Фильтр авто-продаж                                                                                                                                                                                                                                                                                                                                                [mantibuyer:auto]"
      );
      this.updateItems(inventory, player, this.plugin.autoBuyerTypes.get(player));
      player.openInventory(inventory);
   }

   private ItemStack createCategoryButton(Player player, String category) {
      ItemStack itemStack = new ItemStack(Material.BARRIER);
      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      String currentCategory = this.plugin.autoBuyerTypes.get(player);
      boolean isSelected = currentCategory.equalsIgnoreCase(category);
      String var8 = category.toLowerCase();
      switch (var8) {
         case "mine":
            itemStack.setType(Material.NETHERITE_INGOT);
            if (isSelected) {
               itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0❏ &fКатегория: &x&F&F&7&0&0&0Ресурсы из шахты &x&F&F&7&0&0&0❏"));
               lore.add("");
               lore.add(this.colorize("&x&F&F&7&0&0&0▶ &fКатегория выбрана"));
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            } else {
               itemMeta.setDisplayName(this.colorize("&x&0&0&D&8&F&F❏ &fКатегория: &x&0&0&D&8&F&FРесурсы из шахты &x&0&0&D&8&F&F❏"));
               lore.add("");
               lore.add(this.colorize("&x&0&0&D&8&F&F▶ &fПерейти в категорию"));
            }
            break;
         case "mob":
            itemStack.setType(Material.BONE);
            if (isSelected) {
               itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0❏ &fКатегория: &x&F&F&7&0&0&0Ресурсы с мобов &x&F&F&7&0&0&0❏"));
               lore.add("");
               lore.add(this.colorize("&x&F&F&7&0&0&0▶ &fКатегория выбрана"));
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            } else {
               itemMeta.setDisplayName(this.colorize("&x&0&0&D&8&F&F❏ &fКатегория: &x&0&0&D&8&F&FРесурсы с мобов &x&0&0&D&8&F&F❏"));
               lore.add("");
               lore.add(this.colorize("&x&0&0&D&8&F&F▶ &fПерейти в категорию"));
            }
            break;
         case "tree":
            itemStack.setType(Material.OAK_LOG);
            if (isSelected) {
               itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0❏ &fКатегория: &x&F&F&7&0&0&0Древесина &x&F&F&7&0&0&0❏"));
               lore.add("");
               lore.add(this.colorize("&x&F&F&7&0&0&0▶ &fКатегория выбрана"));
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            } else {
               itemMeta.setDisplayName(this.colorize("&x&0&0&D&8&F&F❏ &fКатегория: &x&0&0&D&8&F&FДревесина &x&0&0&D&8&F&F❏"));
               lore.add("");
               lore.add(this.colorize("&x&0&0&D&8&F&F▶ &fПерейти в категорию"));
            }
            break;
         case "fermer":
            itemStack.setType(Material.WHEAT);
            if (isSelected) {
               itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0❏ &fКатегория: &x&F&F&7&0&0&0Культуры &x&F&F&7&0&0&0❏"));
               lore.add("");
               lore.add(this.colorize("&x&F&F&7&0&0&0▶ &fКатегория выбрана"));
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            } else {
               itemMeta.setDisplayName(this.colorize("&x&0&0&D&8&F&F❏ &fКатегория: &x&0&0&D&8&F&FКультуры &x&0&0&D&8&F&F❏"));
               lore.add("");
               lore.add(this.colorize("&x&0&0&D&8&F&F▶ &fПерейти в категорию"));
            }
      }

      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "cathegory");
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "cathegory"), PersistentDataType.STRING, category);
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack createToggleButton(Player player) {
      ItemStack itemStack = new ItemStack(Material.SNOWBALL);
      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (this.plugin.toggledAutoBuyers.get(player)) {
         itemMeta.setDisplayName(this.colorize("&x&F&F&C&9&0&0 • &fАвтоматическая сдача: &x&0&5&F&B&0&0Включена"));
         lore.add("");
         lore.add(this.colorize("&x&F&F&C&9&0&0 ▶ &fНажмите, чтобы &x&F&F&C&9&0&0Выключить &fавто-сдачу"));
         itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "enabled"), PersistentDataType.STRING, "true");
      } else {
         itemMeta.setDisplayName(this.colorize("&x&0&0&D&8&F&F • &fАвтоматическая сдача: &x&F&F&2&2&2&2Выключена"));
         lore.add("");
         lore.add(this.colorize("&x&0&0&D&8&F&F ▶ &fНажмите, чтобы &x&0&0&D&8&F&FВключить &fавто-сдачу"));
         itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "enabled"), PersistentDataType.STRING, "false");
      }

      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "toggle");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private ItemStack createToggleAllButton(Player player, boolean enableAll) {
      List<String> lore = new ArrayList<>();
      ItemStack itemStack;
      ItemMeta itemMeta;
      if (enableAll) {
         itemStack = new ItemStack(Material.SLIME_BALL);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(this.colorize("&x&0&5&F&B&0&0 ✔ Включить все предметы "));
         itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "toggle-type"), PersistentDataType.STRING, "on");
      } else {
         itemStack = new ItemStack(Material.GRAY_DYE);
         itemMeta = itemStack.getItemMeta();
         itemMeta.setDisplayName(this.colorize("&x&F&F&2&2&2&2 ✘ Выключить все предметы "));
         itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "toggle-type"), PersistentDataType.STRING, "off");
      }

      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "all");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private void updateItems(Inventory inventory, Player player, String category) {
      ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta paneMeta = pane.getItemMeta();
      paneMeta.setDisplayName(" ");
      pane.setItemMeta(paneMeta);

      for (int slot = 18; slot < 36; slot++) {
         inventory.setItem(slot, null);
      }

      inventory.setItem(0, pane);
      inventory.setItem(1, pane);
      inventory.setItem(2, this.createCategoryButton(player, "mine"));
      inventory.setItem(3, this.createCategoryButton(player, "mob"));
      inventory.setItem(4, this.createCategoryButton(player, "tree"));
      inventory.setItem(5, this.createCategoryButton(player, "fermer"));
      inventory.setItem(6, this.createLeaveButton(player));
      inventory.setItem(7, pane);
      inventory.setItem(8, pane);

      for (int slot = 9; slot <= 17; slot++) {
         inventory.setItem(slot, pane);
      }

      for (int slot = 36; slot <= 44; slot++) {
         inventory.setItem(slot, pane);
      }

      inventory.setItem(45, this.createToggleAllButton(player, true));
      inventory.setItem(46, pane);
      inventory.setItem(47, pane);
      inventory.setItem(48, pane);
      inventory.setItem(49, this.createToggleButton(player));
      inventory.setItem(50, pane);
      inventory.setItem(51, pane);
      inventory.setItem(52, pane);
      inventory.setItem(53, this.createToggleAllButton(player, false));
      int slot = 18;

      try {
         List<PricesManager.Item> items = this.plugin.pricesManager.getItemsByCategory(this.plugin.autoBuyerTypes.get(player), "priceLow").get();
         double coefficent = this.plugin.koefficentManager.getKoefficent(player.getName()).get();

         for (PricesManager.Item item : items) {
            ItemStack itemStack = new ItemStack(Material.valueOf(item.getName().toUpperCase()));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0 " + item.getDisplayName()));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(this.colorize(""));
            itemLore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f Стандартная цена: &x&F&F&7&0&0&0" + this.formatDouble(item.getPrice()) + " монеток"));
            itemLore.add(this.colorize("&x&F&F&7&0&0&0 &n▍&f Текущая цена: &x&F&F&7&0&0&0" + this.formatDouble(item.getPrice() * coefficent) + " монеток"));
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "item");
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "material"), PersistentDataType.STRING, item.getName());
            if (this.plugin.playerDataManager.isEnabled(player.getName(), item.getName()).get()) {
               itemLore.add(this.colorize("&x&F&F&7&0&0&0 ▍&f Статус: &x&F&F&C&9&0&0Сдаётся автоматически"));
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "enabled"), PersistentDataType.STRING, "true");
               itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            } else {
               itemLore.add(this.colorize("&x&F&F&7&0&0&0 ▍&f Статус: &x&F&F&C&9&0&0Не сдаётся автоматически"));
               itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "enabled"), PersistentDataType.STRING, "false");
            }

            itemLore.add(this.colorize(""));
            itemMeta.setLore(itemLore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(slot, itemStack);
            slot++;
         }
      } catch (Exception var15) {
         var15.printStackTrace();
      }
   }

   private ItemStack createLeaveButton(Player player) {
      ItemStack itemStack = new ItemStack(Material.ARROW);
      ItemMeta itemMeta = itemStack.getItemMeta();
      List<String> lore = new ArrayList<>();
      itemMeta.setDisplayName(this.colorize("&x&F&F&7&0&0&0◀ &fНазад"));
      itemMeta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "leave");
      itemMeta.setLore(lore);
      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   private void toggleAllItems(Player player, String category, boolean enable) {
      try {
         for (PricesManager.Item item : this.plugin.pricesManager.getItemsByCategory(this.plugin.autoBuyerTypes.get(player), "priceLow").get()) {
            this.plugin.playerDataManager.addPlayerData(player.getName(), item.getName(), enable);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView()
            .getTitle()
            .equalsIgnoreCase(
               "Фильтр авто-продаж                                                                                                                                                                                                                                                                                                                                                [mantibuyer:auto]"
            )
         && event.getClickedInventory() != null) {
         Player player = (Player)event.getWhoClicked();
         event.setCancelled(true);
         if (event.getCurrentItem() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
            ItemStack clickedItem = event.getCurrentItem();
            Inventory inventory = event.getClickedInventory();
            PersistentDataContainer dataContainer = clickedItem.getItemMeta().getPersistentDataContainer();
            String type = (String)dataContainer.getOrDefault(new NamespacedKey(this.plugin, "type"), PersistentDataType.STRING, "none");
            String var7 = type.toLowerCase();
            switch (var7) {
               case "toggle":
                  boolean currentState = this.plugin.toggledAutoBuyers.get(player);
                  if (currentState) {
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                     this.plugin.toggledAutoBuyers.put(player, false);
                  } else {
                     player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
                     this.plugin.toggledAutoBuyers.put(player, true);
                  }

                  this.updateItems(inventory, player, this.plugin.autoBuyerTypes.get(player));
                  break;
               case "cathegory":
                  String category = (String)dataContainer.getOrDefault(new NamespacedKey(this.plugin, "cathegory"), PersistentDataType.STRING, "mine");
                  this.plugin.autoBuyerTypes.put(player, category);
                  this.updateItems(inventory, player, this.plugin.autoBuyerTypes.get(player));
                  break;
               case "all":
                  String toggleType = (String)dataContainer.getOrDefault(new NamespacedKey(this.plugin, "toggle-type"), PersistentDataType.STRING, "none");
                  boolean enableAll = toggleType.equalsIgnoreCase("on");
                  this.toggleAllItems(player, this.plugin.autoBuyerTypes.get(player), enableAll);
                  this.updateItems(inventory, player, this.plugin.autoBuyerTypes.get(player));
                  break;
               case "item":
                  String material = (String)dataContainer.getOrDefault(new NamespacedKey(this.plugin, "material"), PersistentDataType.STRING, "none");
                  String enabled = (String)dataContainer.getOrDefault(new NamespacedKey(this.plugin, "enabled"), PersistentDataType.STRING, "true");
                  boolean newState = enabled.equalsIgnoreCase("false");
                  this.plugin.playerDataManager.addPlayerData(player.getName(), material, newState);
                  this.updateItems(inventory, player, this.plugin.autoBuyerTypes.get(player));
                  break;
               case "leave":
                  this.plugin.openBuyerInventory(player);
            }
         }
      }
   }

   private String colorize(String text) {
      return ChatColor.translateAlternateColorCodes('&', text);
   }

   private String formatDouble(double value) {
      return String.format("%.2f", value).replace(',', '.');
   }
}
