package ua.fichatime.fichapromocode.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import ua.fichatime.fichapromocode.FichaPromoCode;
import ua.fichatime.fichapromocode.manager.ConfigManager;

public class Database {
   private final FichaPromoCode plugin;
   private final ConfigManager configManager;
   private Connection connection;
   private boolean isMySQL;

   public Database(FichaPromoCode plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.isMySQL = this.configManager.getStorageType().equalsIgnoreCase("MySQL");
      this.setupDatabase();
   }

   private void setupDatabase() {
      try {
         if (this.isMySQL) {
            this.connection = DriverManager.getConnection(
               "jdbc:mysql://" + this.configManager.getMySQLHost() + "/" + this.configManager.getMySQLDatabase() + "?autoReconnect=true",
               this.configManager.getMySQLUser(),
               this.configManager.getMySQLPassword()
            );
         } else {
            File dataFolder = this.plugin.getDataFolder();
            if (!dataFolder.exists()) {
               dataFolder.mkdirs();
            }

            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/data.db");
         }

         if (this.isMySQL) {
            this.connection
               .createStatement()
               .execute(
                  "CREATE TABLE IF NOT EXISTS promo_activations (id INT AUTO_INCREMENT PRIMARY KEY, player_name VARCHAR(255), ip_address VARCHAR(255), promo_code VARCHAR(255), promo_group VARCHAR(255), activation_count INT NOT NULL DEFAULT 0, UNIQUE KEY unique_player_promo (player_name, promo_code))"
               );
         } else {
            this.connection
               .createStatement()
               .execute(
                  "CREATE TABLE IF NOT EXISTS promo_activations (id INTEGER PRIMARY KEY AUTOINCREMENT, player_name VARCHAR(255), ip_address VARCHAR(255), promo_code VARCHAR(255), promo_group VARCHAR(255), activation_count INTEGER NOT NULL DEFAULT 0, UNIQUE(player_name, promo_code))"
               );
         }
      } catch (SQLException var21) {
         var21.printStackTrace();
      }
   }

   private void ensureConnection() {
      try {
         if (this.connection == null || this.connection.isClosed()) {
            if (this.isMySQL) {
               this.connection = DriverManager.getConnection(
                  "jdbc:mysql://" + this.configManager.getMySQLHost() + "/" + this.configManager.getMySQLDatabase() + "?autoReconnect=true",
                  this.configManager.getMySQLUser(),
                  this.configManager.getMySQLPassword()
               );
            } else {
               File dataFolder = this.plugin.getDataFolder();
               this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/data.db");
            }
         }
      } catch (SQLException var21) {
         var21.printStackTrace();
      }
   }

   public void closeConnection() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   public boolean hasPlayerActivated(String playerName, String promoCode) {
      this.ensureConnection();

      try {
         PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM promo_activations WHERE player_name = ? AND promo_code = ?");
         statement.setString(1, playerName);
         statement.setString(2, promoCode);
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next()) {
            int activationCount = resultSet.getInt("activation_count");
            int playerUses = this.configManager.getRewardPlayer(promoCode);
            return activationCount >= playerUses;
         }
      } catch (SQLException var7) {
         var7.printStackTrace();
      }

      return false;
   }

   public boolean hasIPActivated(String ipAddress, String promoCode) {
      this.ensureConnection();

      try {
         PreparedStatement statement = this.connection
            .prepareStatement("SELECT COUNT(*) as count FROM promo_activations WHERE ip_address = ? AND promo_code = ?");
         statement.setString(1, ipAddress);
         statement.setString(2, promoCode);
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
         }
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

      return false;
   }

   public boolean hasPlayerActivatedGroup(String playerName, String group) {
      this.ensureConnection();

      try {
         PreparedStatement statement = this.connection
            .prepareStatement("SELECT COUNT(*) as count FROM promo_activations WHERE player_name = ? AND promo_group = ?");
         statement.setString(1, playerName);
         statement.setString(2, group);
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
         }
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

      return false;
   }

   public boolean canActivatePromo(String promoCode) {
      this.ensureConnection();
      int globalUses = this.configManager.getRewardAllPlayer(promoCode);
      if (globalUses == Integer.MAX_VALUE) {
         return true;
      } else {
         try {
            PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) as count FROM promo_activations WHERE promo_code = ?");
            statement.setString(1, promoCode);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
               int activations = resultSet.getInt("count");
               return activations < globalUses;
            } else {
               return true;
            }
         } catch (SQLException var61) {
            var61.printStackTrace();
            return false;
         }
      }
   }

   public void activatePromo(String playerName, String ipAddress, String promoCode, String group) {
      this.ensureConnection();

      try {
         if (this.isMySQL) {
            PreparedStatement statement = this.connection
               .prepareStatement(
                  "INSERT INTO promo_activations (player_name, ip_address, promo_code, promo_group, activation_count) VALUES (?, ?, ?, ?, 1) ON DUPLICATE KEY UPDATE activation_count = activation_count + 1"
               );
            statement.setString(1, playerName);
            statement.setString(2, ipAddress);
            statement.setString(3, promoCode);
            statement.setString(4, group);
            statement.executeUpdate();
         } else {
            PreparedStatement checkStatement = this.connection
               .prepareStatement("SELECT activation_count FROM promo_activations WHERE player_name = ? AND promo_code = ?");
            checkStatement.setString(1, playerName);
            checkStatement.setString(2, promoCode);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
               int currentCount = resultSet.getInt("activation_count");
               PreparedStatement updateStatement = this.connection
                  .prepareStatement(
                     "UPDATE promo_activations SET activation_count = ?, ip_address = ?, promo_group = ? WHERE player_name = ? AND promo_code = ?"
                  );
               updateStatement.setInt(1, currentCount + 1);
               updateStatement.setString(2, ipAddress);
               updateStatement.setString(3, group);
               updateStatement.setString(4, playerName);
               updateStatement.setString(5, promoCode);
               updateStatement.executeUpdate();
            } else {
               PreparedStatement insertStatement = this.connection
                  .prepareStatement("INSERT INTO promo_activations (player_name, ip_address, promo_code, promo_group, activation_count) VALUES (?, ?, ?, ?, 1)");
               insertStatement.setString(1, playerName);
               insertStatement.setString(2, ipAddress);
               insertStatement.setString(3, promoCode);
               insertStatement.setString(4, group);
               insertStatement.executeUpdate();
            }
         }
      } catch (SQLException var9) {
         var9.printStackTrace();
      }
   }

   public Map<String, Integer> getPromoStats() {
      this.ensureConnection();
      Map<String, Integer> stats = new HashMap<>();

      try {
         PreparedStatement statement = this.connection
            .prepareStatement("SELECT promo_code, SUM(activation_count) as total FROM promo_activations GROUP BY promo_code");
         ResultSet resultSet = statement.executeQuery();

         while (resultSet.next()) {
            stats.put(resultSet.getString("promo_code"), resultSet.getInt("total"));
         }
      } catch (SQLException var4) {
         var4.printStackTrace();
      }

      return stats;
   }

   public int getTotalActivations() {
      this.ensureConnection();

      try {
         PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) as total FROM promo_activations");
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next()) {
            return resultSet.getInt("total");
         }
      } catch (SQLException var3) {
         var3.printStackTrace();
      }

      return 0;
   }
}
