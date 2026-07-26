package org.dimasik.litebinds.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
   private final HikariDataSource dataSource;
   private final boolean mysql;

   public DatabaseManager(String type, String host, int port, String user, String password, String database, String sqliteFile) {
      this.mysql = type.equalsIgnoreCase("mysql");
      HikariConfig config = new HikariConfig();
      if (this.mysql) {
         config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
         config.setDriverClassName("com.mysql.cj.jdbc.Driver");
         config.setUsername(user);
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
      } else {
         config.setJdbcUrl("jdbc:sqlite:" + sqliteFile);
         config.setDriverClassName("org.sqlite.JDBC");
         config.addDataSourceProperty("journal_mode", "WAL");
         config.addDataSourceProperty("busy_timeout", "5000");
         config.addDataSourceProperty("synchronous", "NORMAL");
         config.addDataSourceProperty("cache_size", "10000");
         config.addDataSourceProperty("temp_store", "MEMORY");
         config.addDataSourceProperty("mmap_size", "268435456");
      }

      config.setMaximumPoolSize(this.mysql ? 10 : 1);
      config.setMinimumIdle(this.mysql ? 2 : 1);
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      this.dataSource = new HikariDataSource(config);
      this.initializeDatabase();
   }

   private void initializeDatabase() {
      String sql;
      if (this.mysql) {
         sql = "CREATE TABLE IF NOT EXISTS player_actions (\n    player VARCHAR(16) PRIMARY KEY,\n    action_drop VARCHAR(32) NOT NULL DEFAULT 'NONE',\n    action_swap VARCHAR(32) NOT NULL DEFAULT 'NONE',\n    action_interact VARCHAR(32) NOT NULL DEFAULT 'NONE'\n)\n";
      } else {
         sql = "CREATE TABLE IF NOT EXISTS player_actions (\n    player TEXT PRIMARY KEY,\n    action_drop TEXT NOT NULL DEFAULT 'NONE',\n    action_swap TEXT NOT NULL DEFAULT 'NONE',\n    action_interact TEXT NOT NULL DEFAULT 'NONE'\n)\n";
      }

      try {
         try (
            Connection connection = this.dataSource.getConnection();
            Statement statement = connection.createStatement();
         ) {
            statement.execute(sql);
         }
      } catch (SQLException var10) {
         throw new RuntimeException("Failed to initialize database", var10);
      }
   }

   private Connection getConnection() throws SQLException {
      return this.dataSource.getConnection();
   }

   public CompletableFuture<Void> savePlayerActions(PlayerActions playerActions) {
      return CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.mysql) {
               sql = "INSERT INTO player_actions (player, action_drop, action_swap, action_interact)\nVALUES (?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\naction_drop = VALUES(action_drop),\naction_swap = VALUES(action_swap),\naction_interact = VALUES(action_interact)\n";
            } else {
               sql = "INSERT INTO player_actions (player, action_drop, action_swap, action_interact)\nVALUES (?, ?, ?, ?)\nON CONFLICT(player) DO UPDATE SET\naction_drop = excluded.action_drop,\naction_swap = excluded.action_swap,\naction_interact = excluded.action_interact\n";
            }

            try {
               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, playerActions.getPlayer());
                  statement.setString(2, playerActions.getActionDrop().name());
                  statement.setString(3, playerActions.getActionSwap().name());
                  statement.setString(4, playerActions.getActionInteract().name());
                  statement.executeUpdate();
               }
            } catch (SQLException var11) {
               throw new RuntimeException("Failed to save player actions", var11);
            }
         }
      );
   }

   public CompletableFuture<Optional<PlayerActions>> getPlayerActions(String player) {
      return CompletableFuture.supplyAsync(() -> {
         String sql = "SELECT * FROM player_actions WHERE player = ?";

         try (
            Connection connection = this.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
         ) {
            statement.setString(1, player);

            try (ResultSet resultSet = statement.executeQuery()) {
               if (resultSet.next()) {
                  PlayerActions actions = new PlayerActions();
                  actions.setPlayer(resultSet.getString("player"));
                  actions.setActionDrop(ActionType.valueOf(resultSet.getString("action_drop")));
                  actions.setActionSwap(ActionType.valueOf(resultSet.getString("action_swap")));
                  actions.setActionInteract(ActionType.valueOf(resultSet.getString("action_interact")));
                  return Optional.of(actions);
               } else {
                  return Optional.empty();
               }
            }
         } catch (SQLException var14) {
            throw new RuntimeException("Failed to get player actions", var14);
         }
      });
   }

   public CompletableFuture<Void> deletePlayerActions(String player) {
      return CompletableFuture.runAsync(() -> {
         String sql = "DELETE FROM player_actions WHERE player = ?";

         try {
            try (
               Connection connection = this.getConnection();
               PreparedStatement statement = connection.prepareStatement(sql);
            ) {
               statement.setString(1, player);
               statement.executeUpdate();
            }
         } catch (SQLException var11) {
            throw new RuntimeException("Failed to delete player actions", var11);
         }
      });
   }

   public CompletableFuture<Boolean> playerExists(String player) {
      return CompletableFuture.supplyAsync(() -> {
         String sql = "SELECT COUNT(*) FROM player_actions WHERE player = ?";

         try (
            Connection connection = this.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
         ) {
            statement.setString(1, player);

            try (ResultSet resultSet = statement.executeQuery()) {
               return resultSet.next() ? resultSet.getInt(1) > 0 : false;
            }
         } catch (SQLException var14) {
            throw new RuntimeException("Failed to check if player exists", var14);
         }
      });
   }

   public CompletableFuture<Void> updateActionDrop(String player, ActionType actionDrop) {
      return CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.mysql) {
               sql = "INSERT INTO player_actions (player, action_drop) VALUES (?, ?) ON DUPLICATE KEY UPDATE action_drop = VALUES(action_drop)";
            } else {
               sql = "INSERT INTO player_actions (player, action_drop, action_swap, action_interact) VALUES (?, ?, 'NONE', 'NONE') ON CONFLICT(player) DO UPDATE SET action_drop = excluded.action_drop";
            }

            try {
               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, player);
                  statement.setString(2, actionDrop.name());
                  statement.executeUpdate();
               }
            } catch (SQLException var12) {
               throw new RuntimeException("Failed to update action drop", var12);
            }
         }
      );
   }

   public CompletableFuture<Void> updateActionSwap(String player, ActionType actionSwap) {
      return CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.mysql) {
               sql = "INSERT INTO player_actions (player, action_swap) VALUES (?, ?) ON DUPLICATE KEY UPDATE action_swap = VALUES(action_swap)";
            } else {
               sql = "INSERT INTO player_actions (player, action_drop, action_swap, action_interact) VALUES (?, 'NONE', ?, 'NONE') ON CONFLICT(player) DO UPDATE SET action_swap = excluded.action_swap";
            }

            try {
               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, player);
                  statement.setString(2, actionSwap.name());
                  statement.executeUpdate();
               }
            } catch (SQLException var12) {
               throw new RuntimeException("Failed to update action swap", var12);
            }
         }
      );
   }

   public CompletableFuture<Void> updateActionInteract(String player, ActionType actionInteract) {
      return CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.mysql) {
               sql = "INSERT INTO player_actions (player, action_interact) VALUES (?, ?) ON DUPLICATE KEY UPDATE action_interact = VALUES(action_interact)";
            } else {
               sql = "INSERT INTO player_actions (player, action_drop, action_swap, action_interact) VALUES (?, 'NONE', 'NONE', ?) ON CONFLICT(player) DO UPDATE SET action_interact = excluded.action_interact";
            }

            try {
               try (
                  Connection connection = this.getConnection();
                  PreparedStatement statement = connection.prepareStatement(sql);
               ) {
                  statement.setString(1, player);
                  statement.setString(2, actionInteract.name());
                  statement.executeUpdate();
               }
            } catch (SQLException var12) {
               throw new RuntimeException("Failed to update action interact", var12);
            }
         }
      );
   }

   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }
}
