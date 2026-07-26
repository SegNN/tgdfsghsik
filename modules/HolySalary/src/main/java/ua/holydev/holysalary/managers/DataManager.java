package ua.holydev.holysalary.managers;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DataManager {
   private final JavaPlugin plugin;
   private File dataFile;
   private FileConfiguration data;

   public DataManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.loadDataFile();
   }

   private void loadDataFile() {
      this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         try {
            this.dataFile.createNewFile();
         } catch (IOException var2) {
            var2.printStackTrace();
         }
      }

      this.data = YamlConfiguration.loadConfiguration(this.dataFile);
   }

   public void saveData() {
      try {
         this.data.save(this.dataFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   public FileConfiguration getData() {
      return this.data;
   }

   public void reloadData() {
      this.data = YamlConfiguration.loadConfiguration(this.dataFile);
   }
}
