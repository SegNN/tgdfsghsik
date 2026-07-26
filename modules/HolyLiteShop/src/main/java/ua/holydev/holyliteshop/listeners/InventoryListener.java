package ua.holydev.holyliteshop.listeners;

import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.data.AuctionData;
import ua.holydev.holyliteshop.data.PlayerShopData;
import ua.holydev.holyliteshop.data.ShopItem;
import ua.holydev.holyliteshop.gui.AuctionGUI;
import ua.holydev.holyliteshop.gui.ShopGUI;
import ua.holydev.holyliteshop.gui.WarehouseGUI;
import ua.holydev.holyliteshop.utils.ColorUtils;
import ua.holydev.holyliteshop.utils.MessageUtils;
import ua.holydev.holyliteshop.utils.TimeUtils;

public class InventoryListener implements Listener {
   private final HolyLiteShop plugin;
   private static final int[] SHOP_ITEM_SLOTS = new int[]{11, 13, 15, 20, 21, 23, 24};

   public InventoryListener(HolyLiteShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player player = (Player)e.getWhoClicked();
         Inventory inv = e.getView().getTopInventory();
         if (inv.getHolder() instanceof ShopGUI || inv.getHolder() instanceof AuctionGUI || inv.getHolder() instanceof WarehouseGUI) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null) {
               if (inv.getHolder() instanceof ShopGUI) {
                  this.handleShopClick(player, e, (ShopGUI)inv.getHolder());
               } else if (inv.getHolder() instanceof AuctionGUI) {
                  this.handleAuctionClick(player, e, (AuctionGUI)inv.getHolder());
               } else if (inv.getHolder() instanceof WarehouseGUI) {
                  this.handleWarehouseClick(player, e, (WarehouseGUI)inv.getHolder());
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Inventory inv = e.getView().getTopInventory();
         if (inv.getHolder() instanceof ShopGUI || inv.getHolder() instanceof AuctionGUI || inv.getHolder() instanceof WarehouseGUI) {
            e.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player) {
         Inventory inv = e.getView().getTopInventory();
         if (inv.getHolder() instanceof ShopGUI) {
            ((ShopGUI)inv.getHolder()).stopUpdateTask();
         } else if (inv.getHolder() instanceof AuctionGUI) {
            ((AuctionGUI)inv.getHolder()).stopUpdateTask();
         }
      }
   }

   private void handleShopClick(Player player, InventoryClickEvent e, ShopGUI shopGUI) {
      if (e.getClickedInventory() == e.getView().getTopInventory()) {
         ItemStack clicked = e.getCurrentItem();
         if (clicked != null && clicked.getType() != Material.AIR) {
            int slot = e.getSlot();
            int itemIndex = this.getItemSlotIndex(slot);
            if (itemIndex != -1) {
               this.processPurchase(player, shopGUI, itemIndex);
            } else {
               this.handleSpecialSlots(player, shopGUI, slot);
            }
         }
      }
   }

   private int getItemSlotIndex(int slot) {
      for (int i = 0; i < SHOP_ITEM_SLOTS.length; i++) {
         if (SHOP_ITEM_SLOTS[i] == slot) {
            return i;
         }
      }

      return -1;
   }

   private void processPurchase(Player player, ShopGUI shopGUI, int itemIndex) {
      PlayerShopData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
      List<String> items = data.getCurrentItems();
      if (itemIndex < items.size()) {
         String itemId = items.get(itemIndex);
         ShopItem shopItem = this.plugin.getDataManager().getShopItemById(itemId);
         if (shopItem != null) {
            int finalPrice = this.plugin.getShopManager().getDiscountedPrice(player.getUniqueId(), itemId, shopItem.getPrice());
            int points = this.plugin.getPlayerPointsAPI().look(player.getUniqueId());
            if (points < finalPrice) {
               player.sendMessage(MessageUtils.getMessage(this.plugin, "not_enough_sapphires", finalPrice));
            } else {
               this.plugin.getPlayerPointsAPI().take(player.getUniqueId(), finalPrice);
               ItemStack item = shopItem.getItemStack();
               this.plugin.getDataManager().addWarehouseItem(player.getUniqueId(), item);
               this.plugin.getDataManager().addPurchaseStat(player.getUniqueId(), itemId, finalPrice);
               String message = MessageUtils.replacePlaceholders(this.plugin, this.plugin.getConfig().getString("messages.purchase_success", ""))
                  .replace("%amount%", TimeUtils.formatNumber(finalPrice));
               player.sendMessage(ColorUtils.color(message));
               player.sendMessage(MessageUtils.getMessage(this.plugin, "purchase_sent_warehouse"));
               shopGUI.incrementPurchaseCount(itemId);
            }
         }
      }
   }

   private void handleSpecialSlots(Player player, ShopGUI shopGUI, int slot) {
      switch (slot) {
         case 40:
            shopGUI.stopUpdateTask();
            player.closeInventory();
            new AuctionGUI(this.plugin, player).open();
            break;
         case 47:
            player.closeInventory();
            player.performCommand("dm open kitshop");
            break;
         case 49:
            shopGUI.stopUpdateTask();
            player.closeInventory();
            new WarehouseGUI(this.plugin, player, 0).open();
            break;
         case 51:
            player.closeInventory();
            player.performCommand("booster");
      }
   }

   private void handleAuctionClick(Player player, InventoryClickEvent e, AuctionGUI auctionGUI) {
      if (e.getClickedInventory() == e.getView().getTopInventory()) {
         ItemStack clicked = e.getCurrentItem();
         if (clicked != null && clicked.getType() != Material.AIR) {
            int slot = e.getSlot();
            if (slot == 45) {
               auctionGUI.stopUpdateTask();
               player.closeInventory();
               new ShopGUI(this.plugin, player).open();
            } else if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
               Map<Integer, Integer> availableBids = auctionGUI.getAvailableBids();
               if (availableBids.containsKey(slot)) {
                  int bidAmount = availableBids.get(slot);
                  AuctionData auction = auctionGUI.getAuction();
                  if (auction == null) {
                     player.sendMessage(MessageUtils.getMessage(this.plugin, "auction_not_available"));
                     player.closeInventory();
                  } else {
                     int playerCurrentBid = 0;
                     if (auction.getCurrentBidder() != null && auction.getCurrentBidder().equals(player.getUniqueId())) {
                        playerCurrentBid = auction.getCurrentPrice();
                     }

                     int amountToPay = bidAmount - playerCurrentBid;
                     if (auction.isSapphireCurrency()) {
                        int points = this.plugin.getPlayerPointsAPI().look(player.getUniqueId());
                        if (points < amountToPay) {
                           player.sendMessage(MessageUtils.getMessage(this.plugin, "not_enough_sapphires", amountToPay));
                           return;
                        }
                     } else if (!this.plugin.getEconomy().has(player, amountToPay)) {
                        player.sendMessage(MessageUtils.getMessage(this.plugin, "not_enough_coins", amountToPay));
                        return;
                     }

                     this.plugin.getAuctionManager().placeBid(player, bidAmount, slot);
                  }
               }
            }
         }
      }
   }

