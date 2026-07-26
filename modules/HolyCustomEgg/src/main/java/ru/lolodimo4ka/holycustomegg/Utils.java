package ru.lolodimo4ka.holycustomegg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Utils {
   private static final Pattern RGB_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

   public Utils() {
   }

   public static String formatColors(String text) {
      if (text == null) {
         return "";
      } else {
         text = processRGBColors(text);
         return ChatColor.translateAlternateColorCodes('&', text);
      }
   }

   private static String processRGBColors(String text) {
      if (text == null) {
         return "";
      } else {
         Matcher matcher = RGB_PATTERN.matcher(text);
         StringBuffer result = new StringBuffer();

         while (matcher.find()) {
            String rgbCode = matcher.group();
            String hex = rgbCode.replace("&x", "").replace("&", "").toLowerCase();

            try {
               ChatColor color = ChatColor.of("#" + hex);
               matcher.appendReplacement(result, color.toString());
            } catch (Exception var6) {
               matcher.appendReplacement(result, ChatColor.WHITE.toString());
            }
         }

         matcher.appendTail(result);
         return result.toString();
      }
   }

   public static List<String> formatColors(List<String> texts) {
      List<String> formatted = new ArrayList<>();

      for (String text : texts) {
         formatted.add(formatColors(text));
      }

      return formatted;
   }

   public static boolean isCustomEgg(ItemStack item, Map<String, ManagerEgg> eggs) {
      if (item == null) {
         return false;
      } else {
         ManagerEgg egg = findEggByItem(item, eggs);
         if (egg == null) {
            return false;
         } else {
            ItemMeta meta = item.getItemMeta();
            return meta == null ? false : meta.hasEnchant(Enchantment.LURE);
         }
      }
   }

   public static ManagerEgg findEggByItem(ItemStack item, Map<String, ManagerEgg> eggs) {
      if (item == null) {
         return null;
      } else {
         for (ManagerEgg egg : eggs.values()) {
            if (item.getType() == egg.getMaterial()) {
               ItemMeta meta = item.getItemMeta();
               if (meta != null && meta.hasEnchant(Enchantment.LURE)) {
                  return egg;
               }
            }
         }

         return null;
      }
   }

   public static ManagerEgg findEggBySpawnerNBT(ItemStack spawnerItem, Map<String, ManagerEgg> eggs, HolyCustomEgg plugin) {
      if (spawnerItem != null && spawnerItem.getType() == Material.SPAWNER) {
         BlockStateMeta meta = (BlockStateMeta)spawnerItem.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            NamespacedKey key = new NamespacedKey(plugin, "holy_egg_id");
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
               String eggId = (String)meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
               return eggs.get(eggId);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
