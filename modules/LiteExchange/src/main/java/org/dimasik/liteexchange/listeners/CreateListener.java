package org.dimasik.liteexchange.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.menu.Create;
import org.dimasik.liteexchange.menu.Main;
import org.dimasik.liteexchange.menu.RateSelect;
import org.dimasik.liteexchange.util.Parser;

public class CreateListener implements Listener {
   public CreateListener() {
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof Create && !((Create)event.getView().getTopInventory().getHolder()).forceClose) {
         Bukkit.getScheduler().runTaskLater(LiteExchange.getInstance(), () -> {
            if (event.getPlayer() != null) {
               new Main((Player)event.getPlayer(), 1);
            }
         }, 1L);
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView().getTopInventory().getHolder() instanceof Create) {
         Player player = (Player)event.getWhoClicked();
         Create create = (Create)event.getView().getTopInventory().getHolder();
         Inventory inventory = event.getView().getTopInventory();
         if (event.getClickedInventory() == null) {
            return;
         }

         if (event.getClickedInventory().equals(inventory)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 9) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins - 50;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 10) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins - 25;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 11) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins - 10;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 12) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins - 5;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 13) {
               try {
                  if (event.isLeftClick()) {
                     try {
                        if (LiteExchange.getInstance().dbCore.getLots().getPlayerLot(player.getName()).get() == null) {
                           double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
                           int summary = create.rate * create.coins;
                           if (balance < summary) {
                              player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас недостаточно монеток"));
                              return;
                           }

                           LiteExchange.getInstance().getEconomy().withdrawPlayer(player.getName(), summary);
                           LiteExchange.getInstance().dbCore.getLots().addLot(player.getName(), create.coins, create.rate);
                           player.sendMessage(
                              Parser.color(
                                 "&b ▶ &fВы успешно &bдобавили заказ &fна продажу монет, теперь, &bв течении 12 часов &fее должен кто-нибудь одобрить."
                              )
                           );
                           player.closeInventory();
                        } else {
                           player.sendMessage(
                              Parser.color(
                                 "&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас уже &x&F&B&2&2&2&2есть активная заявка&x&E&7&E&7&E&7, чтобы создать новую заявку, вам &x&F&B&2&2&2&2нужно отменить предыдущую&x&E&7&E&7&E&7."
                              )
                           );
                        }
                     } catch (Exception var10) {
                     }
                  } else if (event.isRightClick()) {
                     create.forceClose = true;
                     new RateSelect(player, create.rate, create.coins);
                  }
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            } else if (slot == 14) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins + 5;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 15) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins + 10;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 16) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins + 25;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
            } else if (slot == 17) {
               double balance = LiteExchange.getInstance().getEconomy().getBalance(player.getName());
               int summary = create.coins + 50;
               if (create.rate > 0) {
                  summary = Math.min((int)balance / create.rate, summary);
               }

               summary = Math.max(summary, 5);
               summary = Math.min(summary, 100000);
               create.forceClose = true;
               new Create(player, create.rate, summary);
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
