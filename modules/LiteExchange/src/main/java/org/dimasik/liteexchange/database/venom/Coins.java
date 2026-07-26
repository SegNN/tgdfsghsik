package org.dimasik.liteexchange.database.venom;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Coins {
   private HikariDataSource dataSource;
   private String tablePrefix;

   public Coins(HikariDataSource dataSource, String prefix) {
      this.dataSource = dataSource;
      this.tablePrefix = prefix;
   }

   public CompletableFuture<Integer> getBalance(String playerName) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT value FROM " + this.tablePrefix + "coins WHERE player_name = ?");
            ) {
               statement.setString(1, playerName.toLowerCase());
               ResultSet resultSet = statement.executeQuery();
               if (!resultSet.next()) {
                  return 0;
               }

               int balance = resultSet.getInt("value");
               var5 = Math.max(0, balance);
            }

            return var5;
         } catch (SQLException var11) {
            var11.printStackTrace();
            return 0;
         }
      });
   }

   public CompletableFuture<Void> setBalance(String playerName, int amount) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               int safeAmount = Math.max(0, amount);

               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + this.tablePrefix + "coins (player_name, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?"
                  );
               ) {
                  statement.setString(1, playerName.toLowerCase());
                  statement.setInt(2, safeAmount);
                  statement.setInt(3, safeAmount);
                  statement.executeUpdate();
               }
            } catch (SQLException var12) {
               var12.printStackTrace();
            }
         }
      );
   }

   public CompletableFuture<Void> addBalance(String playerName, int amount) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               int safeAmount = Math.max(0, amount);

               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO " + this.tablePrefix + "coins (player_name, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = GREATEST(0, value + ?)"
                  );
               ) {
                  statement.setString(1, playerName.toLowerCase());
                  statement.setInt(2, safeAmount);
                  statement.setInt(3, safeAmount);
                  statement.executeUpdate();
               }
            } catch (SQLException var12) {
               var12.printStackTrace();
            }
         }
      );
   }

   public CompletableFuture<Boolean> takeBalance(String playerName, int amount) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               try (Connection connection = this.dataSource.getConnection()) {
                  PreparedStatement checkStmt = connection.prepareStatement("SELECT value FROM " + this.tablePrefix + "coins WHERE player_name = ?");
                  int currentBalance = 0;

                  try {
                     checkStmt.setString(1, playerName.toLowerCase());
                     ResultSet rs = checkStmt.executeQuery();
                     if (rs.next()) {
                        currentBalance = rs.getInt("value");
                     }
                  } finally {
                     checkStmt.close();
                  }

                  if (currentBalance < amount) {
                     if (connection != null) {
                        connection.close();
                     }

                     return false;
                  }

                  try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE " + this.tablePrefix + "coins SET value = GREATEST(0, value - ?) WHERE player_name = ?"
                     )) {
                     statement.setInt(1, amount);
                     statement.setString(2, playerName.toLowerCase());
                     statement.executeUpdate();
                  }
               }

               return true;
            } catch (SQLException var20) {
               var20.printStackTrace();
               return false;
            }
         }
      );
   }
}
