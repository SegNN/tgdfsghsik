package org.dimasik.holyenchantments.enchantscore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.HolyEnchantments;
import org.dimasik.holyenchantments.utils.MColor;

public class EnchantManager {
   private final HolyEnchantments plugin;
   private static EnchantManager instance;
   public final HashMap<Integer, String> levelsFormat;
   private final HashMap<NamespacedKey, MEnchant> enchantsNameSpaced = new HashMap<>();
   private final HashMap<String, NamespacedKey> enchantsNames = new HashMap<>();
   private final NamespacedKey bookItemKeyName;
   private final NamespacedKey bookItemKeyLvl;

   public EnchantManager(HolyEnchantments plugin) {
      instance = this;
      this.plugin = plugin;
      this.bookItemKeyName = new NamespacedKey(plugin, "holyenchantments-book-name");
      this.bookItemKeyLvl = new NamespacedKey(plugin, "holyenchantments-book-lvl");
      this.levelsFormat = new HashMap<>();

      for (String key : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("display-levels")).getKeys(false)) {
         this.levelsFormat.put(Integer.parseInt(key), plugin.getConfig().getString("display-levels." + key));
      }
   }

   public ItemMeta applyEnchant(ItemMeta meta, MEnchant menchant, int level) {
      if (level == 0) {
         List<String> lore = new ArrayList<>();
         String displayName = MColor.prefix(menchant.getDisplayName());
         if (meta.getLore() != null) {
            List<String> oldLore = meta.getLore();

            for (int i = 0; i < oldLore.size(); i++) {
               if (oldLore.get(i).startsWith(displayName.replace(" %level", ""))) {
                  oldLore.remove(i);
               }
            }

            meta.getPersistentDataContainer().remove(menchant.getNamespacedKey());
            lore.addAll(oldLore);
         }

         meta.setLore(lore);
         return meta;
      } else {
         List<String> lore = new ArrayList<>();
         String displayName = MColor.prefix(menchant.getDisplayName());
         if (meta.getLore() != null) {
            List<String> oldLore = meta.getLore();

            for (int ix = 0; ix < oldLore.size(); ix++) {
               if (oldLore.get(ix).startsWith(displayName.replace(" %level", ""))) {
                  oldLore.remove(ix);
               }
            }

            lore.addAll(oldLore);
         }

         lore.add(0, displayName.replace("%level", this.levelsFormat.get(level)));
         meta.setLore(lore);
         meta.getPersistentDataContainer().set(menchant.getNamespacedKey(), PersistentDataType.INTEGER, level);
         return meta;
      }
   }

   public ItemStack getEnchantBook(MEnchant menchant, int lvl) {
      ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
      item.setItemMeta(this.applyEnchant(item.getItemMeta(), menchant, lvl));
      return item;
   }

   public String getFormatLevels(Integer lvl) {
      return this.levelsFormat.get(lvl);
   }

   public static EnchantManager getInstance() {
      return instance;
   }

   public NamespacedKey getBookItemKeyLvl() {
      return this.bookItemKeyLvl;
   }

   public NamespacedKey getBookItemKeyName() {
      return this.bookItemKeyName;
   }

   public HashMap<NamespacedKey, MEnchant> getEnchantsNameSpaced() {
      return this.enchantsNameSpaced;
   }

   public HashMap<String, NamespacedKey> getEnchantsNames() {
      return this.enchantsNames;
   }

   public void registerEnchant(String key, NamespacedKey namespacedKey, MEnchant enchant) {
      this.enchantsNames.put(key, namespacedKey);
      this.enchantsNameSpaced.put(namespacedKey, enchant);
   }
}
