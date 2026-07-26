package ru.holydev.holystar.manager;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
   private final JavaPlugin plugin;
   private final Map<Integer, ConfigManager.StarTier> starTiers = new LinkedHashMap<>();
   private final Map<Integer, ConfigManager.StarTier> rainbowStars = new LinkedHashMap<>();
   private float hue = 0.0F;
   private static final float HUE_INCREMENT = 0.01F;

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.loadConfig();
      this.startRainbowAnimation();
   }

   private void loadConfig() {
      this.plugin.saveDefaultConfig();
      FileConfiguration config = this.plugin.getConfig();
      if (config.isConfigurationSection("stars")) {
         ConfigurationSection starsSection = config.getConfigurationSection("stars");

         for (String key : starsSection.getKeys(false)) {
            ConfigurationSection tierSection = starsSection.getConfigurationSection(key);
            int price = tierSection.getInt("price");
            String name = tierSection.getString("name");
            boolean isRainbow = tierSection.getBoolean("rainbow", false);
            ConfigManager.StarTier starTier = new ConfigManager.StarTier(name, price, isRainbow);
            this.starTiers.put(price, starTier);
            if (!isRainbow) {
               this.rainbowStars.put(price, starTier);
            }
         }
      }
   }

   private void startRainbowAnimation() {
      this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
         this.hue += 0.01F;
         if (this.hue > 1.0F) {
            this.hue = 0.0F;
         }
      }, 0L, 1L);
   }

   public String getStarForCount(int count) {
      String defaultStar = "";
      int highestTier = 0;

      for (Entry<Integer, ConfigManager.StarTier> entry : this.starTiers.entrySet()) {
         if (count >= entry.getKey() && entry.getKey() > highestTier) {
            highestTier = entry.getKey();
         }
      }

      if (highestTier > 0) {
         ConfigManager.StarTier tier = this.starTiers.get(highestTier);
         if (tier.isRainbow()) {
            return this.getRainbowStar() + " ";
         }

         defaultStar = tier.getName() + " ";
      }

      return defaultStar;
   }

   private String getRainbowStar() {
      Color color = Color.getHSBColor(this.hue, 1.0F, 1.0F);
      return String.format("&#%02x%02x%02x%s", color.getRed(), color.getGreen(), color.getBlue(), "★");
   }

   private static class StarTier {
      private final String name;
      private final int price;
      private final boolean rainbow;

      public StarTier(String name, int price, boolean rainbow) {
         this.name = name;
         this.price = price;
         this.rainbow = rainbow;
      }

      public String getName() {
         return this.name;
      }

      public int getPrice() {
         return this.price;
      }

      public boolean isRainbow() {
         return this.rainbow;
      }
   }
}
