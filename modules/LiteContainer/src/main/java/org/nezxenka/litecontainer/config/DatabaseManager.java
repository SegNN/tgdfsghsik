package org.nezxenka.litecontainer.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DatabaseManager {
   private final Plugin plugin;
   private Connection connection;
   private String type;
   private String mysqlHost;
   private int mysqlPort;
   private String mysqlDatabase;
   private String mysqlUsername;
   private String mysqlPassword;
   private String sqliteFile;
   private boolean autoConnect;

   public DatabaseManager(Plugin plugin, FileConfiguration config) {
      this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
      ConfigurationSection dbSection = config.getConfigurationSection("database");
      if (dbSection != null) {
         this.type = dbSection.getString("type", "sqlite");
         this.mysqlHost = dbSection.getString("mysql.host", "localhost");
         this.mysqlPort = dbSection.getInt("mysql.port", 3306);
         this.mysqlDatabase = dbSection.getString("mysql.database", "litecontainer");
         this.mysqlUsername = dbSection.getString("mysql.username", "root");
         this.mysqlPassword = dbSection.getString("mysql.password", "");
         this.sqliteFile = dbSection.getString("sqlite.file", "database.db");
         this.autoConnect = dbSection.getBoolean("autoconnect", true);
      } else {
         this.type = config.getString("type", "sqlite");
         this.mysqlHost = config.getString("mysql.host", "localhost");
         this.mysqlPort = config.getInt("mysql.port", 3306);
         this.mysqlDatabase = config.getString("mysql.database", "litecontainer");
         this.mysqlUsername = config.getString("mysql.username", "root");
         this.mysqlPassword = config.getString("mysql.password", "");
         this.sqliteFile = config.getString("sqlite.file", "database.db");
         this.autoConnect = config.getBoolean("autoconnect", true);
      }

      if (this.autoConnect) {
         this.connect();
      }
   }

   public void connect() {
      try {
         if (this.type.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://"
               + this.mysqlHost
               + ":"
               + this.mysqlPort
               + "/"
               + this.mysqlDatabase
               + "?useSSL=false&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
            this.connection = DriverManager.getConnection(url, this.mysqlUsername, this.mysqlPassword);
            this.plugin.getLogger().info("Успешное подключение к MySQL!");
         } else {
            File dataFolder = this.plugin.getDataFolder();
            if (!dataFolder.exists()) {
               dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, this.sqliteFile);
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            this.plugin.getLogger().info("Успешное подключение к SQLite!");
         }

         this.createTable();
      } catch (ClassNotFoundException | SQLException var3) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка подключения к базе данных: " + var3.getMessage(), (Throwable)var3);
         throw new RuntimeException("Failed to connect to database", var3);
      }
   }

   public void disconnect() {
      if (this.connection != null) {
         try {
            if (!this.connection.isClosed()) {
               this.connection.close();
               this.plugin.getLogger().info("Отключение от базы данных завершено");
            }
         } catch (SQLException var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Ошибка при отключении от базы данных", (Throwable)var2);
         }
      }
   }

   private void createTable() throws SQLException {
      String createTableSQL;
      if (this.type.equalsIgnoreCase("mysql")) {
         createTableSQL = "CREATE TABLE IF NOT EXISTS player_containers (player VARCHAR(36) PRIMARY KEY, player_real VARCHAR(36), value INT DEFAULT 0, total_sapphires DOUBLE DEFAULT 0, lifetime_sapphires DOUBLE DEFAULT 0) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
      } else {
         createTableSQL = "CREATE TABLE IF NOT EXISTS player_containers (player VARCHAR(36) PRIMARY KEY, player_real VARCHAR(36), value INT DEFAULT 0, total_sapphires DOUBLE DEFAULT 0, lifetime_sapphires DOUBLE DEFAULT 0);";
      }

      try (PreparedStatement stmt = this.connection.prepareStatement(createTableSQL)) {
         stmt.execute();
      }
   }

   private void checkConnection() throws SQLException {
      if (this.connection == null || this.connection.isClosed() || !this.connection.isValid(2)) {
         this.plugin.getLogger().warning("Переподключение к базе данных...");
         this.disconnect();
         this.connect();
      }
   }

   public int getValue(@NotNull String player) {
      try {
         this.checkConnection();
         String sql = "SELECT value FROM player_containers WHERE player = LOWER(?);";

         int var5;
         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, player);

            try (ResultSet rs = stmt.executeQuery()) {
               var5 = rs.next() ? rs.getInt("value") : 0;
            }
         }

         return var5;
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения значения для игрока: " + player, (Throwable)var11);
         return 0;
      }
   }

   public void setValue(@NotNull String player, int value) {
      try {
         this.checkConnection();
         String sql;
         if (this.type.equalsIgnoreCase("mysql")) {
            sql = "INSERT INTO player_containers (player, player_real, value) VALUES (LOWER(?), ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value), player_real = VALUES(player_real);";
         } else {
            sql = "INSERT INTO player_containers (player, player_real, value) VALUES (LOWER(?), ?, ?) ON CONFLICT(player) DO UPDATE SET value = excluded.value, player_real = excluded.player_real;";
         }

         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, player);
            stmt.setString(2, player);
            stmt.setInt(3, value);
            stmt.executeUpdate();
         }
      } catch (SQLException var9) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка установки значения для игрока: " + player, (Throwable)var9);
      }
   }

   public void addSapphires(@NotNull String player, double sapphires) {
      try {
         this.checkConnection();
         String sql;
         if (this.type.equalsIgnoreCase("mysql")) {
            sql = "INSERT INTO player_containers (player, player_real, total_sapphires, lifetime_sapphires) VALUES (LOWER(?), ?, ?, ?) ON DUPLICATE KEY UPDATE total_sapphires = total_sapphires + VALUES(total_sapphires), lifetime_sapphires = lifetime_sapphires + VALUES(lifetime_sapphires), player_real = VALUES(player_real);";
         } else {
            sql = "INSERT INTO player_containers (player, player_real, total_sapphires, lifetime_sapphires) VALUES (LOWER(?), ?, ?, ?) ON CONFLICT(player) DO UPDATE SET total_sapphires = total_sapphires + excluded.total_sapphires, lifetime_sapphires = lifetime_sapphires + excluded.lifetime_sapphires, player_real = excluded.player_real;";
         }

         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, player);
            stmt.setString(2, player);
            stmt.setDouble(3, sapphires);
            stmt.setDouble(4, sapphires);
            stmt.executeUpdate();
         }
      } catch (SQLException var10) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка добавления сапфиров для игрока: " + player, (Throwable)var10);
      }
   }

   public double getTotalSapphires(@NotNull String player) {
      try {
         this.checkConnection();
         String sql = "SELECT total_sapphires FROM player_containers WHERE player = LOWER(?);";

         double var5;
         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, player);

            try (ResultSet rs = stmt.executeQuery()) {
               var5 = rs.next() ? rs.getDouble("total_sapphires") : 0.0;
            }
         }

         return var5;
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения total_sapphires для игрока: " + player, (Throwable)var11);
         return 0.0;
      }
   }

   public double getLifetimeSapphires(@NotNull String player) {
      try {
         this.checkConnection();
         String sql = "SELECT lifetime_sapphires FROM player_containers WHERE player = LOWER(?);";

         double var5;
         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, player);

            try (ResultSet rs = stmt.executeQuery()) {
               var5 = rs.next() ? rs.getDouble("lifetime_sapphires") : 0.0;
            }
         }

         return var5;
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения lifetime_sapphires для игрока: " + player, (Throwable)var11);
         return 0.0;
      }
   }

   public List<String> getTopPlayersByLifetimeSapphires() {
      List<String> list = new ArrayList<>();

      try {
         this.checkConnection();
         String sql = "SELECT player_real FROM player_containers ORDER BY lifetime_sapphires DESC LIMIT 10;";

         try (
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
         ) {
            while (rs.next()) {
               list.add(rs.getString("player_real"));
            }
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения топа игроков", (Throwable)var11);
      }

      return list;
   }

   public List<Double> getTopSapphires() {
      List<Double> list = new ArrayList<>();

      try {
         this.checkConnection();
         String sql = "SELECT lifetime_sapphires FROM player_containers ORDER BY lifetime_sapphires DESC LIMIT 10;";

         try (
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
         ) {
            while (rs.next()) {
               list.add(rs.getDouble("lifetime_sapphires"));
            }
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения топа сапфиров", (Throwable)var11);
      }

      return list;
   }

   public String getRealName(String playerLower) {
      try {
         this.checkConnection();
         String sql = "SELECT player_real FROM player_containers WHERE player = LOWER(?);";

         String var5;
         try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            stmt.setString(1, playerLower);

            try (ResultSet rs = stmt.executeQuery()) {
               var5 = rs.next() ? rs.getString("player_real") : playerLower;
            }
         }

         return var5;
      } catch (SQLException var11) {
         this.plugin.getLogger().log(Level.SEVERE, "Ошибка получения реального имени для: " + playerLower, (Throwable)var11);
         return playerLower;
      }
   }

   public boolean isConnected() {
      try {
         return this.connection != null && !this.connection.isClosed() && this.connection.isValid(2);
      } catch (SQLException var2) {
         return false;
      }
   }
}
