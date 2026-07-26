package ru.holydev.holycustom.menus.star;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.holydev.holycustom.holders.StarMenuHolder;
import ru.holydev.holycustom.items.MenuItems;
import ru.holydev.holycustom.items.StarItem;

public class StarMenu {
   private final MenuItems items;
   private final StarItem starItem;

   public StarMenu(MenuItems items, StarItem starItem) {
      this.items = items;
      this.starItem = starItem;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new StarMenuHolder(), 54, "Звезда статуса");
      inventory.setItem(12, this.starItem.getStar(Material.PINK_SHULKER_BOX, "&x&F&B&7&7&7&7", 1000, 1, player));
      inventory.setItem(13, this.starItem.getStar(Material.YELLOW_SHULKER_BOX, "&x&E&E&B&E&0&0", 2000, 2, player));
      inventory.setItem(14, this.starItem.getStar(Material.LIGHT_BLUE_SHULKER_BOX, "&x&2&A&F&B&F&F", 3500, 3, player));
      inventory.setItem(20, this.starItem.getStar(Material.RED_SHULKER_BOX, "&x&F&B&4&F&0&0", 7500, 4, player));
      inventory.setItem(21, this.starItem.getStar(Material.LIME_SHULKER_BOX, "&x&2&D&F&D&0&0", 12500, 5, player));
      inventory.setItem(22, this.starItem.getStar(Material.PURPLE_SHULKER_BOX, "&x&E&0&0&0&F&F", 20000, 6, player));
      inventory.setItem(23, this.starItem.getStar(Material.RED_SHULKER_BOX, "&x&F&0&0&0&0&0", 35000, 7, player));
      inventory.setItem(24, this.starItem.getStar(Material.MAGENTA_SHULKER_BOX, "&x&F&F&0&0&9&5", 50000, 8, player));
      inventory.setItem(39, this.starItem.getStar(Material.WHITE_SHULKER_BOX, "&x&F&8&5&2&0&0", 75000, 9, player));
      inventory.setItem(41, this.starItem.getStar(Material.MAGENTA_SHULKER_BOX, "&x&F&8&5&2&0&0", 125000, 10, player));
      inventory.setItem(45, this.items.getStarHelp());
      inventory.setItem(53, this.items.back());
      player.openInventory(inventory);
   }
}
