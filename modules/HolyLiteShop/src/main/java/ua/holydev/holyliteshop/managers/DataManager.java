package ua.holydev.holyliteshop.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.data.PlayerShopData;
import ua.holydev.holyliteshop.data.ShopItem;
import ua.holydev.holyliteshop.database.DatabaseManager;
import ua.holydev.holyliteshop.database.impl.MySQLDatabase;
import ua.holydev.holyliteshop.database.impl.SQLiteDatabase;
import ua.holydev.holyliteshop.utils.ColorUtils;

public class DataManager {
   private final HolyLiteShop plugin;
   private final Map<UUID, PlayerShopData> playerDataMap;
   private final List<ShopItem> availableItems;
   private final DatabaseManager databaseManager;
   private File dataFile;
   private FileConfiguration dataConfig;

   public DataManager(HolyLiteShop plugin) {
      this.plugin = plugin;
      this.playerDataMap = new HashMap<>();
      this.availableItems = new ArrayList<>();
      String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
      if (type.equals("mysql")) {
         this.databaseManager = new MySQLDatabase(plugin);
      } else {
         this.databaseManager = new SQLiteDatabase(plugin);
      }

      this.loadData();
   }

   private void loadData() {
      this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
      if (!this.dataFile.exists()) {
         try {
            this.dataFile.createNewFile();
         } catch (IOException var2) {
            var2.printStackTrace();
         }
      }

      this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
      this.loadItems();
   }

   private void loadItems() {
      ConfigurationSection itemsSection = this.dataConfig.getConfigurationSection("items");
      if (itemsSection != null) {
         for (String key : itemsSection.getKeys(false)) {
            String path = "items." + key;
            String name = this.dataConfig.getString(path + ".name");
            ItemStack item = this.dataConfig.getItemStack(path + ".item");
            int price = this.dataConfig.getInt(path + ".price");
            boolean isAuction = this.dataConfig.getBoolean(path + ".auction", false);
            int stepPrice = this.dataConfig.getInt(path + ".stepPrice", 0);
            boolean isSapphireCurrency = this.dataConfig.getBoolean(path + ".isSapphireCurrency", true);
            if (item != null) {
               this.availableItems.add(new ShopItem(key, name, item, price, isAuction, stepPrice, isSapphireCurrency));
            }
         }
      }
   }

   public void saveAllData() {
      this.saveItems();

      try {
         this.dataConfig.save(this.dataFile);
      } catch (IOException var4) {
         var4.printStackTrace();
      }

      for (Entry<UUID, PlayerShopData> entry : this.playerDataMap.entrySet()) {
         PlayerShopData data = entry.getValue();
         this.databaseManager.savePlayerShop(entry.getKey(), data.getCurrentItems(), data.getDiscounts(), data.getLastRefresh());
      }
   }

   private void saveItems() {
      this.dataConfig.set("items", null);

      for (ShopItem item : this.availableItems) {
         String path = "items." + item.getId();
         this.dataConfig.set(path + ".name", item.getName());
         this.dataConfig.set(path + ".item", item.getItemStack());
         this.dataConfig.set(path + ".price", item.getPrice());
         this.dataConfig.set(path + ".auction", item.isAuction());
         this.dataConfig.set(path + ".stepPrice", item.getStepPrice());
         this.dataConfig.set(path + ".isSapphireCurrency", item.isSapphireCurrency());
      }
   }

   public PlayerShopData getPlayerData(UUID playerId) {
      if (!this.playerDataMap.containsKey(playerId)) {
         PlayerShopData data = new PlayerShopData(playerId);
         List<String> currentItems = new ArrayList<>();
         Map<String, Double> discounts = new HashMap<>();
         long[] lastRefresh = new long[]{0L};
         this.databaseManager.loadPlayerShop(playerId, currentItems, discounts, lastRefresh);
         if (!currentItems.isEmpty()) {
            data.setCurrentItems(currentItems);
            data.setDiscounts(discounts);
         }

         data.setLastRefresh(lastRefresh[0]);
         this.playerDataMap.put(playerId, data);
      }

      return this.playerDataMap.get(playerId);
   }

   public List<ShopItem> getAvailableItems() {
      return new ArrayList<>(this.availableItems);
   }

   public String addShopItem(ItemStack item, int price, boolean isAuction, int stepPrice, boolean isSapphireCurrency) {
      String id = "item_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
      String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
         ? ChatColor.stripColor(item.getItemMeta().getDisplayName())
         : item.getType().toString();
      this.availableItems.add(new ShopItem(id, name, item, price, isAuction, stepPrice, isSapphireCurrency));
      this.saveAllData();
      return name;
   }

   public ShopItem getShopItemById(String id) {
      for (ShopItem item : this.availableItems) {
         if (item.getId().equals(id)) {
            return item;
         }
      }

      return null;
   }

   public boolean removeShopItem(String name) {
      for (int i = 0; i < this.availableItems.size(); i++) {
         if (this.availableItems.get(i).getName().equalsIgnoreCase(name)) {
            this.availableItems.remove(i);
            this.saveAllData();
            return true;
         }
      }

      return false;
   }

   public void sendItemsList(CommandSender sender) {
      if (this.availableItems.isEmpty()) {
         sender.sendMessage(ColorUtils.color("&#ff2222Список предметов пуст!"));
      } else {
         sender.sendMessage(ColorUtils.color(" Список предметов"));

         for (ShopItem item : this.availableItems) {
            String type = item.isAuction() ? "&#ff5555[Аукцион]" : "&#55ff55[Магазин]";
            String stepInfo = item.isAuction() ? " &#d5dbdc| Шаг: &#ffffff" + item.getStepPrice() : "";
            String currency = item.isSapphireCurrency() ? "сапфиров" : "монет";
            sender.sendMessage(
               ColorUtils.color(type + " &#ffffff" + item.getName() + " &#d5dbdc| Цена: &#ffffff" + item.getPrice() + " " + currency + stepInfo)
            );
         }
      }
   }

   public void addWarehouseItem(UUID playerId, ItemStack item) {
      this.databaseManager.addWarehouseItem(playerId, item);
   }

   public List<ItemStack> getWarehouseItems(UUID playerId) {
      return this.databaseManager.getWarehouseItems(playerId);
   }

   public ItemStack removeWarehouseItem(UUID playerId, int index) {
      return this.databaseManager.removeWarehouseItem(playerId, index);
   }

   public void addPurchaseStat(UUID playerId, String itemId, int amount) {
      this.databaseManager.addPurchaseStat(playerId, itemId, amount);
   }

   public Map<String, Integer> getPlayerPurchaseStats(UUID playerId) {
      return this.databaseManager.getPlayerPurchaseStats(playerId);
   }

   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }
}
