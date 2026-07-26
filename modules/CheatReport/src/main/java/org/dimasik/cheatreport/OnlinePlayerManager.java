package org.dimasik.cheatreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class OnlinePlayerManager {
   private final DatabaseManager databaseManager;
   private final CheatReport plugin;
   private final Set<String> onlinePlayers = Collections.synchronizedSet(new HashSet<>());

   public OnlinePlayerManager(DatabaseManager databaseManager, CheatReport plugin) {
      this.databaseManager = databaseManager;
      this.plugin = plugin;
   }

   public void addPlayer(Player player) {
      this.onlinePlayers.add(player.getName().toLowerCase());
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
         String query = "INSERT INTO online_players (nickname, server) VALUES (?, ?) ON DUPLICATE KEY UPDATE server = ?";
         String repQuery = "INSERT IGNORE INTO reputation (player) VALUES (?)";

         try {
            Connection conn = this.databaseManager.getConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement(query);

               try {
                  stmt.setString(1, player.getName());
                  String server = this.plugin.getConfig().getString("mysql.server", "Анархия #1");
                  stmt.setString(2, server);
                  stmt.setString(3, server);
                  stmt.executeUpdate();
               } catch (Throwable var12) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var10) {
                        var12.addSuppressed(var10);
                     }
                  }

                  throw var12;
               }

               if (stmt != null) {
                  stmt.close();
               }

               stmt = conn.prepareStatement(repQuery);

               try {
                  stmt.setString(1, player.getName());
                  stmt.executeUpdate();
               } catch (Throwable var11) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var9) {
                        var11.addSuppressed(var9);
                     }
                  }

                  throw var11;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var13) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            this.plugin.getLogger().severe("Ошибка при добавлении игрока: " + var14.getMessage());
         }
      });
   }

   public void removePlayer(Player player) {
      this.onlinePlayers.remove(player.getName().toLowerCase());
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
         String query = "DELETE FROM online_players WHERE nickname = ?";

         try {
            Connection conn = this.databaseManager.getConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement(query);

               try {
                  stmt.setString(1, player.getName());
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
            this.plugin.getLogger().severe("Ошибка при удалении игрока: " + var11.getMessage());
         }
      });
   }

   public void toggleOnlyOnline(Player player, boolean onlyOnline) {
      String query = "UPDATE reputation SET onlyOnline = ? WHERE player = ?";

      try {
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement(query);

            try {
               stmt.setBoolean(1, onlyOnline);
               stmt.setString(2, player.getName());
               stmt.executeUpdate();
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
      } catch (SQLException var12) {
         this.plugin.getLogger().severe("Ошибка при переключении онлайн режима: " + var12.getMessage());
      }
   }

   public boolean isOnlyOnline(Player player) {
      String query = "SELECT onlyOnline FROM reputation WHERE player = ?";

      try {
         Connection conn = this.databaseManager.getConnection();

         boolean var6;
         label83: {
            try {
               PreparedStatement stmt = conn.prepareStatement(query);

               label78: {
                  try {
                     stmt.setString(1, player.getName());
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        break label78;
                     }

                     var6 = rs.getBoolean("onlyOnline");
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
                  break label83;
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

            return false;
         }

         if (conn != null) {
            conn.close();
         }

         return var6;
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка при проверке онлайн режима: " + var11.getMessage());
         return false;
      }
   }

   public boolean isPlayerOnline(String playerName) {
      return this.onlinePlayers.contains(playerName.toLowerCase());
   }
}
