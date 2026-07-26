package org.dimasik.cheatreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;

public class ReputationManager {
   private final DatabaseManager databaseManager;
   private final CheatReport plugin;
   private final BatchDataHelper batchHelper;

   public ReputationManager(DatabaseManager databaseManager, CheatReport plugin) {
      this.databaseManager = databaseManager;
      this.plugin = plugin;
      this.batchHelper = new BatchDataHelper(databaseManager);
   }

   public BatchDataHelper.PlayerReputationData getPlayerReputation(String playerName) {
      try {
         Connection conn = this.databaseManager.getConnection();

         BatchDataHelper.PlayerReputationData var5;
         label79: {
            try {
               PreparedStatement stmt = conn.prepareStatement(
                  "SELECT totalSuccessCheck, totalFailureCheck, totalSuccessReports, totalFailureReports FROM reputation WHERE player = ?"
               );

               label74: {
                  try {
                     stmt.setString(1, playerName);
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        break label74;
                     }

                     var5 = new BatchDataHelper.PlayerReputationData(
                        rs.getInt("totalSuccessCheck"), rs.getInt("totalFailureCheck"), rs.getInt("totalSuccessReports"), rs.getInt("totalFailureReports")
                     );
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
                  break label79;
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

            return new BatchDataHelper.PlayerReputationData(0, 0, 0, 0);
         }

         if (conn != null) {
            conn.close();
         }

         return var5;
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка при получении репутации: " + var10.getMessage());
         return new BatchDataHelper.PlayerReputationData(0, 0, 0, 0);
      }
   }

   public void updateReputation(Report report, boolean accepted, int reportCount) {
      this.plugin
         .getServer()
         .getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               try {
                  Connection conn = this.databaseManager.getConnection();

                  try {
                     String updateSender = "INSERT INTO reputation (player, totalSuccessCheck, totalFailureCheck) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE totalSuccessCheck = totalSuccessCheck + ?, totalFailureCheck = totalFailureCheck + ?";
                     PreparedStatement stmt = conn.prepareStatement(updateSender);

                     try {
                        stmt.setString(1, report.sender);
                        stmt.setInt(2, accepted ? reportCount : 0);
                        stmt.setInt(3, accepted ? 0 : reportCount);
                        stmt.setInt(4, accepted ? reportCount : 0);
                        stmt.setInt(5, accepted ? 0 : reportCount);
                        stmt.executeUpdate();
                     } catch (Throwable var14) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var12) {
                              var14.addSuppressed(var12);
                           }
                        }

                        throw var14;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }

                     String updateTarget = "INSERT INTO reputation (player, totalSuccessReports, totalFailureReports) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE totalSuccessReports = totalSuccessReports + ?, totalFailureReports = totalFailureReports + ?";
                     PreparedStatement stmtx = conn.prepareStatement(updateTarget);

                     try {
                        stmtx.setString(1, report.target);
                        stmtx.setInt(2, accepted ? reportCount : 0);
                        stmtx.setInt(3, accepted ? 0 : reportCount);
                        stmtx.setInt(4, accepted ? reportCount : 0);
                        stmtx.setInt(5, accepted ? 0 : reportCount);
                        stmtx.executeUpdate();
                     } catch (Throwable var13) {
                        if (stmtx != null) {
                           try {
                              stmtx.close();
                           } catch (Throwable var11) {
                              var13.addSuppressed(var11);
                           }
                        }

                        throw var13;
                     }

                     if (stmtx != null) {
                        stmtx.close();
                     }
                  } catch (Throwable var15) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var10) {
                           var15.addSuppressed(var10);
                        }
                     }

                     throw var15;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var16) {
                  this.plugin.getLogger().severe("Ошибка обновления репутации: " + var16.getMessage());
               }
            }
         );
   }

   public boolean playerExists(String playerName) {
      try {
         Connection conn = this.databaseManager.getConnection();

         boolean var4;
         try {
            PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM reputation WHERE player = ? LIMIT 1");

            try {
               stmt.setString(1, playerName);
               var4 = stmt.executeQuery().next();
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

         return var4;
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка проверки игрока: " + var10.getMessage());
         return false;
      }
   }

   public void setSortType(Player player, SortType sortType) {
      String query = "INSERT INTO reputation (player, sortType) VALUES (?, ?) ON DUPLICATE KEY UPDATE sortType = ?";

      try {
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement(query);

            try {
               stmt.setString(1, player.getName());
               stmt.setString(2, sortType.name());
               stmt.setString(3, sortType.name());
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
         this.plugin.getLogger().severe("Ошибка при обновлении типа сортировки: " + var12.getMessage());
      }
   }

   public SortType getSortType(Player player) {
      try {
         Connection conn = this.databaseManager.getConnection();

         SortType var5;
         label78: {
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT sortType FROM reputation WHERE player = ?");

               label80: {
                  try {
                     stmt.setString(1, player.getName());
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        this.setSortType(player, SortType.NEWEST_FIRST);
                        var5 = SortType.NEWEST_FIRST;
                        break label80;
                     }

                     var5 = SortType.valueOf(rs.getString("sortType"));
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
      } catch (SQLException | IllegalArgumentException var10) {
         this.plugin.getLogger().severe("Ошибка при получении типа сортировки: " + var10.getMessage());
         return SortType.NEWEST_FIRST;
      }
   }

   public int getSuccessChecks(String player) {
      return this.getPlayerReputation(player).successChecks;
   }

   public int getFailureChecks(String player) {
      return this.getPlayerReputation(player).failureChecks;
   }

   public int getSuccessReports(String player) {
      return this.getPlayerReputation(player).successReports;
   }

   public int getFailureReports(String player) {
      return this.getPlayerReputation(player).failureReports;
   }
}
