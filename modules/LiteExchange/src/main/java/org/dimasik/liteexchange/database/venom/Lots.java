package org.dimasik.liteexchange.database.venom;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dimasik.liteexchange.LiteExchange;
import org.dimasik.liteexchange.lot.Lot;
import org.dimasik.liteexchange.util.Parser;

public class Lots {
   private HikariDataSource dataSource;
   private String tablePrefix;

   public Lots(HikariDataSource dataSource, String tablePrefix) {
      this.dataSource = dataSource;
      this.tablePrefix = tablePrefix;
   }

   public CompletableFuture<Void> addLot(String playerName, int coins, int rate) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + this.tablePrefix + "lots (player_name, coins, rate) VALUES (?, ?, ?)");
         ) {
            statement.setString(1, playerName);
            statement.setInt(2, coins);
            statement.setInt(3, rate);
            statement.executeUpdate();
         } catch (SQLException var121) {
            var121.printStackTrace();
         }
      });
   }

   public CompletableFuture<Void> updateLot(int id, int coins, int rate) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE " + this.tablePrefix + "lots SET coins = ?, rate = ? WHERE id = ?");
         ) {
            statement.setInt(1, coins);
            statement.setInt(2, rate);
            statement.setInt(3, id);
            statement.executeUpdate();
         } catch (SQLException var121) {
            var121.printStackTrace();
         }
      });
   }

   public CompletableFuture<Void> removeLot(int id) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + this.tablePrefix + "lots WHERE id = ?");
         ) {
            statement.setInt(1, id);
            statement.executeUpdate();
         } catch (SQLException var101) {
            var101.printStackTrace();
         }
      });
   }

   public CompletableFuture<List<Lot>> getAllLots() {
      this.revokeExpiredLots();
      return CompletableFuture.supplyAsync(
         () -> {
            ArrayList lots = new ArrayList();

            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.tablePrefix + "lots ORDER BY rate DESC");
            ) {
               ResultSet resultSet = statement.executeQuery();

               while (resultSet.next()) {
                  lots.add(
                     new Lot(
                        resultSet.getInt("id"),
                        resultSet.getString("player_name"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("rate"),
                        resultSet.getTimestamp("createTime").getTime()
                     )
                  );
               }
            } catch (SQLException var101) {
               var101.printStackTrace();
            }

            return lots;
         }
      );
   }

   public CompletableFuture<Integer> getMaxRate() {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT MAX(rate) AS max_rate FROM " + this.tablePrefix + "lots");
            ) {
               ResultSet resultSet = statement.executeQuery();
               if (!resultSet.next()) {
                  return 100000;
               }

               int maxRate = resultSet.getInt("max_rate");
               if (maxRate <= 0) {
                  return 100000;
               }

               var5 = resultSet.getInt("max_rate");
            }

            return var5;
         } catch (SQLException var11) {
            var11.printStackTrace();
            return 100000;
         }
      });
   }

   public CompletableFuture<List<Lot>> getExpiredLots() {
      return CompletableFuture.supplyAsync(
         () -> {
            ArrayList lots = new ArrayList();

            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement(
                  "SELECT * FROM " + this.tablePrefix + "lots WHERE createTime < NOW() - INTERVAL 12 HOUR"
               );
            ) {
               ResultSet resultSet = statement.executeQuery();

               while (resultSet.next()) {
                  lots.add(
                     new Lot(
                        resultSet.getInt("id"),
                        resultSet.getString("player_name"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("rate"),
                        resultSet.getTimestamp("createTime").getTime()
                     )
                  );
               }
            } catch (SQLException var101) {
               var101.printStackTrace();
            }

            return lots;
         }
      );
   }

   public void revokeExpiredLots() {
      CompletableFuture.runAsync(
            () -> {
               try {
                  Connection connection = this.dataSource.getConnection();

                  try {
                     PreparedStatement selectStmt = connection.prepareStatement(
                        "SELECT * FROM " + this.tablePrefix + "lots WHERE createTime < NOW() - INTERVAL 12 HOUR"
                     );
                     List<Lot> expiredLots = new ArrayList<>();

                     try {
                        ResultSet resultSet = selectStmt.executeQuery();

                        while (resultSet.next()) {
                           expiredLots.add(
                              new Lot(
                                 resultSet.getInt("id"),
                                 resultSet.getString("player_name"),
                                 resultSet.getInt("coins"),
                                 resultSet.getInt("rate"),
                                 resultSet.getTimestamp("createTime").getTime()
                              )
                           );
                        }
                     } finally {
                        selectStmt.close();
                     }

                     if (!expiredLots.isEmpty()) {
                        PreparedStatement deleteStmt = connection.prepareStatement(
                           "DELETE FROM " + this.tablePrefix + "lots WHERE createTime < NOW() - INTERVAL 12 HOUR"
                        );

                        try {
                           deleteStmt.executeUpdate();
                        } finally {
                           deleteStmt.close();
                        }

                        for (Lot lot : expiredLots) {
                           Player seller = Bukkit.getPlayerExact(lot.getPlayerName());
                           if (seller != null && seller.isOnline()) {
                              seller.sendMessage(
                                 Parser.color(
                                    "&b▶ &x&E&7&E&7&E&7Ваша заявка на продажу монеток не была выполнена в срок и вам было возвращено &x&B&E&C&8&0&0"
                                       + Parser.formatNumber(lot.getCoins() * lot.getRate())
                                       + " монеток"
                                 )
                              );
                           }

                           LiteExchange.getInstance().getEconomy().depositPlayer(lot.getPlayerName(), lot.getCoins() * lot.getRate());
                        }
                     }
                  } finally {
                     connection.close();
                  }
               } catch (SQLException var24) {
                  var24.printStackTrace();
               }
            }
         )
         .exceptionally(throwable -> {
            LiteExchange.getInstance().getLogger().warning("Ошибка при удалении просроченных лотов: " + throwable.getMessage());
            return null;
         });
   }

   public CompletableFuture<Lot> getPlayerLot(String player) {
      this.revokeExpiredLots();
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Lot var5;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.tablePrefix + "lots WHERE player_name = ? LIMIT 1");
               ) {
                  statement.setString(1, player);
                  ResultSet resultSet = statement.executeQuery();
                  if (!resultSet.next()) {
                     return null;
                  }

                  var5 = new Lot(
                     resultSet.getInt("id"),
                     resultSet.getString("player_name"),
                     resultSet.getInt("coins"),
                     resultSet.getInt("rate"),
                     resultSet.getTimestamp("createTime").getTime()
                  );
               }

               return var5;
            } catch (SQLException var11) {
               var11.printStackTrace();
               return null;
            }
         }
      );
   }

   public void revokeLot(Lot lot) {
      int moneyAmount = lot.getCoins() * lot.getRate();
      String playerName = lot.getPlayerName();
      String var10003 = Parser.formatNumber(moneyAmount);
      this.removeLot(lot.getId())
         .thenRun(
            () -> {
               try {
                  Player seller = Bukkit.getPlayerExact(playerName);
                  if (seller != null && seller.isOnline()) {
                     seller.sendMessage(
                        Parser.color(
                           "&b▶ &x&E&7&E&7&E&7Ваша заявка на продажу монеток не была выполнена в срок и вам было возвращено &x&B&E&C&8&0&0"
                              + var10003
                              + " монеток"
                        )
                     );
                  }

                  LiteExchange.getInstance().getEconomy().depositPlayer(playerName, moneyAmount);
                  LiteExchange.getInstance().getLogger().info("Монеты возвращены игроку " + playerName + ": " + moneyAmount);
               } catch (Exception var4x) {
                  LiteExchange.getInstance().getLogger().warning("Ошибка при возврате монеток игроку " + playerName + ": " + var4x.getMessage());
                  var4x.printStackTrace();
               }
            }
         );
   }

   public CompletableFuture<Boolean> lotExists(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Boolean var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + this.tablePrefix + "lots WHERE id = ?");
            ) {
               statement.setInt(1, id);
               ResultSet resultSet = statement.executeQuery();
               var5 = resultSet.next();
            }

            return var5;
         } catch (SQLException var11) {
            var11.printStackTrace();
            return false;
         }
      });
   }

   public CompletableFuture<Lot> getLot(int id) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Lot var5;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.tablePrefix + "lots WHERE id = ?");
               ) {
                  statement.setInt(1, id);
                  ResultSet resultSet = statement.executeQuery();
                  if (!resultSet.next()) {
                     return null;
                  }

                  var5 = new Lot(
                     resultSet.getInt("id"),
                     resultSet.getString("player_name"),
                     resultSet.getInt("coins"),
                     resultSet.getInt("rate"),
                     resultSet.getTimestamp("createTime").getTime()
                  );
               }

               return var5;
            } catch (SQLException var11) {
               var11.printStackTrace();
               return null;
            }
         }
      );
   }

   public void buyLot(Player buyer, Lot lot) {
      LiteExchange.getInstance()
         .dbCore
         .getCoins()
         .takeBalance(buyer.getName(), lot.getCoins())
         .thenAccept(
            success -> {
               if (!success) {
                  buyer.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас недостаточно коинов для покупки!"));
               } else {
                  try {
                     String var10001 = lot.getPlayerName();
                     String var10003 = buyer.getName();
                     int moneyAmount = lot.getCoins() * lot.getRate();
                     Player seller = Bukkit.getPlayerExact(var10001);
                     if (seller != null && seller.isOnline()) {
                        seller.sendMessage(
                           Parser.color(
                              " &b▶ &x&E&7&E&7&E&7Игрок &f"
                                 + var10003
                                 + " &x&E&7&E&7&E&7купил у Вас &6"
                                 + Parser.formatNumber(moneyAmount)
                                 + " монеток &fза &x&C&0&C&A&0&0"
                                 + Parser.formatNumber(lot.getCoins())
                                 + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0|&f."
                           )
                        );
                     }

                     String msg = Parser.formatNumber(moneyAmount);
                     buyer.sendMessage(
                        Parser.color(
                           "&x&0&5&F&7&0&0▶ &x&E&7&E&7&E&7Вы успешно закупили &6"
                              + msg
                              + " монеток &fна бюджет &x&C&0&C&A&0&0"
                              + Parser.formatNumber(lot.getCoins())
                              + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0|&f."
                        )
                     );
                     LiteExchange.getInstance().getEconomy().depositPlayer(buyer.getName(), moneyAmount);
                     LiteExchange.getInstance().dbCore.getCoins().addBalance(var10001, lot.getCoins());
                     LiteExchange.getInstance().dbCore.getLogs().logTransaction(var10001, buyer.getName(), "PAY", lot.getCoins(), lot.getRate());
                     this.removeLot(lot.getId());
                  } catch (Exception var9) {
                     LiteExchange.getInstance().getLogger().warning("Ошибка при покупке лота: " + var9.getMessage());
                     var9.printStackTrace();
                  }
               }
            }
         );
   }

   public void buyLotCount(Player buyer, Lot lot, int coins) {
      LiteExchange.getInstance()
         .dbCore
         .getCoins()
         .takeBalance(buyer.getName(), coins)
         .thenAccept(
            success -> {
               if (!success) {
                  buyer.sendMessage(Parser.color("&x&F&B&2&2&2&2 ▶ &x&E&7&E&7&E&7У вас недостаточно коинов для покупки!"));
               } else {
                  try {
                     String var10001 = lot.getPlayerName();
                     String var10003 = buyer.getName();
                     int moneyAmount = coins * lot.getRate();
                     Player seller = Bukkit.getPlayerExact(var10001);
                     if (seller != null && seller.isOnline()) {
                        seller.sendMessage(
                           Parser.color(
                              " &b▶ &x&E&7&E&7&E&7Игрок &f"
                                 + var10003
                                 + " &x&E&7&E&7&E&7купил у Вас &6"
                                 + Parser.formatNumber(moneyAmount)
                                 + " монеток &fза &x&C&0&C&A&0&0"
                                 + Parser.formatNumber(coins)
                                 + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0|&f."
                           )
                        );
                     }

                     String msg = Parser.formatNumber(moneyAmount);
                     buyer.sendMessage(
                        Parser.color(
                           "&x&0&5&F&7&0&0▶ &x&E&7&E&7&E&7Вы успешно закупили &6"
                              + msg
                              + " монеток &fна бюджет &x&C&0&C&A&0&0"
                              + Parser.formatNumber(coins)
                              + " &x&C&0&C&A&0&0|&x&F&2&F&F&0&0&l❘&x&C&0&C&A&0&0|&f."
                        )
                     );
                     LiteExchange.getInstance().getEconomy().depositPlayer(buyer.getName(), moneyAmount);
                     LiteExchange.getInstance().dbCore.getCoins().addBalance(var10001, coins);
                     LiteExchange.getInstance().dbCore.getLogs().logTransaction(var10001, buyer.getName(), "PAY", coins, lot.getRate());
                     if (coins == lot.getCoins()) {
                        this.removeLot(lot.getId());
                     } else {
                        this.updateLot(lot.getId(), lot.getCoins() - coins, lot.getRate());
                     }
                  } catch (Exception var10) {
                     LiteExchange.getInstance().getLogger().warning("Ошибка при покупке лота: " + var10.getMessage());
                     var10.printStackTrace();
                  }
               }
            }
         );
   }
}
