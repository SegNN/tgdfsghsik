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
import org.dimasik.liteauction.backend.storage.models.Bid;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;

public class Bids extends AbstractTable {
   public Bids(HikariDataSource dataSource) {
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
                  String sql = "CREATE TABLE IF NOT EXISTS bids (id INT AUTO_INCREMENT PRIMARY KEY, item_id INT NOT NULL, player VARCHAR(16) NOT NULL, price INT NOT NULL, FOREIGN KEY (item_id) REFERENCES bid_items(id) ON DELETE CASCADE)";
                  sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);
                  statement.execute(sql);
               }
            } catch (SQLException var9) {
               throw new RuntimeException(var9);
            }
         }
      );
   }

   public CompletableFuture<Integer> addBid(int itemId, String player, int price) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var8;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("INSERT INTO bids (item_id, player, price) VALUES (?, ?, ?)", 1);
            ) {
               statement.setInt(1, itemId);
               statement.setString(2, player);
               statement.setInt(3, price);
               int affectedRows = statement.executeUpdate();
               if (affectedRows == 0) {
                  throw new SQLException("Creating bid failed, no rows affected.");
               }

               try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                  if (!generatedKeys.next()) {
                     throw new SQLException("Creating bid failed, no ID obtained.");
                  }

                  var8 = generatedKeys.getInt(1);
               }
            }

            return var8;
         } catch (SQLException var16) {
            throw new RuntimeException(var16);
         }
      });
   }

   public CompletableFuture<Optional<Bid>> getBidById(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM bids WHERE id = ?");
            ) {
               statement.setInt(1, id);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractBidFromResultSet(rs));
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

   public CompletableFuture<List<Bid>> getBidsByItemId(int itemId, int beginIndex, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<Bid> bids = new ArrayList<>();

         try {
            Object var8;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM bids WHERE item_id = ? ORDER BY price ASC LIMIT ? OFFSET ?");
            ) {
               statement.setInt(1, itemId);
               statement.setInt(2, pageSize);
               statement.setInt(3, beginIndex);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     bids.add(this.extractBidFromResultSet(rs));
                  }

                  var8 = bids;
               }
            }

            return (List<Bid>)var8;
         } catch (SQLException var16) {
            throw new RuntimeException(var16);
         }
      });
   }

   public CompletableFuture<Integer> getBidsByItemIdCount(int itemId) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM bids WHERE item_id = ?");
            ) {
               statement.setInt(1, itemId);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return rs.getInt(1);
                  }

                  var5 = 0;
               }
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException(var13);
         }
      });
   }

   public CompletableFuture<List<Bid>> getPlayerBids(String player, int page, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<Bid> bids = new ArrayList<>();

         try {
            Object var8;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM bids WHERE player = ? ORDER BY id DESC LIMIT ? OFFSET ?");
            ) {
               statement.setString(1, player);
               statement.setInt(2, pageSize);
               statement.setInt(3, (page - 1) * pageSize);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     bids.add(this.extractBidFromResultSet(rs));
                  }

                  var8 = bids;
               }
            }

            return (List<Bid>)var8;
         } catch (SQLException var16) {
            throw new RuntimeException(var16);
         }
      });
   }

   public CompletableFuture<Integer> getPlayerBidsCount(String player) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM bids WHERE player = ?");
            ) {
               statement.setString(1, player);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return rs.getInt(1);
                  }

                  var5 = 0;
               }
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException(var13);
         }
      });
   }

   public CompletableFuture<Optional<Bid>> getHighestBidForItem(int itemId) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM bids WHERE item_id = ? ORDER BY price DESC LIMIT 1");
            ) {
               statement.setInt(1, itemId);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractBidFromResultSet(rs));
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

   public CompletableFuture<Integer> getBidCountForItem(int itemId) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM bids WHERE item_id = ?");
            ) {
               statement.setInt(1, itemId);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return rs.getInt(1);
                  }

                  var5 = 0;
               }
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException(var13);
         }
      });
   }

   public CompletableFuture<Void> updateBid(Bid bid) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE bids SET item_id = ?, player = ?, price = ? WHERE id = ?");
            ) {
               statement.setInt(1, bid.getItemId());
               statement.setString(2, bid.getPlayer());
               statement.setInt(3, bid.getPrice());
               statement.setInt(4, bid.getId());
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Void> deleteBid(int id) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM bids WHERE id = ?");
            ) {
               statement.setInt(1, id);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Void> deleteBidsByItemId(int itemId) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM bids WHERE item_id = ?");
            ) {
               statement.setInt(1, itemId);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   private Bid extractBidFromResultSet(ResultSet rs) throws SQLException {
      return new Bid(rs.getInt("id"), rs.getInt("item_id"), rs.getString("player"), rs.getInt("price"));
   }
}