   private void handleWarehouseClick(Player player, InventoryClickEvent e, WarehouseGUI warehouseGUI) {
      if (e.getClickedInventory() == e.getView().getTopInventory()) {
         ItemStack clicked = e.getCurrentItem();
         if (clicked != null && clicked.getType() != Material.AIR) {
            int slot = e.getSlot();
            if (slot == 48) {
               if (warehouseGUI.getPage() > 0) {
                  player.closeInventory();
                  new WarehouseGUI(this.plugin, player, warehouseGUI.getPage() - 1).open();
               }
            } else if (slot == 50) {
               List<ItemStack> warehouseItems = this.plugin.getDataManager().getWarehouseItems(player.getUniqueId());
               int totalPages = Math.max(1, (int)Math.ceil(warehouseItems.size() / 36.0));
               int nextPage = warehouseGUI.getPage() + 1;
               if (nextPage < totalPages) {
                  player.closeInventory();
                  new WarehouseGUI(this.plugin, player, nextPage).open();
               }
            } else if (slot >= 9 && slot <= 44) {
               int index = warehouseGUI.getPage() * 36 + (slot - 9);
               ItemStack item = this.plugin.getDataManager().removeWarehouseItem(player.getUniqueId(), index);
               if (item != null) {
                  if (player.getInventory().firstEmpty() != -1) {
                     player.getInventory().addItem(new ItemStack[]{item});
                     player.sendMessage(MessageUtils.getMessage(this.plugin, "item_received"));
                  } else {
                     player.getWorld().dropItem(player.getLocation(), item);
                     player.sendMessage(MessageUtils.getMessage(this.plugin, "item_dropped"));
                  }

                  player.closeInventory();
                  new WarehouseGUI(this.plugin, player, warehouseGUI.getPage()).open();
               }
            }
         }
      }
   }
}
