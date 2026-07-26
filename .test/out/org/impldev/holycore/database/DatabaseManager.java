package org.impldev.holycore.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import org.impldev.holycore.HolyCore;

public class DatabaseManager {
   private final HolyCore plugin;
   private final String host;
   private final String database;
   private final String username;
   private final String password;
   private final int port;

   public DatabaseManager(HolyCore plugin) {
      this.plugin = plugin;
      this.host = plugin.getConfig().getString("database.host", "localhost");
      this.port = plugin.getConfig().getInt("database.port", 3306);
      this.database = plugin.getConfig().getString("database.database", "holycore");
      this.username = plugin.getConfig().getString("database.user", "root");
      this.password = plugin.getConfig().getString("database.password", "");
   }

   private Connection getConnection() throws SQLException {
      String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
      return DriverManager.getConnection(url, this.username, this.password);
   }

   public void initializeDatabase() {
      try {
         Connection conn = this.getConnection();

         try {
            Statement stmt = conn.createStatement();

            try {
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS holy_first_join_rewards (id INT AUTO_INCREMENT PRIMARY KEY,player_uuid VARCHAR(36) NOT NULL UNIQUE,player_name VARCHAR(16) NOT NULL,reward_given BOOLEAN NOT NULL DEFAULT FALSE,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
               );
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS holy_rewards (id INT AUTO_INCREMENT PRIMARY KEY,reward_type VARCHAR(20) NOT NULL,player_uuid VARCHAR(36) NOT NULL,player_name VARCHAR(16) NOT NULL,reward_given BOOLEAN NOT NULL DEFAULT FALSE,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
               );
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
         } catch (Throwable var81) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var5) {
                  var81.addSuppressed(var5);
               }
            }

            throw var81;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var9) {
         this.plugin.getLogger().severe("Database Ошибка инициализации: " + var9.getMessage());
      }
   }

   public CompletableFuture<Boolean> hasPlayerReceivedFirstJoinRewardAsync(String playerUUID) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Connection conn = this.getConnection();

            Boolean result;
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT reward_given FROM holy_first_join_rewards WHERE player_uuid = ?");

               try {
                  stmt.setString(1, playerUUID);
                  ResultSet rs = stmt.executeQuery();
                  result = rs.next() && rs.getBoolean("reward_given");
               } catch (Throwable var91) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var8) {
                        var91.addSuppressed(var8);
                     }
                  }

                  throw var91;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }

            return result;
         } catch (SQLException var11) {
            return false;
         }
      });
   }

   public CompletableFuture<Boolean> saveFirstJoinRewardAsync(String playerUUID, String playerName) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Connection conn = this.getConnection();

               Boolean result;
               try {
                  PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO holy_first_join_rewards (player_uuid, player_name, reward_given) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE player_name = ?, reward_given = ?"
                  );

                  try {
                     stmt.setString(1, playerUUID);
                     stmt.setString(2, playerName);
                     stmt.setBoolean(3, true);
                     stmt.setString(4, playerName);
                     stmt.setBoolean(5, true);
                     result = stmt.executeUpdate() > 0;
                  } catch (Throwable var101) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var9) {
                           var101.addSuppressed(var9);
                        }
                     }

                     throw var101;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable var11) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                     }
                  }

                  throw var11;
               }

               if (conn != null) {
                  conn.close();
               }

               return result;
            } catch (SQLException var12) {
               return false;
            }
         }
      );
   }

   public CompletableFuture<Integer> getRewardCountAsync(String rewardType) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Connection conn = this.getConnection();

            Integer result;
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM holy_rewards WHERE reward_type = ? AND reward_given = TRUE");

               try {
                  stmt.setString(1, rewardType);
                  ResultSet rs = stmt.executeQuery();
                  result = rs.next() ? rs.getInt("count") : 0;
               } catch (Throwable var91) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var8) {
                        var91.addSuppressed(var8);
                     }
                  }

                  throw var91;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }

            return result;
         } catch (SQLException var11) {
            return 0;
         }
      });
   }

   public CompletableFuture<Boolean> saveRewardAsync(String rewardType, String playerUUID, String playerName) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Connection conn = this.getConnection();

               Boolean result;
               try {
                  PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO holy_rewards (reward_type, player_uuid, player_name, reward_given) VALUES (?, ?, ?, ?)"
                  );

                  try {
                     stmt.setString(1, rewardType);
                     stmt.setString(2, playerUUID);
                     stmt.setString(3, playerName);
                     stmt.setBoolean(4, true);
                     result = stmt.executeUpdate() > 0;
                  } catch (Throwable var111) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var10) {
                           var111.addSuppressed(var10);
                        }
                     }

                     throw var111;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable var12) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (conn != null) {
                  conn.close();
               }

               return result;
            } catch (SQLException var13) {
               return false;
            }
         }
      );
   }
}
