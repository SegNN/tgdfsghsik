package org.dimasik.playtimeapi.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.configuration.file.FileConfiguration;
import org.dimasik.playtimeapi.models.PlayerData;

public class DatabaseManager {
   private final HikariDataSource dataSource;
   private final String type;
   private final String insertSql;
   private final String selectSql;
   private final String createTableSql;

   public DatabaseManager(FileConfiguration config, File dataFolder) {
      this.type = config.getString("database.type", "SQLITE").toUpperCase();
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setPoolName("PlayTimeAPI-Pool");
      hikariConfig.setMinimumIdle(2);
      hikariConfig.setIdleTimeout(30000L);
      hikariConfig.setMaxLifetime(600000L);
      hikariConfig.setConnectionTimeout(5000L);
      if ("MYSQL".equals(this.type)) {
         String host = config.getString("database.host", "localhost");
         String port = config.getString("database.port", "3306");
         String database = config.getString("database.database", "playtimeapi");
         String user = config.getString("database.user", "root");
         String password = config.getString("database.password", "");
         hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true");
         hikariConfig.setUsername(user);
         hikariConfig.setPassword(password);
         hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
         hikariConfig.setMaximumPoolSize(10);
         this.insertSql = "INSERT INTO playtime_data (uuid, total_playtime, total_active_time, total_idle_time, current_server, last_login, last_activity, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE total_playtime = VALUES(total_playtime), total_active_time = VALUES(total_active_time), total_idle_time = VALUES(total_idle_time), current_server = VALUES(current_server), last_login = VALUES(last_login), last_activity = VALUES(last_activity), last_update = VALUES(last_update)";
      } else {
         String fileName = config.getString("database.file", "playtime.db");
         File dbFile = new File(dataFolder, fileName);
         hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
         hikariConfig.setDriverClassName("org.sqlite.JDBC");
         hikariConfig.setMaximumPoolSize(1);
         this.insertSql = "INSERT OR REPLACE INTO playtime_data (uuid, total_playtime, total_active_time, total_idle_time, current_server, last_login, last_activity, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      }

      this.selectSql = "SELECT total_playtime, total_active_time, total_idle_time, current_server, last_login, last_activity FROM playtime_data WHERE uuid = ?";
      this.createTableSql = "CREATE TABLE IF NOT EXISTS playtime_data (uuid VARCHAR(36) PRIMARY KEY, total_playtime BIGINT NOT NULL DEFAULT 0, total_active_time BIGINT NOT NULL DEFAULT 0, total_idle_time BIGINT NOT NULL DEFAULT 0, current_server VARCHAR(32) DEFAULT '', last_login BIGINT NOT NULL DEFAULT 0, last_activity BIGINT NOT NULL DEFAULT 0, last_update BIGINT NOT NULL DEFAULT 0)";
      this.dataSource = new HikariDataSource(hikariConfig);
      this.initializeDatabase();
   }

   private void initializeDatabase() {
      try (
         Connection conn = this.dataSource.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         stmt.execute(this.createTableSql);
      } catch (SQLException var9) {
         var9.printStackTrace();
      }
   }

   public CompletableFuture<PlayerData> getPlayerDataAsync(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(this.selectSql);
         ) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                  PlayerData data = new PlayerData(uuid);
                  data.setTotalPlayTime(rs.getLong("total_playtime"));
                  data.setTotalActiveTime(rs.getLong("total_active_time"));
                  data.setIdleTime(rs.getLong("total_idle_time"));
                  data.setCurrentServer(rs.getString("current_server"));
                  data.setLastLogin(rs.getLong("last_login"));
                  data.setLastActivity(rs.getLong("last_activity"));
                  return data;
               } else {
                  return null;
               }
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
            return null;
         }
      });
   }

   public CompletableFuture<Void> savePlayerDataAsync(PlayerData data) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(this.insertSql);
         ) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setLong(2, data.getTotalPlayTime());
            stmt.setLong(3, data.getTotalActiveTime());
            stmt.setLong(4, data.getIdleTime());
            stmt.setString(5, data.getCurrentServer());
            stmt.setLong(6, data.getLastLogin());
            stmt.setLong(7, data.getLastActivity());
            stmt.setLong(8, System.currentTimeMillis());
            stmt.executeUpdate();
         } catch (SQLException var10) {
            var10.printStackTrace();
         }
      });
   }

   public void closeConnection() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }
}
