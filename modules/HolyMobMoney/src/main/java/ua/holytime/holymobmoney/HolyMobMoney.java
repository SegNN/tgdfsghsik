package ua.holytime.holymobmoney;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class HolyMobMoney extends JavaPlugin implements Listener {
   private Economy economy;
   private Map<EntityType, HolyMobMoney.MobReward> mobRewards;
   private String actionbarMessage;
   private DecimalFormat decimalFormat;
   private Map<String, Double> permissionMultipliers;

   public HolyMobMoney() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      this.loadConfig();
      if (!this.setupEconomy()) {
         this.getLogger().severe("Vault не найден");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.getServer().getPluginManager().registerEvents(this, this);
      }
   }

   public void onDisable() {
   }

   private void loadConfig() {
      this.mobRewards = new HashMap<>();
      this.decimalFormat = new DecimalFormat("0.00");
      this.permissionMultipliers = new HashMap<>();
      if (this.getConfig().contains("mobs") && this.getConfig().isConfigurationSection("mobs")) {
         for (String mobKey : this.getConfig().getConfigurationSection("mobs").getKeys(false)) {
            try {
               EntityType entityType = EntityType.valueOf(mobKey.toUpperCase());
               double min = this.getConfig().getDouble("mobs." + mobKey + ".min");
               double max = this.getConfig().getDouble("mobs." + mobKey + ".max");
               String displayName = this.getConfig().getString("mobs." + mobKey + ".display-name");
               this.mobRewards.put(entityType, new HolyMobMoney.MobReward(min, max, displayName));
            } catch (IllegalArgumentException var9) {
               this.getLogger().warning("Неверный тип моба: " + mobKey);
            }
         }
      }

      if (this.getConfig().contains("permission-multipliers") && this.getConfig().isConfigurationSection("permission-multipliers")) {
         for (String permission : this.getConfig().getConfigurationSection("permission-multipliers").getKeys(false)) {
            double multiplier = this.getConfig().getDouble("permission-multipliers." + permission);
            this.permissionMultipliers.put(permission, multiplier);
         }
      }

      this.actionbarMessage = ChatColor.translateAlternateColorCodes(
         '&', this.getConfig().getString("actionbar-message", "&6▶ &fВы убили %mob% &fи получили ¤&%money%")
      );
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy)rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (event.getEntity().getKiller() != null) {
         Player player = event.getEntity().getKiller();
         EntityType entityType = event.getEntityType();
         if (this.mobRewards.containsKey(entityType)) {
            HolyMobMoney.MobReward reward = this.mobRewards.get(entityType);
            double amount = this.getRandomAmount(reward.min, reward.max);
            double multiplier = this.getPermissionMultiplier(player);
            double finalAmount = amount * multiplier;
            this.economy.depositPlayer(player, finalAmount);
            this.sendActionBar(player, reward.displayName, finalAmount);
         }
      }
   }

   private double getPermissionMultiplier(Player player) {
      double multiplier = 1.0;

      for (Entry<String, Double> entry : this.permissionMultipliers.entrySet()) {
         if (player.hasPermission(entry.getKey())) {
            multiplier = Math.max(multiplier, entry.getValue());
         }
      }

      return multiplier;
   }

   private double getRandomAmount(double min, double max) {
      Random random = new Random();
      return min + (max - min) * random.nextDouble();
   }

   private void sendActionBar(Player player, String mobName, double amount) {
      String message = this.actionbarMessage.replace("%mob%", mobName).replace("%money%", this.decimalFormat.format(amount));

      try {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
      } catch (Exception var7) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6▶ &fВы убили " + mobName + " и получили ¤" + this.decimalFormat.format(amount)));
      }
   }

   private static class MobReward {
      public final double min;
      public final double max;
      public final String displayName;

      public MobReward(double min, double max, String displayName) {
         this.min = min;
         this.max = max;
         this.displayName = displayName;
      }
   }
}
