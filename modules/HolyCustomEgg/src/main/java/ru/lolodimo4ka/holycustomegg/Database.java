package ru.lolodimo4ka.holycustomegg;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class Database {
   private final HolyCustomEgg plugin;
   private HikariDataSource dataSource;

   public Database(HolyCustomEgg plugin) {
      this.plugin = plugin;
      this.initializeDataSource();
      this.createTable();
   }

   private void initializeDataSource() {
      FileConfiguration config = this.plugin.getConfig();
      String type = config.getString("database.type", "sqlite");
      HikariConfig hikariConfig = new HikariConfig();
      if ("mysql".equalsIgnoreCase(type)) {
         String host = config.getString("database.host", "localhost");
         String database = config.getString("database.database", "minecraft");
         hikariConfig.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
         hikariConfig.setUsername(config.getString("database.user", "root"));
         hikariConfig.setPassword(config.getString("database.password", ""));
         hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
         hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
         hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
         hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
         hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
         hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
         hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
         hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
      } else {
         hikariConfig.setJdbcUrl("jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/spawners.db");
      }

      hikariConfig.setMaximumPoolSize(10);
      hikariConfig.setMinimumIdle(2);
      hikariConfig.setConnectionTimeout(30000L);
      hikariConfig.setIdleTimeout(600000L);
      hikariConfig.setMaxLifetime(1800000L);
      hikariConfig.setPoolName("HolyCustomEgg-Hikari");
      hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
      this.dataSource = new HikariDataSource(hikariConfig);
   }

   private void createTable() {
      CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.isMySQL()) {
               sql = "CREATE TABLE IF NOT EXISTS active_spawners (id INT AUTO_INCREMENT PRIMARY KEY,world_uuid VARCHAR(36) NOT NULL,x DOUBLE NOT NULL,y DOUBLE NOT NULL,z DOUBLE NOT NULL,egg_id VARCHAR(255) NOT NULL,last_change_time BIGINT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY unique_location (world_uuid, x, y, z))";
            } else {
               sql = "CREATE TABLE IF NOT EXISTS active_spawners (id INTEGER PRIMARY KEY AUTOINCREMENT,world_uuid VARCHAR(36) NOT NULL,x REAL NOT NULL,y REAL NOT NULL,z REAL NOT NULL,egg_id VARCHAR(255) NOT NULL,last_change_time BIGINT NOT NULL,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,UNIQUE(world_uuid, x, y, z))";
            }

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  Statement stmt = conn.createStatement();

                  try {
                     stmt.execute(sql);
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
            } catch (SQLException var10) {
            }
         }
      );
   }

   public CompletableFuture<Void> saveActiveSpawner(Location location, String eggId, long lastChangeTime) {
      return CompletableFuture.runAsync(
         () -> {
            String sql;
            if (this.isMySQL()) {
               sql = "INSERT INTO active_spawners (world_uuid, x, y, z, egg_id, last_change_time) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE egg_id = VALUES(egg_id), last_change_time = VALUES(last_change_time)";
            } else {
               sql = "INSERT OR REPLACE INTO active_spawners (world_uuid, x, y, z, egg_id, last_change_time) VALUES (?, ?, ?, ?, ?, ?)";
            }

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement stmt = conn.prepareStatement(sql);

                  try {
                     stmt.setString(1, location.getWorld().getUID().toString());
                     stmt.setDouble(2, location.getX());
                     stmt.setDouble(3, location.getY());
                     stmt.setDouble(4, location.getZ());
                     stmt.setString(5, eggId);
                     stmt.setLong(6, lastChangeTime);
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
            } catch (SQLException var14) {
            }
         }
      );
   }

   public CompletableFuture<Void> removeActiveSpawner(Location location) {
      return CompletableFuture.runAsync(() -> {
         String sql = "DELETE FROM active_spawners WHERE world_uuid = ? AND x = ? AND y = ? AND z = ?";

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement(sql);

               try {
                  stmt.setString(1, location.getWorld().getUID().toString());
                  stmt.setDouble(2, location.getX());
                  stmt.setDouble(3, location.getY());
                  stmt.setDouble(4, location.getZ());
                  stmt.executeUpdate();
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
         } catch (SQLException var11) {
         }
      });
   }

   public CompletableFuture<Map<Location, HolyCustomEgg.ActiveSpawner>> loadAllActiveSpawners() {
      return CompletableFuture.supplyAsync(() -> {
         Map<Location, HolyCustomEgg.ActiveSpawner> spawners = new HashMap<>();
         String sql = "SELECT world_uuid, x, y, z, egg_id, last_change_time FROM active_spawners";

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement(sql);

               try {
                  ResultSet rs = stmt.executeQuery();

                  try {
                     while (rs.next()) {
                        try {
                           UUID worldUuid = UUID.fromString(rs.getString("world_uuid"));
                           World world = this.plugin.getServer().getWorld(worldUuid);
                           if (world != null) {
                              double x = rs.getDouble("x");
                              double y = rs.getDouble("y");
                              double z = rs.getDouble("z");
                              String eggId = rs.getString("egg_id");
                              long lastChangeTime = rs.getLong("last_change_time");
                              Location location = new Location(world, x, y, z);
                              ManagerEgg egg = this.plugin.getEggs().get(eggId);
                              if (egg != null) {
                                 if (location.getChunk().isLoaded() && location.getBlock().getType() != Material.SPAWNER) {
                                    this.removeActiveSpawner(location);
                                 } else {
                                    HolyCustomEgg.ActiveSpawner activeSpawner = this.plugin.new ActiveSpawner(location, egg, lastChangeTime);
                                    spawners.put(location, activeSpawner);
                                 }
                              }
                           }
                        } catch (Exception var23) {
                        }
                     }
                  } catch (Throwable var24) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var22) {
                           var24.addSuppressed(var22);
                        }
                     }

                     throw var24;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var25) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var21) {
                        var25.addSuppressed(var21);
                     }
                  }

                  throw var25;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var26) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var20) {
                     var26.addSuppressed(var20);
                  }
               }

               throw var26;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
         }

         return spawners;
      });
   }

   private boolean isMySQL() {
      String type = this.plugin.getConfig().getString("database.type", "sqlite");
      return "mysql".equalsIgnoreCase(type);
   }

   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }
}
