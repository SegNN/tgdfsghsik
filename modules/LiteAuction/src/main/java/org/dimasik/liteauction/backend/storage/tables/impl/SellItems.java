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
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;

public class SellItems extends AbstractTable {
   public SellItems(HikariDataSource dataSource) {
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
                  String sql = "CREATE TABLE IF NOT EXISTS sell_items (id INT AUTO_INCREMENT PRIMARY KEY, player VARCHAR(16) NOT NULL, itemstack TEXT NOT NULL, tags TEXT, price INT NOT NULL, amount INT NOT NULL, by_one BOOLEAN NOT NULL, create_time BIGINT NOT NULL)";
                  sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);
                  statement.execute(sql);
               }
            } catch (SQLException var9) {
               throw new RuntimeException(var9);
            }
         }
      );
   }

   public CompletableFuture<Integer> addItem(String player, String itemStack, Set<String> tags, int price, int amount, boolean byOne) {
      return CompletableFuture.supplyAsync(
         () -> {
            long createTime = System.currentTimeMillis();

            try {
               Integer var13;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO sell_items (player, itemstack, tags, price, amount, by_one, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)", 1
                  );
               ) {
                  statement.setString(1, player);
                  statement.setString(2, itemStack);
                  statement.setString(3, String.join(",", tags));
                  statement.setInt(4, price);
                  statement.setInt(5, amount);
                  statement.setBoolean(6, byOne);
                  statement.setLong(7, createTime);
                  int affectedRows = statement.executeUpdate();
                  if (affectedRows == 0) {
                     throw new SQLException("Creating item failed, no rows affected.");
                  }

                  try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                     if (!generatedKeys.next()) {
                        throw new SQLException("Creating item failed, no ID obtained.");
                     }

                     var13 = generatedKeys.getInt(1);
                  }
               }

               return var13;
            } catch (SQLException var21) {
               throw new RuntimeException(var21);
            }
         }
      );
   }

   public CompletableFuture<List<SellItem>> getAllItems(int page, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<SellItem> items = new ArrayList<>();

         try {
            Object var7;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM sell_items ORDER BY id ASC LIMIT ? OFFSET ?");
            ) {
               statement.setInt(1, pageSize);
               statement.setInt(2, (page - 1) * pageSize);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     items.add(this.extractSellItemFromResultSet(rs));
                  }

                  var7 = items;
               }
            }

            return (List<SellItem>)var7;
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
               ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM sell_items");
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

   public CompletableFuture<Optional<SellItem>> getItemById(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM sell_items WHERE id = ?");
            ) {
               statement.setInt(1, id);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractSellItemFromResultSet(rs));
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

   public CompletableFuture<List<SellItem>> getPlayerItems(String player, int page, int pageSize) {
      return CompletableFuture.supplyAsync(
         () -> {
            List<SellItem> items = new ArrayList<>();

            try {
               Object var8;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM sell_items WHERE player = ? ORDER BY create_time DESC LIMIT ? OFFSET ?"
                  );
               ) {
                  statement.setString(1, player);
                  statement.setInt(2, pageSize);
                  statement.setInt(3, (page - 1) * pageSize);

                  try (ResultSet rs = statement.executeQuery()) {
                     while (rs.next()) {
                        items.add(this.extractSellItemFromResultSet(rs));
                     }

                     var8 = items;
                  }
               }

               return (List<SellItem>)var8;
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
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM sell_items WHERE player = ?");
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

   public CompletableFuture<List<SellItem>> getExpiredPlayerItems(String player, int page, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<SellItem> items = new ArrayList<>();
         long twelveHoursAgo = System.currentTimeMillis() - ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200L) * 1000L;

         try {
            Object var10;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM sell_items WHERE player = ? AND create_time < ? LIMIT ? OFFSET ?");
            ) {
               statement.setString(1, player);
               statement.setLong(2, twelveHoursAgo);
               statement.setInt(3, pageSize);
               statement.setInt(4, (page - 1) * pageSize);

               try (ResultSet rs = statement.executeQuery()) {
                  while (rs.next()) {
                     items.add(this.extractSellItemFromResultSet(rs));
                  }

                  var10 = items;
               }
            }

            return (List<SellItem>)var10;
         } catch (SQLException var18) {
            throw new RuntimeException(var18);
         }
      });
   }

   public CompletableFuture<Integer> getExpiredPlayerItemsCount(String player) {
      return CompletableFuture.supplyAsync(() -> {
         long twelveHoursAgo = System.currentTimeMillis() - ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200L) * 1000L;

         try {
            Integer var7;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM sell_items WHERE player = ? AND create_time < ?");
            ) {
               statement.setString(1, player);
               statement.setLong(2, twelveHoursAgo);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return rs.getInt(1);
                  }

                  var7 = 0;
               }
            }

            return var7;
         } catch (SQLException var15) {
            throw new RuntimeException(var15);
         }
      });
   }

   public CompletableFuture<List<SellItem>> getItems(MarketSortingType sortingType, Set<String> filters, int page, int pageSize) {
      return CompletableFuture.supplyAsync(() -> {
         List<SellItem> items = new ArrayList<>();

         try {
            Object var22;
            try (Connection connection = this.dataSource.getConnection()) {
               String sql = this.buildQuery(sortingType, filters) + " LIMIT ? OFFSET ?";

               try (PreparedStatement statement = connection.prepareStatement(sql)) {
                  int paramIndex = 1;
                  if (filters != null && !filters.isEmpty()) {
                     for (String filter : filters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  statement.setInt(paramIndex++, pageSize);
                  statement.setInt(paramIndex, (page - 1) * pageSize);

                  try (ResultSet rs = statement.executeQuery()) {
                     while (rs.next()) {
                        items.add(this.extractSellItemFromResultSet(rs));
                     }

                     var22 = items;
                  }
               }
            }

            return (List<SellItem>)var22;
         } catch (SQLException var19) {
            throw new RuntimeException(var19);
         }
      });
   }

   public CompletableFuture<Integer> getItemsCount(MarketSortingType sortingType, Set<String> filters) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var16;
            try (Connection connection = this.dataSource.getConnection()) {
               String countSql = this.buildCountQuery(filters);

               try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                  if (filters != null && !filters.isEmpty()) {
                     int paramIndex = 1;

                     for (String filter : filters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  try (ResultSet rs = statement.executeQuery()) {
                     if (rs.next()) {
                        return rs.getInt(1);
                     }

                     var16 = 0;
                  }
               }
            }

            return var16;
         } catch (SQLException var14) {
            throw new RuntimeException(var14);
         }
      });
   }

   public CompletableFuture<List<SellItem>> getItems(
      MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter, int page, int pageSize
   ) {
      return CompletableFuture.supplyAsync(() -> {
         List<SellItem> items = new ArrayList<>();

         try {
            Object var25;
            try (Connection connection = this.dataSource.getConnection()) {
               String sql = this.buildQuery(sortingType, additionalFilters, categoryFilter) + " LIMIT ? OFFSET ?";

               try (PreparedStatement statement = connection.prepareStatement(sql)) {
                  int paramIndex = 1;
                  if (categoryFilter != CategoryType.ALL) {
                     for (String tag : categoryFilter.getTags()) {
                        statement.setString(paramIndex++, "%" + tag + "%");
                     }
                  }

                  if (additionalFilters != null && !additionalFilters.isEmpty()) {
                     for (String filter : additionalFilters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  statement.setInt(paramIndex++, pageSize);
                  statement.setInt(paramIndex, (page - 1) * pageSize);

                  try (ResultSet rs = statement.executeQuery()) {
                     while (rs.next()) {
                        items.add(this.extractSellItemFromResultSet(rs));
                     }

                     var25 = items;
                  }
               }
            }

            return (List<SellItem>)var25;
         } catch (SQLException var20) {
            throw new RuntimeException(var20);
         }
      });
   }

   public CompletableFuture<Integer> getItemsCount(MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Integer var20;
            try (Connection connection = this.dataSource.getConnection()) {
               String countSql = this.buildCountQuery(additionalFilters, categoryFilter);

               try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                  int paramIndex = 1;
                  if (categoryFilter != CategoryType.ALL) {
                     for (String tag : categoryFilter.getTags()) {
                        statement.setString(paramIndex++, "%" + tag + "%");
                     }
                  }

                  if (additionalFilters != null && !additionalFilters.isEmpty()) {
                     for (String filter : additionalFilters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  try (ResultSet rs = statement.executeQuery()) {
                     if (rs.next()) {
                        return rs.getInt(1);
                     }

                     var20 = 0;
                  }
               }
            }

            return var20;
         } catch (SQLException var16) {
            throw new RuntimeException(var16);
         }
      });
   }

   public CompletableFuture<List<SellItem>> getItems(
      String owner, MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter, int page, int pageSize
   ) {
      return owner == null ? this.getItems(sortingType, additionalFilters, categoryFilter, page, pageSize) : CompletableFuture.supplyAsync(() -> {
         List<SellItem> items = new ArrayList<>();

         try {
            Object var27;
            try (Connection connection = this.dataSource.getConnection()) {
               String sql = this.buildOwnerQuery(owner, sortingType, additionalFilters, categoryFilter) + " LIMIT ? OFFSET ?";

               try (PreparedStatement statement = connection.prepareStatement(sql)) {
                  int paramIndex = 1;
                  statement.setString(paramIndex++, owner);
                  if (categoryFilter != CategoryType.ALL) {
                     for (String tag : categoryFilter.getTags()) {
                        statement.setString(paramIndex++, "%" + tag + "%");
                     }
                  }

                  if (additionalFilters != null && !additionalFilters.isEmpty()) {
                     for (String filter : additionalFilters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  statement.setInt(paramIndex++, pageSize);
                  statement.setInt(paramIndex, (page - 1) * pageSize);

                  try (ResultSet rs = statement.executeQuery()) {
                     while (rs.next()) {
                        items.add(this.extractSellItemFromResultSet(rs));
                     }

                     var27 = items;
                  }
               }
            }

            return (List<SellItem>)var27;
         } catch (SQLException var21) {
            throw new RuntimeException(var21);
         }
      });
   }

   public CompletableFuture<Integer> getItemsCount(String owner, MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter) {
      return owner == null ? this.getItemsCount(sortingType, additionalFilters, categoryFilter) : CompletableFuture.supplyAsync(() -> {
         try {
            Integer var22;
            try (Connection connection = this.dataSource.getConnection()) {
               String countSql = this.buildOwnerCountQuery(owner, additionalFilters, categoryFilter);

               try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                  int paramIndex = 1;
                  statement.setString(paramIndex++, owner);
                  if (categoryFilter != CategoryType.ALL) {
                     for (String tag : categoryFilter.getTags()) {
                        statement.setString(paramIndex++, "%" + tag + "%");
                     }
                  }

                  if (additionalFilters != null && !additionalFilters.isEmpty()) {
                     for (String filter : additionalFilters) {
                        statement.setString(paramIndex++, "%" + filter + "%");
                     }
                  }

                  try (ResultSet rs = statement.executeQuery()) {
                     if (rs.next()) {
                        return rs.getInt(1);
                     }

                     var22 = 0;
                  }
               }
            }

            return var22;
         } catch (SQLException var17) {
            throw new RuntimeException(var17);
         }
      });
   }

   private String buildQuery(MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter) {
      StringBuilder sql = new StringBuilder("SELECT * FROM sell_items");
      List<String> conditions = new ArrayList<>();
      if (categoryFilter != CategoryType.ALL) {
         StringBuilder categoryCondition = new StringBuilder("(");
         List<String> tagConditions = new ArrayList<>();

         for (String tag : categoryFilter.getTags()) {
            tagConditions.add("tags LIKE ?");
         }

         categoryCondition.append(String.join(" OR ", tagConditions));
         categoryCondition.append(")");
         conditions.add(categoryCondition.toString());
      }

      if (additionalFilters != null && !additionalFilters.isEmpty()) {
         StringBuilder filtersCondition = new StringBuilder("(");
         List<String> filterConditions = new ArrayList<>();

         for (String filter : additionalFilters) {
            filterConditions.add("tags LIKE ?");
         }

         filtersCondition.append(String.join(" AND ", filterConditions));
         filtersCondition.append(")");
         conditions.add(filtersCondition.toString());
      }

      if (!conditions.isEmpty()) {
         sql.append(" WHERE ");
         sql.append(String.join(" AND ", conditions));
      }

      sql.append(this.getOrderByClause(sortingType));
      return sql.toString();
   }

   private String buildOwnerQuery(String owner, MarketSortingType sortingType, Set<String> additionalFilters, CategoryType categoryFilter) {
      StringBuilder sql = new StringBuilder("SELECT * FROM sell_items WHERE player = ?");
      if (categoryFilter != CategoryType.ALL) {
         sql.append(" AND (");

         for (int i = 0; i < categoryFilter.getTags().size(); i++) {
            if (i > 0) {
               sql.append(" OR ");
            }

            sql.append("tags LIKE ?");
         }

         sql.append(")");
      }

      if (additionalFilters != null && !additionalFilters.isEmpty()) {
         sql.append(" AND (");

         for (int i = 0; i < additionalFilters.size(); i++) {
            if (i > 0) {
               sql.append(" AND ");
            }

            sql.append("tags LIKE ?");
         }

         sql.append(")");
      }

      sql.append(this.getOrderByClause(sortingType));
      return sql.toString();
   }

   private String buildCountQuery(Set<String> filters) {
      StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items");
      if (filters != null && !filters.isEmpty()) {
         sql.append(" WHERE ");

         for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
               sql.append(" AND ");
            }

            sql.append("tags LIKE ?");
         }
      }

      return sql.toString();
   }

   private String buildCountQuery(Set<String> additionalFilters, CategoryType categoryFilter) {
      StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items");
      List<String> conditions = new ArrayList<>();
      if (categoryFilter != CategoryType.ALL) {
         StringBuilder categoryCondition = new StringBuilder("(");
         List<String> tagConditions = new ArrayList<>();

         for (String tag : categoryFilter.getTags()) {
            tagConditions.add("tags LIKE ?");
         }

         categoryCondition.append(String.join(" OR ", tagConditions));
         categoryCondition.append(")");
         conditions.add(categoryCondition.toString());
      }

      if (additionalFilters != null && !additionalFilters.isEmpty()) {
         StringBuilder filtersCondition = new StringBuilder("(");
         List<String> filterConditions = new ArrayList<>();

         for (String filter : additionalFilters) {
            filterConditions.add("tags LIKE ?");
         }

         filtersCondition.append(String.join(" AND ", filterConditions));
         filtersCondition.append(")");
         conditions.add(filtersCondition.toString());
      }

      if (!conditions.isEmpty()) {
         sql.append(" WHERE ");
         sql.append(String.join(" AND ", conditions));
      }

      return sql.toString();
   }

   private String buildOwnerCountQuery(String owner, Set<String> additionalFilters, CategoryType categoryFilter) {
      StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items WHERE player = ?");
      if (categoryFilter != CategoryType.ALL) {
         sql.append(" AND (");

         for (int i = 0; i < categoryFilter.getTags().size(); i++) {
            if (i > 0) {
               sql.append(" OR ");
            }

            sql.append("tags LIKE ?");
         }

         sql.append(")");
      }

      if (additionalFilters != null && !additionalFilters.isEmpty()) {
         sql.append(" AND (");

         for (int i = 0; i < additionalFilters.size(); i++) {
            if (i > 0) {
               sql.append(" AND ");
            }

            sql.append("tags LIKE ?");
         }

         sql.append(")");
      }

      return sql.toString();
   }

   private String getOrderByClause(MarketSortingType sortingType) {
      return " ORDER BY " + switch (sortingType) {
         case CHEAPEST_FIRST -> "(price * amount) ASC";
         case EXPENSIVE_FIRST -> "(price * amount) DESC";
         case CHEAPEST_PER_UNIT -> "price ASC";
         case EXPENSIVE_PER_UNIT -> "price DESC";
         case NEWEST_FIRST -> "create_time DESC";
         case OLDEST_FIRST -> "create_time ASC";
         default -> "id ASC";
      };
   }

   public CompletableFuture<Void> updateItem(SellItem item) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "UPDATE sell_items SET player = ?, itemstack = ?, tags = ?, price = ?, amount = ?, by_one = ?, create_time = ? WHERE id = ?"
                  );
               ) {
                  statement.setString(1, item.getPlayer());
                  statement.setString(2, item.getItemStack());
                  statement.setString(3, String.join(",", item.getTags()));
                  statement.setInt(4, item.getPrice());
                  statement.setInt(5, item.getAmount());
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

   public CompletableFuture<Optional<SellItem>> getItem(int id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM sell_items WHERE id = ?");
            ) {
               statement.setInt(1, id);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractSellItemFromResultSet(rs));
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

   public CompletableFuture<Void> deleteItem(int id) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM sell_items WHERE id = ?");
            ) {
               statement.setInt(1, id);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Void> moveExpiredItems() {
      return CompletableFuture.runAsync(
         () -> {
            long twelveHoursAgo = System.currentTimeMillis() - ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200L) * 1000L;

            try {
               try (Connection connection = this.dataSource.getConnection()) {
                  connection.setAutoCommit(false);

                  try (
                     PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM sell_items WHERE create_time < ?");
                     PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO unsold_items (player, itemstack, tags, amount, price, by_one, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)"
                     );
                     PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM sell_items WHERE id = ?");
                  ) {
                     selectStatement.setLong(1, twelveHoursAgo);

                     try (ResultSet rs = selectStatement.executeQuery()) {
                        while (rs.next()) {
                           SellItem item = this.extractSellItemFromResultSet(rs);
                           ItemStack itemStack = item.decodeItemStack();
                           LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + item.getId());
                           LiteAuction.getInstance()
                              .getCommunicationManager()
                              .publishMessage(
                                 "hover",
                                 item.getPlayer()
                                    + " "
                                    + ItemHoverUtil.getHoverItemMessage(
                                       Parser.color(
                                          "&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx"
                                             + item.getAmount()
                                             + " &fоказался слишком дорогой или никому не нужен. Заберите предмет с Аукциона!"
                                       ),
                                       itemStack
                                    )
                              );
                           insertStatement.setString(1, item.getPlayer());
                           insertStatement.setString(2, item.getItemStack());
                           insertStatement.setString(3, String.join(",", item.getTags()));
                           insertStatement.setInt(4, item.getAmount());
                           insertStatement.setInt(5, item.getPrice());
                           insertStatement.setBoolean(6, item.isByOne());
                           insertStatement.setLong(7, System.currentTimeMillis());
                           insertStatement.addBatch();
                           deleteStatement.setInt(1, item.getId());
                           deleteStatement.addBatch();
                        }

                        insertStatement.executeBatch();
                        deleteStatement.executeBatch();
                        connection.commit();
                     }
                  } catch (SQLException var19) {
                     connection.rollback();
                     throw var19;
                  }
               }
            } catch (SQLException var21) {
               throw new RuntimeException(var21);
            }
         }
      );
   }

   private String buildQuery(MarketSortingType sortingType, Set<String> filters) {
      StringBuilder sql = new StringBuilder("SELECT * FROM sell_items");
      if (filters != null && !filters.isEmpty()) {
         sql.append(" WHERE ");

         for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
               sql.append(" AND ");
            }

            sql.append("tags LIKE ?");
         }
      }

      sql.append(this.getOrderByClause(sortingType));
      return sql.toString();
   }

   private SellItem extractSellItemFromResultSet(ResultSet rs) throws SQLException {
      Set<String> tags = new HashSet<>();
      String tagsStr = rs.getString("tags");
      if (tagsStr != null && !tagsStr.isEmpty()) {
         tags.addAll(Arrays.asList(tagsStr.split(",")));
      }

      return new SellItem(
         rs.getInt("id"),
         rs.getString("player"),
         rs.getString("itemstack"),
         tags,
         rs.getInt("price"),
         rs.getInt("amount"),
         rs.getBoolean("by_one"),
         rs.getLong("create_time"),
         false
      );
   }
}
