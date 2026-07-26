package ru.kotikstasika.kotikdang.menu;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.kotikstasika.kotikdang.KotikDang;
import ru.kotikstasika.kotikdang.util.ColorUtil;

public class UniqueShulkerMenu implements InventoryHolder {
   private final Inventory inventory;

   public UniqueShulkerMenu(Inventory inventory) {
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public static Inventory create(KotikDang plugin, int lootLevel) {
      String title = ColorUtil.colorize(plugin.getConfig().getString("menu.title", "Уникальный шалкер"));
      int size = plugin.getConfig().getInt("menu.size", 36);
      return Bukkit.createInventory(new UniqueShulkerMenu(null), size, title);
   }
}
