package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.HistoryItem;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;

public class HistoryItems extends AbstractTable {
   public HistoryItems(HikariDataSource dataSource) {
      super(dataSource);
   }

   @Override
   public CompletableFuture<Void> createTable() {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  Statement statement = connection.createStatement();
               ) {
                  String sql = "CREATE TABLE IF NOT EXISTS history (id INT AUTO_INCREMENT PRIMARY KEY, player VARCHAR(16) NOT NULL, buyer VARCHAR(16), itemStack TEXT NOT NULL, amount INT NOT NULL, price INT NOT NULL, time BIGINT NOT NULL, displayed BOOLEAN DEFAULT FALSE)";
                  sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);
                  statement.execute(sql);
               }
            } catch (SQLException var9) {
               throw new RuntimeException(var9);
            }
         }
      );
   }

   public CompletableFuture<Integer> addHistory(String player, String buyer, String itemStack, int amount, int price) {
      return CompletableFuture.supplyAsync(
         () -> {
            long currentTime = System.currentTimeMillis();

            try {
               Integer var12;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO history (player, buyer, itemStack, amount, price, time, displayed) VALUES (?, ?, ?, ?, ?, ?, ?)", 1
                  );
               ) {
                  statement.setString(1, player);
                  statement.setString(2, buyer);
                  statement.setString(3, itemStack);
                  statement.setInt(4, amount);
                  statement.setInt(5, price);
                  statement.setLong(6, currentTime);
                  statement.setBoolean(7, false);
                  int affectedRows = statement.executeUpdate();
                  if (affectedRows == 0) {
                     throw new SQLException("Creating history record failed, no rows affected.");
                  }

                  try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                     if (!generatedKeys.next()) {
                        throw new SQLException("Creating history record failed, no ID obtained.");
                     }

                     var12 = generatedKeys.getInt(1);
                  }
               }

               return var12;
            } catch (SQLException var20) {
               throw new RuntimeException(var20);
            }
         }
      );
   }

   public CompletableFuture<List<HistoryItem>> getPlayerHistory(String player, boolean onlyNotDisplayed) {
      return CompletableFuture.supplyAsync(() -> {
         List<HistoryItem> history = new ArrayList<>();
         String sql = "SELECT * FROM history WHERE player = ?";
         if (onlyNotDisplayed) {
            sql = sql + " AND displayed = FALSE";
         }

         sql = sql + " ORDER BY time ASC";

         try {
            Object var8;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement(sql);
            ) {
               statement.setString(1, player);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     history.add(this.extractHistoryItemFromResultSet(rs));
                  }

                  var8 = history;
               }
            }

            return (List<HistoryItem>)var8;
         } catch (SQLException var16) {
            throw new RuntimeException(var16);
         }
      });
   }

   public CompletableFuture<Void> setDisplayed(int id, boolean displayed) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE history SET displayed = ? WHERE id = ?");
            ) {
               statement.setBoolean(1, displayed);
               statement.setInt(2, id);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> deleteById(int id) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM history WHERE id = ?");
            ) {
               statement.setInt(1, id);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Optional<HistoryItem>> getById(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM history WHERE id = ?");
            ) {
               statement.setInt(1, id);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractHistoryItemFromResultSet(rs));
                  }

                  var5 = Optional.empty();
               }
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException(var13);
         }
      });
   }

   private HistoryItem extractHistoryItemFromResultSet(ResultSet rs) throws SQLException {
      return new HistoryItem(
         rs.getInt("id"),
         rs.getString("player"),
         rs.getString("buyer"),
         rs.getString("itemStack"),
         rs.getInt("amount"),
         rs.getInt("price"),
         rs.getLong("time"),
         rs.getBoolean("displayed")
      );
   }
}
