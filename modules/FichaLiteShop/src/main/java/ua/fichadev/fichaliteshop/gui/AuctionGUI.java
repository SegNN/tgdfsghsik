package ua.fichadev.fichaliteshop.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import ua.fichadev.fichaliteshop.FichaLiteShop;
import ua.fichadev.fichaliteshop.data.AuctionData;
import ua.fichadev.fichaliteshop.data.Bid;
import ua.fichadev.fichaliteshop.utils.ColorUtils;
import ua.fichadev.fichaliteshop.utils.TimeUtils;

public class AuctionGUI implements InventoryHolder {
   private static final int[] BID_SLOTS = new int[]{18, 19, 28, 37, 46, 47, 48, 39, 30, 21, 22, 23, 32, 41, 50, 51, 52, 43, 34, 25, 26};
   private static final int[] GLASS_SLOTS = new int[]{
      0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 24, 27, 29, 31, 33, 35, 36, 38, 40, 42, 44, 49
   };
   private final FichaLiteShop plugin;
   private final Player player;
   private AuctionData auction;
   private final Map<Integer, Integer> availableBids;
   private Inventory inventory;
   private BukkitTask updateTask;

   public AuctionGUI(FichaLiteShop plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
      this.auction = plugin.getAuctionManager().getCurrentAuction();
      this.availableBids = new HashMap<>();
   }

   public void open() {
      if (this.auction == null) {
         this.player.sendMessage(ColorUtils.color("&#ff2222Аукцион недоступен!"));
      } else {
         ItemStack auctionItemStack = this.auction.getItemStack();
         ItemMeta itemMeta = auctionItemStack.getItemMeta();
         String itemName = itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : auctionItemStack.getType().toString();
         this.inventory = Bukkit.createInventory(this, 54, ColorUtils.color("Ставки за " + itemName));
         this.updateInventory();
         this.startUpdateTask();
         this.player.openInventory(this.inventory);
      }
   }

   private void updateInventory() {
      this.auction = this.plugin.getAuctionManager().getCurrentAuction();
      if (this.auction == null) {
         this.stopUpdateTask();
         this.player.closeInventory();
      } else {
         this.inventory.clear();
         this.availableBids.clear();
         this.setupGlassPane();
         this.setupAuctionItem();
         this.setupBidSlots();
         this.setupNavigationButtons();
      }
   }

   private void setupGlassPane() {
      ItemStack glass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta meta = glass.getItemMeta();
      meta.setDisplayName(ColorUtils.color("&r"));
      glass.setItemMeta(meta);

      for (int slot : GLASS_SLOTS) {
         this.inventory.setItem(slot, glass);
      }
   }

   private void setupAuctionItem() {
      ItemStack auctionItem = this.auction.getItemStack();
      ItemMeta meta = auctionItem.getItemMeta();
      String currencyName = this.getCurrencyName();
      String currencyColor = this.getCurrencyColor();
      List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&l&#ff7000&n▍&r&#ffffff До конца: &r&#ff7000" + TimeUtils.formatTime(this.auction.getRemainingTime())));
      lore.add(
         ColorUtils.color(
            " &r&l&#ff7000&n▍&r&#ffffff Текущая цена: " + currencyColor + TimeUtils.formatNumber(this.auction.getCurrentPrice()) + " " + currencyName
         )
      );
      lore.add(
         ColorUtils.color(" &r&l&#ff7000▍&r&#ffffff Цена шага: " + currencyColor + TimeUtils.formatNumber(this.auction.getStepPrice()) + " " + currencyName)
      );
      lore.add(ColorUtils.color("&r"));
      if (this.auction.getCurrentBidder() != null) {
         String bidderName = Bukkit.getOfflinePlayer(this.auction.getCurrentBidder()).getName();
         lore.add(ColorUtils.color(" &r&#ff7000● &r&#ffffffЛидер торгов: &r&#ff7000" + bidderName));
      }

      if (meta != null) {
         meta.setLore(lore);
         auctionItem.setItemMeta(meta);
      }

      this.inventory.setItem(4, auctionItem);
   }

   private void setupBidSlots() {
      List<Bid> bids = this.plugin.getAuctionManager().getBidsForCurrentAuction();
      Integer lastBidSlot = this.plugin.getAuctionManager().getLastBidSlot();
      int lastSlotIndex = this.getLastSlotIndex(lastBidSlot);
      if (lastSlotIndex != -1) {
         this.setupCompletedBids(bids, lastSlotIndex);
         this.setupAvailableBids(lastSlotIndex + 1);
      } else {
         this.setupAvailableBids(0);
      }
   }

   private int getLastSlotIndex(Integer lastBidSlot) {
      if (lastBidSlot == null) {
         return -1;
      } else {
         for (int i = 0; i < BID_SLOTS.length; i++) {
            if (BID_SLOTS[i] == lastBidSlot) {
               return i;
            }
         }

         return -1;
      }
   }

