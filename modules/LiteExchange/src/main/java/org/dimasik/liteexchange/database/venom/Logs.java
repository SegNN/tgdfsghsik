package org.dimasik.liteexchange.database.venom;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Logs {
   private HikariDataSource dataSource;
   private String tablePrefix;

   public Logs(HikariDataSource dataSource, String tablePrefix) {
      this.dataSource = dataSource;
      this.tablePrefix = tablePrefix;
   }

   public CompletableFuture<Void> logTransaction(String playerName, String target, String action, int coins, int rate) {
      return CompletableFuture.runAsync(
         () -> {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement(
                  "INSERT INTO " + this.tablePrefix + "logs (player_name, target, action, coins, rate) VALUES (?, ?, ?, ?, ?)"
               );
            ) {
               statement.setString(1, playerName);
               statement.setString(2, target);
               statement.setString(3, action);
               statement.setInt(4, coins);
               statement.setInt(5, rate);
               statement.executeUpdate();
            } catch (SQLException var141) {
               var141.printStackTrace();
            }
         }
      );
   }

   public CompletableFuture<List<Logs.Transaction>> getTransactions(String playerName) {
      return CompletableFuture.supplyAsync(
         () -> {
            ArrayList transactions = new ArrayList();

            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement(
                  "SELECT * FROM " + this.tablePrefix + "logs WHERE player_name = ? OR target = ? ORDER BY time DESC"
               );
            ) {
               statement.setString(1, playerName);
               statement.setString(2, playerName);
               ResultSet resultSet = statement.executeQuery();

               while (resultSet.next()) {
                  transactions.add(
                     new Logs.Transaction(
                        resultSet.getInt("id"),
                        resultSet.getString("player_name"),
                        resultSet.getString("target"),
                        resultSet.getTimestamp("time").getTime(),
                        resultSet.getString("action"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("rate")
                     )
                  );
               }
            } catch (SQLException var111) {
               var111.printStackTrace();
            }

            return transactions;
         }
      );
   }

   public CompletableFuture<List<Logs.Transaction>> getFilteredTransactions(
      String playerName, String target, String action, Integer rate, Integer coins, String date
   ) {
      return CompletableFuture.supplyAsync(
         () -> {
            ArrayList transactions = new ArrayList();

            try (Connection connection = this.dataSource.getConnection()) {
               StringBuilder query = new StringBuilder("SELECT * FROM " + this.tablePrefix + "logs WHERE 1=1");
               List<Object> params = new ArrayList<>();
               if (playerName != null) {
                  query.append(" AND player_name = ?");
                  params.add(playerName.toLowerCase());
               }

               if (target != null) {
                  query.append(" AND target = ?");
                  params.add(target.toLowerCase());
               }

               if (action != null) {
                  query.append(" AND action = ?");
                  params.add(action);
               }

               if (rate != null) {
                  query.append(" AND rate = ?");
                  params.add(rate);
               }

               if (coins != null) {
                  query.append(" AND coins = ?");
                  params.add(coins);
               }

               if (date != null) {
                  String[] dateParts = date.split(":");
                  if (dateParts.length == 3) {
                     query.append(" AND DATE(time) = ?");
                     params.add(String.format("%s-%s-%s", dateParts[2], dateParts[1], dateParts[0]));
                  }
               }

               query.append(" ORDER BY time ASC LIMIT 100");
               PreparedStatement statement = connection.prepareStatement(query.toString());

               for (int i = 0; i < params.size(); i++) {
                  statement.setObject(i + 1, params.get(i));
               }

               ResultSet resultSet = statement.executeQuery();

               while (resultSet.next()) {
                  transactions.add(
                     new Logs.Transaction(
                        resultSet.getInt("id"),
                        resultSet.getString("player_name"),
                        resultSet.getString("target"),
                        resultSet.getTimestamp("time").getTime(),
                        resultSet.getString("action"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("rate")
                     )
                  );
               }
            } catch (SQLException var16) {
               var16.printStackTrace();
            }

            return transactions;
         }
      );
   }

   public static class Transaction {
      private int id;
      private String playerName;
      private String target;
      private long time;
      private String action;
      private int coins;
      private int rate;

      public Transaction(int id, String playerName, String target, long time, String action, int coins, int rate) {
         this.id = id;
         this.playerName = playerName;
         this.target = target;
         this.time = time;
         this.action = action;
         this.coins = coins;
         this.rate = rate;
      }

      public int getId() {
         return this.id;
      }

      public String getPlayerName() {
         return this.playerName;
      }

      public String getTarget() {
         return this.target;
      }

      public long getTime() {
         return this.time;
      }

      public String getAction() {
         return this.action;
      }

      public int getCoins() {
         return this.coins;
      }

      public int getRate() {
         return this.rate;
      }
   }
}
