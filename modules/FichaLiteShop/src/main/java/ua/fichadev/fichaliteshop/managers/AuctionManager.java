package ua.fichadev.fichaliteshop.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.fichadev.fichaliteshop.FichaLiteShop;
import ua.fichadev.fichaliteshop.data.AuctionData;
import ua.fichadev.fichaliteshop.data.Bid;
import ua.fichadev.fichaliteshop.data.ShopItem;
import ua.fichadev.fichaliteshop.utils.ColorUtils;
import ua.fichadev.fichaliteshop.utils.MessageUtils;

public class AuctionManager {
   private final FichaLiteShop plugin;
   private AuctionData currentAuction;
   private Integer lastBidSlot;

   public AuctionManager(FichaLiteShop plugin) {
      this.plugin = plugin;
      this.lastBidSlot = null;
      this.generateNewAuction();
   }

   public void startAuctionCheckTask() {
      Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
         if (this.currentAuction != null && this.currentAuction.isExpired()) {
            this.endAuction();
         }
      }, 20L, 20L);
   }

   private void generateNewAuction() {
      List<ShopItem> auctionItems = new ArrayList<>();

      for (ShopItem item : this.plugin.getDataManager().getAvailableItems()) {
         if (item.isAuction()) {
            auctionItems.add(item);
         }
      }

      if (!auctionItems.isEmpty()) {
         ShopItem selected = auctionItems.get(ThreadLocalRandom.current().nextInt(auctionItems.size()));
         int durationHours = this.plugin.getConfig().getInt("auction_duration_hours");
         long durationMillis = durationHours * 60L * 60L * 1000L;
         int stepPrice = selected.getStepPrice() > 0 ? selected.getStepPrice() : Math.max(1, selected.getPrice() / 1000);
         this.currentAuction = new AuctionData(
            selected.getId(), selected.getItemStack(), selected.getPrice(), stepPrice, durationMillis, selected.isSapphireCurrency()
         );
         this.lastBidSlot = null;
         if (this.plugin.getConfig().getBoolean("notify_auction_start", true)) {
            Bukkit.broadcastMessage(MessageUtils.getMessage(this.plugin, "auction_started"));
         }
      }
   }

   public AuctionData getCurrentAuction() {
      return this.currentAuction;
   }

   public List<Bid> getBidsForCurrentAuction() {
      return (List<Bid>)(this.currentAuction == null ? new ArrayList<>() : this.currentAuction.getBids());
   }

   public Integer getLastBidSlot() {
      return this.lastBidSlot;
   }

   public boolean placeBid(Player player, int amount, int clickedSlot) {
      if (this.currentAuction == null) {
         return false;
      } else if (amount <= this.currentAuction.getCurrentPrice()) {
         player.sendMessage(MessageUtils.getMessage(this.plugin, "bid_must_be_higher"));
         return false;
      } else {
         UUID previousBidder = this.currentAuction.getCurrentBidder();
         int previousAmount = this.currentAuction.getCurrentPrice();
         int amountToPay = amount;
         if (previousBidder != null && previousBidder.equals(player.getUniqueId())) {
            amountToPay = amount - previousAmount;
         }

         if (!this.processPayment(player, amountToPay)) {
            return false;
         } else {
            this.refundPreviousBidder(previousBidder, previousAmount, player);
            this.currentAuction.setCurrentBidder(player.getUniqueId());
            this.currentAuction.setCurrentPrice(amount);
            this.currentAuction.addBid(new Bid(player.getUniqueId(), player.getName(), amount));
            this.lastBidSlot = clickedSlot == 26 ? 18 : clickedSlot;
            this.extendAuctionIfNeeded();
            String message = MessageUtils.getMessageWithCurrency(this.plugin, "bid_placed", amount, this.currentAuction.isSapphireCurrency());
            player.sendMessage(ColorUtils.color(message));
            return true;
         }
      }
   }

   private boolean processPayment(Player player, int amount) {
      if (this.currentAuction.isSapphireCurrency()) {
         int points = this.plugin.getPlayerPointsAPI().look(player.getUniqueId());
         if (points < amount) {
            player.sendMessage(MessageUtils.getMessage(this.plugin, "not_enough_sapphires", amount));
            return false;
         }

         this.plugin.getPlayerPointsAPI().take(player.getUniqueId(), amount);
      } else {
         if (!this.plugin.getEconomy().has(player, amount)) {
            player.sendMessage(MessageUtils.getMessage(this.plugin, "not_enough_coins", amount));
            return false;
         }

         this.plugin.getEconomy().withdrawPlayer(player, amount);
      }

      return true;
   }

   private void refundPreviousBidder(UUID previousBidder, int previousAmount, Player currentPlayer) {
      if (previousBidder != null && !previousBidder.equals(currentPlayer.getUniqueId())) {
         Player prevPlayer = Bukkit.getPlayer(previousBidder);
         if (this.currentAuction.isSapphireCurrency()) {
            this.plugin.getPlayerPointsAPI().give(previousBidder, previousAmount);
            if (prevPlayer != null && prevPlayer.isOnline() && this.plugin.getConfig().getBoolean("notify_bid_outbid", true)) {
               String message = MessageUtils.getMessageWithCurrency(this.plugin, "bid_outbid", previousAmount, true);
               prevPlayer.sendMessage(ColorUtils.color(message));
            }
         } else {
            this.plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(previousBidder), previousAmount);
            if (prevPlayer != null && prevPlayer.isOnline() && this.plugin.getConfig().getBoolean("notify_bid_outbid", true)) {
               String message = MessageUtils.getMessageWithCurrency(this.plugin, "bid_outbid", previousAmount, false);
               prevPlayer.sendMessage(ColorUtils.color(message));
            }
         }
      }
   }

   private void extendAuctionIfNeeded() {
      long remaining = this.currentAuction.getRemainingTime();
      int minTime = this.plugin.getConfig().getInt("auction_min_time_for_extension") * 1000;
      int extensionTime = this.plugin.getConfig().getInt("auction_time_extension_seconds") * 1000;
      if (remaining < minTime) {
         this.currentAuction.setEndTime(System.currentTimeMillis() + extensionTime);
      }
   }

   private void endAuction() {
      if (this.currentAuction != null) {
         UUID winner = this.currentAuction.getCurrentBidder();
         if (winner != null) {
            ItemStack item = this.currentAuction.getItemStack();
            this.plugin.getDataManager().addWarehouseItem(winner, item);
            Player winnerPlayer = Bukkit.getPlayer(winner);
            if (winnerPlayer != null && winnerPlayer.isOnline()) {
               winnerPlayer.sendMessage(MessageUtils.getMessage(this.plugin, "auction_won"));
               winnerPlayer.sendMessage(MessageUtils.getMessage(this.plugin, "open_warehouse"));
            }

            if (this.plugin.getConfig().getBoolean("notify_auction_end", true)) {
               String message = MessageUtils.getMessage(this.plugin, "auction_winner");
               Bukkit.broadcastMessage(message + (winnerPlayer != null ? winnerPlayer.getName() : ""));
            }
         } else if (this.plugin.getConfig().getBoolean("notify_auction_end", true)) {
            Bukkit.broadcastMessage(MessageUtils.getMessage(this.plugin, "auction_no_bids"));
         }

         this.generateNewAuction();
      }
   }
}
