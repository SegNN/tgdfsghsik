package ua.holydev.holysalary.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
   private final JavaPlugin plugin;
   private FileConfiguration config;
   private int initialCooldown;
   private int cooldownSeconds;
   private int baseReward;
   private int rewardGrowth;
   private double sapphireChance;
   private int sapphireBaseAmount;
   private int sapphireGrowth;
   private String moneyCommand;
   private String sapphireCommand;
   private final Map<String, Integer> rankRewards = new HashMap<>();
   private final List<String> rankOrder;

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.rankOrder = new ArrayList<>();
      this.loadConfig();
   }

   public void loadConfig() {
      this.plugin.saveDefaultConfig();
      this.plugin.reloadConfig();
      this.config = this.plugin.getConfig();
      this.initialCooldown = this.config.getInt("cooldown.initial-seconds", 3600);
      this.cooldownSeconds = this.config.getInt("cooldown.seconds", 3600);
      this.baseReward = this.config.getInt("rewards.base", 67000);
      this.rewardGrowth = this.config.getInt("rewards.growth-per-claim", 15000);
      this.sapphireChance = this.config.getDouble("sapphire.chance", 10.0);
      this.sapphireBaseAmount = this.config.getInt("sapphire.base-amount", 5);
      this.sapphireGrowth = this.config.getInt("sapphire.growth-per-claim", 2);
      this.moneyCommand = this.config.getString("commands.money", "eco give %player% %amount%");
      this.sapphireCommand = this.config.getString("commands.sapphire", "p give %player% %amount%");
      this.rankRewards.clear();
      this.rankOrder.clear();
      if (this.config.contains("ranks")) {
         for (String rank : this.config.getConfigurationSection("ranks").getKeys(false)) {
            int reward = this.config.getInt("ranks." + rank);
            String lowerRank = rank.toLowerCase();
            this.rankRewards.put(lowerRank, reward);
            this.rankOrder.add(lowerRank);
         }
      }

      if (!this.rankRewards.containsKey("default")) {
         this.rankRewards.put("default", this.baseReward);
         this.rankOrder.add("default");
      }
   }

   public int getInitialCooldown() {
      return this.initialCooldown;
   }

   public int getCooldownSeconds() {
      return this.cooldownSeconds;
   }

   public int getBaseReward() {
      return this.baseReward;
   }

   public int getRewardGrowth() {
      return this.rewardGrowth;
   }

   public double getSapphireChance() {
      return this.sapphireChance;
   }

   public int getSapphireBaseAmount() {
      return this.sapphireBaseAmount;
   }

   public int getSapphireGrowth() {
      return this.sapphireGrowth;
   }

   public String getMoneyCommand() {
      return this.moneyCommand;
   }

   public String getSapphireCommand() {
      return this.sapphireCommand;
   }

   public Map<String, Integer> getRankRewards() {
      return this.rankRewards;
   }

   public List<String> getRankOrder() {
      return this.rankOrder;
   }

   public FileConfiguration getConfig() {
      return this.config;
   }
}
