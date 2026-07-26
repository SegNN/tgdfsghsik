package ua.holydev.holycases.menu.impl.version.v1_16_5;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holycases.HolyCases;
import ua.holydev.holycases.menu.Menu;
import ua.holydev.holycases.menu.MenuItem;
import ua.holydev.holycases.menu.MenuManager;

public class MenuManagerImpl implements MenuManager {
   private Map<Inventory, Menu> menus = new HashMap<>();

   public MenuManagerImpl(HolyCases plugin) {
      new MenuListener(plugin, this.menus);
   }

   @Override
   public Menu createMenu() {
      Menu menu = new MenuImpl();
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public Menu createMenu(String name) {
      Menu menu = new MenuImpl(name);
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public Menu createMenu(int size) {
      Menu menu = new MenuImpl(size);
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public Menu createMenu(InventoryType type) {
      Menu menu = new MenuImpl(type);
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public Menu createMenu(String name, int size) {
      Menu menu = new MenuImpl(size, name);
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public Menu createMenu(String name, InventoryType type) {
      Menu menu = new MenuImpl(type, name);
      this.menus.put(menu.getInventory(), menu);
      return menu;
   }

   @Override
   public MenuItem createItem(ItemStack item) {
      return new MenuItemImpl(item);
   }

   @Override
   public MenuItem createItem(ItemStack item, BiConsumer<Player, ClickType> onClick) {
      return new MenuItemImpl(item, onClick);
   }
}
