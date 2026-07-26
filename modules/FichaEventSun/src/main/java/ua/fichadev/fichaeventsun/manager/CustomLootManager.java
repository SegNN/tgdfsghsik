package ua.fichadev.fichaeventsun.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ua.fichadev.fichaeventsun.FichaEventSun;

public class CustomLootManager {
   private final FichaEventSun plugin;
   private File dataFile;
   private FileConfiguration dataConfig;

   public CustomLootManager(FichaEventSun plugin) {
      this.plugin = plugin;
      this.setupFile();
   }

   private void setupFile() {
      this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         try {
            this.plugin.getDataFolder().mkdirs();
            this.dataFile.createNewFile();
         } catch (IOException var2) {
            this.plugin.getLogger().severe("не удалось создать data.yml");
         }
      }

      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
   }

   public void save() {
      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var2) {
         this.plugin.getLogger().severe("не удалось сохранить data.yml");
      }
   }

   public void reload() {
      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
   }

   public boolean addItem(String id, ItemStack item) {
      if (this.dataConfig.contains("items." + id)) {
         return false;
      } else {
         this.dataConfig.set("items." + id, item);
         this.save();
         return true;
      }
   }

   public boolean removeItem(String id) {
      if (!this.dataConfig.contains("items." + id)) {
         return false;
      } else {
         this.dataConfig.set("items." + id, null);
         this.save();
         return true;
      }
   }

   public ItemStack getItem(String id) {
      return this.dataConfig.getItemStack("items." + id);
   }

   public List<String> getItemIds() {
      if (!this.dataConfig.contains("items")) {
         return new ArrayList<>();
      } else {
         Set<String> keys = this.dataConfig.getConfigurationSection("items").getKeys(false);
         return new ArrayList<>(keys);
      }
   }

   public List<ItemStack> getAllItems() {
      List<ItemStack> items = new ArrayList<>();

      for (String id : this.getItemIds()) {
         ItemStack item = this.getItem(id);
         if (item != null) {
            items.add(item.clone());
         }
      }

      return items;
   }

   public ItemStack getRandomItem() {
      List<ItemStack> items = this.getAllItems();
      if (items.isEmpty()) {
         return null;
      } else {
         int index = (int)(Math.random() * items.size());
         return items.get(index);
      }
   }
}
