package ru.holydev.holycustom.items;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holystar.HolyStar;
import ru.holydev.holystar.star.StarManager;

public class StarItem {
   public StarItem() {
   }

   public static String convertInt(int i) {
      if (i >= 1 && i <= 3999) {
         int[] arabicValues = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
         String[] romanSymbols = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
         StringBuilder result = new StringBuilder();

         for (int j = 0; j < arabicValues.length; j++) {
            while (i >= arabicValues[j]) {
               result.append(romanSymbols[j]);
               i -= arabicValues[j];
            }
         }

         return result.toString();
      } else {
         throw new IllegalArgumentException("Число должно быть в диапазоне от 1 до 3999");
      }
   }

   public ItemStack getStar(Material material, String color, int price, int level, Player player) {
      ItemStack shulker = new ItemStack(material);
      ItemMeta itemMeta = shulker.getItemMeta();
      itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', color + convertInt(level) + " Звезда статуса (" + level + " ур.)"));
      List<String> lore = new ArrayList<>();
      if (HolyCustom.getHolyStar() == null) {
         lore.add(ChatColor.RED + "Ошибка: HolyStar не загружен!");
         itemMeta.setLore(lore);
         shulker.setItemMeta(itemMeta);
         return shulker;
      } else {
         StarManager starManager = new StarManager(HolyStar.getDatabase());
         int donated = starManager.get(player);
         lore.add(ChatColor.translateAlternateColorCodes('&', ""));
         lore.add(ChatColor.translateAlternateColorCodes('&', "&fПожертвовано:"));
         if (donated >= price) {
            lore.add(ChatColor.translateAlternateColorCodes('&', color + donated + "₽ / " + price + "₽ [100%]"));
         } else {
            int percentage = (int)((double)donated / price * 100.0);
            lore.add(ChatColor.translateAlternateColorCodes('&', color + donated + "₽ / " + price + "₽ [" + percentage + "%]"));
            lore.add(ChatColor.translateAlternateColorCodes('&', ""));
            lore.add(ChatColor.translateAlternateColorCodes('&', " &x&F&F&0&0&0&0✘ &fДля активации звезды, Вам"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "   &f необходимо &x&F&F&0&0&0&0пожертвовать"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "   &f через сайт &x&F&F&0&0&0&0еще " + (price - donated)) + "₽");
         }

         lore.add(ChatColor.translateAlternateColorCodes('&', ""));
         itemMeta.setLore(lore);
         shulker.setItemMeta(itemMeta);
         return shulker;
      }
   }
}
