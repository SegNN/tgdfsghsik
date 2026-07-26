package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.storage.models.GuiData;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;

public class GuiDatas extends AbstractTable {
   public GuiDatas(HikariDataSource dataSource) {
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
                  String sql = "CREATE TABLE IF NOT EXISTS gui_datas (id INT AUTO_INCREMENT PRIMARY KEY, player VARCHAR(16) NOT NULL UNIQUE, auction_type VARCHAR(20) NOT NULL, category_type VARCHAR(50) NOT NULL, market_sorting_type VARCHAR(30), bids_sorting_type VARCHAR(30), additional_filters TEXT)";
                  sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);
                  statement.execute(sql);
               }
            } catch (SQLException var9) {
               throw new RuntimeException(var9);
            }
         }
      );
   }

   public CompletableFuture<GuiData> getOrDefault(String player) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               GuiData var5;
               try (Connection connection = this.dataSource.getConnection()) {
                  Optional<GuiData> existingData = this.getGuiDataByPlayer(player).join();
                  if (existingData.isPresent()) {
                     return existingData.get();
                  }

                  GuiData defaultGuiData = new GuiData(
                     0, player, AuctionType.MARKET, CategoryType.ALL, MarketSortingType.CHEAPEST_FIRST, BidsSortingType.CHEAPEST_FIRST, new HashSet<>()
                  );
                  this.saveGuiData(defaultGuiData).join();
                  var5 = this.getGuiDataByPlayer(player).join().orElse(defaultGuiData);
               }

               return var5;
            } catch (Exception var8) {
               throw new RuntimeException(var8);
            }
         }
      );
   }

   public CompletableFuture<Void> saveGuiData(GuiData guiData) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO gui_datas (player, auction_type, category_type, market_sorting_type, bids_sorting_type, additional_filters) VALUES (?, ?, ?, ?, ?, ?)",
                     1
                  );
               ) {
                  this.setGuiDataParameters(statement, guiData);
                  statement.executeUpdate();

                  try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                        guiData.setId(generatedKeys.getInt(1));
                     }
                  }
               }
            } catch (SQLException var13) {
               throw new RuntimeException(var13);
            }
         }
      );
   }

   public CompletableFuture<Void> saveOrUpdateGuiData(GuiData guiData) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (Connection connection = this.dataSource.getConnection()) {
               Optional<GuiData> existingData = this.getGuiDataByPlayer(guiData.getPlayer()).join();
               if (existingData.isPresent()) {
                  this.updateGuiData(guiData).join();
               } else {
                  this.saveGuiData(guiData).join();
               }
            }
         } catch (SQLException var7) {
            throw new RuntimeException(var7);
         }
      });
   }

   private CompletableFuture<Void> updateGuiData(GuiData guiData) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "UPDATE gui_datas SET auction_type = ?, category_type = ?, market_sorting_type = ?, bids_sorting_type = ?, additional_filters = ? WHERE player = ?"
                  );
               ) {
                  statement.setString(1, guiData.getAuctionType().name());
                  statement.setString(2, guiData.getCategoryType().name());
                  if (guiData.getMarketSortingType() != null) {
                     statement.setString(3, guiData.getMarketSortingType().name());
                  } else {
                     statement.setNull(3, 12);
                  }

                  if (guiData.getBidsSortingType() != null) {
                     statement.setString(4, guiData.getBidsSortingType().name());
                  } else {
                     statement.setNull(4, 12);
                  }

                  if (guiData.getAdditionalFilters() != null && !guiData.getAdditionalFilters().isEmpty()) {
                     statement.setString(5, String.join(",", guiData.getAdditionalFilters()));
                  } else {
                     statement.setNull(5, 12);
                  }

                  statement.setString(6, guiData.getPlayer());
                  statement.executeUpdate();
               }
            } catch (SQLException var10) {
               throw new RuntimeException(var10);
            }
         }
      );
   }

   public CompletableFuture<Optional<GuiData>> getGuiDataByPlayer(String player) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Optional var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM gui_datas WHERE player = ?");
            ) {
               statement.setString(1, player);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return Optional.of(this.extractGuiDataFromResultSet(rs));
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

   public CompletableFuture<Void> deleteGuiData(String player) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("DELETE FROM gui_datas WHERE player = ?");
            ) {
               statement.setString(1, player);
               statement.executeUpdate();
            }
         } catch (SQLException var10) {
            throw new RuntimeException(var10);
         }
      });
   }

   public CompletableFuture<Void> updateAuctionType(String player, AuctionType auctionType) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE gui_datas SET auction_type = ? WHERE player = ?");
            ) {
               statement.setString(1, auctionType.name());
               statement.setString(2, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> updateCategoryType(String player, CategoryType categoryType) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE gui_datas SET category_type = ? WHERE player = ?");
            ) {
               statement.setString(1, categoryType.name());
               statement.setString(2, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> updateMarketSortingType(String player, MarketSortingType sortingType) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE gui_datas SET market_sorting_type = ? WHERE player = ?");
            ) {
               statement.setString(1, sortingType.name());
               statement.setString(2, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> updateBidsSortingType(String player, BidsSortingType sortingType) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE gui_datas SET bids_sorting_type = ? WHERE player = ?");
            ) {
               statement.setString(1, sortingType.name());
               statement.setString(2, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   public CompletableFuture<Void> updateAdditionalFilters(String player, Set<String> additionalFilters) {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("UPDATE gui_datas SET additional_filters = ? WHERE player = ?");
            ) {
               if (additionalFilters != null && !additionalFilters.isEmpty()) {
                  statement.setString(1, String.join(",", additionalFilters));
               } else {
                  statement.setNull(1, 12);
               }

               statement.setString(2, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException(var11);
         }
      });
   }

   private void setGuiDataParameters(PreparedStatement statement, GuiData guiData) throws SQLException {
      statement.setString(1, guiData.getPlayer());
      statement.setString(2, guiData.getAuctionType().name());
      statement.setString(3, guiData.getCategoryType().name());
      if (guiData.getMarketSortingType() != null) {
         statement.setString(4, guiData.getMarketSortingType().name());
      } else {
         statement.setNull(4, 12);
      }

      if (guiData.getBidsSortingType() != null) {
         statement.setString(5, guiData.getBidsSortingType().name());
      } else {
         statement.setNull(5, 12);
      }

      if (guiData.getAdditionalFilters() != null && !guiData.getAdditionalFilters().isEmpty()) {
         statement.setString(6, String.join(",", guiData.getAdditionalFilters()));
      } else {
         statement.setNull(6, 12);
      }
   }

   private GuiData extractGuiDataFromResultSet(ResultSet rs) throws SQLException {
      Set<String> additionalFilters = new HashSet<>();
      String filtersStr = rs.getString("additional_filters");
      if (filtersStr != null && !filtersStr.isEmpty()) {
         additionalFilters.addAll(Set.of(filtersStr.split(",")));
      }

      MarketSortingType marketSortingType = null;
      String marketSortingStr = rs.getString("market_sorting_type");
      if (marketSortingStr != null && !marketSortingStr.isEmpty()) {
         marketSortingType = MarketSortingType.valueOf(marketSortingStr);
      }

      BidsSortingType bidsSortingType = null;
      String bidsSortingStr = rs.getString("bids_sorting_type");
      if (bidsSortingStr != null && !bidsSortingStr.isEmpty()) {
         bidsSortingType = BidsSortingType.valueOf(bidsSortingStr);
      }

      return new GuiData(
         rs.getInt("id"),
         rs.getString("player"),
         AuctionType.valueOf(rs.getString("auction_type")),
         CategoryType.valueOf(rs.getString("category_type")),
         marketSortingType,
         bidsSortingType,
         additionalFilters
      );
   }
}
