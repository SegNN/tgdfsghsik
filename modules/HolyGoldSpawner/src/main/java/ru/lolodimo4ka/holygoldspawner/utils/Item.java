package ru.lolodimo4ka.holygoldspawner.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holygoldspawner.events.Config;

public class Item {
   private final JavaPlugin plugin;
   private final Config configManager;
   private Material spawnerItemType;
   private List<String> spawnerLore;

   public Item(JavaPlugin plugin, Config configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.loadItemConfig();
   }

   public void reloadConfig() {
      this.loadItemConfig();
   }

   private void loadItemConfig() {
      try {
         this.spawnerItemType = Material.valueOf(this.configManager.getConfig().getString("settings.spawner.material", "SPAWNER"));
      } catch (IllegalArgumentException var2) {
         this.spawnerItemType = Material.SPAWNER;
      }

      this.spawnerLore = this.configManager.translateHexColors(this.configManager.getConfig().getStringList("settings.spawner.lore"));
   }

   public ItemStack createGoldenSpawner(int durability) {
      ItemStack spawner = new ItemStack(this.spawnerItemType);
      ItemMeta meta = spawner.getItemMeta();
      if (meta == null) {
         return spawner;
      } else {
         meta.setDisplayName(this.configManager.getSpawnerName());
         List<String> lore = new ArrayList<>();
         if (this.spawnerLore != null && !this.spawnerLore.isEmpty()) {
            for (String line : this.spawnerLore) {
               if (line != null && !line.isEmpty()) {
                  double breakChance = (this.configManager.maxDurability - durability) * this.configManager.breakChanceMultiplier * 100.0;
                  lore.add(
                     line.replace("%radius%", String.valueOf(this.configManager.spawnerRadius))
                        .replace("%min%", this.configManager.getDecimalFormat().format(this.configManager.minCoins))
                        .replace("%max%", this.configManager.getDecimalFormat().format(this.configManager.maxCoins))
                        .replace("%durability%", String.valueOf(durability))
                        .replace("%max_durability%", String.valueOf(this.configManager.maxDurability))
                        .replace("%break_chance%", this.configManager.getPercentFormat().format(breakChance))
                  );
               }
            }
         }

         if (!lore.isEmpty()) {
            meta.setLore(lore);
         }

         meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "golden_spawner"), PersistentDataType.INTEGER, 1);
         meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "durability"), PersistentDataType.INTEGER, durability);
         spawner.setItemMeta(meta);
         return spawner;
      }
   }

   public boolean isGoldenSpawner(ItemStack item) {
      if (item != null && item.getType() == this.spawnerItemType && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "golden_spawner"), PersistentDataType.INTEGER);
      } else {
         return false;
      }
   }

   public int getSpawnerDurability(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return (Integer)meta.getPersistentDataContainer()
            .getOrDefault(new NamespacedKey(this.plugin, "durability"), PersistentDataType.INTEGER, this.configManager.maxDurability);
      } else {
         return this.configManager.maxDurability;
      }
   }
}
