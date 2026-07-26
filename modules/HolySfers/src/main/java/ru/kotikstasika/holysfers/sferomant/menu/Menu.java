package ru.kotikstasika.holysfers.sferomant.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.kotikstasika.holysfers.HolySfers;
import ru.kotikstasika.holysfers.utils.Utils;

public class Menu implements Listener {
   private final HolySfers plugin = HolySfers.getInstance();
   private final Utils utils = HolySfers.getInstance().getUtils();
   private static FileConfiguration config;
   private final Menu.SferomantHolder holder = new Menu.SferomantHolder();

   public Menu() {
      this.loadConfig();
   }

   private void loadConfig() {
      File configFile = new File(this.plugin.getDataFolder(), "sferomant.yml");
      if (!configFile.exists()) {
         this.plugin.saveResource("sferomant.yml", false);
      }

      config = YamlConfiguration.loadConfiguration(configFile);
   }

   public void openMenu(Player player) {
      String title = ChatColor.translateAlternateColorCodes('&', config.getString("menu_title", "Сферомант"));
      int size = 45;
      Inventory inventory = Bukkit.createInventory(this.holder, size, title);
      ConfigurationSection itemsSection = config.getConfigurationSection("items");
      if (itemsSection != null) {
         for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
               int slot = itemSection.getInt("slot", 0);
               if (slot >= 0 && slot < size) {
                  ItemStack item = this.createMenuItem(itemSection);
                  if (item != null) {
                     inventory.setItem(slot, item);
                  }
               }
            }
         }
      }

      player.openInventory(inventory);
   }

   private ItemStack createMenuItem(ConfigurationSection itemSection) {
      String materialName = itemSection.getString("material", "STONE");
      int amount = itemSection.getInt("amount", 1);
      ItemStack item;
      if (materialName.startsWith("basehead-")) {
         String base64 = materialName.substring(9);
         item = new ItemStack(Material.PLAYER_HEAD, amount);
         item = this.utils.getSkull(item, base64);
      } else {
         Material material = Material.valueOf(materialName.toUpperCase());
         item = new ItemStack(material, amount);
      }

      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String displayName = itemSection.getString("display_name", "");
         if (!displayName.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
         }

         List<String> lore = itemSection.getStringList("lore");
         if (!lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();

            for (String line : lore) {
               coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            meta.setLore(coloredLore);
         }

         boolean hideAttributes = itemSection.getBoolean("hide_attributes", false);
         if (hideAttributes) {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         }

         item.setItemMeta(meta);
      }

      return item;
   }

   @Generated
   public static FileConfiguration getConfig() {
      return config;
   }

   public static class SferomantHolder implements InventoryHolder {
      public SferomantHolder() {
      }

      public Inventory getInventory() {
         return null;
      }
   }
}
