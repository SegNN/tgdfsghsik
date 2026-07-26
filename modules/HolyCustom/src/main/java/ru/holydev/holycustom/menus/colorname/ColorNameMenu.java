package ru.holydev.holycustom.menus.colorname;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.holydev.holycustom.holders.ColorNameMenuHolder;
import ru.holydev.holycustom.items.MenuItems;

public class ColorNameMenu {
   private final MenuItems items;

   public ColorNameMenu(MenuItems items) {
      this.items = items;
   }

   public void open(Player player) {
      Inventory inventory = Bukkit.createInventory(new ColorNameMenuHolder(), 54, "Цвет никнейма");
      inventory.setItem(12, this.items.getNameItem(Material.PINK_SHULKER_BOX, "&x&F&B&7&7&7&7", "#FB7777", 1, player.getName()));
      inventory.setItem(13, this.items.getNameItem(Material.YELLOW_SHULKER_BOX, "&x&E&E&B&E&0&0", "#EEBE00", 2, player.getName()));
      inventory.setItem(14, this.items.getNameItem(Material.LIGHT_BLUE_SHULKER_BOX, "&x&2&A&F&B&F&F", "#2AFBFF", 3, player.getName()));
      inventory.setItem(29, this.items.getNameItem(Material.ORANGE_SHULKER_BOX, "&x&F&B&4&F&0&0", "#FB4F00", 4, player.getName()));
      inventory.setItem(30, this.items.getNameItem(Material.LIME_SHULKER_BOX, "&x&2&D&F&D&0&0", "#2DFD00", 5, player.getName()));
      inventory.setItem(31, this.items.getNameItem(Material.PURPLE_SHULKER_BOX, "&x&E&0&0&0&F&F", "#E000FF", 6, player.getName()));
      inventory.setItem(32, this.items.getNameItem(Material.RED_SHULKER_BOX, "&x&F&F&0&0&0&0", "#FF0000", 7, player.getName()));
      inventory.setItem(33, this.items.getNameItem(Material.MAGENTA_SHULKER_BOX, "&x&F&F&0&0&9&5", "#FF0095", 8, player.getName()));
      inventory.setItem(40, this.items.getNameItem(Material.WHITE_SHULKER_BOX, "&x&F&F&F&F&F&F", "#FFFFFF", 9, player.getName()));
      player.openInventory(inventory);
   }
}
