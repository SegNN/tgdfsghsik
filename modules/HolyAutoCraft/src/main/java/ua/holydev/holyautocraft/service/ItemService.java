package ua.holydev.holyautocraft.service;

import java.util.List;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.holydev.holyautocraft.HolyAutoCraft;
import ua.holydev.holyautocraft.core.Config;

public class ItemService {
   public ItemService() {
   }

   public static void applyAutoCraftProperties(ItemStack item, HolyAutoCraft plugin) {
      Config cfg = plugin.getConfigManager();
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(cfg.getCraftName());
         if (cfg.isGlowEnabled()) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         meta.getPersistentDataContainer().set(cfg.getCraftKey(), PersistentDataType.BYTE, (byte)1);
         List<String> lore = cfg.getCraftLore();
         if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
         }

         item.setItemMeta(meta);
      }
   }
}
