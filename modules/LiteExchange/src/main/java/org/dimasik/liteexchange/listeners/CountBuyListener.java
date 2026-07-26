package org.dimasik.liteexchange.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.EditableLot;
import org.dimasik.liteexchange.lot.Lot;
import org.dimasik.liteexchange.menu.CountBuy;
import org.dimasik.liteexchange.menu.Main;
import org.dimasik.liteexchange.util.Parser;

public class CountBuyListener implements Listener {
   public CountBuyListener() {
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof CountBuy) {
         CountBuy countBuy = (CountBuy)event.getView().getTopInventory().getHolder();
         if (!countBuy.forceClose) {
            Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
               if (event.getPlayer() != null) {
                  new Main((Player)event.getPlayer(), 1);
               }
            }, 1L);
         }
      }
   }

   @EventHandler
   public void onInvClick(InventoryClickEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof CountBuy) {
         Player player = (Player)event.getWhoClicked();
         CountBuy countBuy = (CountBuy)event.getView().getTopInventory().getHolder();
         Inventory inventory = event.getView().getTopInventory();
         if (event.getClickedInventory() == null) {
            return;
         }

         if (event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            EditableLot editableLot = countBuy.lot;
            if (slot == 9) {
               try {
                  Lot original = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (original == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoins = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins - 50;
                  next = Math.min(playerCoins, next);
                  next = Math.min(original.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var19) {
               }
            } else if (slot == 10) {
               try {
                  Lot original = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (original == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoins = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins - 25;
                  next = Math.min(playerCoins, next);
                  next = Math.min(original.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var18) {
               }
            } else if (slot == 11) {
               try {
                  Lot original = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (original == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoins = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins - 10;
                  next = Math.min(playerCoins, next);
                  next = Math.min(original.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var17) {
               }
            } else if (slot == 12) {
               try {
                  Lot original = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (original == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoins = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins - 5;
                  next = Math.min(playerCoins, next);
                  next = Math.min(original.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var16) {
               }
            } else if (slot == 13) {
               try {
                  if (event.isLeftClick()) {
                     Lot original = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                     if (original == null) {
                        player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                        player.closeInventory();
                        return;
                     }

                     int playerCoins = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                     if (original.getCoins() < editableLot.getCoins()) {
                        player.sendMessage(
                           Parser.color(
                              "&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Какой-то игрок уже купил часть этой заявки и вы больше не можете купить такое количество монет."
                           )
                        );
                        player.closeInventory();
                        return;
                     }

                     if (editableLot.getCoins() > playerCoins) {
                        player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас недостаточно коинов для закупки"));
                        player.closeInventory();
                        return;
                     }

                     LiteExchange.getInstance().dbCore.getLots().buyLotCount(player, original, editableLot.getCoins());
                     player.closeInventory();
                  } else if (event.isRightClick()) {
                     player.closeInventory();
                  }
               } catch (Exception var15) {
               }
            } else if (slot == 14) {
               try {
                  Lot originalx = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (originalx == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoinsx = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins + 5;
                  next = Math.min(playerCoinsx, next);
                  next = Math.min(originalx.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var14) {
               }
            } else if (slot == 15) {
               try {
                  Lot originalx = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (originalx == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoinsx = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins + 10;
                  next = Math.min(playerCoinsx, next);
                  next = Math.min(originalx.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var13) {
               }
            } else if (slot == 16) {
               try {
                  Lot originalx = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (originalx == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoinsx = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins + 25;
                  next = Math.min(playerCoinsx, next);
                  next = Math.min(originalx.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var12) {
               }
            } else if (slot == 17) {
               try {
                  Lot originalx = LiteExchange.getInstance().dbCore.getLots().getLot(countBuy.originalLotId).get();
                  if (originalx == null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     return;
                  }

                  int playerCoinsx = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                  int next = countBuy.lot.coins + 50;
                  next = Math.min(playerCoinsx, next);
                  next = Math.min(originalx.getCoins(), next);
                  next = Math.max(next, 5);
                  editableLot.setCoins(next);
                  countBuy.forceClose = true;
                  new CountBuy(player, editableLot, editableLot.id);
               } catch (Exception var11) {
               }
            }
         } else if (event.isShiftClick()) {
            event.setCancelled(true);
         }
      }
   }

   public boolean checkCooldown(Player player) {
      Integer timestamp = (int)System.currentTimeMillis();
      if (LiteExchange.cooldowns.getOrDefault(player.getName(), 0) < timestamp) {
         LiteExchange.cooldowns.put(player.getName(), timestamp + 100);
         return false;
      } else {
         return true;
      }
   }
}
