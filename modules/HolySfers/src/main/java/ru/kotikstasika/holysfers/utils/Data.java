package ru.kotikstasika.holysfers.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.holysfers.HolySfers;

public class Data {
   private final HolySfers plugin = HolySfers.getInstance();
   private File dataFile;
   private YamlConfiguration dataConfig;

   public Data() {
      this.setup();
   }

   private void setup() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdir();
      }

      this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         try {
            this.dataFile.createNewFile();
         } catch (IOException var2) {
            var2.printStackTrace();
         }
      }

      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
   }

   public void saveItem(String name, ItemStack item) {
      this.dataConfig.set(name, item);
      this.save();
   }

   public void deleteItem(String name) {
      this.dataConfig.set(name, null);
      this.save();
   }

   public ItemStack getItem(String name) {
      return this.dataConfig.getItemStack(name);
   }

   private void save() {
      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   public List<String> getSavedItemNames() {
      return new ArrayList<>(this.dataConfig.getKeys(false));
   }
}
