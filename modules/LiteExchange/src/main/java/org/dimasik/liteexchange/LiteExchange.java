package org.dimasik.liteexchange;

import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteexchange.commands.CoinsCommand;
import org.dimasik.liteexchange.commands.ExchangeCommand;
import org.dimasik.liteexchange.database.DBCore;
import org.dimasik.liteexchange.listeners.CountBuyListener;
import org.dimasik.liteexchange.listeners.CreateListener;
import org.dimasik.liteexchange.listeners.MainListener;
import org.dimasik.liteexchange.listeners.RateSelectListener;
import org.dimasik.liteexchange.lot.RateData;
import org.dimasik.liteexchange.util.CoinsPlaceholder;

public final class LiteExchange extends JavaPlugin {
   public DBCore dbCore;
   public ExchangeCommand exchangeCommand;
   public CoinsCommand coinsCommand;
   private static LiteExchange instance;
   private CoinsPlaceholder coinsPlaceholder;
   private Economy economy = null;
   public static HashMap<Player, Integer> selectedIds = new HashMap<>();
   public static HashMap<Player, RateData> rateDatas = new HashMap<>();
   public static HashMap<String, Integer> cooldowns = new HashMap<>();

   public LiteExchange() {
   }

   public void onEnable() {
      this.saveDefaultConfig();
      FileConfiguration config = this.getConfig();
      instance = this;
      if (!this.setupEconomy()) {
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.getLogger().info("§dhttps://t.me/FichaHentai");
         this.getLogger().info("§dhttps://t.me/FichaDev");
         this.dbCore = new DBCore(
            this,
            config.getString("database.host", "host"),
            config.getInt("database.port", 3306),
            config.getString("database.name", "exchange"),
            config.getString("database.user", "root"),
            config.getString("database.password", "password")
         );
         this.exchangeCommand = new ExchangeCommand();
         this.coinsCommand = new CoinsCommand();
         this.getCommand("exchange").setExecutor(this.exchangeCommand);
         this.getCommand("exchange").setTabCompleter(this.exchangeCommand);
         this.getCommand("coins").setExecutor(this.coinsCommand);
         this.getCommand("coins").setTabCompleter(this.coinsCommand);
         this.getServer().getPluginManager().registerEvents(new MainListener(), this);
         this.getServer().getPluginManager().registerEvents(new CountBuyListener(), this);
         this.getServer().getPluginManager().registerEvents(new CreateListener(), this);
         this.getServer().getPluginManager().registerEvents(new RateSelectListener(), this);
         if (config.getBoolean("isHead", true)) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> this.dbCore.getLots().revokeExpiredLots(), 60L, 60L);
         }

         if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.coinsPlaceholder = new CoinsPlaceholder();
            this.coinsPlaceholder.register();
         }
      }
   }

   public static LiteExchange getInstance() {
      return instance;
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

   public Economy getEconomy() {
      return this.economy;
   }

   public void onDisable() {
      this.getLogger().info("§dhttps://t.me/FichaHentai");
      this.getLogger().info("§dhttps://t.me/FichaDev");
      if (this.coinsPlaceholder != null) {
         try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
               && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()
               && this.coinsPlaceholder.isRegistered()) {
               this.coinsPlaceholder.unregister();
            }
         } catch (Exception var2) {
         }
      }
   }
}
