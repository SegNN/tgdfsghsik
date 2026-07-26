package ru.kotikstasika.eventpolana.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import ru.kotikstasika.eventpolana.EventPolana;

public class PluginConfig {
   private final EventPolana plugin;
   private final FileConfiguration config;

   public PluginConfig(EventPolana plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
   }

   public String getWorldName() {
      return this.config.getString("event.world", "world");
   }

   public int getFlowerSpawnRadius() {
      return this.config.getInt("event.flower_spawn_radius", 25);
   }

   public int getFlowerBreakRadius() {
      return this.config.getInt("event.flower_break_radius", 35);
   }

   public String getSchematicName() {
      return this.config.getString("event.schematic_name", "event_polana.schem");
   }

   public int getFlowersMaxConcurrent() {
      return this.config.getInt("flowers.max_concurrent_flowers", 100);
   }

   public double getGardenerPlayerSearchRadius() {
      return this.config.getDouble("gardener.player_search_radius", 50.0);
   }

   public int getGardenerLookUpdateInterval() {
      return this.config.getInt("gardener.look_update_interval", 10);
   }

   public String getGardenerProfession() {
      return this.config.getString("gardener.profession", "FARMER");
   }

   public String getGardenerVillagerType() {
      return this.config.getString("gardener.villager_type", "PLAINS");
   }

   public int getGardenerLevel() {
      return this.config.getInt("gardener.level", 5);
   }

   public int getShearsMinDurability() {
      return this.config.getInt("shears.min_durability", 1);
   }

   public int getShearsMaxDurability() {
      return this.config.getInt("shears.max_durability", 10);
   }

   public String getShearsDisplayName() {
      return this.config.getString("shears.display_name", "&6Ножницы");
   }

   public List<String> getShearsDescription() {
      return this.config.getStringList("shears.description");
   }

   public int getDeleteMinutesDefault() {
      return this.config.getInt("durations.delete.default", 10);
   }

   public int getDeleteMinutesEpic() {
      return this.config.getInt("durations.delete.epic", 20);
   }

   public int getDeleteMinutesLegendary() {
      return this.config.getInt("durations.delete.legendary", 30);
   }

   public int getRespawnShearsSecondsDefault() {
      return this.config.getInt("respawn_shears.default", 30);
   }

   public int getRespawnShearsSecondsEpic() {
      return this.config.getInt("respawn_shears.epic", 20);
   }

   public int getRespawnShearsSecondsLegendary() {
      return this.config.getInt("respawn_shears.legendary", 10);
   }

   public int getSpawnFlowerSecondsDefault() {
      return this.config.getInt("spawn_flower.default", 30);
   }

   public int getSpawnFlowerSecondsEpic() {
      return this.config.getInt("spawn_flower.epic", 20);
   }

   public int getSpawnFlowerSecondsLegendary() {
      return this.config.getInt("spawn_flower.legendary", 10);
   }

   public int getLootLevelDefault() {
      return this.config.getInt("loot.default", 1);
   }

   public int getLootLevelEpic() {
      return this.config.getInt("loot.epic", 2);
   }

   public int getLootLevelLegendary() {
      return this.config.getInt("loot.legendary", 3);
   }

   public String getMessageSpawnTitle() {
      return this.config.getString("messages.spawn.title", "&x&4&3&B&C&0&6&lЦветочная поляна");
   }

   public String getMessageRarityNameDefault() {
      return this.config
         .getString(
            "messages.spawn.rarity_names.default",
            "&x&E&2&8&9&8&9&lО&x&E&0&8&D&8&D&lб&x&D&E&9&0&9&0&lы&x&D&C&9&4&9&4&lч&x&D&A&9&8&9&8&lн&x&D&8&9&B&9&B&lа&x&D&6&9&F&9&F&lя"
         );
   }

   public String getMessageRarityNameEpic() {
      return this.config
         .getString(
            "messages.spawn.rarity_names.epic",
            "&x&D&C&1&C&D&E&lЭ&x&D&7&1&9&E&2&lп&x&D&2&1&5&E&6&lи&x&C&D&1&2&E&A&lч&x&C&8&0&E&E&F&lе&x&C&2&0&B&F&3&lс&x&B&D&0&7&F&7&lк&x&B&8&0&4&F&B&lа&x&B&3&0&0&F&F&lя"
         );
   }

   public String getMessageRarityNameLegendary() {
      return this.config
         .getString(
            "messages.spawn.rarity_names.legendary",
            "&x&1&C&B&F&D&E&lЛ&x&1&9&C&4&E&1&lе&x&1&6&C&A&E&5&lг&x&1&4&C&F&E&8&lе&x&1&1&D&5&E&B&lн&x&0&E&D&A&E&F&lд&x&0&B&D&F&F&2&lа&x&0&8&E&5&F&5&lр&x&0&6&E&A&F&8&lн&x&0&3&F&0&F&C&lа&x&0&0&F&5&F&F&lя"
         );
   }

   public String getMessageCoordinatesFormat() {
      return this.config.getString("messages.spawn.coordinates_format", "&fX: &x&4&3&B&C&0&6{x} &fY: &x&4&3&B&C&0&6{y} &fZ: &x&4&3&B&C&0&6{z}");
   }

   public String getMessageSchematicNotFound() {
      return this.config.getString("messages.errors.schematic_not_found", "&cНе удалось загрузить схематику поляны!");
   }

   public String getSoundShearsGiven() {
      return this.config.getString("sounds.shears_given", "ENTITY_VILLAGER_YES");
   }

   public String getSoundFlowerBreak() {
      return this.config.getString("sounds.flower_break", "BLOCK_GRASS_BREAK");
   }

   public String getSoundLootDrop() {
      return this.config.getString("sounds.loot_drop", "ENTITY_ITEM_PICKUP");
   }

   public String getParticleFlowerBreakType() {
      return this.config.getString("particles.flower_break.type", "VILLAGER_HAPPY");
   }

   public int getParticleFlowerBreakCount() {
      return this.config.getInt("particles.flower_break.count", 10);
   }

   public double getParticleFlowerBreakOffsetX() {
      return this.config.getDouble("particles.flower_break.offset_x", 0.3);
   }

   public double getParticleFlowerBreakOffsetY() {
      return this.config.getDouble("particles.flower_break.offset_y", 0.3);
   }

   public double getParticleFlowerBreakOffsetZ() {
      return this.config.getDouble("particles.flower_break.offset_z", 0.3);
   }

   public String formatMessage(String message, String... replacements) {
      String formatted = message;

      for (int i = 0; i < replacements.length; i += 2) {
         if (i + 1 < replacements.length) {
            formatted = formatted.replace("{" + replacements[i] + "}", replacements[i + 1]);
         }
      }

      return formatted;
   }
}
