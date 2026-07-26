package ua.fichatime.fichaspawneroved.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichatime.fichaspawneroved.FichaSpawneroved;
import ua.fichatime.fichaspawneroved.Utils.Color;

public class Menu implements InventoryHolder {
   private final Inventory inventory;
   private final FichaSpawneroved plugin;

   public Menu(FichaSpawneroved plugin) {
      this.plugin = plugin;
      ConfigurationSection items = plugin.getConfig().getConfigurationSection("item");
      this.inventory = Bukkit.createInventory(this, 27, "Хранитель яиц");
      this.addMenuItem(items, "Ender-eggs", 10);
      this.addMenuItem(items, "eggs-1", 12);
      this.addMenuItem(items, "eggs-2", 14);
      this.addMenuItem(items, "eggs-3", 16);
   }

   private void addMenuItem(ConfigurationSection items, String key, int slot) {
      ConfigurationSection itemSection = items.getConfigurationSection(key);
      if (itemSection != null) {
         ItemStack item = new ItemStack(Material.valueOf(itemSection.getString("material")), itemSection.getInt("amount", 1));
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            meta.setDisplayName(Color.hex(itemSection.getString("name")));
            meta.setLore(Color.hexList(itemSection.getStringList("lore")));
            item.setItemMeta(meta);
         }

         this.inventory.setItem(slot, item);
      }
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
