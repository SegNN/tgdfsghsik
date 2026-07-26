package ru.holydev.holystar.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import ru.holydev.holystar.HolyStar;

public class DatabaseManager {
   private final HolyStar plugin;
   private HikariDataSource dataSource;

   public DatabaseManager(HolyStar plugin) {
      this.plugin = plugin;
   }

   public void connect() {
      String host = this.plugin.getConfig().getString("database.host", "localhost");
      int port = this.plugin.getConfig().getInt("database.port", 3306);
      String database = this.plugin.getConfig().getString("database.name", "lite_stars");
      String username = this.plugin.getConfig().getString("database.username", "plugins");
      String password = this.plugin.getConfig().getString("database.password", "");
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
      config.setUsername(username);
      config.setPassword(password);
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.addDataSourceProperty("useServerPrepStmts", "true");
      config.addDataSourceProperty("useLocalSessionState", "true");
      config.addDataSourceProperty("rewriteBatchedStatements", "true");
      config.addDataSourceProperty("cacheResultSetMetadata", "true");
      config.addDataSourceProperty("cacheServerConfiguration", "true");
      config.addDataSourceProperty("elideSetAutoCommits", "true");
      config.addDataSourceProperty("maintainTimeStats", "false");
      this.dataSource = new HikariDataSource(config);
      this.createTable();
   }

   public void disconnect() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   public void createTable() {
      CompletableFuture.runAsync(() -> {
         String sql = "CREATE TABLE IF NOT EXISTS player_containers (player VARCHAR(16) PRIMARY KEY, value INTEGER);";

         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
         ) {
            stmt.execute();
         } catch (SQLException var10) {
            var10.printStackTrace();
         }
      });
   }

   public CompletableFuture<Void> setValue(String player, int value) {
      return CompletableFuture.runAsync(() -> {
         String sql = "INSERT INTO player_containers (player, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?;";

         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
         ) {
            stmt.setString(1, player);
            stmt.setInt(2, value);
            stmt.setInt(3, value);
            stmt.executeUpdate();
         } catch (SQLException var12) {
            var12.printStackTrace();
         }
      });
   }

   public CompletableFuture<Integer> getValue(String player) {
      return CompletableFuture.supplyAsync(() -> {
         String sql = "SELECT value FROM player_containers WHERE player = ?;";

         try {
            Integer var6;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement stmt = connection.prepareStatement(sql);
            ) {
               stmt.setString(1, player);

               try (ResultSet rs = stmt.executeQuery()) {
                  if (rs.next()) {
                     return rs.getInt("value");
                  }

                  var6 = 0;
               }
            }

            return var6;
         } catch (SQLException var14) {
            var14.printStackTrace();
            return 0;
         }
      });
   }
}
