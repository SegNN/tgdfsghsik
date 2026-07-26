package org.nezxenka.litecontainer.config;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.nezxenka.litecontainer.utils.ItemRarity;

public class PluginConfigManager {
   private final Plugin plugin;
   private FileConfiguration mainConfig;
   private FileConfiguration animationConfig;
   private FileConfiguration openMenuConfig;
   private List<String> openBroadcast;
   private List<Integer> animationSlots;
   private EnumMap<ItemRarity, Material> rarities;
   private String animationMenuTitle;
   private String noPermission;
   private int animationItemTicks;

   public PluginConfigManager(Plugin plugin) {
      this.plugin = plugin;
      plugin.saveDefaultConfig();
      this.mainConfig = plugin.getConfig();
      this.loadAnimationConfig();
      this.loadOpenMenuConfig();
      this.loadValues();
   }

   private void loadAnimationConfig() {
      File configFile = new File(this.plugin.getDataFolder(), "menu/animation_menu.yml");
      if (!configFile.exists()) {
         this.plugin.saveResource("menu/animation_menu.yml", false);
      }

      this.animationConfig = YamlConfiguration.loadConfiguration(configFile);
   }

   private void loadOpenMenuConfig() {
      File configFile = new File(this.plugin.getDataFolder(), "menu/open_menu.yml");
      if (!configFile.exists()) {
         this.plugin.saveResource("menu/open_menu.yml", false);
      }

      this.openMenuConfig = YamlConfiguration.loadConfiguration(configFile);
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.mainConfig = this.plugin.getConfig();
      this.loadAnimationConfig();
      this.loadOpenMenuConfig();
      this.loadValues();
   }

   public void loadValues() {
      ConfigurationSection messagesSection = this.mainConfig.getConfigurationSection("messages");
      if (messagesSection != null) {
         this.openBroadcast = messagesSection.getStringList("open_message");
         this.noPermission = messagesSection.getString("no_permission", "&cНет прав!");
      } else {
         this.openBroadcast = this.mainConfig.getStringList("messages.open_message");
         this.noPermission = this.mainConfig.getString("messages.no_permission", "&cНет прав!");
      }

      this.animationSlots = this.animationConfig.getIntegerList("item_slots");
      this.animationItemTicks = this.animationConfig.getInt("item_ticks", 10);
      this.animationMenuTitle = this.animationConfig.getString("title", "Контейнеры");
      this.loadRarities();
   }

   private void loadRarities() {
      this.rarities = new EnumMap<>(ItemRarity.class);
      if (this.animationConfig.contains("rarity_forms")) {
         ConfigurationSection raritySection = this.animationConfig.getConfigurationSection("rarity_forms");

         for (String key : raritySection.getKeys(false)) {
            try {
               ItemRarity rarity = ItemRarity.valueOf(key.toUpperCase());
               String materialName = raritySection.getString(key + ".material", "BLACK_STAINED_GLASS_PANE");
               Material material = Material.valueOf(materialName.toUpperCase());
               this.rarities.put(rarity, material);
            } catch (IllegalArgumentException var7) {
               this.plugin.getLogger().severe("Неизвестная редкость или материал: " + key);
            }
         }
      }
   }

   public FileConfiguration getMainConfig() {
      return this.mainConfig;
   }

   public FileConfiguration getAnimationConfig() {
      return this.animationConfig;
   }

   public FileConfiguration getOpenMenuConfig() {
      return this.openMenuConfig;
   }

   public List<String> getOpenBroadcast() {
      return this.openBroadcast;
   }

   public List<Integer> getAnimationSlots() {
      return this.animationSlots;
   }

   public EnumMap<ItemRarity, Material> getRarities() {
      return this.rarities;
   }

   public String getAnimationMenuTitle() {
      return this.animationMenuTitle;
   }

   public String getNoPermission() {
      return this.noPermission;
   }

   public int getAnimationItemTicks() {
      return this.animationItemTicks;
   }
}
