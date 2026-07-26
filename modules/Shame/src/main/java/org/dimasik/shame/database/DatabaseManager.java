package org.dimasik.shame.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
   private final HikariDataSource dataSource;

   public DatabaseManager(String host, String database, String user, String password, int port) {
      this.dataSource = this.createDataSource(host, database, user, password, port);
      this.initializeDatabase();
   }

   public DatabaseManager(String host, String database, String user, String password) {
      this(host, database, user, password, 3306);
   }

   private HikariDataSource createDataSource(String host, String database, String user, String password, int port) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
      config.setUsername(user);
      config.setPassword(password);
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      config.setAutoCommit(true);
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
      return new HikariDataSource(config);
   }

   private void initializeDatabase() {
      try {
         try (
            Connection connection = this.dataSource.getConnection();
            Statement statement = connection.createStatement();
         ) {
            String createDBSQL = "CREATE DATABASE IF NOT EXISTS " + connection.getCatalog() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            statement.execute(createDBSQL);
            statement.execute("USE " + connection.getCatalog());
            String createTableSQL = "CREATE TABLE IF NOT EXISTS nicknames (nickname VARCHAR(16) PRIMARY KEY NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            statement.execute(createTableSQL);
         }
      } catch (SQLException var9) {
         throw new RuntimeException("Failed to initialize database", var9);
      }
   }

   public CompletableFuture<Boolean> addNickname(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         if (nickname != null && nickname.length() <= 16 && !nickname.trim().isEmpty()) {
            String sql = "INSERT INTO nicknames (nickname) VALUES (?)";

            try {
               Boolean var5;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, nickname.trim());
                  statement.executeUpdate();
                  var5 = true;
               }

               return var5;
            } catch (SQLException var11) {
               if (var11.getErrorCode() == 1062) {
                  return false;
               } else {
                  throw new RuntimeException("Failed to add nickname: " + nickname, var11);
               }
            }
         } else {
            return false;
         }
      });
   }

   public CompletableFuture<Boolean> nicknameExists(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         if (nickname != null && !nickname.trim().isEmpty()) {
            String sql = "SELECT 1 FROM nicknames WHERE nickname = ?";

            try {
               Boolean var6;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, nickname.trim());

                  try (ResultSet resultSet = statement.executeQuery()) {
                     var6 = resultSet.next();
                  }
               }

               return var6;
            } catch (SQLException var14) {
               throw new RuntimeException("Failed to check nickname existence: " + nickname, var14);
            }
         } else {
            return false;
         }
      });
   }

   public CompletableFuture<Boolean> deleteNickname(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         if (nickname != null && !nickname.trim().isEmpty()) {
            String sql = "DELETE FROM nicknames WHERE nickname = ?";

            try {
               Boolean var6;
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, nickname.trim());
                  int affectedRows = statement.executeUpdate();
                  var6 = affectedRows > 0;
               }

               return var6;
            } catch (SQLException var11) {
               throw new RuntimeException("Failed to delete nickname: " + nickname, var11);
            }
         } else {
            return false;
         }
      });
   }

   public CompletableFuture<Integer> getNicknameCount() {
      return CompletableFuture.supplyAsync(() -> {
         String sql = "SELECT COUNT(*) as count FROM nicknames";

         try {
            Integer var5;
            try (
               Connection connection = this.dataSource.getConnection();
               Statement statement = connection.createStatement();
               ResultSet resultSet = statement.executeQuery(sql);
            ) {
               if (resultSet.next()) {
                  return resultSet.getInt("count");
               }

               var5 = 0;
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException("Failed to get nickname count", var13);
         }
      });
   }

   public void shutdown() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
         System.out.println("Database connection pool closed");
      }
   }

   public boolean isConnected() {
      return this.dataSource != null && !this.dataSource.isClosed();
   }

   @Override
   protected void finalize() throws Throwable {
      try {
         this.shutdown();
      } finally {
         super.finalize();
      }
   }
}
