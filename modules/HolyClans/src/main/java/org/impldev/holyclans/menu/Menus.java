package org.impldev.holyclans.menu;

import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.impldev.holyclans.Texts;
import net.kyori.adventure.text.Component;

/**
 * Хелперы для создания предметов в меню.
 */
public final class Menus {
   private Menus() {}

   public static ItemStack item(Material mat, String name) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(Texts.color(name));
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack item(Material mat, String name, List<String> lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(Texts.color(name));
      meta.lore(lore.stream().map(Texts::color).toList());
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack glass(Material mat) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(Component.empty());
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack back() {
      return item(Material.ARROW, "&r&x&f&f&2&2&2&2◀ &r&fНазад");
   }

   public static ItemStack close() {
      return item(Material.ARROW, "&r&x&f&f&2&2&2&2◀ &r&fЗакрыть меню");
   }

   public static ItemStack coloredChestplate(int rgb, boolean selected) {
      ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
      LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
      meta.setColor(Color.fromRGB(rgb));
      meta.displayName(Component.empty());
      if (selected) {
         meta.lore(List.of(Texts.color("&r&x&0&5&f&b&0&0✔ Этот цвет брони установлен"), Component.empty()));
         meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
         meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
      } else {
         meta.lore(List.of(Texts.color("&r &r&x&0&0&d&8&f&f▶&r&f Нажмите, чтобы установить этот цвет"), Component.empty()));
      }
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
      meta.addItemFlags(ItemFlag.HIDE_DYE);
      item.setItemMeta(meta);
      return item;
   }
}
