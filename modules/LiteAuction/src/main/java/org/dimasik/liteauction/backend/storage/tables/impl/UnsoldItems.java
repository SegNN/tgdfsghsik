package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;

public class UnsoldItems extends AbstractTable {
   public UnsoldItems(HikariDataSource dataSource) {
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
                  String sql = "CREATE TABLE IF NOT EXISTS unsold_items (id INT AUTO_INCREMENT PRIMARY KEY, player VARCHAR(16) NOT NULL, itemstack TEXT NOT NULL, tags TEXT, price INT NOT NULL, amount INT NOT NULL, by_one BOOLEAN NOT NULL, create_time BIGINT NOT NULL)";
                  sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);
                  statement.execute(sql);
               }
            } catch (SQLException var9) {
               throw new RuntimeException(var9);
            }
         }
      );
   }

   public CompletableFuture<Void> addItem(String player, String itemStack, Set<String> tags, int price, int amount, boolean byOne) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO unsold_items (player, itemstack, tags, amount, price, by_one, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)"
                  );
               ) {
                  statement.setString(1, player);
                  statement.setString(2, itemStack);
                  statement.setString(3, String.join(",", tags));
                  statement.setInt(4, amount);
                  statement.setInt(5, price);
                  statement.setBoolean(6, byOne);
                  statement.setLong(7, System.currentTimeMillis());
                  statement.executeUpdate();
               }
            } catch (SQLException var15) {
               throw new RuntimeException(var15);
            }
         }
      );
   }

   public CompletableFuture<List<UnsoldItem>> getAllItems(int page, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<UnsoldItem> items = new ArrayList<>();

         try {
            Object var7;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM unsold_items ORDER BY id ASC LIMIT ? OFFSET ?");
            ) {
               statement.setInt(1, pageSize);
               statement.setInt(2, (page - 1) * pageSize);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     items.add(this.extractUnsoldItemFromResultSet(rs));
                  }

                  var7 = items;
               }
            }

            return (List<UnsoldItem>)var7;
         } catch (SQLException var15) {
            throw new RuntimeException(var15);
         }
      });
   }

   public CompletableFuture<Integer> getAllItemsCount() {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var4;
            try (
               Connection connection = this.dataSource.getConnection();
               Statement statement = connection.createStatement();
               ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM unsold_items");
            ) {
               if (rs.next()) {
                  return rs.getInt(1);
               }

               var4 = 0;
            }

            return var4;
         } catch (SQLException var12) {
            throw new RuntimeException(var12);
         }
      });
   }

   public CompletableFuture<Optional<UnsoldItem>> getItemById(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM unsold_items WHERE id = ?");
            ) {
               statement.setInt(1, id);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractUnsoldItemFromResultSet(rs));
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

   public CompletableFuture<List<UnsoldItem>> getAllPlayerItems(String player) {
      return CompletableFuture.supplyAsync(() -> {
         List<UnsoldItem> items = new ArrayList<>();

         try {
            Object var6;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM unsold_items WHERE player = ?");
            ) {
               statement.setString(1, player);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     items.add(this.extractUnsoldItemFromResultSet(rs));
                  }

                  var6 = items;
               }
            }

            return (List<UnsoldItem>)var6;
         } catch (SQLException var14) {
            throw new RuntimeException(var14);
         }
      });
   }

   public CompletableFuture<List<UnsoldItem>> getPlayerItems(String player, int page, int pageSize) {
      return CompletableFuture.supplyAsync(
         () -> {
            List<UnsoldItem> items = new ArrayList<>();

            try {
               Object var8;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM unsold_items WHERE player = ? ORDER BY create_time DESC LIMIT ? OFFSET ?"
                  );
               ) {
                  statement.setString(1, player);
                  statement.setInt(2, pageSize);
                  statement.setInt(3, (page - 1) * pageSize);

                  try (ResultSet rs = statement.executeQuery()) {
                     while (rs.next()) {
                        items.add(this.extractUnsoldItemFromResultSet(rs));
                     }

                     var8 = items;
                  }
               }

               return (List<UnsoldItem>)var8;
            } catch (SQLException var16) {
               throw new RuntimeException(var16);
            }
         }
      );
   }

   public CompletableFuture<Integer> getPlayerItemsCount(String player) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM unsold_items WHERE player = ?");
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

   public CompletableFuture<Integer> deleteExpiredItems() {
      return CompletableFuture.supplyAsync(() -> {
         long sevenDaysAgo = System.currentTimeMillis() - ConfigManager.getLong("settings/settings.yml", "lifetime.unsold", 604800L) * 1000L;

         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM unsold_items WHERE create_time < ?");
            ) {
               statement.setLong(1, sevenDaysAgo);
               var5 = statement.executeUpdate();
            }

            return var5;
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> updateItem(UnsoldItem item) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "UPDATE unsold_items SET player = ?, itemstack = ?, tags = ?, amount = ?, price = ?, by_one = ?, create_time = ? WHERE id = ?"
                  );
               ) {
                  statement.setString(1, item.getPlayer());
                  statement.setString(2, item.getItemStack());
                  statement.setString(3, String.join(",", item.getTags()));
                  statement.setInt(4, item.getAmount());
                  statement.setInt(5, item.getPrice());
                  statement.setBoolean(6, item.isByOne());
                  statement.setLong(7, item.getCreateTime());
                  statement.setInt(8, item.getId());
                  statement.executeUpdate();
               }
            } catch (SQLException var10) {
               throw new RuntimeException(var10);
            }
         }
      );
   }

   public CompletableFuture<Void> deleteItem(int id) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM unsold_items WHERE id = ?");
            ) {
               statement.setInt(1, id);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Void> resellItems(String player) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (Connection connection = this.dataSource.getConnection()) {
                  connection.setAutoCommit(false);

                  try (
                     PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM unsold_items WHERE player = ?");
                     PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO sell_items (player, itemstack, tags, price, amount, by_one, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)", 1
                     );
                     PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM unsold_items WHERE id = ?");
                  ) {
                     selectStatement.setString(1, player);

                     try (ResultSet rs = selectStatement.executeQuery()) {
                        while (rs.next()) {
                           UnsoldItem unsoldItem = this.extractUnsoldItemFromResultSet(rs);
                           insertStatement.setString(1, unsoldItem.getPlayer());
                           insertStatement.setString(2, unsoldItem.getItemStack());
                           insertStatement.setString(3, String.join(",", unsoldItem.getTags()));
                           insertStatement.setInt(4, unsoldItem.getPrice());
                           insertStatement.setInt(5, unsoldItem.getAmount());
                           insertStatement.setBoolean(6, unsoldItem.isByOne());
                           insertStatement.setLong(7, System.currentTimeMillis());
                           insertStatement.addBatch();
                           deleteStatement.setInt(1, unsoldItem.getId());
                           deleteStatement.addBatch();
                        }

                        insertStatement.executeBatch();
                        deleteStatement.executeBatch();
                        connection.commit();
                     }
                  } catch (SQLException var18) {
                     connection.rollback();
                     throw var18;
                  }
               }
            } catch (SQLException var20) {
               throw new RuntimeException(var20);
            }
         }
      );
   }

   private UnsoldItem extractUnsoldItemFromResultSet(ResultSet rs) throws SQLException {
      Set<String> tags = new HashSet<>();
      String tagsStr = rs.getString("tags");
      if (tagsStr != null && !tagsStr.isEmpty()) {
         tags.addAll(Arrays.asList(tagsStr.split(",")));
      }

      return new UnsoldItem(
         rs.getInt("id"),
         rs.getString("player"),
         rs.getString("itemstack"),
         tags,
         rs.getInt("price"),
         rs.getInt("amount"),
         rs.getBoolean("by_one"),
         rs.getLong("create_time")
      );
   }
}
