package stickdev.dimasik.stickhwtnt.tnt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

public class TnTData {
   private final JavaPlugin plugin;
   private final Yaml yaml;
   private final File dataFile;

   public TnTData(JavaPlugin plugin) {
      this.plugin = plugin;
      this.dataFile = new File(plugin.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         plugin.saveResource("data.yml", false);
      }

      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(FlowStyle.BLOCK);
      Representer representer = new Representer();
      representer.getPropertyUtils().setSkipMissingProperties(true);
      this.yaml = new Yaml(new Constructor(), representer, options);
   }

   public void addData(Location location, TnTType type) throws IOException {
      Map<String, Map<String, String>> data = this.loadData();
      Map<String, String> tntData = new HashMap<>();
      tntData.put("TnTType", type.name());
      data.put(this.serializeLocation(location), tntData);
      this.saveData(data);
   }

   public void removeData(Location location) throws IOException {
      Map<String, Map<String, String>> data = this.loadData();
      data.remove(this.serializeLocation(location));
      this.saveData(data);
   }

   public void updateData(Location oldLocation, Location newLocation) throws IOException {
      Map<String, Map<String, String>> data = this.loadData();
      String oldKey = this.serializeLocation(oldLocation);
      if (data.containsKey(oldKey)) {
         Map<String, String> tntData = data.get(oldKey);
         data.remove(oldKey);
         data.put(this.serializeLocation(newLocation), tntData);
         this.saveData(data);
      }
   }

   public TnTType getTntType(Location location) throws IOException {
      Map<String, Map<String, String>> data = this.loadData();
      String key = this.serializeLocation(location);
      return data.containsKey(key) ? TnTType.valueOf(data.get(key).get("TnTType")) : null;
   }

   private Map<String, Map<String, String>> loadData() throws IOException {
      if (!this.dataFile.exists()) {
         return new HashMap<>();
      } else {
         try {
            FileInputStream inputStream = new FileInputStream(this.dataFile);

            Object var3;
            try {
               Map<String, Map<String, String>> data = (Map<String, Map<String, String>>)this.yaml.load(inputStream);
               var3 = data == null ? new HashMap() : data;
            } catch (Throwable var5) {
               try {
                  inputStream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }

               throw var5;
            }

            inputStream.close();
            return (Map<String, Map<String, String>>)var3;
         } catch (IOException var6) {
            throw var6;
         }
      }
   }

   private void saveData(Map<String, Map<String, String>> data) throws IOException {
      try {
         FileWriter writer = new FileWriter(this.dataFile);

         try {
            this.yaml.dump(data, writer);
         } catch (Throwable var6) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         writer.close();
      } catch (IOException var7) {
         throw var7;
      }
   }

   private String serializeLocation(Location location) {
      return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
   }
}
