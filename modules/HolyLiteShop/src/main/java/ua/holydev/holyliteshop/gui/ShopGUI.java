package ua.holydev.holyliteshop.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.data.AuctionData;
import ua.holydev.holyliteshop.data.PlayerShopData;
import ua.holydev.holyliteshop.data.ShopItem;
import ua.holydev.holyliteshop.utils.ColorUtils;
import ua.holydev.holyliteshop.utils.TimeUtils;

public class ShopGUI implements InventoryHolder {
   private static final int[] SHOP_SLOTS = new int[]{11, 13, 15, 20, 21, 23, 24};
   private final HolyLiteShop plugin;
   private final Player player;
   private Inventory inventory;
   private BukkitTask updateTask;
   private final Map<String, Integer> purchaseCount;
   private final SimpleDateFormat dateFormat;

   public ShopGUI(HolyLiteShop plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.purchaseCount = new HashMap<>();
      this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      this.dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
   }

   public void open() {
      this.inventory = Bukkit.createInventory(this, 54, ColorUtils.color("Премиум-магазин"));
      this.updateInventory();
      this.startUpdateTask();
      this.player.openInventory(this.inventory);
   }

   private void updateInventory() {
      this.inventory.clear();
      PlayerShopData data = this.plugin.getDataManager().getPlayerData(this.player.getUniqueId());
      List<String> items = data.getCurrentItems();
      this.setupDecorativeItems();
      this.setupShopItems(data, items);
      this.setupAuctionItem();
      this.setupInfoBook();
      this.setupNavigationButtons();
      this.setupStatsMap();
   }

   private void setupDecorativeItems() {
      ItemStack gray = this.createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
      ItemStack orange = this.createGlassPane(Material.ORANGE_STAINED_GLASS_PANE);
      this.inventory.setItem(0, gray);
      this.inventory.setItem(8, gray);
      int[] orangeSlots = new int[]{9, 17, 18, 26, 27, 35, 36, 44};

      for (int slot : orangeSlots) {
         this.inventory.setItem(slot, orange);
      }
   }

   private ItemStack createGlassPane(Material material) {
      ItemStack pane = new ItemStack(material);
      ItemMeta meta = pane.getItemMeta();
      meta.setDisplayName(ColorUtils.color("&r"));
      pane.setItemMeta(meta);
      return pane;
   }

   private void setupShopItems(PlayerShopData data, List<String> items) {
      for (int i = 0; i < Math.min(items.size(), SHOP_SLOTS.length); i++) {
         String itemId = items.get(i);
         ShopItem shopItem = this.plugin.getDataManager().getShopItemById(itemId);
         if (shopItem != null) {
            ItemStack displayItem = this.createShopDisplayItem(shopItem, data, itemId);
            this.inventory.setItem(SHOP_SLOTS[i], displayItem);
         }
      }
   }

   private ItemStack createShopDisplayItem(ShopItem shopItem, PlayerShopData data, String itemId) {
      ItemStack displayItem = shopItem.getItemStack();
      ItemMeta meta = displayItem.getItemMeta();
      int originalPrice = shopItem.getPrice();
      double discount = data.getDiscounts().getOrDefault(itemId, 0.0);
      int finalPrice = this.plugin.getShopManager().getDiscountedPrice(this.player.getUniqueId(), itemId, originalPrice);
      int bought = this.purchaseCount.getOrDefault(itemId, 0);
      String currencyName = this.plugin.getConfig().getString("currencies.sapphires", "сапфиров ☀");
      List<String> lore = new ArrayList<>();
      if (meta != null && meta.hasLore()) {
         lore.addAll(meta.getLore());
      }

      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#d5dbdc До обновления: &r&#ff7000" + TimeUtils.formatTime(this.getTimeUntilRefresh(data))));
      lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#d5dbdc Скидка: &r&#ffc900" + String.format("%.1f", discount) + "%"));
      lore.add(ColorUtils.color("&r &r&l&#ff7000▍&r&#d5dbdc Куплено: &r&#ff7000" + bought + " шт."));
      lore.add(ColorUtils.color("&r"));
      lore.add(
         ColorUtils.color(
            "&r &r&#ff7000▶ &r&#ffffffКупить за &r&#aaaaaa&m"
               + TimeUtils.formatNumber(originalPrice)
               + "&r &#FB0FD4"
               + TimeUtils.formatNumber(finalPrice)
               + " "
               + currencyName
         )
      );
      if (meta != null) {
         meta.setLore(lore);
         displayItem.setItemMeta(meta);
      }

      return displayItem;
   }

