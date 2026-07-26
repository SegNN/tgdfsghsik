package org.dimasik.buyer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PricesManager {
   private final DatabaseConnector databaseConnector;

   public PricesManager(DatabaseConnector databaseConnector) {
      this.databaseConnector = databaseConnector;
   }

   public CompletableFuture<Void> addPrice(String name, String displayName, double points, double price, String category) {
      return CompletableFuture.runAsync(
         () -> {
            String query = "INSERT INTO prices (name, displayName, points, price, category) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE displayName = ?, points = ?, price = ?, category = ?";

            try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
               statement.setString(1, name);
               statement.setString(2, displayName);
               statement.setDouble(3, points);
               statement.setDouble(4, price);
               statement.setString(5, category);
               statement.setString(6, displayName);
               statement.setDouble(7, points);
               statement.setDouble(8, price);
               statement.setString(9, category);
               statement.executeUpdate();
            } catch (SQLException var22) {
               var22.printStackTrace();
            }
         }
      );
   }

   public CompletableFuture<List<PricesManager.Item>> getItemsByCategory(String category, String sortType) {
      return CompletableFuture.supplyAsync(() -> {
         List<PricesManager.Item> items = new ArrayList<>();
         String query = "SELECT name, displayName, points, price FROM prices WHERE category = ?";
         switch (sortType) {
            case "priceLow":
               query = query + " ORDER BY price ASC";
               break;
            case "priceHigh":
               query = query + " ORDER BY price DESC";
               break;
            case "nameLow":
               query = query + " ORDER BY displayName ASC";
               break;
            case "nameHigh":
               query = query + " ORDER BY displayName DESC";
         }

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, category);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
               String name = resultSet.getString("name");
               String displayName = resultSet.getString("displayName");
               double points = resultSet.getDouble("points");
               double price = resultSet.getDouble("price");
               items.add(new PricesManager.Item(name, displayName, points, price));
            }
         } catch (SQLException var24) {
            var24.printStackTrace();
         }

         return items;
      });
   }

   public CompletableFuture<Double> getPoints(String name) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT points FROM prices WHERE name = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? 0.0 : resultSet.getDouble("points");
         } catch (SQLException var19) {
            var19.printStackTrace();
            return 0.0;
         }
      });
   }

   public CompletableFuture<Double> getPrice(String name) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT price FROM prices WHERE name = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? 0.0 : resultSet.getDouble("price");
         } catch (SQLException var19) {
            var19.printStackTrace();
            return 0.0;
         }
      });
   }

   public CompletableFuture<String> getCategory(String name) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT category FROM prices WHERE name = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? null : resultSet.getString("category");
         } catch (SQLException var19) {
            var19.printStackTrace();
            return null;
         }
      });
   }

   public CompletableFuture<String> getDisplayName(String name) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT displayName FROM prices WHERE name = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? null : resultSet.getString("displayName");
         } catch (SQLException var19) {
            var19.printStackTrace();
            return null;
         }
      });
   }

   public static class Item {
      private final String name;
      private final String displayName;
      private final double points;
      private final double price;

      public Item(String name, String displayName, double points, double price) {
         this.name = name;
         this.displayName = displayName;
         this.points = points;
         this.price = price;
      }

      public String getName() {
         return this.name;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public double getPoints() {
         return this.points;
      }

      public double getPrice() {
         return this.price;
      }

      @Override
      public String toString() {
         return "Item{name='" + this.name + '\'' + ", displayName='" + this.displayName + '\'' + ", points=" + this.points + ", price=" + this.price + '}';
      }
   }
}
