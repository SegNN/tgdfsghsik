package ua.fichadev.fichabambuk.item;

import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import ua.fichadev.fichabambuk.util.ColorUtils;

public final class BambukItem {
   private static final String NAME = "&#13C600Л&#12BB00у&#12B101К &#109B02Б&#12B101а&#0F8603М&#0E7B03Б&#0E7104У&#0D6604к";
   private static final String LORE = "&fМемное оружие ЗАКО";
   private final NamespacedKey key;

   public BambukItem(Plugin plugin) {
      this.key = new NamespacedKey(plugin, "bambuk_bow");
   }

   public NamespacedKey key() {
      return this.key;
   }

   public ItemStack create() {
      ItemStack bow = new ItemStack(Material.BOW);
      ItemMeta meta = bow.getItemMeta();
      meta.setDisplayName(ColorUtils.colorize("&#13C600Л&#12BB00у&#12B101К &#109B02Б&#12B101а&#0F8603М&#0E7B03Б&#0E7104У&#0D6604к"));
      meta.setLore(Collections.singletonList(ColorUtils.colorize("&fМемное оружие ЗАКО")));
      meta.addEnchant(Enchantment.INFINITY, 1, true);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
      meta.setUnbreakable(true);
      meta.getPersistentDataContainer().set(this.key, PersistentDataType.BYTE, (byte)1);
      bow.setItemMeta(meta);
      return bow;
   }

   public boolean matches(ItemStack stack) {
      if (stack != null && stack.getType() == Material.BOW) {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            Byte v = (Byte)meta.getPersistentDataContainer().get(this.key, PersistentDataType.BYTE);
            return v != null && v == 1;
         }
      } else {
         return false;
      }
   }
}
