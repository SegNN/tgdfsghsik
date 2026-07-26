package ru.kotikstasika.eventship.items;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.KeyType;

public class KeyItem {
   public KeyItem() {
   }

   public static ItemStack createKey(KeyType keyType, EventShip plugin) {
      ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
      ItemMeta meta = item.getItemMeta();
      String configKey = keyType.getConfigKey();
      String displayName = plugin.getConfig().getString("keys." + configKey + ".displayName");
      List<String> lore = plugin.getConfig().getStringList("keys." + configKey + ".lore");
      String nbtTag = plugin.getConfig().getString("keys." + configKey + ".nbtTag");
      meta.setDisplayName(displayName.replace("&", "§"));
      meta.setLore(lore.stream().map(line -> line.replace("&", "§")).collect(Collectors.toList()));
      meta.addEnchant(Enchantment.UNBREAKING, 1, true);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON});
      NamespacedKey key = new NamespacedKey(plugin, "key_type");
      meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, nbtTag);
      item.setItemMeta(meta);
      return item;
   }

   public static boolean isKey(ItemStack item, EventShip plugin) {
      if (item != null && item.getType() == Material.TRIPWIRE_HOOK) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            NamespacedKey key = new NamespacedKey(plugin, "key_type");
            return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
         }
      } else {
         return false;
      }
   }

   public static String getKeyType(ItemStack item, EventShip plugin) {
      if (!isKey(item, plugin)) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         NamespacedKey key = new NamespacedKey(plugin, "key_type");
         return (String)meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
      }
   }
}
