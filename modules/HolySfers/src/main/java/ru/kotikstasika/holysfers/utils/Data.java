package ru.kotikstasika.holysfers.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.holysfers.HolySfers;

public class Data {
   private static final String INTERNAL_PREFIX = "internal: ";
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
      this.repairLegacySkulls();
   }

   /**
    * data.yml still holds ItemStacks serialized by CraftBukkit 1.16.5. Their skull
    * profile lives in the opaque {@code internal} blob in the legacy GameProfile NBT
    * layout, which 1.21.4 silently decodes into an empty profile - the head then
    * renders as the default Steve/Alex skin. Re-apply those textures in memory so both
    * {@link #getItem(String)} and any later save see the real profile.
    */
   private void repairLegacySkulls() {
      Map<String, String> internals = this.readInternalBlobs();
      if (internals.isEmpty()) {
         return;
      }

      int repaired = 0;

      for (String key : this.dataConfig.getKeys(false)) {
         ItemStack item;
         try {
            item = this.dataConfig.getItemStack(key);
         } catch (Exception var6) {
            continue;
         }

         if (LegacySkullFix.repair(item, internals.get(key))) {
            this.dataConfig.set(key, item);
            repaired++;
         }
      }

      if (repaired > 0 && this.plugin != null) {
         this.plugin.getLogger().info("Restored legacy (1.16.5) skull textures for " + repaired + " item(s) in data.yml");
      }
   }

   /**
    * Reads the raw {@code internal} values straight out of data.yml, keyed by top-level
    * entry name. YamlConfiguration deserializes ConfigurationSerializable values eagerly,
    * so the blob is no longer reachable through the loaded config.
    */
   private Map<String, String> readInternalBlobs() {
      Map<String, String> internals = new HashMap<>();
      if (this.dataFile == null || !this.dataFile.isFile()) {
         return internals;
      }

      try (BufferedReader reader = Files.newBufferedReader(this.dataFile.toPath(), StandardCharsets.UTF_8)) {
         String key = null;
         String line;

         while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
               continue;
            }

            if (!Character.isWhitespace(line.charAt(0))) {
               key = line.endsWith(":") ? line.substring(0, line.length() - 1) : null;
               continue;
            }

            String trimmed = line.trim();
            if (key != null && trimmed.startsWith(INTERNAL_PREFIX)) {
               internals.put(key, trimmed.substring(INTERNAL_PREFIX.length()).trim());
            }
         }
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      return internals;
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
