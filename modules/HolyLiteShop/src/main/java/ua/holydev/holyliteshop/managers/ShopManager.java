package ua.holydev.holyliteshop.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.data.PlayerShopData;
import ua.holydev.holyliteshop.data.ShopItem;
import ua.holydev.holyliteshop.utils.MessageUtils;

public class ShopManager {
   private final HolyLiteShop plugin;

   public ShopManager(HolyLiteShop plugin) {
      this.plugin = plugin;
   }

   public void startRefreshTask() {
      int interval = this.getRefreshIntervalMinutes();
      long ticks = interval * 60L * 20L;
      Bukkit.getScheduler().runTaskTimer(this.plugin, this::refreshAllPlayers, ticks, ticks);
   }

   public void refreshAllPlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.refreshPlayerShop(player.getUniqueId(), true);
      }
   }

   public void refreshPlayerShop(UUID playerId, boolean force) {
      PlayerShopData data = this.plugin.getDataManager().getPlayerData(playerId);
      if (!force) {
         int interval = this.getRefreshIntervalMinutes();
         long timeSinceRefresh = System.currentTimeMillis() - data.getLastRefresh();
         if (timeSinceRefresh < interval * 60L * 1000L) {
            return;
         }
      }

      List<ShopItem> nonAuctionItems = this.getNonAuctionItems();
      int itemsPerPlayer = this.getItemsPerPlayer();
      List<String> selectedItems = new ArrayList<>();
      Map<String, Double> discounts = new HashMap<>();
      Random random = ThreadLocalRandom.current();
      Collections.shuffle(nonAuctionItems, random);
      double minDiscount = this.getMinDiscountPercent();
      double maxDiscount = this.getMaxDiscountPercent();

      for (int i = 0; i < Math.min(itemsPerPlayer, nonAuctionItems.size()); i++) {
         ShopItem item = nonAuctionItems.get(i);
         selectedItems.add(item.getId());
         double discount = minDiscount + random.nextDouble() * (maxDiscount - minDiscount);
         discount = Math.round(discount * 100.0) / 100.0;
         discounts.put(item.getId(), discount);
      }

      data.setCurrentItems(selectedItems);
      data.setDiscounts(discounts);
      data.setLastRefresh(System.currentTimeMillis());
      this.plugin.getDataManager().getDatabaseManager().savePlayerShop(playerId, selectedItems, discounts, data.getLastRefresh());
      this.notifyPlayerIfOnline(playerId);
   }

   private List<ShopItem> getNonAuctionItems() {
      List<ShopItem> nonAuctionItems = new ArrayList<>();

      for (ShopItem item : this.plugin.getDataManager().getAvailableItems()) {
         if (!item.isAuction()) {
            nonAuctionItems.add(item);
         }
      }

      return nonAuctionItems;
   }

   private void notifyPlayerIfOnline(UUID playerId) {
      if (this.plugin.getConfig().getBoolean("notify_new_items", true)) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline()) {
            player.sendMessage(MessageUtils.getMessage(this.plugin, "shop_items_refreshed"));
         }
      }
   }

   public int getDiscountedPrice(UUID playerId, String itemId, int originalPrice) {
      PlayerShopData data = this.plugin.getDataManager().getPlayerData(playerId);
      double discount = data.getDiscount(itemId);
      int discountedPrice = originalPrice - (int)(originalPrice * discount / 100.0);
      return Math.max(1, discountedPrice);
   }

   public int getRefreshIntervalMinutes() {
      boolean earlyWipeMode = this.plugin.getConfig().getBoolean("early_wipe_mode");
      return earlyWipeMode ? this.plugin.getConfig().getInt("early_wipe_interval_minutes") : this.plugin.getConfig().getInt("refresh_interval_minutes");
   }

   public int getItemsPerPlayer() {
      return this.plugin.getConfig().getInt("items_per_player");
   }

   public double getMinDiscountPercent() {
      return this.plugin.getConfig().getDouble("discount_min_percent");
   }

   public double getMaxDiscountPercent() {
      return this.plugin.getConfig().getDouble("discount_max_percent");
   }
}
