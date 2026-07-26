package org.dimasik.holyfreeze;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
   private final String serverName;
   private final String connectionUrl;
   private Connection connection;

   public DatabaseManager(String serverName, String host, String database, String username, String password) {
      this.serverName = serverName;
      this.connectionUrl = "jdbc:mysql://" + host + "/" + database + "?user=" + username + "&password=" + password + "&autoReconnect=true";
      this.initializeDatabase();
   }

   private void initializeDatabase() {
      CompletableFuture.runAsync(() -> {
         try {
            this.reconnectIfNeeded();
            Statement statement = this.connection.createStatement();

            try {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_servers (nickname VARCHAR(16) PRIMARY KEY, server_name VARCHAR(64) NOT NULL)");
            } catch (Throwable var5) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (SQLException var6) {
            var6.printStackTrace();
         }
      });
   }

   private void reconnectIfNeeded() throws SQLException {
      if (this.connection == null || this.connection.isClosed() || !this.connection.isValid(1)) {
         this.connection = DriverManager.getConnection(this.connectionUrl);
      }
   }

   public CompletableFuture<Void> addPlayer(String nickname) {
      return CompletableFuture.runAsync(() -> {
         try {
            this.reconnectIfNeeded();
            String sql = "INSERT INTO player_servers (nickname, server_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE server_name = ?";
            PreparedStatement statement = this.connection.prepareStatement(sql);

            try {
               statement.setString(1, nickname);
               statement.setString(2, this.serverName);
               statement.setString(3, this.serverName);
               statement.executeUpdate();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (SQLException var8) {
            var8.printStackTrace();
         }
      });
   }

   public CompletableFuture<String> getServerByPlayer(String nickname) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            this.reconnectIfNeeded();
            String sql = "SELECT server_name FROM player_servers WHERE nickname = ?";
            PreparedStatement statement = this.connection.prepareStatement(sql);

            String var5;
            label79: {
               try {
                  statement.setString(1, nickname);
                  ResultSet resultSet = statement.executeQuery();

                  label74: {
                     try {
                        if (!resultSet.next()) {
                           break label74;
                        }

                        var5 = resultSet.getString("server_name");
                     } catch (Throwable var9) {
                        if (resultSet != null) {
                           try {
                              resultSet.close();
                           } catch (Throwable var8) {
                              var9.addSuppressed(var8);
                           }
                        }

                        throw var9;
                     }

                     if (resultSet != null) {
                        resultSet.close();
                     }
                     break label79;
                  }

                  if (resultSet != null) {
                     resultSet.close();
                  }
               } catch (Throwable var10) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                     }
                  }

                  throw var10;
               }

               if (statement != null) {
                  statement.close();
               }

               return null;
            }

            if (statement != null) {
               statement.close();
            }

            return var5;
         } catch (SQLException var11) {
            var11.printStackTrace();
            return null;
         }
      });
   }

   public CompletableFuture<Void> removePlayersByServer() {
      return CompletableFuture.runAsync(() -> {
         try {
            this.reconnectIfNeeded();
            String sql = "DELETE FROM player_servers WHERE server_name = ?";
            PreparedStatement statement = this.connection.prepareStatement(sql);

            try {
               statement.setString(1, this.serverName);
               statement.executeUpdate();
            } catch (Throwable var6) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (SQLException var7) {
            var7.printStackTrace();
         }
      });
   }

   public CompletableFuture<Void> removePlayer(String nickname) {
      return CompletableFuture.runAsync(() -> {
         try {
            this.reconnectIfNeeded();
            String sql = "DELETE FROM player_servers WHERE nickname = ?";
            PreparedStatement statement = this.connection.prepareStatement(sql);

            try {
               statement.setString(1, nickname);
               statement.executeUpdate();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (SQLException var8) {
            var8.printStackTrace();
         }
      });
   }
}
