package org.dimasik.cheatreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BatchDataHelper {
   private final DatabaseManager databaseManager;

   public BatchDataHelper(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
   }

   public Map<String, BatchDataHelper.PlayerReputationData> getBatchReputation(Collection<String> playerNames) throws SQLException {
      Map<String, BatchDataHelper.PlayerReputationData> result = new HashMap<>();
      if (playerNames.isEmpty()) {
         return result;
      } else {
         String placeholders = String.join(",", Collections.nCopies(playerNames.size(), "?"));
         String query = "SELECT player, totalSuccessCheck, totalFailureCheck, totalSuccessReports, totalFailureReports FROM reputation WHERE player IN ("
            + placeholders
            + ")";
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement(query);

            try {
               int index = 1;

               for (String player : playerNames) {
                  stmt.setString(index++, player);
               }

               ResultSet rs = stmt.executeQuery();

               while (rs.next()) {
                  String player = rs.getString("player");
                  BatchDataHelper.PlayerReputationData data = new BatchDataHelper.PlayerReputationData(
                     rs.getInt("totalSuccessCheck"), rs.getInt("totalFailureCheck"), rs.getInt("totalSuccessReports"), rs.getInt("totalFailureReports")
                  );
                  result.put(player, data);
               }
            } catch (Throwable var13) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var14) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }
            }

            throw var14;
         }

         if (conn != null) {
            conn.close();
         }

         for (String player : playerNames) {
            result.putIfAbsent(player, new BatchDataHelper.PlayerReputationData(0, 0, 0, 0));
         }

         return result;
      }
   }

   public Map<String, String> getBatchOnlineStatus(Collection<String> playerNames) throws SQLException {
      Map<String, String> result = new HashMap<>();
      if (playerNames.isEmpty()) {
         return result;
      } else {
         String placeholders = String.join(",", Collections.nCopies(playerNames.size(), "?"));
         String query = "SELECT nickname, server FROM online_players WHERE nickname IN (" + placeholders + ")";
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement(query);

            try {
               int index = 1;

               for (String player : playerNames) {
                  stmt.setString(index++, player);
               }

               ResultSet rs = stmt.executeQuery();

               while (rs.next()) {
                  String nickname = rs.getString("nickname");
                  String server = rs.getString("server");
                  result.put(nickname, server != null ? "Онлайн | " + server : "Оффлайн");
               }
            } catch (Throwable var13) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var14) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }
            }

            throw var14;
         }

         if (conn != null) {
            conn.close();
         }

         for (String player : playerNames) {
            result.putIfAbsent(player, "Оффлайн");
         }

         return result;
      }
   }

   public Map<String, Integer> getBatchReportsCount(Collection<String> targets) throws SQLException {
      Map<String, Integer> result = new HashMap<>();
      if (targets.isEmpty()) {
         return result;
      } else {
         String placeholders = String.join(",", Collections.nCopies(targets.size(), "?"));
         String query = "SELECT target, COUNT(*) as count FROM reports WHERE target IN (" + placeholders + ") AND type = 'EXPECT' GROUP BY target";
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement(query);

            try {
               int index = 1;

               for (String target : targets) {
                  stmt.setString(index++, target);
               }

               ResultSet rs = stmt.executeQuery();

               while (rs.next()) {
                  String target = rs.getString("target");
                  int count = rs.getInt("count");
                  result.put(target, count);
               }
            } catch (Throwable var13) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var14) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }
            }

            throw var14;
         }

         if (conn != null) {
            conn.close();
         }

         for (String target : targets) {
            result.putIfAbsent(target, 0);
         }

         return result;
      }
   }

   public static class PlayerReputationData {
      public final int successChecks;
      public final int failureChecks;
      public final int successReports;
      public final int failureReports;

      public PlayerReputationData(int successChecks, int failureChecks, int successReports, int failureReports) {
         this.successChecks = successChecks;
         this.failureChecks = failureChecks;
         this.successReports = successReports;
         this.failureReports = failureReports;
      }
   }
}
