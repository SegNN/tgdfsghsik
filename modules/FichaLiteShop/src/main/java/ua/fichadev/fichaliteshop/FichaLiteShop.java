package ua.fichadev.fichaliteshop;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ua.fichadev.fichaliteshop.commands.LiteShopCommand;
import ua.fichadev.fichaliteshop.commands.ShopCommand;
import ua.fichadev.fichaliteshop.listeners.InventoryListener;
import ua.fichadev.fichaliteshop.managers.AuctionManager;
import ua.fichadev.fichaliteshop.managers.DataManager;
import ua.fichadev.fichaliteshop.managers.ShopManager;

public class FichaLiteShop extends JavaPlugin {
   private static FichaLiteShop instance;
   private Economy economy;
   private PlayerPointsAPI playerPointsAPI;
   private DataManager dataManager;
   private ShopManager shopManager;
   private AuctionManager auctionManager;

   public FichaLiteShop() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      if (this.setupEconomy() && this.setupPlayerPoints()) {
         this.dataManager = new DataManager(this);
         this.shopManager = new ShopManager(this);
         this.auctionManager = new AuctionManager(this);
         this.registerCommands();
         this.registerListeners();
         this.shopManager.startRefreshTask();
         this.shopManager.refreshAllPlayers();
         this.auctionManager.startAuctionCheckTask();
      } else {
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void onDisable() {
      if (this.dataManager != null) {
         this.dataManager.saveAllData();
         this.dataManager.getDatabaseManager().close();
      }
   }

   private void registerCommands() {
      LiteShopCommand liteShopCommand = new LiteShopCommand(this);
      ShopCommand shopCommand = new ShopCommand(this);
      this.getCommand("shop").setExecutor(shopCommand);
      this.getCommand("shop").setTabCompleter(shopCommand);
      this.getCommand("liteshop").setExecutor(liteShopCommand);
      this.getCommand("liteshop").setTabCompleter(liteShopCommand);
   }

   private void registerListeners() {
      Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
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

   private boolean setupPlayerPoints() {
      if (this.getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
         return false;
      } else {
         PlayerPoints playerPoints = (PlayerPoints)this.getServer().getPluginManager().getPlugin("PlayerPoints");
         this.playerPointsAPI = playerPoints.getAPI();
         return this.playerPointsAPI != null;
      }
   }

   public static FichaLiteShop getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public PlayerPointsAPI getPlayerPointsAPI() {
      return this.playerPointsAPI;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public ShopManager getShopManager() {
      return this.shopManager;
   }

   public AuctionManager getAuctionManager() {
      return this.auctionManager;
   }

   public String getSapphiresCurrencyName() {
      return this.getConfig().getString("currencies.sapphires", "сапфиров ☀");
   }

   public String getCoinsCurrencyName() {
      return this.getConfig().getString("currencies.coins", "монеток ¤");
   }
}
