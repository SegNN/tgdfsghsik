package ru.lolodimo4ka.holygoldspawner.events;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
   private final JavaPlugin plugin;
   private File configFile;
   private FileConfiguration config;
   private DecimalFormat decimalFormat;
   private DecimalFormat percentFormat;
   public int checkInterval;
   public int spawnerRadius;
   public double minCoins;
   public double maxCoins;
   public boolean showMessages;
   public boolean preventMobSpawning;
   public int maxDurability;
   public int durabilityCheckInterval;
   public int durabilityLossPerCheck;
   public double breakChanceMultiplier;
   public boolean coordsEncryptionEnabled;
   public boolean showZCoord;
   public int customDigits;
   public boolean neighborPenaltyEnabled;
   public int neighborCheckRadius;
   public double penaltyPerSpawner;
   public double maxPenalty;
   public int neighborCheckInterval;
   public boolean spawnerPlacementEnabled;
   public boolean durabilityOnlyWhenActive;
   public boolean dailyIncreaseEnabled;
   public double dailyMinIncrease;
   public double dailyMaxIncrease;
   public int dailyIncreaseInterval;
   public String pattern1digit;
   public String pattern2digits;
   public String pattern3digits;
   public String pattern4digits;

   public Config(JavaPlugin plugin) {
      this.plugin = plugin;
      this.setupFormats();
      this.setupConfig();
   }

   private void setupFormats() {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      this.decimalFormat = new DecimalFormat("#0.00", symbols);
      this.percentFormat = new DecimalFormat("#0.0000");
   }

   public void setupConfig() {
      this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
      if (!this.configFile.exists()) {
         this.plugin.saveDefaultConfig();
      }

      this.config = YamlConfiguration.loadConfiguration(this.configFile);
      this.loadConfigValues();
   }

   private void loadConfigValues() {
      this.checkInterval = this.config.getInt("settings.check-interval", 5);
      this.spawnerRadius = this.config.getInt("settings.spawner-radius", 6);
      this.minCoins = this.config.getDouble("settings.min-coins", 5900.0);
      this.maxCoins = this.config.getDouble("settings.max-coins", 6300.99);
      this.showMessages = true;
      this.preventMobSpawning = false;
      this.spawnerPlacementEnabled = true;
      this.coordsEncryptionEnabled = true;
      this.customDigits = 3;
      this.pattern1digit = "*";
      this.pattern2digits = "•*";
      this.pattern3digits = "*•*";
      this.pattern4digits = "•*•*";
      this.maxDurability = this.config.getInt("settings.durability.max-durability", 5000);
      this.durabilityCheckInterval = this.config.getInt("settings.durability.durability-check-interval", 600);
      this.durabilityLossPerCheck = this.config.getInt("settings.durability.durability-loss-per-check", 1);
      this.breakChanceMultiplier = this.config.getDouble("settings.durability.break-chance-multiplier", 1.0E-4);
      this.neighborPenaltyEnabled = this.config.getBoolean("settings.neighbor-penalty.enabled", true);
      this.neighborCheckRadius = this.config.getInt("settings.neighbor-penalty.radius", 10);
      this.penaltyPerSpawner = this.config.getDouble("settings.neighbor-penalty.penalty-per-spawner", 3.0);
      this.maxPenalty = this.config.getDouble("settings.neighbor-penalty.max-penalty", 60.0);
      this.neighborCheckInterval = this.config.getInt("settings.neighbor-penalty.check-interval", 1);
      this.durabilityOnlyWhenActive = this.config.getBoolean("settings.durability.only-when-active", true);
      this.dailyIncreaseEnabled = this.config.getBoolean("settings.daily-increase.enabled", true);
      this.dailyMinIncrease = this.config.getDouble("settings.daily-increase.min-increase", 100.0);
      this.dailyMaxIncrease = this.config.getDouble("settings.daily-increase.max-increase", 200.0);
      this.dailyIncreaseInterval = this.config.getInt("settings.daily-increase.interval", 86400);
   }

   public String getSpawnerName() {
      return this.translateHexColors(this.config.getString("settings.spawner.name", "&6Золотой Спавнер"));
   }

   public boolean isShowMessages() {
      return true;
   }

   public boolean isPreventMobSpawning() {
      return false;
   }

   public String getDisplayEntity() {
      return "PIG";
   }

   public boolean isSpawnerPlacementEnabled() {
      return true;
   }

   public String getPlacementSound() {
      return "ENTITY_WITHER_SPAWN";
   }

   public boolean isCoordsEncryptionEnabled() {
      return true;
   }

   public int getCustomDigits() {
      return 3;
   }

   public String getPattern1digit() {
      return "*";
   }

   public String getPattern2digits() {
      return "•*";
   }

   public String getPattern3digits() {
      return "*•*";
   }

   public String getPattern4digits() {
      return "•*•*";
   }

   public String translateHexColors(String text) {
      return text == null
         ? ""
         : text.replace("&x", "§x")
            .replace("&", "§")
            .replaceAll("§x([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])([§0-9a-fA-F])", "§x$1$2$3$4$5$6");
   }

   public List<String> translateHexColors(List<String> list) {
      if (list == null) {
         return new ArrayList<>();
      } else {
         List<String> translated = new ArrayList<>();

         for (String line : list) {
            translated.add(this.translateHexColors(line));
         }

         return translated;
      }
   }

   public DecimalFormat getDecimalFormat() {
      return this.decimalFormat;
   }

   public DecimalFormat getPercentFormat() {
      return this.percentFormat;
   }

   public void reloadConfig() {
      this.config = YamlConfiguration.loadConfiguration(this.configFile);
      this.loadConfigValues();
   }

   public void saveData(String path, Object value) {
      this.config.set(path, value);

      try {
         this.config.save(this.configFile);
      } catch (IOException var4) {
      }
   }

   public FileConfiguration getConfig() {
      return this.config;
   }
}
