package org.dimasik.buyer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class PlayerDataManager {
   private final DatabaseConnector databaseConnector;

   public PlayerDataManager(DatabaseConnector databaseConnector) {
      this.databaseConnector = databaseConnector;
   }

   public CompletableFuture<Void> addPlayerData(String player, String material, boolean enabled) {
      return CompletableFuture.runAsync(() -> {
         String query = "INSERT INTO playerdata (player, material, enabled) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE enabled = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, player);
            statement.setString(2, material);
            statement.setBoolean(3, enabled);
            statement.setBoolean(4, enabled);
            statement.executeUpdate();
         } catch (SQLException var18) {
            var18.printStackTrace();
         }
      });
   }

   public CompletableFuture<Boolean> isEnabled(String player, String material) {
      return CompletableFuture.supplyAsync(() -> {
         String query = "SELECT enabled FROM playerdata WHERE player = ? AND material = ?";

         try (PreparedStatement statement = this.databaseConnector.getConnection().prepareStatement(query)) {
            statement.setString(1, player);
            statement.setString(2, material);
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next() ? false : resultSet.getBoolean("enabled");
         } catch (SQLException var20) {
            var20.printStackTrace();
            return false;
         }
      });
   }
}
