package ua.holydev.holypotion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.holydev.holypotion.HolyPotion;

public class PotionManager {
   private final HolyPotion plugin;
   private final NamespacedKey typeKey;
   /**
    * The plugin used to be called FichaPotion, so items handed out before the rename carry
    * `fichapotion:type` instead of `holypotion:type`. NBT strings are length prefixed, so those
    * items cannot be rewritten in place - they are read through this legacy key instead.
    */
   private final NamespacedKey legacyTypeKey;

   public PotionManager(HolyPotion plugin) {
      this.plugin = plugin;
      this.typeKey = new NamespacedKey(plugin, "type");
      this.legacyTypeKey = NamespacedKey.fromString("fichapotion:type");
   }

   public NamespacedKey getTypeKey() {
      return this.typeKey;
   }

   public void savePotion(String type, ItemStack item) {
      FileConfiguration config = this.plugin.getConfig();
      config.set("potions." + type, item);
      this.plugin.saveConfig();
   }

   public ItemStack getPotion(String type) {
      FileConfiguration config = this.plugin.getConfig();
      ItemStack itemStack = config.getItemStack("potions." + type);
      if (itemStack == null) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta != null) {
            meta.getPersistentDataContainer().set(this.typeKey, PersistentDataType.STRING, type);
            itemStack.setItemMeta(meta);
         }

         return itemStack;
      }
   }

   public String getPotionType(ItemStack item) {
      if (item == null || !item.hasItemMeta()) {
         return "none";
      } else {
         var container = item.getItemMeta().getPersistentDataContainer();
         String type = container.get(this.typeKey, PersistentDataType.STRING);
         if (type == null && this.legacyTypeKey != null) {
            type = container.get(this.legacyTypeKey, PersistentDataType.STRING);
         }

         return type == null ? "none" : type;
      }
   }

   public List<String> getPotionTypes() {
      FileConfiguration config = this.plugin.getConfig();
      if (!config.contains("potions")) {
         return new ArrayList<>();
      } else {
         Set<String> keys = config.getConfigurationSection("potions").getKeys(false);
         return new ArrayList<>(keys);
      }
   }
}