   private void setupAuctionItem() {
      AuctionData auction = this.plugin.getAuctionManager().getCurrentAuction();
      if (auction != null) {
         ItemStack auctionItem = auction.getItemStack();
         ItemMeta meta = auctionItem.getItemMeta();
         String currencyName = auction.isSapphireCurrency()
            ? this.plugin.getConfig().getString("currencies.sapphires", "сапфиров ☀")
            : this.plugin.getConfig().getString("currencies.coins", "монет ¤");
         String currencyColor = auction.isSapphireCurrency() ? "&#FB0FD4" : "&#fca000";
         List<String> lore = new ArrayList<>();
         if (meta != null && meta.hasLore()) {
            lore.addAll(meta.getLore());
         }

         lore.add(ColorUtils.color("&r"));
         lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff До конца: &r&#ff7000" + TimeUtils.formatTime(auction.getRemainingTime())));
         lore.add(
            ColorUtils.color(" &r&l&#ff7000▍&r&#ffffff Текущая цена: " + currencyColor + TimeUtils.formatNumber(auction.getCurrentPrice()) + " " + currencyName)
         );
         lore.add(ColorUtils.color("&r"));
         if (auction.getCurrentBidder() != null) {
            String bidderName = Bukkit.getOfflinePlayer(auction.getCurrentBidder()).getName();
            lore.add(ColorUtils.color(" &r&#ff7000● &r&#ffffffЛидер торгов: &r&#00bdff" + bidderName));
            lore.add(ColorUtils.color("&r"));
         }

         lore.add(ColorUtils.color(" &r&#ff7000▶ &r&#ffffffНажмите, чтобы сделать ставку"));
         if (meta != null) {
            meta.setLore(lore);
            auctionItem.setItemMeta(meta);
         }

         this.inventory.setItem(40, auctionItem);
      }
   }

   private void setupInfoBook() {
      ItemStack infoBook = new ItemStack(Material.BOOK);
      ItemMeta meta = infoBook.getItemMeta();
      meta.setDisplayName(ColorUtils.color(" &r&#00c7ffПомощь по премиум-магазину"));
      List<String> lore = new ArrayList<>();
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#00bdff Когда обновится товар?"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 Все товары в премиум-магазине"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 обновляются раз в некоторое время."));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 Узнать, когда обновится товар, вы"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 Можете в описании предмета."));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#00bdff Ограниченное число предметов"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 Некоторые товары можно купить только"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 ограниченное число раз, внимательно"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 читайте описание предмета."));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#00bdff Склад купленных товаров"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 Чтобы открыть склад купленных"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#e7e7e7 предметов, пропишите команду:"));
      lore.add(ColorUtils.color(" &r&#00bdff&n▍&r&#00bdff /shop chest"));
      lore.add(ColorUtils.color(" &r&#00bdff▍"));
      lore.add(ColorUtils.color("&r"));
      meta.setLore(lore);
      infoBook.setItemMeta(meta);
      this.inventory.setItem(45, infoBook);
   }

   private void setupNavigationButtons() {
      this.inventory.setItem(47, this.createNavigationButton(Material.DIAMOND_CHESTPLATE, " &r&#ff7000❏ Наборы ❏", "kits"));
      this.inventory.setItem(49, this.createNavigationButton(Material.CHEST, " &r&#00c7ff❏ Склад купленных товаров ❏", "warehouse"));
      this.inventory.setItem(51, this.createNavigationButton(Material.POTION, " &r&#ff7000❏ Бустеры ❏", "boosters"));
   }

