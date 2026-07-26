package ua.fichatime.fichaspawneroved.item;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichatime.fichaspawneroved.FichaSpawneroved;
import ua.fichatime.fichaspawneroved.Utils.Color;

public class Item {
   private final FichaSpawneroved plugin;

   public Item(FichaSpawneroved plugin) {
      this.plugin = plugin;
   }

   public ItemStack createEgg(EggType type) {
      ConfigurationSection itemSection = this.plugin.getConfig().getConfigurationSection("item." + type.getConfigKey());
      return this.createEggFromSection(itemSection);
   }

   public ItemStack createShardEgg() {
      return this.createEgg(EggType.SHARD_EGG);
   }

   public ItemStack createEndShardEgg() {
      return this.createEgg(EggType.END_SPLINTER_EGG);
   }

   private ItemStack createEggFromSection(ConfigurationSection itemSection) {
      if (itemSection == null) {
         return new ItemStack(Material.BARRIER);
      } else {
         String materialName = itemSection.getString("material");
         if (materialName == null) {
            return new ItemStack(Material.BARRIER);
         } else {
            ItemStack item = new ItemStack(Material.valueOf(materialName));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
               String name = itemSection.getString("name");
               if (name != null) {
                  meta.setDisplayName(Color.hex(name));
               }

               List<String> loreList = itemSection.getStringList("lore");
               if (!loreList.isEmpty()) {
                  meta.setLore(Color.hexList(loreList));
               }

               item.setItemMeta(meta);
            }

            return item;
         }
      }
   }
}
