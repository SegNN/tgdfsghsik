package ru.kotikstasika.holyliteend.end;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.holyliteend.HolyLiteEnd;

public class EggItem {
   private static HolyLiteEnd plugin;

   public EggItem() {
   }

   public static void initialize(HolyLiteEnd mainPlugin) {
      plugin = mainPlugin;
   }

   private static NamespacedKey getEggKey() {
      return new NamespacedKey(plugin, "dragon_egg");
   }

   private static NamespacedKey getValueKey() {
      return new NamespacedKey(plugin, "egg_value");
   }

   private static NamespacedKey getBaseValueKey() {
      return new NamespacedKey(plugin, "base_value");
   }

   public static int getBaseValue(ItemStack item) {
      if (!isDragonEgg(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().get(getBaseValueKey(), PersistentDataType.INTEGER);
      }
   }

   public static NamespacedKey getPlaceTimeKey() {
      return new NamespacedKey(plugin, "place_time");
   }

   public static ItemStack createDragonEgg(int baseValue) {
      ItemStack item = new ItemStack(Material.DRAGON_EGG);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&',
            "&x&6&A&2&0&A&FЯ&x&7&4&2&3&B&FЙ&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа ["
               + baseValue
               + "/1500]"
         )
      );
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&8Особенности:"));
      lore.add(
         ChatColor.translateAlternateColorCodes(
            '&', "&5&o&x&E&7&E&7&E&7 - первый &x&9&1&2&D&E&Fсапфир&x&E&7&E&7&E&7 через &x&9&1&2&D&E&F3 минуты&x&E&7&E&7&E&7"
         )
      );
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&E&7&E&7&E&7 - узнать стоимость: &x&9&1&2&D&E&F/egg price"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&E&7&E&7&E&7 - обменять на сапфиры: &x&9&1&2&D&E&F/egg swap"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&E&7&E&7&E&7 - прятать можно только в &x&9&1&2&D&E&Fобычном сундуке"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o&x&E&7&E&7&E&7 - информация о событии: &x&9&1&2&D&E&F/warp end"));
      lore.add(ChatColor.translateAlternateColorCodes('&', "&5&o"));
      meta.setLore(lore);
      meta.getPersistentDataContainer().set(getEggKey(), PersistentDataType.BYTE, (byte)1);
      meta.getPersistentDataContainer().set(getValueKey(), PersistentDataType.INTEGER, baseValue);
      meta.getPersistentDataContainer().set(getBaseValueKey(), PersistentDataType.INTEGER, baseValue);
      meta.getPersistentDataContainer().set(getPlaceTimeKey(), PersistentDataType.LONG, System.currentTimeMillis());
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack createDragonEggFromKill() {
      return createDragonEgg(777);
   }

   public static boolean isDragonEgg(ItemStack item) {
      if (item != null && item.getType() == Material.DRAGON_EGG) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(getEggKey(), PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public static int getEggValue(ItemStack item) {
      if (!isDragonEgg(item)) {
         return 0;
      } else {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer().get(getValueKey(), PersistentDataType.INTEGER);
      }
   }

   public static void updateEggValue(ItemStack item, int newValue) {
      if (isDragonEgg(item)) {
         ItemMeta meta = item.getItemMeta();
         meta.getPersistentDataContainer().set(getValueKey(), PersistentDataType.INTEGER, newValue);
         meta.setDisplayName(
            ChatColor.translateAlternateColorCodes(
               '&',
               "&x&6&A&2&0&A&FЯ&x&7&4&2&3&B&FЙ&x&7&E&2&6&C&Fц&x&8&7&2&A&D&Fо &x&9&1&2&D&E&FД&x&9&B&3&0&F&Fр&x&9&1&2&D&E&Fа&x&8&7&2&A&D&Fк&x&7&E&2&6&C&Fо&x&7&4&2&3&B&Fн&x&6&A&2&0&A&Fа ["
                  + newValue
                  + "/1500]"
            )
         );
         item.setItemMeta(meta);
      }
   }

   public static void setPlaceTime(ItemStack item) {
      if (isDragonEgg(item)) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            meta.getPersistentDataContainer().set(getPlaceTimeKey(), PersistentDataType.LONG, System.currentTimeMillis());
            item.setItemMeta(meta);
         }
      }
   }
}