   private void setupCompletedBids(List<Bid> bids, int lastSlotIndex) {
      String currencyName = this.getCurrencyName();
      String currencyColor = this.getCurrencyColor();

      for (int i = 0; i <= lastSlotIndex; i++) {
         Bid bid = i < bids.size() ? bids.get(i) : bids.get(bids.size() - 1);
         ItemStack completedBid = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
         ItemMeta meta = completedBid.getItemMeta();
         meta.setDisplayName(ColorUtils.color(" &r&#14ff00Установлена ставка"));
         List<String> lore = new ArrayList<>();
         lore.add(ColorUtils.color("&r"));
         lore.add(ColorUtils.color(" &r&#14ff00&n▍"));
         lore.add(ColorUtils.color(" &r&#14ff00&n▍&r&#ffffff Владелец ставки: &r&#14ff00" + bid.getPlayerName()));
         lore.add(ColorUtils.color(" &r&#14ff00&n▍&r&#ffffff Сумма ставки: " + currencyColor + TimeUtils.formatNumber(bid.getAmount()) + " " + currencyName));
         lore.add(ColorUtils.color(" &r&#14ff00&n▍"));
         lore.add(
            ColorUtils.color(
               " &r&#14ff00&n▍&r&#ffffff Текущая цена лота: " + currencyColor + TimeUtils.formatNumber(this.auction.getCurrentPrice()) + " " + currencyName
            )
         );
         lore.add(ColorUtils.color(" &r&#14ff00▍"));
         lore.add(ColorUtils.color("&r"));
         meta.setLore(lore);
         completedBid.setItemMeta(meta);
         this.inventory.setItem(BID_SLOTS[i], completedBid);
      }
   }

   private void setupAvailableBids(int startIndex) {
      String currencyName = this.getCurrencyName();
      String currencyColor = this.getCurrencyColor();

      for (int i = startIndex; i < BID_SLOTS.length; i++) {
         int bidAmount = this.auction.getCurrentPrice() + this.auction.getStepPrice() * (i - startIndex + 1);
         this.availableBids.put(BID_SLOTS[i], bidAmount);
         ItemStack availableBid = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
         ItemMeta meta = availableBid.getItemMeta();
         meta.setDisplayName(ColorUtils.color(" &r&#ff7000Доступна ставка"));
         List<String> lore = new ArrayList<>();
         lore.add(ColorUtils.color("&r"));
         lore.add(ColorUtils.color(" &r&#ff7000▶ &r&#ffffffCделать ставку в " + currencyColor + TimeUtils.formatNumber(bidAmount) + " " + currencyName));
         lore.add(ColorUtils.color("&r"));
         meta.setLore(lore);
         availableBid.setItemMeta(meta);
         this.inventory.setItem(BID_SLOTS[i], availableBid);
      }
   }

   private void setupNavigationButtons() {
      ItemStack backButton = new ItemStack(Material.ARROW);
      ItemMeta backMeta = backButton.getItemMeta();
      backMeta.setDisplayName(ColorUtils.color(" &r&#ff2222◀ &r&#ffffffНазад"));
      backButton.setItemMeta(backMeta);
      this.inventory.setItem(45, backButton);
      ItemStack infoBook = this.createInfoBook();
      this.inventory.setItem(53, infoBook);
   }

   private ItemStack createInfoBook() {
      ItemStack book = new ItemStack(Material.BOOK);
      ItemMeta meta = book.getItemMeta();
      meta.setDisplayName(ColorUtils.color(" &r&#00c7ffИнформация про систему ставок"));
      List<String> lore = new ArrayList<>();
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#ffffff&n                                                     "));
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color("     &r&#9cf9ffКак купить данный предмет?"));
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#e7e7e7Чтобы купить предмет, Вы должны"));
      lore.add(ColorUtils.color(" &r&#e7e7e7перебить самую большую ставку,"));
      lore.add(ColorUtils.color(" &r&#e7e7e7установленную за данный предмет."));
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color("     &r&#9cf9ffКогда я получу свой предмет?"));
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#e7e7e7Если Вы перебили последнюю ставку,"));
      lore.add(ColorUtils.color(" &r&#e7e7e7то по окончанию проведения аукциона,"));
      lore.add(ColorUtils.color(" &r&#e7e7e7вы получите предмет на склад"));
      lore.add(ColorUtils.color(" &r&#e7e7e7купленных предметов: &r&#9cf9ff/shop chest"));
      lore.add(ColorUtils.color("&r"));
      lore.add(ColorUtils.color(" &r&#ffffff&n                                                     "));
      lore.add(ColorUtils.color("&r"));
      meta.setLore(lore);
      book.setItemMeta(meta);
      return book;
   }

   private String getCurrencyName() {
      return this.auction.isSapphireCurrency()
         ? this.plugin.getConfig().getString("currencies.sapphires", "сапфиров ☀")
         : this.plugin.getConfig().getString("currencies.coins", "монет ¤");
   }

   private String getCurrencyColor() {
      return this.auction.isSapphireCurrency() ? "&#fb0fd4" : "&#fca000";
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

   public Inventory getInventory() {
      return this.inventory;
   }

   public Map<Integer, Integer> getAvailableBids() {
      return this.availableBids;
   }

   public AuctionData getAuction() {
      return this.auction;
   }
}
