package ru.nezxenka.holypvp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseManager {
   private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
   private static String URL = "";
   private static String USER = "";
   private static String PASSWORD = "";
   private Connection connection;
   private JavaPlugin plugin;

   public DatabaseManager(JavaPlugin plugin) {
      this.plugin = plugin;
      URL = "jdbc:mysql://" + plugin.getConfig().getString("database.host") + "/" + plugin.getConfig().getString("database.database") + "?autoReconnect=true";
      USER = plugin.getConfig().getString("database.user");
      PASSWORD = plugin.getConfig().getString("database.password");
      this.initialize();
   }

   private boolean checkConnection() {
      try {
         return this.connection != null && !this.connection.isClosed() ? true : this.initialize();
      } catch (SQLException var2) {
         LOGGER.log(Level.SEVERE, "Failed to check database connection", (Throwable)var2);
         return false;
      }
   }

   private boolean initialize() {
      try {
         this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
         this.createTableIfNotExists();
         return true;
      } catch (SQLException var2) {
         LOGGER.log(Level.SEVERE, "Failed to connect to the database", (Throwable)var2);
         return false;
      }
   }

   public CompletableFuture<Void> createTableIfNotExists() {
      return CompletableFuture.runAsync(
         () -> {
            if (!this.checkConnection()) {
               LOGGER.severe("Cannot create table - no database connection");
            } else {
               String sql = "CREATE TABLE IF NOT EXISTS player_profiles (nickname VARCHAR(16) PRIMARY KEY,server VARCHAR(32) NOT NULL,fight_type ENUM('DUEL', 'DEADLY_FIGHT') NOT NULL,inventory_base64 LONGBLOB NOT NULL,hash LONGBLOB NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

               try (Statement statement = this.connection.createStatement()) {
                  statement.execute(sql);
               } catch (SQLException var151) {
                  LOGGER.log(Level.SEVERE, "Failed to create table", (Throwable)var151);
               }
            }
         }
      );
   }

   public CompletableFuture<Boolean> createPlayerProfile(PlayerProfile profile) {
      return CompletableFuture.supplyAsync(() -> {
         if (!this.checkConnection()) {
            return false;
         } else {
            String sql = "INSERT INTO player_profiles (nickname, server, fight_type, inventory_base64, hash) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
               statement.setString(1, profile.getNickname());
               statement.setString(2, profile.getServer());
               statement.setString(3, profile.getFightType().toString());
               statement.setString(4, profile.getHash());
               statement.setString(5, profile.getHash());
               statement.executeUpdate();
               return true;
            } catch (SQLException var171) {
               LOGGER.log(Level.WARNING, "Failed to create player profile for " + profile.getNickname(), (Throwable)var171);
               return false;
            }
         }
      });
   }

   public CompletableFuture<PlayerProfile> getPlayerProfile(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         if (!this.checkConnection()) {
            return null;
         } else {
            String sql = "SELECT * FROM player_profiles WHERE nickname = ?";

            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
               statement.setString(1, nickname);
               ResultSet resultSet = statement.executeQuery();
               if (resultSet.next()) {
                  String server = resultSet.getString("server");
                  PlayerProfile.FightType fightType = PlayerProfile.FightType.valueOf(resultSet.getString("fight_type"));
                  String inventoryBase64 = resultSet.getString("inventory_base64");
                  Inventory inventory = PlayerProfile.base64ToInventory(inventoryBase64);
                  return new PlayerProfile(nickname, server, fightType, inventory);
               } else {
                  return null;
               }
            } catch (SQLException | IOException var22) {
               LOGGER.log(Level.WARNING, "Failed to get player profile for " + nickname, (Throwable)var22);
               return null;
            }
         }
      });
   }

   public CompletableFuture<Boolean> updatePlayerProfile(PlayerProfile profile) {
      return CompletableFuture.supplyAsync(() -> {
         if (!this.checkConnection()) {
            return false;
         } else {
            String sql = "UPDATE player_profiles SET server = ?, fight_type = ?, inventory_base64 = ?, hash = ? WHERE nickname = ?";

            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
               statement.setString(1, profile.getServer());
               statement.setString(2, profile.getFightType().toString());
               statement.setString(3, profile.getHash());
               statement.setString(4, profile.getHash());
               statement.setString(5, profile.getNickname());
               int affectedRows = statement.executeUpdate();
               return affectedRows > 0;
            } catch (SQLException var17) {
               LOGGER.log(Level.WARNING, "Failed to update player profile for " + profile.getNickname(), (Throwable)var17);
               return false;
            }
         }
      });
   }

   public CompletableFuture<Boolean> deletePlayerProfile(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         if (!this.checkConnection()) {
            return false;
         } else {
            String sql = "DELETE FROM player_profiles WHERE nickname = ?";

            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
               statement.setString(1, nickname);
               int affectedRows = statement.executeUpdate();
               return affectedRows > 0;
            } catch (SQLException var17) {
               LOGGER.log(Level.WARNING, "Failed to delete player profile for " + nickname, (Throwable)var17);
               return false;
            }
         }
      });
   }

   public CompletableFuture<List<PlayerProfile>> getAllPlayerProfiles() {
      return CompletableFuture.supplyAsync(() -> {
         List<PlayerProfile> profiles = new ArrayList<>();
         if (!this.checkConnection()) {
            LOGGER.warning("Connection failed, returning empty list");
            return profiles;
         } else {
            String sql = "SELECT * FROM player_profiles";

            try (
               Statement statement = this.connection.createStatement();
               ResultSet resultSet = statement.executeQuery(sql);
            ) {
               while (resultSet.next()) {
                  try {
                     String nickname = resultSet.getString("nickname");
                     String server = resultSet.getString("server");
                     PlayerProfile.FightType fightType = PlayerProfile.FightType.valueOf(resultSet.getString("fight_type"));
                     String inventoryBase64 = resultSet.getString("inventory_base64");
                     Inventory inventory = PlayerProfile.base64ToInventory(inventoryBase64);
                     PlayerProfile profile = new PlayerProfile(nickname, server, fightType, inventory);
                     profiles.add(profile);
                  } catch (IOException var38) {
                     LOGGER.log(Level.WARNING, "Failed to deserialize inventory for player: " + resultSet.getString("nickname"), (Throwable)var38);
                  }
               }
            } catch (SQLException var431) {
               LOGGER.log(Level.SEVERE, "Failed to get all player profiles", (Throwable)var431);
            }

            return profiles;
         }
      });
   }

   public void close() {
      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (SQLException var2) {
            LOGGER.log(Level.SEVERE, "Failed to close database connection", (Throwable)var2);
         }
      }
   }
}
