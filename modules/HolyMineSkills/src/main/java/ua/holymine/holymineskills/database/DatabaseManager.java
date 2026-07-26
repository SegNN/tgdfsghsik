package ua.holymine.holymineskills.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.java.JavaPlugin;
import ua.holymine.holymineskills.config.ConfigManager;
import ua.holymine.holymineskills.util.Logger;

public class DatabaseManager {
   private final JavaPlugin plugin;
   private final ConfigManager configManager;
   private HikariDataSource dataSource;
   private final ExecutorService executor;
   private final ConcurrentHashMap<UUID, Double> playerPointsCache;
   private final ConcurrentHashMap<UUID, Long> cacheTimestamps;
   private static final long CACHE_DURATION = 300000L;

   public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.executor = Executors.newFixedThreadPool(2, r -> {
         Thread thread = new Thread(r);
         thread.setName("HolyMineSkills-DB-" + thread.getId());
         thread.setDaemon(true);
         return thread;
      });
      this.playerPointsCache = new ConcurrentHashMap<>();
      this.cacheTimestamps = new ConcurrentHashMap<>();
   }

   public boolean initialize() {
      try {
         return !this.createConnectionPool() ? false : this.createTables();
      } catch (Exception var2) {
         Logger.severe("Ошибка инициализации базы данных: " + var2.getMessage());
         var2.printStackTrace();
         return false;
      }
   }

   private boolean createConnectionPool() {
      try {
         HikariConfig hikariConfig = new HikariConfig();
         hikariConfig.setJdbcUrl(
            String.format(
               "jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&characterEncoding=utf8&useUnicode=true&serverTimezone=UTC",
               this.configManager.getDbHost(),
               this.configManager.getDbPort(),
               this.configManager.getDbName()
            )
         );
         hikariConfig.setUsername(this.configManager.getDbUser());
         hikariConfig.setPassword(this.configManager.getDbPassword());
         hikariConfig.setMaximumPoolSize(5);
         hikariConfig.setMinimumIdle(2);
         hikariConfig.setConnectionTimeout(10000L);
         hikariConfig.setIdleTimeout(300000L);
         hikariConfig.setMaxLifetime(600000L);
         hikariConfig.setLeakDetectionThreshold(30000L);
         hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
         hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
         hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
         this.dataSource = new HikariDataSource(hikariConfig);
         Connection testConn = this.dataSource.getConnection();

         boolean var8;
         label48: {
            try {
               if (testConn.isValid(5)) {
                  var8 = true;
                  break label48;
               }

               Logger.severe("Соединение с базой данных недействительно");
               var8 = false;
            } catch (Throwable var6) {
               if (testConn != null) {
                  try {
                     testConn.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (testConn != null) {
               testConn.close();
            }

            return var8;
         }

         if (testConn != null) {
            testConn.close();
         }

         return var8;
      } catch (Exception var71) {
         Logger.severe("Ошибка при создании пула соединений: " + var71.getMessage());
         return false;
      }
   }

   private boolean createTables() {
      try {
         Connection conn = this.dataSource.getConnection();

         boolean var5;
         try {
            Statement stmt = conn.createStatement();

            try {
               String createPlayerPointsTable = "CREATE TABLE IF NOT EXISTS player_points (uuid VARCHAR(36) PRIMARY KEY,username VARCHAR(16) NOT NULL,points DOUBLE DEFAULT 0.0,total_blocks BIGINT DEFAULT 0,last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,INDEX idx_username (username),INDEX idx_points (points)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
               String createBonusHistoryTable = "CREATE TABLE IF NOT EXISTS bonus_history (id INT AUTO_INCREMENT PRIMARY KEY,uuid VARCHAR(36) NOT NULL,bonus_name VARCHAR(50) NOT NULL,bonus_level INT NOT NULL,required_points DOUBLE NOT NULL,unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,INDEX idx_uuid (uuid),INDEX idx_bonus_name (bonus_name),INDEX idx_unlocked_at (unlocked_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
               stmt.execute(createPlayerPointsTable);
               stmt.execute(createBonusHistoryTable);
               var5 = true;
            } catch (Throwable var8) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var9) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (conn != null) {
            conn.close();
         }

         return var5;
      } catch (Exception var10) {
         Logger.severe("Ошибка при создании таблиц: " + var10.getMessage());
         return false;
      }
   }

   public CompletableFuture<Double> getPointsAsync(UUID playerUuid) {
      if (this.playerPointsCache.containsKey(playerUuid)) {
         Long timestamp = this.cacheTimestamps.get(playerUuid);
         if (timestamp != null && System.currentTimeMillis() - timestamp < 300000L) {
            return CompletableFuture.completedFuture(this.playerPointsCache.get(playerUuid));
         }
      }

      return CompletableFuture.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Double points;
            label78: {
               Double var7;
               try {
                  PreparedStatement stmt = conn.prepareStatement("SELECT points FROM player_points WHERE uuid = ?");

                  label80: {
                     try {
                        stmt.setString(1, playerUuid.toString());
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                           double pointsx = rs.getDouble("points");
                           this.playerPointsCache.put(playerUuid, pointsx);
                           this.cacheTimestamps.put(playerUuid, System.currentTimeMillis());
                           var7 = pointsx;
                           break label80;
                        }

                        this.playerPointsCache.put(playerUuid, 0.0);
                        this.cacheTimestamps.put(playerUuid, System.currentTimeMillis());
                        points = 0.0;
                     } catch (Throwable var10) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var9) {
                              var10.addSuppressed(var9);
                           }
                        }

                        throw var10;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                     break label78;
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

               return var7;
            }

            if (conn != null) {
               conn.close();
            }

            return points;
         } catch (Exception var121) {
            Logger.severe("Ошибка при получении очков игрока: " + var121.getMessage());
            return this.playerPointsCache.getOrDefault(playerUuid, 0.0);
         }
      }, this.executor);
   }

   public CompletableFuture<Void> addPointsAsync(UUID playerUuid, String username, double points) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_points (uuid, username, points) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE points = points + ?, username = ?"
                  );

                  try {
                     stmt.setString(1, playerUuid.toString());
                     stmt.setString(2, username);
                     stmt.setDouble(3, points);
                     stmt.setDouble(4, points);
                     stmt.setString(5, username);
                     stmt.executeUpdate();
                     Double currentPoints = this.playerPointsCache.get(playerUuid);
                     if (currentPoints != null) {
                        this.playerPointsCache.put(playerUuid, currentPoints + points);
                     } else {
                        this.playerPointsCache.put(playerUuid, points);
                     }

                     this.cacheTimestamps.put(playerUuid, System.currentTimeMillis());
                  } catch (Throwable var11) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
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
            } catch (Exception var131) {
               Logger.severe("Ошибка при добавлении очков игроку: " + var131.getMessage());
            }
         },
         this.executor
      );
   }

   public CompletableFuture<Void> addTotalBlocksAsync(UUID playerUuid, String username, long blocks) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_points (uuid, username, total_blocks) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE total_blocks = total_blocks + ?, username = ?"
                  );

                  try {
                     stmt.setString(1, playerUuid.toString());
                     stmt.setString(2, username);
                     stmt.setLong(3, blocks);
                     stmt.setLong(4, blocks);
                     stmt.setString(5, username);
                     stmt.executeUpdate();
                  } catch (Throwable var11) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
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
            } catch (Exception var131) {
               Logger.severe("Ошибка при добавлении блоков игроку: " + var131.getMessage());
            }
         },
         this.executor
      );
   }

   public CompletableFuture<Void> addTotalBlocksAsync(UUID playerUuid, long blocks) {
      return this.addTotalBlocksAsync(playerUuid, "unknown", blocks);
   }

   public CompletableFuture<Long> getTotalBlocksMinedAsync(UUID playerUuid) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Long var5;
            label78: {
               try {
                  PreparedStatement stmt = conn.prepareStatement("SELECT total_blocks FROM player_points WHERE uuid = ?");

                  label80: {
                     try {
                        stmt.setString(1, playerUuid.toString());
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                           var5 = rs.getLong("total_blocks");
                           break label80;
                        }

                        var5 = 0L;
                     } catch (Throwable var8) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var7) {
                              var8.addSuppressed(var7);
                           }
                        }

                        throw var8;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                     break label78;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable var9) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (conn != null) {
                  conn.close();
               }

               return var5;
            }

            if (conn != null) {
               conn.close();
            }

            return var5;
         } catch (Exception var101) {
            Logger.severe("Ошибка при получении количества блоков игрока: " + var101.getMessage());
            return 0L;
         }
      }, this.executor);
   }

   public CompletableFuture<Void> recordBonusUnlockAsync(UUID playerUuid, String bonusName, int bonusLevel, double requiredPoints) {
      return CompletableFuture.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement("INSERT INTO bonus_history (uuid, bonus_name, bonus_level, required_points) VALUES (?, ?, ?, ?)");

               try {
                  stmt.setString(1, playerUuid.toString());
                  stmt.setString(2, bonusName);
                  stmt.setInt(3, bonusLevel);
                  stmt.setDouble(4, requiredPoints);
                  stmt.executeUpdate();
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
         } catch (Exception var141) {
            Logger.severe("Ошибка при записи разблокировки бонуса: " + var141.getMessage());
         }
      }, this.executor);
   }

   public CompletableFuture<DatabaseManager.PlayerStats> getTopPlayersAsync(int limit) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               List<DatabaseManager.PlayerData> players = new ArrayList<>();
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement stmt = conn.prepareStatement("SELECT uuid, username, points FROM player_points ORDER BY points DESC LIMIT ?");

                  try {
                     stmt.setInt(1, limit);
                     ResultSet rs = stmt.executeQuery();

                     while (rs.next()) {
                        DatabaseManager.PlayerData playerData = new DatabaseManager.PlayerData(
                           UUID.fromString(rs.getString("uuid")), rs.getString("username"), rs.getDouble("points")
                        );
                        players.add(playerData);
                     }
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

               return new DatabaseManager.PlayerStats(players);
            } catch (Exception var111) {
               Logger.severe("Ошибка при получении топ игроков: " + var111.getMessage());
               return new DatabaseManager.PlayerStats(new ArrayList<>());
            }
         },
         this.executor
      );
   }

   public void shutdown() {
      try {
         this.playerPointsCache.clear();
         this.cacheTimestamps.clear();
         if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();

            try {
               if (!this.executor.awaitTermination(10L, TimeUnit.SECONDS)) {
                  this.executor.shutdownNow();
               }
            } catch (InterruptedException var2) {
               this.executor.shutdownNow();
            }
         }

         if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
         }
      } catch (Exception var3) {
         Logger.severe("Ошибка при отключении DatabaseManager: " + var3.getMessage());
      }
   }

   public static class PlayerData {
      private final UUID uuid;
      private final String username;
      private final double points;

      public PlayerData(UUID uuid, String username, double points) {
         this.uuid = uuid;
         this.username = username;
         this.points = points;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getUsername() {
         return this.username;
      }

      public double getPoints() {
         return this.points;
      }
   }

   public static class PlayerStats {
      private final List<DatabaseManager.PlayerData> players;

      public PlayerStats(List<DatabaseManager.PlayerData> players) {
         this.players = players;
      }

      public List<DatabaseManager.PlayerData> getPlayers() {
         return this.players;
      }
   }
}
