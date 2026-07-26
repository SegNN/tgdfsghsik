package ru.lolodimo4ka.holycustomegg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class ConfigEgg {
   private final Map<String, ManagerEgg> eggs = new HashMap<>();
   private final FileConfiguration config;

   public ConfigEgg(FileConfiguration config) {
      this.config = config;
      this.loadEggs();
   }

   private void loadEggs() {
      this.eggs.clear();
      if (this.config.contains("eggs")) {
         for (String eggKey : this.config.getConfigurationSection("eggs").getKeys(false)) {
            String path = "eggs." + eggKey + ".";
            String name = this.config.getString(path + "name");
            List<String> lore = this.config.getStringList(path + "lore");
            String materialName = this.config.getString(path + "material");
            String spawnerName = this.config.getString(path + "spawner");
            List<String> spawnerLore = this.config.getStringList(path + "spawnerlore");
            if (name != null && materialName != null) {
               Material material;
               try {
                  material = Material.valueOf(materialName.toUpperCase());
               } catch (IllegalArgumentException var11) {
                  continue;
               }

               if (lore == null) {
                  lore = new ArrayList<>();
               }

               if (spawnerLore == null) {
                  spawnerLore = new ArrayList<>();
               }

               if (spawnerName == null) {
                  spawnerName = name;
               }

               ManagerEgg egg = new ManagerEgg(eggKey, name, lore, material, spawnerName, spawnerLore);
               this.loadMobsForEgg(egg, path);
               this.eggs.put(eggKey, egg);
            }
         }
      }
   }

   private void loadMobsForEgg(ManagerEgg egg, String path) {
      List<String> configFields = List.of("name", "lore", "material", "spawner", "spawnerlore");

      for (String key : this.config.getConfigurationSection(path).getKeys(false)) {
         if (!configFields.contains(key)) {
            double chance = this.config.getDouble(path + key);
            if (!(chance <= 0.0)) {
               if (key.equalsIgnoreCase("zombie_baby")) {
                  egg.addMob(EntityType.ZOMBIE, chance, true);
               } else if (key.equalsIgnoreCase("zombie")) {
                  egg.addMob(EntityType.ZOMBIE, chance, false);
               } else {
                  try {
                     EntityType entityType = EntityType.valueOf(key.toUpperCase());
                     egg.addMob(entityType, chance);
                  } catch (IllegalArgumentException var9) {
                  }
               }
            }
         }
      }
   }

   public Map<String, ManagerEgg> getEggs() {
      return new HashMap<>(this.eggs);
   }
}