   private ItemStack createNavigationButton(Material material, String title, String type) {
      ItemStack button = new ItemStack(material);
      ItemMeta meta = button.getItemMeta();
      meta.setDisplayName(ColorUtils.color(title));
      List<String> lore = new ArrayList<>();
      lore.add(ColorUtils.color("&r"));
      switch (type) {
         case "kits":
            lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff В этом разделе &r&#ff7000можно купить"));
            lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff &r&#ff7000наборы топовых привилегий&r&#ffffff или"));
            lore.add(ColorUtils.color(" &r&l&#ff7000▍&r&#ffffff универсальные киты для PvP."));
            break;
         case "warehouse":
            lore.add(ColorUtils.color(" &r&l&#00c7ff&n▍&r&#ffffff В этот разделе можно &r&#00c7ffполучить"));
            lore.add(ColorUtils.color(" &r&l&#00c7ff▍&r&#00c7ff предметы &r&#ffffffс премиум-аукциона."));
            break;
         case "boosters":
            lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff В этом разделе &r&#ff7000можно купить"));
            lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff &r&#ff7000постоянные бустеры &r&#ffffffдля улучшения"));
            lore.add(ColorUtils.color(" &r&l&#ff7000▍&r&#ffffff ваших показателей в игре."));
      }

      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#ff7000▶ &r&#ffffffНажмите, чтобы открыть"));
      meta.setLore(lore);
      button.setItemMeta(meta);
      return button;
   }

   private void setupStatsMap() {
      ItemStack statsMap = new ItemStack(Material.FILLED_MAP);
      ItemMeta meta = statsMap.getItemMeta();
      String currentDate = this.dateFormat.format(new Date());
      meta.setDisplayName(ColorUtils.color(" &r&#00c7ffВаши траты &r&#555555| &r&#ffffff" + currentDate));
      List<String> lore = new ArrayList<>();
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color("&r"));
      Map<String, Integer> purchaseStats = this.plugin.getDataManager().getPlayerPurchaseStats(this.player.getUniqueId());
      if (purchaseStats.isEmpty()) {
         lore.add(ColorUtils.color("&r &#ff0e00✘ &r&#ffffffВы ничего &#ff0e00не покупали"));
         lore.add(ColorUtils.color("&r    &r&#ffffffв премиум-магазине."));
      } else {
         int totalSpent = purchaseStats.values().stream().mapToInt(Integer::intValue).sum();
         lore.add(ColorUtils.color(" &r&#00c7ff✓ &r&#ffffffВсего потрачено:"));
         lore.add(ColorUtils.color("    &r&#FB0FD4" + TimeUtils.formatNumber(totalSpent) + " сапфиров ☀"));
      }

      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color("&r"));
      meta.setLore(lore);
      statsMap.setItemMeta(meta);
      this.inventory.setItem(53, statsMap);
   }

   private void startUpdateTask() {
      this.updateTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::updateInventory, 20L, 20L);
   }

   public void stopUpdateTask() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }
   }

   public void incrementPurchaseCount(String itemId) {
      this.purchaseCount.put(itemId, this.purchaseCount.getOrDefault(itemId, 0) + 1);
   }

   private long getTimeUntilRefresh(PlayerShopData data) {
      int interval = this.plugin.getConfig().getBoolean("early_wipe_mode")
         ? this.plugin.getConfig().getInt("early_wipe_interval_minutes")
         : this.plugin.getConfig().getInt("refresh_interval_minutes");
      long nextRefresh = data.getLastRefresh() + interval * 60L * 1000L;
      long timeLeft = nextRefresh - System.currentTimeMillis();
      return Math.max(0L, timeLeft);
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
