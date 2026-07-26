package org.nezxenka.litecontainer.config;

import java.util.Map;
import org.nezxenka.litecontainer.util.LegacyBlobReader;
import org.nezxenka.litecontainer.util.LegacySkullFix;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.nezxenka.litecontainer.utils.ContainerItem;
import org.nezxenka.litecontainer.utils.ItemRarity;

public class LootManager {
   private final Plugin plugin;
   private final File configFile;
   private FileConfiguration config;
   private final Map<ItemRarity, List<ContainerItem>> itemsByRarity = new ConcurrentHashMap<>();
   private final Map<String, ContainerItem> itemsByName = new ConcurrentHashMap<>();
   private boolean initialized = false;

   public LootManager(Plugin plugin) {
      this.plugin = plugin;
      this.configFile = new File(plugin.getDataFolder(), "items.yml");
      this.loadConfig();
      this.loadAllItems();
   }

   private void loadConfig() {
      if (!this.configFile.exists()) {
         this.plugin.saveResource("items.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(this.configFile);
   }

   public synchronized void loadAllItems() {
      this.itemsByRarity.clear();
      this.itemsByName.clear();

      for (ItemRarity rarity : ItemRarity.values()) {
         this.itemsByRarity.put(rarity, new ArrayList<>());
      }

      // головы из 1.16.5 хранят профиль в старом формате, 1.21.4 разбирает его в пустой
      // профиль -> текстура пропадает. Чиним при загрузке, файл на диске не трогаем.
      Map<String, String> legacyBlobs = LegacyBlobReader.read(this.configFile);
      int repairedSkulls = 0;
      ConfigurationSection itemsSection = this.config.getConfigurationSection("items");
      if (itemsSection != null) {
         for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
               try {
                  ItemRarity rarity = ItemRarity.valueOf(itemSection.getString("rarity", "DEFAULT").toUpperCase());
                  double value = itemSection.getDouble("value", 0.0);
                  ItemStack itemStack = itemSection.getItemStack("item");
                  if (itemStack != null) {
                     if (LegacySkullFix.repair(itemStack, legacyBlobs.get("items." + key + ".item"))) {
                        repairedSkulls++;
                     }

                     ContainerItem containerItem = new ContainerItem(key, rarity, value, itemStack);
                     this.itemsByRarity.get(rarity).add(containerItem);
                     this.itemsByName.put(key, containerItem);
                  }
               } catch (IllegalArgumentException var10) {
                  this.plugin.getLogger().warning("Неизвестная редкость для предмета: " + key);
               }
            }
         }

         if (repairedSkulls > 0) {
            this.plugin.getLogger().info("Restored legacy (1.16.5) skull textures for " + repairedSkulls + " item(s) in items.yml");
         }

         this.initialized = true;
      }
   }

   public void addItem(String name, ItemRarity rarity, double value, ItemStack item) {
      ConfigurationSection itemSection = this.config.createSection("items." + name);
      itemSection.set("rarity", rarity.toString());
      itemSection.set("value", value);
      itemSection.set("item", item.clone());

      try {
         this.config.save(this.configFile);
      } catch (IOException var11) {
         this.plugin.getLogger().severe("Не удалось сохранить items.yml: " + var11.getMessage());
      }

      ContainerItem containerItem = new ContainerItem(name, rarity, value, item.clone());
      synchronized (this) {
         this.itemsByRarity.computeIfAbsent(rarity, k -> new ArrayList<>()).add(containerItem);
         this.itemsByName.put(name, containerItem);
      }
   }

   public boolean removeItem(String name) {
      if (!this.config.contains("items." + name)) {
         return false;
      } else {
         this.config.set("items." + name, null);

         try {
            this.config.save(this.configFile);
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Не удалось сохранить items.yml: " + var7.getMessage());
         }

         synchronized (this) {
            ContainerItem removed = this.itemsByName.remove(name);
            if (removed != null) {
               List<ContainerItem> rarityItems = this.itemsByRarity.get(removed.rarity());
               if (rarityItems != null) {
                  rarityItems.removeIf(item -> item.name().equals(name));
               }
            }

            return true;
         }
      }
   }

   public ContainerItem getRandomItem(ItemRarity rarity) {
      if (!this.initialized) {
         this.loadAllItems();
      }

      List<ContainerItem> items = this.itemsByRarity.get(rarity);
      if (items != null && !items.isEmpty()) {
         int randomIndex = ThreadLocalRandom.current().nextInt(items.size());
         return items.get(randomIndex);
      } else {
         return null;
      }
   }

   public Collection<ContainerItem> getAllItems() {
      return this.itemsByName.values();
   }

   public ContainerItem getItemByName(String name) {
      return this.itemsByName.get(name);
   }

   public List<ContainerItem> getItemsByRarity(ItemRarity rarity) {
      return Collections.unmodifiableList(this.itemsByRarity.getOrDefault(rarity, Collections.emptyList()));
   }

   public void saveConfig() {
      try {
         this.config.save(this.configFile);
      } catch (IOException var2) {
         this.plugin.getLogger().severe("Не удалось сохранить items.yml: " + var2.getMessage());
      }
   }

   public void reloadConfig() {
      this.loadConfig();
      this.loadAllItems();
   }
}
