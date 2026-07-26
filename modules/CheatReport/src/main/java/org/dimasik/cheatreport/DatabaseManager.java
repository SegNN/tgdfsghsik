package org.dimasik.cheatreport;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;

public class DatabaseManager {
   private HikariDataSource dataSource;
   private CheatReport plugin;

   public DatabaseManager(CheatReport plugin) {
      this.plugin = plugin;
      this.setupDataSource();
      this.createTables();
   }

   private void setupDataSource() {
      HikariConfig config = new HikariConfig();
      String host = this.plugin.getConfig().getString("mysql.host", "localhost");
      int port = this.plugin.getConfig().getInt("mysql.port", 3306);
      String database = this.plugin.getConfig().getString("mysql.database", "cheatreport");
      String user = this.plugin.getConfig().getString("mysql.user", "root");
      String password = this.plugin.getConfig().getString("mysql.password", "");
      String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
      config.setJdbcUrl(jdbcUrl);
      config.setUsername(user);
      config.setPassword(password);
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      config.setLeakDetectionThreshold(5000L);
      config.setPoolName("CheatReport-Hikari");
      this.dataSource = new HikariDataSource(config);
   }

   public Connection getConnection() throws SQLException {
      return this.dataSource.getConnection();
   }

   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
         Bukkit.getLogger().info("[CheatReport] Пул соединений закрыт");
      }
   }

   private void createTables() {
      try {
         Connection conn = this.getConnection();

         try {
            Statement stmt = conn.createStatement();

            try {
               stmt.executeUpdate(
                  "CREATE TABLE IF NOT EXISTS reports (id INT AUTO_INCREMENT PRIMARY KEY, sender VARCHAR(255) NOT NULL, target VARCHAR(255) NOT NULL, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, reason TEXT, type VARCHAR(50) DEFAULT 'EXPECT', admin VARCHAR(255), mode VARCHAR(255), INDEX idx_target (target), INDEX idx_type (type), INDEX idx_admin (admin), INDEX idx_date (date))"
               );
               stmt.executeUpdate(
                  "CREATE TABLE IF NOT EXISTS reputation (player VARCHAR(255) PRIMARY KEY, totalSuccessCheck INT DEFAULT 0, totalFailureCheck INT DEFAULT 0, totalSuccessReports INT DEFAULT 0, totalFailureReports INT DEFAULT 0, sortType VARCHAR(50) DEFAULT 'NEWEST_FIRST', onlyOnline BOOLEAN DEFAULT FALSE)"
               );
               stmt.executeUpdate("CREATE TABLE IF NOT EXISTS online_players (nickname VARCHAR(255) PRIMARY KEY, server VARCHAR(255))");
               stmt.executeUpdate(
                  "CREATE TABLE IF NOT EXISTS report_cooldowns (sender VARCHAR(255), target VARCHAR(255), cooldown_end TIMESTAMP, PRIMARY KEY (sender, target), INDEX idx_cooldown (cooldown_end))"
               );
               this.addMissingColumns();
            } catch (Throwable var7) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var8) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var9) {
      }
   }

   private void addMissingColumns() {
      String[] columnsToCheck = new String[]{"totalSuccessCheck", "totalFailureCheck", "totalSuccessReports", "totalFailureReports", "sortType", "onlyOnline"};

      try {
         Connection conn = this.getConnection();

         try {
            for (String column : columnsToCheck) {
               if (!this.columnExists(conn, "reputation", column)) {
                  Statement stmt = conn.createStatement();

                  try {
                     String dataType = this.getColumnDataType(column);
                     stmt.executeUpdate("ALTER TABLE reputation ADD COLUMN " + column + " " + dataType);
                     Bukkit.getLogger().info("[CheatReport] Добавлена колонка: " + column);
                  } catch (Throwable var12) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var11) {
                           var12.addSuppressed(var11);
                        }
                     }

                     throw var12;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               }
            }
         } catch (Throwable var13) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }
            }

            throw var13;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var14) {
         Bukkit.getLogger().severe("[CheatReport] Ошибка при добавлении колонок: " + var14.getMessage());
      }
   }

   private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
      String query = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
      PreparedStatement stmt = conn.prepareStatement(query);

      boolean var7;
      try {
         stmt.setString(1, tableName);
         stmt.setString(2, columnName);
         ResultSet rs = stmt.executeQuery();
         var7 = rs.next() && rs.getInt(1) > 0;
      } catch (Throwable var9) {
         if (stmt != null) {
            try {
               stmt.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (stmt != null) {
         stmt.close();
      }

      return var7;
   }

   private String getColumnDataType(String columnName) {
      switch (columnName) {
         case "totalSuccessCheck":
         case "totalFailureCheck":
         case "totalSuccessReports":
         case "totalFailureReports":
            return "INT DEFAULT 0";
         case "sortType":
            return "VARCHAR(50) DEFAULT 'NEWEST_FIRST'";
         case "onlyOnline":
            return "BOOLEAN DEFAULT FALSE";
         default:
            return "VARCHAR(255)";
      }
   }
}
