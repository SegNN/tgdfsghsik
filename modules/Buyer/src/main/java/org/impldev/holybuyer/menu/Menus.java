package org.impldev.holybuyer.menu;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.impldev.holybuyer.Texts;

/** Мелкие помощники для сборки предметов меню. */
public final class Menus {
   public static final ItemStack FILLER = pane(Material.GRAY_STAINED_GLASS_PANE);

   private Menus() {
   }

   public static ItemStack pane(Material material) {
      return item(material, "", List.of());
   }

   public static ItemStack item(Material material, String name, List<String> lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Texts.color(name));
         if (!lore.isEmpty()) {
            meta.setLore(Texts.color(lore));
         }

         // Зачарования на витрине - украшение: подсказка про них только мешает читать описание.
         meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static ItemStack item(Material material, String name, String... lore) {
      return item(material, name, Arrays.asList(lore));
   }
}
