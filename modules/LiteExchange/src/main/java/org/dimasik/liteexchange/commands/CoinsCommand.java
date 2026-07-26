package org.dimasik.liteexchange.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.database.venom.Logs;
import org.dimasik.liteexchange.util.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoinsCommand implements TabExecutor {
   public CoinsCommand() {
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      boolean psender = false;
      if (sender instanceof Player) {
         psender = true;
      }

      if (args.length < 1) {
         sender.sendMessage(Parser.color(""));
         sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fИспользование: &x&F&B&2&2&2&2/coins give/take/set/get/logs"));
         sender.sendMessage(Parser.color(""));
         return true;
      } else {
         if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fИспользование: &x&F&B&2&2&2&2/coins give [игрок] [количество]"));
               sender.sendMessage(Parser.color(""));
               return true;
            }

            String playerFilter = args[1];

            try {
               int coins = Integer.parseInt(args[2]);
               LiteExchange.getInstance().dbCore.getCoins().addBalance(playerFilter, coins);
               LiteExchange.getInstance().dbCore.getLogs().logTransaction(playerFilter, psender ? sender.getName() : "Console", "GIVE", coins, 0);
               if (psender) {
                  sender.sendMessage(Parser.color(""));
                  sender.sendMessage(Parser.color("&b ▶ &fБаланс игрока &b" + playerFilter + " &fпополнен на &b" + Parser.formatNumber(coins) + " коинов"));
                  sender.sendMessage(Parser.color(""));
               }
            } catch (NumberFormatException var20) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fКоличество должно быть &x&F&B&2&2&2&2целым числом"));
               sender.sendMessage(Parser.color(""));
               return true;
            }
         } else if (args[0].equalsIgnoreCase("take")) {
            if (args.length < 3) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fИспользование: &x&F&B&2&2&2&2/coins take [игрок] [количество]"));
               sender.sendMessage(Parser.color(""));
               return true;
            }

            String playerFilter = args[1];

            try {
               int coins = Integer.parseInt(args[2]);
               LiteExchange.getInstance().dbCore.getCoins().takeBalance(playerFilter, coins);
               LiteExchange.getInstance().dbCore.getLogs().logTransaction(playerFilter, psender ? sender.getName() : "Console", "TAKE", coins, 0);
               if (psender) {
                  sender.sendMessage(Parser.color(""));
                  sender.sendMessage(Parser.color("&b ▶ &fС баланса игрока &b" + playerFilter + " &fснято &b" + Parser.formatNumber(coins) + " коинов"));
                  sender.sendMessage(Parser.color(""));
               }
            } catch (NumberFormatException var19) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fКоличество должно быть &x&F&B&2&2&2&2целым числом"));
               sender.sendMessage(Parser.color(""));
               return true;
            }
         } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fИспользование: &x&F&B&2&2&2&2/coins set [игрок] [количество]"));
               sender.sendMessage(Parser.color(""));
               return true;
            }

            String playerFilter = args[1];

            try {
               int coins = Integer.parseInt(args[2]);
               LiteExchange.getInstance().dbCore.getCoins().setBalance(playerFilter, coins);
               LiteExchange.getInstance().dbCore.getLogs().logTransaction(playerFilter, psender ? sender.getName() : "Console", "SET", coins, 0);
               if (psender) {
                  sender.sendMessage(Parser.color(""));
                  sender.sendMessage(Parser.color("&b ▶ &fБаланс игрока &b" + playerFilter + " &fустановлен на &b" + Parser.formatNumber(coins) + " коинов"));
                  sender.sendMessage(Parser.color(""));
               }
            } catch (NumberFormatException var18) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fКоличество должно быть &x&F&B&2&2&2&2целым числом"));
               sender.sendMessage(Parser.color(""));
               return true;
            }
         } else if (args[0].equalsIgnoreCase("get")) {
            if (args.length < 2) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fИспользование: &x&F&B&2&2&2&2/coins get [игрок]"));
               sender.sendMessage(Parser.color(""));
               return true;
            }

            String playerFilter = args[1];

            try {
               int coins = LiteExchange.getInstance().dbCore.getCoins().getBalance(playerFilter).get();
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&b ▶ &fБаланс игрока &b" + playerFilter + " &f- &b" + Parser.formatNumber(coins) + " коинов"));
               sender.sendMessage(Parser.color(""));
            } catch (Exception var17) {
               sender.sendMessage(Parser.color(""));
               sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fПроизошла ошибка ответа &x&F&B&2&2&2&2базы данных"));
               sender.sendMessage(Parser.color(""));
               return true;
            }
         } else if (args[0].equalsIgnoreCase("logs")) {
            String playerFilter = null;
            String targetFilter = null;
            String actionFilter = null;
            Integer rateFilter = null;
            Integer coinsFilter = null;
            String dateFilter = null;

            for (int i = 1; i < args.length; i++) {
               if (args[i].equalsIgnoreCase("-p") && i + 1 < args.length) {
                  targetFilter = args[i + 1].toLowerCase();
                  i++;
               } else if (args[i].equalsIgnoreCase("-t") && i + 1 < args.length) {
                  dateFilter = args[i + 1];
                  i++;
               } else if (args[i].equalsIgnoreCase("-a") && i + 1 < args.length) {
                  actionFilter = args[i + 1].toUpperCase();
                  i++;
               } else if (args[i].equalsIgnoreCase("-r") && i + 1 < args.length) {
                  try {
                     rateFilter = Integer.parseInt(args[i + 1]);
                     i++;
                  } catch (NumberFormatException var16) {
                     sender.sendMessage(Parser.color(""));
                     sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fКурс должен быть &x&F&B&2&2&2&2целым числом"));
                     sender.sendMessage(Parser.color(""));
                     return true;
                  }
               } else if (args[i].equalsIgnoreCase("-c") && i + 1 < args.length) {
                  try {
                     coinsFilter = Integer.parseInt(args[i + 1]);
                     i++;
                  } catch (NumberFormatException var15) {
                     sender.sendMessage(Parser.color(""));
                     sender.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &fКоличество должно быть &x&F&B&2&2&2&2целым числом"));
                     sender.sendMessage(Parser.color(""));
                     return true;
                  }
               } else if (playerFilter == null) {
                  playerFilter = args[i].toLowerCase();
               }
            }

            String finalPlayerFilter = playerFilter;
            String finalTargetFilter = targetFilter;
            LiteExchange.getInstance()
               .dbCore
               .getLogs()
               .getFilteredTransactions(playerFilter, targetFilter, actionFilter, rateFilter, coinsFilter, dateFilter)
               .thenAccept(
                  transactions -> {
                     List<String> messages = new ArrayList<>();
                     messages.add(Parser.color(""));
                     if (transactions.isEmpty()) {
                        messages.add(Parser.color("&x&F&B&2&2&2&2 ▶ &fПо вашему запросу &x&F&B&2&2&2&2не найдено &fни одной из записей."));
                     } else {
                        for (Logs.Transaction transaction : transactions) {
                           String time = new SimpleDateFormat("HH:mm:ss").format(new Date(transaction.getTime()));
                           String action = transaction.getAction();
                           String playerName = transaction.getPlayerName();
                           String targetName = transaction.getTarget();
                           int transactionCoins = transaction.getCoins();
                           int rate = transaction.getRate();
                           messages.add(
                              Parser.color(
                                 String.format("&b &n▍&7 [%s] &b%s &f» &b%s &7| &b%d &7| &b%s", time, targetName, playerName, transactionCoins, action)
                              )
                           );
                        }

                        String filterInfo = "";
                        if (finalPlayerFilter != null) {
                           filterInfo = filterInfo + " игрока " + finalPlayerFilter;
                        }

                        if (finalTargetFilter != null) {
                           filterInfo = filterInfo + " цели " + finalTargetFilter;
                        }

                        messages.add(Parser.color("&b ▶ &fОтображены логи" + filterInfo + "&f. &7&o(" + transactions.size() + ")"));
                     }

                     messages.add(Parser.color(""));
                     if (sender instanceof Player) {
                        Bukkit.getScheduler().runTask(LiteExchange.getInstance(), () -> {
                           for (String messagex : messages) {
                              sender.sendMessage(messagex);
                           }
                        });
                     } else {
                        for (String message : messages) {
                           sender.sendMessage(message);
                        }
                     }
                  }
               );
            return true;
         }

         return true;
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("give");
         completions.add("take");
         completions.add("set");
         completions.add("get");
         completions.add("logs");
      } else if (args.length == 2) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            completions.add(player.getName());
         }
      } else if (args.length == 3) {
         if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set")) {
            completions.add("1");
            completions.add("10");
            completions.add("50");
            completions.add("100");
            completions.add("500");
         } else if (args[0].equalsIgnoreCase("logs")) {
            completions.add("-p");
            completions.add("-t");
            completions.add("-a");
            completions.add("-r");
            completions.add("-c");
         }
      } else if (args[0].equalsIgnoreCase("logs")) {
         String var10 = args[args.length - 2];
         switch (var10) {
            case "-p":
               for (Player player : Bukkit.getOnlinePlayers()) {
                  completions.add(player.getName());
               }
               break;
            case "-t":
               completions.add("день:месяц:год");
               break;
            case "-a":
               completions.add("GIVE/TAKE/PAY/SET");
               break;
            case "-r":
               completions.add("[курс]");
               break;
            case "-c":
               completions.add("[кол-во коинов]");
               break;
            default:
               completions.add("-p");
               completions.add("-t");
               completions.add("-a");
               completions.add("-r");
               completions.add("-c");
         }
      }

      List<String> returnList = new ArrayList<>();

      for (String s : completions) {
         if (s.startsWith(args[args.length - 1])) {
            returnList.add(s);
         }
      }

      return returnList;
   }

}
