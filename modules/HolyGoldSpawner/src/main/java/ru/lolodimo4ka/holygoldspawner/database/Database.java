package ru.lolodimo4ka.holygoldspawner.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lolodimo4ka.holygoldspawner.managers.SpawnerManager;

public class Database {
   private final JavaPlugin plugin;
   private HikariDataSource dataSource;
   private String databaseType;
   private boolean isShuttingDown = false;
   private boolean initialized = false;

   public Database(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
      this.databaseType = this.plugin.getConfig().getString("database.type", "sqlite").toLowerCase();

      try {
         HikariConfig config = new HikariConfig();
         if (this.databaseType.equals("mysql")) {
            String host = this.plugin.getConfig().getString("database.host", "localhost");
            int port = this.plugin.getConfig().getInt("database.port", 3306);
            String database = this.plugin.getConfig().getString("database.database", "minecraft");
            String username = this.plugin.getConfig().getString("database.user", "root");
            String password = this.plugin.getConfig().getString("database.password", "");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
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
            this.plugin.getDataFolder().mkdirs();
            String jdbcUrl = "jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/spawners.db";
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.addDataSourceProperty("foreign_keys", "true");
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");
         }

         config.setMaximumPoolSize(10);
         config.setMinimumIdle(2);
         config.setConnectionTimeout(30000L);
         config.setIdleTimeout(600000L);
         config.setMaxLifetime(1800000L);
         config.setLeakDetectionThreshold(60000L);
         config.setConnectionTestQuery("SELECT 1");
         this.dataSource = new HikariDataSource(config);
         this.createTable();
         this.initialized = true;
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   private void createTable() {
      String sql;
      if (this.databaseType.equals("mysql")) {
         sql = "CREATE TABLE IF NOT EXISTS golden_spawners (id INT AUTO_INCREMENT PRIMARY KEY,world VARCHAR(255) NOT NULL,x INT NOT NULL,y INT NOT NULL,z INT NOT NULL,owner_uuid VARCHAR(36) NOT NULL,durability INT NOT NULL,placement_time BIGINT NOT NULL,UNIQUE KEY unique_location (world, x, y, z))";
      } else {
         sql = "CREATE TABLE IF NOT EXISTS golden_spawners (id INTEGER PRIMARY KEY AUTOINCREMENT,world TEXT NOT NULL,x INTEGER NOT NULL,y INTEGER NOT NULL,z INTEGER NOT NULL,owner_uuid TEXT NOT NULL,durability INTEGER NOT NULL,placement_time BIGINT NOT NULL,UNIQUE(world, x, y, z))";
      }

      try {
         Connection connection = this.dataSource.getConnection();

         try {
            Statement statement = connection.createStatement();

            try {
               statement.execute(sql);
            } catch (Throwable var8) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var9) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var10) {
         var10.printStackTrace();
      }
   }

   public void loadSpawners(SpawnerManager spawnerManager) {
      if (!this.isShuttingDown && this.plugin.isEnabled() && this.initialized) {
         String sql = "SELECT * FROM golden_spawners";

         try {
            Connection connection = this.dataSource.getConnection();

            try {
               Statement statement = connection.createStatement();

               try {
                  ResultSet resultSet = statement.executeQuery(sql);

                  try {
                     int loadedCount = 0;

                     while (resultSet.next()) {
                        try {
                           String worldName = resultSet.getString("world");
                           int x = resultSet.getInt("x");
                           int y = resultSet.getInt("y");
                           int z = resultSet.getInt("z");
                           UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));
                           int durability = resultSet.getInt("durability");
                           long placementTime = resultSet.getLong("placement_time");
                           Bukkit.getScheduler().runTask(this.plugin, () -> {
                              Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                              if (location.getWorld() != null) {
                                 spawnerManager.addSpawnerToMemory(location, owner, durability, placementTime);
                              }
                           });
                           loadedCount++;
                        } catch (Exception var18) {
                        }
                     }
                  } catch (Throwable var19) {
                     if (resultSet != null) {
                        try {
                           resultSet.close();
                        } catch (Throwable var17) {
                           var19.addSuppressed(var17);
                        }
                     }

                     throw var19;
                  }

                  if (resultSet != null) {
                     resultSet.close();
                  }
               } catch (Throwable var20) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var16) {
                        var20.addSuppressed(var16);
                     }
                  }

                  throw var20;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var21) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var15) {
                     var21.addSuppressed(var15);
                  }
               }

               throw var21;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var22) {
            var22.printStackTrace();
         }
      }
   }

   public CompletableFuture<Void> saveSpawners(SpawnerManager spawnerManager) {
      return !this.isShuttingDown && this.plugin.isEnabled() && this.initialized
         ? CompletableFuture.runAsync(() -> this.saveSpawnersSync(spawnerManager))
         : CompletableFuture.completedFuture(null);
   }

   public void saveSpawnersSync(SpawnerManager spawnerManager) {
      if (!this.isShuttingDown && this.plugin.isEnabled() && this.initialized) {
         try {
            Connection connection = this.dataSource.getConnection();

            try {
               connection.setAutoCommit(false);

               try {
                  String clearSql = "DELETE FROM golden_spawners";
                  PreparedStatement statement = connection.prepareStatement(clearSql);

                  try {
                     statement.executeUpdate();
                  } catch (Throwable var13) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var13.addSuppressed(var12);
                        }
                     }

                     throw var13;
                  }

                  if (statement != null) {
                     statement.close();
                  }

                  String insertSql = "INSERT INTO golden_spawners (world, x, y, z, owner_uuid, durability, placement_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
                  PreparedStatement statementx = connection.prepareStatement(insertSql);

                  try {
                     int batchCount = 0;

                     for (Location location : spawnerManager.getGoldenSpawners()) {
                        if (location.getWorld() != null) {
                           statementx.setString(1, location.getWorld().getName());
                           statementx.setInt(2, location.getBlockX());
                           statementx.setInt(3, location.getBlockY());
                           statementx.setInt(4, location.getBlockZ());
                           UUID owner = spawnerManager.getSpawnerOwner(location);
                           if (owner != null) {
                              statementx.setString(5, owner.toString());
                              statementx.setInt(6, spawnerManager.getSpawnerDurability(location));
                              statementx.setLong(7, spawnerManager.getSpawnerPlacementTime(location));
                              statementx.addBatch();
                              if (++batchCount % 100 == 0) {
                                 statementx.executeBatch();
                              }
                           }
                        }
                     }

                     if (batchCount % 100 != 0) {
                        statementx.executeBatch();
                     }
                  } catch (Throwable var14) {
                     if (statementx != null) {
                        try {
                           statementx.close();
                        } catch (Throwable var11) {
                           var14.addSuppressed(var11);
                        }
                     }

                     throw var14;
                  }

                  if (statementx != null) {
                     statementx.close();
                  }

                  connection.commit();
               } catch (Exception var15) {
                  connection.rollback();
                  throw var15;
               }
            } catch (Throwable var16) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var10) {
                     var16.addSuppressed(var10);
                  }
               }

               throw var16;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }
      }
   }

   public void addSpawner(Location location, UUID owner, int durability) {
      if (!this.isShuttingDown && this.plugin.isEnabled() && this.initialized) {
         CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO golden_spawners (world, x, y, z, owner_uuid, durability, placement_time) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try {
               Connection connection = this.dataSource.getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(sql);

                  try {
                     statement.setString(1, location.getWorld().getName());
                     statement.setInt(2, location.getBlockX());
                     statement.setInt(3, location.getBlockY());
                     statement.setInt(4, location.getBlockZ());
                     statement.setString(5, owner.toString());
                     statement.setInt(6, durability);
                     statement.setLong(7, System.currentTimeMillis());
                     statement.executeUpdate();
                  } catch (Throwable var11) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var12) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException var13) {
            }
         });
      }
   }

   public void removeSpawner(Location location) {
      if (!this.isShuttingDown && this.plugin.isEnabled() && this.initialized) {
         CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM golden_spawners WHERE world = ? AND x = ? AND y = ? AND z = ?";

            try {
               Connection connection = this.dataSource.getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(sql);

                  try {
                     statement.setString(1, location.getWorld().getName());
                     statement.setInt(2, location.getBlockX());
                     statement.setInt(3, location.getBlockY());
                     statement.setInt(4, location.getBlockZ());
                     statement.executeUpdate();
                  } catch (Throwable var9) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var8) {
                           var9.addSuppressed(var8);
                        }
                     }

                     throw var9;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var10) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                     }
                  }

                  throw var10;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException var11) {
            }
         });
      }
   }

   public void updateDurability(Location location, int durability) {
      if (!this.isShuttingDown && this.plugin.isEnabled() && this.initialized) {
         CompletableFuture.runAsync(() -> {
            String sql = "UPDATE golden_spawners SET durability = ? WHERE world = ? AND x = ? AND y = ? AND z = ?";

            try {
               Connection connection = this.dataSource.getConnection();

               try {
                  PreparedStatement statement = connection.prepareStatement(sql);

                  try {
                     statement.setInt(1, durability);
                     statement.setString(2, location.getWorld().getName());
                     statement.setInt(3, location.getBlockX());
                     statement.setInt(4, location.getBlockY());
                     statement.setInt(5, location.getBlockZ());
                     statement.executeUpdate();
                  } catch (Throwable var10) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var11) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                     }
                  }

                  throw var11;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (SQLException var12) {
            }
         });
      }
   }

   public void closeConnection() {
      this.isShuttingDown = true;
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   public boolean isConnected() {
      return this.dataSource != null && !this.dataSource.isClosed() && this.initialized;
   }

   public String getDatabaseType() {
      return this.databaseType;
   }
}
