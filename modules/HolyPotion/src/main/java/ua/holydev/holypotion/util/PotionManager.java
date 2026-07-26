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

   public PotionManager(HolyPotion plugin) {
      this.plugin = plugin;
      this.typeKey = new NamespacedKey(plugin, "type");
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
      return item != null && item.hasItemMeta()
         ? (String)item.getItemMeta().getPersistentDataContainer().getOrDefault(this.typeKey, PersistentDataType.STRING, "none")
         : "none";
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
