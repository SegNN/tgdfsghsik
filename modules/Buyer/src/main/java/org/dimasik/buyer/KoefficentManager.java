package org.dimasik.buyer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class KoefficentManager {
   private final DatabaseConnector databaseConnector;

   public KoefficentManager(DatabaseConnector databaseConnector) {
      this.databaseConnector = databaseConnector;
   }

   private double calculateKoefficent(double points) {
      double koeff = 1.0 + points / 600000.0;
      return Math.min(koeff, 5.0);
   }

   private double roundPoints(double points) {
      return Math.round(points * 10000.0) / 10000.0;
   }

   public CompletableFuture<Void> addKoefficent(String player, double additionalPoints) {
      return CompletableFuture.runAsync(() -> {
         double roundedPoints = this.roundPoints(additionalPoints);
         double currentPoints = this.getPoints(player).join();
         double newPoints = currentPoints + roundedPoints;
         double newKoeff = this.calculateKoefficent(newPoints);
         String query = "INSERT INTO koefficent (player, points, koefficent) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE points = ?, koefficent = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, player);
            statement.setDouble(2, newPoints);
            statement.setDouble(3, newKoeff);
            statement.setDouble(4, newPoints);
            statement.setDouble(5, newKoeff);
            statement.executeUpdate();
         } catch (SQLException var26) {
            var26.printStackTrace();
         }
      });
   }

   public CompletableFuture<Double> getPoints(String player) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT points FROM koefficent WHERE player = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, player);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? 0.0 : this.roundPoints(resultSet.getDouble("points"));
         } catch (SQLException var19) {
            var19.printStackTrace();
            return 0.0;
         }
      });
   }

   public CompletableFuture<Double> getKoefficent(String player) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT koefficent FROM koefficent WHERE player = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, player);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? 1.0 : resultSet.getDouble("koefficent");
         } catch (SQLException var19) {
            var19.printStackTrace();
            return 1.0;
         }
      });
   }
}
