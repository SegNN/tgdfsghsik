package org.dimasik.liteexchange.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.EditableLot;
import org.dimasik.liteexchange.lot.Lot;
import org.dimasik.liteexchange.menu.CountBuy;
import org.dimasik.liteexchange.menu.Create;
import org.dimasik.liteexchange.menu.Main;
import org.dimasik.liteexchange.util.Parser;

public class MainListener implements Listener {
   public MainListener() {
   }

   @EventHandler
   public void onInvClick(InventoryClickEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof Main) {
         Player player = (Player)event.getWhoClicked();
         Main main = (Main)event.getView().getTopInventory().getHolder();
         Inventory inventory = event.getView().getTopInventory();
         if (event.getClickedInventory() == null) {
            return;
         }

         if (event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 40) {
               try {
                  new Create(player, LiteExchange.getInstance().dbCore.getLots().getMaxRate().get(), 5);
               } catch (Exception var17) {
               }
            } else if (slot == 49) {
               try {
                  int lots = LiteExchange.getInstance().dbCore.getLots().getAllLots().get().size();
                  int balance = lots / 36 + (lots % 36 == 0 ? 0 : 1);
                  new Main(player, Math.min(balance, main.page));
               } catch (Exception var16) {
               }
            } else if (slot == 48) {
               try {
                  int lots = LiteExchange.getInstance().dbCore.getLots().getAllLots().get().size();
                  int balance = lots / 36 + (lots % 36 == 0 ? 0 : 1);
                  new Main(player, Math.max(1, Math.min(balance, main.page - 1)));
               } catch (Exception var15) {
               }
            } else if (slot == 50) {
               try {
                  int lots = LiteExchange.getInstance().dbCore.getLots().getAllLots().get().size();
                  int balance = lots / 36 + (lots % 36 == 0 ? 0 : 1);
                  new Main(player, Math.min(balance, main.page + 1));
               } catch (Exception var14) {
               }
            } else if (slot == 45) {
               try {
                  Lot lot = LiteExchange.getInstance().dbCore.getLots().getPlayerLot(player.getName()).get();
                  if (lot != null) {
                     player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Вы сняли заявку на покупку коинов."));
                     LiteExchange.getInstance().dbCore.getLots().revokeLot(lot);
                     player.closeInventory();
                  }
               } catch (Exception var13) {
               }
            } else if (main.lots.containsKey(slot)) {
               Lot lot = main.lots.get(slot);
               if (lot.getPlayerName().equalsIgnoreCase(player.getName())) {
                  player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Нельзя покупать свой же лот"));
                  return;
               }

               if (event.isLeftClick()) {
                  if (LiteExchange.selectedIds.getOrDefault(player, -1) == lot.getId()) {
                     try {
                        int balance = LiteExchange.getInstance().dbCore.getCoins().getBalance(player.getName()).get();
                        if (balance < lot.getCoins()) {
                           player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас недостаточно коинов для закупки"));
                        } else if (LiteExchange.getInstance().dbCore.getLots().lotExists(lot.getId()).get()) {
                           LiteExchange.getInstance().dbCore.getLots().buyLot(player, lot);
                           player.closeInventory();
                        } else {
                           player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                        }
                     } catch (Exception var12) {
                     }
                  } else {
                     try {
                        LiteExchange.selectedIds.put(player, lot.getId());
                        int balance = LiteExchange.getInstance().dbCore.getLots().getAllLots().get().size();
                        int pages = balance / 36 + (balance % 36 == 0 ? 0 : 1);
                        new Main(player, Math.min(pages, main.page));
                     } catch (Exception var11) {
                     }
                  }
               } else if (event.isRightClick()) {
                  try {
                     if (LiteExchange.getInstance().dbCore.getLots().lotExists(lot.getId()).get()) {
                        EditableLot editableLot = LiteExchange.getInstance().dbCore.getLots().getLot(lot.getId()).get().toEditable();
                        editableLot.setCoins(5);
                        new CountBuy(player, editableLot, editableLot.id);
                     } else {
                        player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Эта заявка была выкуплена/удалена."));
                     }
                  } catch (Exception var10) {
                  }
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
