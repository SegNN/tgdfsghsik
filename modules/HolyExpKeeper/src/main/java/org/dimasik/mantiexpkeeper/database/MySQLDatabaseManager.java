package org.dimasik.mantiexpkeeper.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLDatabaseManager implements DatabaseManager {
   private HikariDataSource dataSource;
   private final YamlConfiguration config;
   private final Logger logger;

   public MySQLDatabaseManager(YamlConfiguration config, Logger logger) {
      this.config = config;
      this.logger = logger;
   }

   @Override
   public void init() throws Exception {
      String fullHost = this.config.getString("database.host");
      String database = this.config.getString("database.database");
      String user = this.config.getString("database.user");
      String password = this.config.getString("database.password");
      String host = fullHost;
      int port = 3306;
      if (fullHost.contains(":")) {
         String[] parts = fullHost.split(":");
         host = parts[0];
         port = Integer.parseInt(parts[1]);
      }

      String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setJdbcUrl(url);
      hikariConfig.setUsername(user);
      hikariConfig.setPassword(password);
      hikariConfig.setMaximumPoolSize(10);
      hikariConfig.setMinimumIdle(2);
      hikariConfig.setConnectionTimeout(30000L);
      hikariConfig.setIdleTimeout(600000L);
      hikariConfig.setMaxLifetime(1800000L);
      hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
      this.dataSource = new HikariDataSource(hikariConfig);

      try (
         Connection connection = this.dataSource.getConnection();
         Statement stmt = connection.createStatement();
      ) {
         stmt.execute(
            "CREATE TABLE IF NOT EXISTS abilities (player VARCHAR(36) NOT NULL, ability VARCHAR(64) NOT NULL, expiry BIGINT NOT NULL, PRIMARY KEY (player, ability)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
         );
         this.logger.info("База данных MySQL успешно инициализирована: " + database + " на " + host + ":" + port);
      }
   }

   @Override
   public void close() {
      if (this.dataSource != null) {
         this.dataSource.close();
      }
   }

   @Override
   public CompletableFuture<Void> cleanExpiredAbilities(Executor executor) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM abilities WHERE expiry != -1 AND expiry < ?");
         ) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.executeUpdate();
         } catch (SQLException var33) {
            throw new RuntimeException("Ошибка при очистке истекших способностей", var33);
         }
      }, executor);
   }

   @Override
   public CompletableFuture<Void> addTime(Executor executor, String player, String ability, long expiry) {
      return CompletableFuture.runAsync(
         () -> {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement stmt = connection.prepareStatement(
                  "INSERT INTO abilities (player, ability, expiry) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE expiry = VALUES(expiry)"
               );
            ) {
               stmt.setString(1, player);
               stmt.setString(2, ability);
               stmt.setLong(3, expiry);
               stmt.executeUpdate();
            } catch (SQLException var37) {
               throw new RuntimeException("Ошибка при добавлении времени способности", var37);
            }
         },
         executor
      );
   }

   @Override
   public CompletableFuture<Void> addInf(Executor executor, String player, String ability) {
      return CompletableFuture.runAsync(
         () -> {
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement stmt = connection.prepareStatement(
                  "INSERT INTO abilities (player, ability, expiry) VALUES (?, ?, -1) ON DUPLICATE KEY UPDATE expiry = VALUES(expiry)"
               );
            ) {
               stmt.setString(1, player);
               stmt.setString(2, ability);
               stmt.executeUpdate();
            } catch (SQLException var35) {
               throw new RuntimeException("Ошибка при добавлении бесконечной способности", var35);
            }
         },
         executor
      );
   }

   @Override
   public CompletableFuture<Void> removePlayer(Executor executor, String player, String ability) {
      return CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM abilities WHERE player = ? AND ability = ?");
         ) {
            stmt.setString(1, player);
            stmt.setString(2, ability);
            stmt.executeUpdate();
         } catch (SQLException var35) {
            throw new RuntimeException("Ошибка при удалении способности", var35);
         }
      }, executor);
   }

   @Override
   public CompletableFuture<Boolean> hasAbility(Executor executor, String player, String ability) {
      return CompletableFuture.supplyAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT expiry FROM abilities WHERE player = ? AND ability = ?");
         ) {
            stmt.setString(1, player);
            stmt.setString(2, ability);

            Boolean expiry;
            try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                  long expiryx = rs.getLong("expiry");
                  return expiryx == -1L || expiryx > System.currentTimeMillis();
               }

               expiry = false;
            }

            return expiry;
         } catch (SQLException var70) {
            throw new RuntimeException("Ошибка при проверке способности", var70);
         }
      }, executor);
   }

   @Override
   public CompletableFuture<String> getAbilityTime(Executor executor, String player, String ability) {
      return CompletableFuture.supplyAsync(() -> {
         try (
            Connection connection = this.dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT expiry FROM abilities WHERE player = ? AND ability = ?");
         ) {
            stmt.setString(1, player);
            stmt.setString(2, ability);

            String var11;
            try (ResultSet rs = stmt.executeQuery()) {
               if (!rs.next()) {
                  return null;
               }

               long expiry = rs.getLong("expiry");
               var11 = expiry == -1L ? "**.**.****  **:**:**" : new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss").format(new Date(expiry));
            }

            return var11;
         } catch (SQLException var70) {
            throw new RuntimeException("Ошибка при получении времени способности", var70);
         }
      }, executor);
   }
}
