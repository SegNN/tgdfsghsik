package org.dimasik.liteexchange.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.Lot;
import org.dimasik.liteexchange.menu.Main;
import org.dimasik.liteexchange.util.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExchangeCommand implements TabExecutor {
   public ExchangeCommand() {
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(Parser.color(""));
         sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fТолько игрок может использовать эту команду."));
         sender.sendMessage(Parser.color(""));
         return true;
      } else if (args.length >= 2 && args[0].equalsIgnoreCase("buy")) {
         int amount;
         try {
            amount = this.parseAmount(args[1]);
         } catch (NumberFormatException var8) {
            player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Вы указали неверное количество монеток"));
            return true;
         }

         if (amount <= 0) {
            player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Сумма должна быть больше нуля."));
            return true;
         } else {
            this.handleBuy(player, amount);
            return true;
         }
      } else {
         new Main(player, 1);
         return true;
      }
   }

   private void handleBuy(Player player, int targetMoney) {
      LiteExchange plugin = LiteExchange.getInstance();
      CompletableFuture.runAsync(() -> {
         try {
            int playerCoins = plugin.dbCore.getCoins().getBalance(player.getName()).get();
            if (playerCoins <= 0) {
               player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас нет коинов для закупки."));
               return;
            }

            List<Lot> lots = plugin.dbCore.getLots().getAllLots().get();
            if (lots == null || lots.isEmpty()) {
               player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7На бирже нет активных заявок."));
               return;
            }

            int moneyLeft = targetMoney;

            for (Lot lot : lots) {
               if (moneyLeft <= 0) {
                  break;
               }

               if (!lot.getPlayerName().equalsIgnoreCase(player.getName())) {
                  Lot freshLot = plugin.dbCore.getLots().getLot(lot.getId()).get();
                  if (freshLot != null) {
                     int currentCoins = plugin.dbCore.getCoins().getBalance(player.getName()).get();
                     if (currentCoins <= 0) {
                        break;
                     }

                     int availableMoney = freshLot.getCoins() * freshLot.getRate();
                     int moneyFromThisLot = Math.min(moneyLeft, availableMoney);
                     int coinsNeeded = moneyFromThisLot / freshLot.getRate();
                     if (coinsNeeded > 0) {
                        coinsNeeded = Math.min(coinsNeeded, currentCoins);
                        coinsNeeded = Math.min(coinsNeeded, freshLot.getCoins());
                        if (coinsNeeded > 0) {
                           int actualMoney = coinsNeeded * freshLot.getRate();
                           plugin.dbCore.getLots().buyLotCount(player, freshLot, coinsNeeded);
                           moneyLeft -= actualMoney;
                        }
                     }
                  }
               }
            }
         } catch (Exception var14) {
            player.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7Произошла ошибка при закупке. Попробуйте позже."));
            var14.printStackTrace();
         }
      });
   }

   private int parseAmount(String input) {
      String lower = input.toLowerCase();
      int multiplier = 1;
      if (lower.endsWith("kk")) {
         multiplier = 1000000;
         lower = lower.substring(0, lower.length() - 2);
      } else if (lower.endsWith("k")) {
         multiplier = 1000;
         lower = lower.substring(0, lower.length() - 1);
      }

      int base = Integer.parseInt(lower);
      return base * multiplier;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         if ("buy".startsWith(args[0].toLowerCase())) {
            completions.add("buy");
         }
      } else if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
         completions.add("<количество>");
      }

      return completions;
   }
}
