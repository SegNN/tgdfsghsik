package org.dimasik.cheatreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.nezxenka.syncservice.api.SyncAPI;

public class ReportManager implements CommandExecutor {
   private final CheatReport plugin;
   private final DatabaseManager databaseManager;
   private final ReputationManager reputationManager;
   private final BatchDataHelper batchHelper;
   private final ReportGUI reportGUI;

   public ReportManager(CheatReport plugin, DatabaseManager databaseManager, ReputationManager reputationManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
      this.reputationManager = reputationManager;
      this.batchHelper = new BatchDataHelper(databaseManager);
      this.reportGUI = new ReportGUI(this, databaseManager, plugin, reputationManager, this.batchHelper);
   }

   public void openReportsMenu(Player player, int page) {
      this.reportGUI.openReportsMenu(player, page);
   }

   public void sendReport(String sender, String target, String reason, Boolean checkCd) {
      this.plugin
         .getServer()
         .getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               try {
                  Connection conn = this.databaseManager.getConnection();

                  label102: {
                     try {
                        if (checkCd && this.hasCooldown(conn, sender, target)) {
                           break label102;
                        }

                        String insertQuery = "INSERT INTO reports (sender, target, reason, type, mode) VALUES (?, ?, ?, 'EXPECT', ?)";
                        PreparedStatement stmt = conn.prepareStatement(insertQuery);

                        try {
                           stmt.setString(1, sender);
                           stmt.setString(2, target);
                           stmt.setString(3, reason);
                           stmt.setString(4, this.plugin.getConfig().getString("mysql.server", "Анархия #1"));
                           stmt.executeUpdate();
                        } catch (Throwable var15) {
                           if (stmt != null) {
                              try {
                                 stmt.close();
                              } catch (Throwable var13) {
                                 var15.addSuppressed(var13);
                              }
                           }

                           throw var15;
                        }

                        if (stmt != null) {
                           stmt.close();
                        }

                        this.plugin
                           .getServer()
                           .getScheduler()
                           .runTask(
                              this.plugin,
                              () -> {
                                 int reports = this.getReportsCountForPlayer(target);
                                 String server = this.plugin.getConfig().getString("mysql.server", "Анархия #1");
                                 if (checkCd) {
                                    if (reports == 0) {
                                       Bukkit.dispatchCommand(
                                          Bukkit.getConsoleSender(),
                                          "litebans broadcast broadcast-type:report &f\n&x&F&7&6&E&0&6 &n◢&f Новая жалоба на &x&8&6&E&F&8&8"
                                             + target
                                             + "\n&x&F&7&6&E&0&6 ◤&f Режим: &x&D&B&A&9&4&6"
                                             + server
                                             + " &8| &x&D&B&A&9&4&6/report list&f\n"
                                       );
                                    } else {
                                       Bukkit.dispatchCommand(
                                          Bukkit.getConsoleSender(),
                                          "litebans broadcast broadcast-type:report &f\n&x&F&7&6&E&0&6 &n◢&f Новая жалоба на &x&8&6&E&F&8&8"
                                             + target
                                             + " &7( уже "
                                             + reports
                                             + " ).\n&x&F&7&6&E&0&6 ◤&f Режим: &x&D&B&A&9&4&6"
                                             + server
                                             + " &8| &x&D&B&A&9&4&6/report list&f\n"
                                       );
                                    }
                                 } else {
                                    String ping = "[неизвестно]";
                                    Player targetPlayer = Bukkit.getPlayer(target);
                                    if (targetPlayer != null) {
                                       ping = targetPlayer.getPing() + " ⇆";
                                    }

                                    String message = "&7[&x&F&B&0&8&0&8&lHAC&7]&f &x&F&B&0&8&0&8(%player% &7%ping%&x&F&B&0&8&0&8) &7провалил &6проверку античита &7() &f(&x&F&B&0&8&0&8%reports%VL&7&f) &7[%server%]";
                                    String escaped = message.replace("%player%", target)
                                       .replace("%ping%", ping)
                                       .replace("%server%", server)
                                       .replace("%reports%", reports + "");
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "litebans broadcast broadcast-type:report " + escaped);
                                 }
                              }
                           );
                        if (checkCd) {
                           String updateQuery = "INSERT INTO report_cooldowns (sender, target, cooldown_end) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE cooldown_end = ?";
                           PreparedStatement stmtx = conn.prepareStatement(updateQuery);

                           try {
                              Timestamp cooldownEnd = new Timestamp(System.currentTimeMillis() + 1800000L);
                              stmtx.setString(1, sender);
                              stmtx.setString(2, target);
                              stmtx.setTimestamp(3, cooldownEnd);
                              stmtx.setTimestamp(4, cooldownEnd);
                              stmtx.executeUpdate();
                           } catch (Throwable var14) {
                              if (stmtx != null) {
                                 try {
                                    stmtx.close();
                                 } catch (Throwable var12) {
                                    var14.addSuppressed(var12);
                                 }
                              }

                              throw var14;
                           }

                           if (stmtx != null) {
                              stmtx.close();
                           }
                        }
                     } catch (Throwable var16) {
                        if (conn != null) {
                           try {
                              conn.close();
                           } catch (Throwable var11) {
                              var16.addSuppressed(var11);
                           }
                        }

                        throw var16;
                     }

                     if (conn != null) {
                        conn.close();
                     }

                     return;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var17) {
                  this.plugin.getLogger().severe("Ошибка при отправке репорта: " + var17.getMessage());
               }
            }
         );
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fИспользование: &x&3&1&D&E&2&A/cheatreport [игрок] [причина]"));
         return true;
      } else if (args[0].equalsIgnoreCase("list")) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
         } else if (!sender.hasPermission("cheatreport.reports")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fУ вас &x&3&1&D&E&2&Aнет прав &fдля выполнения этой команды."));
            return true;
         } else {
            Player player = (Player)sender;
            if (args.length >= 2) {
               String target = args[1];
               if (!this.reputationManager.playerExists(target)) {
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fИгрок &x&3&1&D&E&2&A" + target + " &fне существует."));
                  return true;
               }

               this.reportGUI.openPlayerReportsMenu(player, target, 1);
            } else {
               this.openReportsMenu(player, 1);
            }

            return true;
         }
      } else if (args.length < 2) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fИспользование: &x&3&1&D&E&2&A/cheatreport [игрок] [причина]"));
         return true;
      } else {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            String target = args[0];
            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (player.getName().equalsIgnoreCase(target)) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fВы не можете отправить репорт на самого себя."));
               return true;
            }

            if (!this.reputationManager.playerExists(target)) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fИгрок &x&3&1&D&E&2&A" + target + " &fне существует."));
               return true;
            }

            if (this.hasCooldown(player.getName(), target)) {
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     " &x&0&E&C&E&F&4▶ &fВы &x&3&1&D&E&2&Aуже отправляли &fрепорт на этого игрока. &x&3&1&D&E&2&AПовторная отправка &fрепорта будет доступна через &x&3&1&D&E&2&A30 минут."
                  )
               );
               return true;
            }

            this.sendReport(player.getName(), target, reason, true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fРепорт успешно &x&3&1&D&E&2&Aотправлен."));
         } else {
            String targetPlayer = args[0];
            String reasonx = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            this.sendReport("System", targetPlayer, reasonx, false);
         }

         return true;
      }
   }

   private boolean hasCooldown(Connection conn, String sender, String target) throws SQLException {
      PreparedStatement stmt = conn.prepareStatement("SELECT cooldown_end FROM report_cooldowns WHERE sender = ? AND target = ?");

      boolean var7;
      label49: {
         try {
            stmt.setString(1, sender);
            stmt.setString(2, target);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
               Timestamp cooldownEnd = rs.getTimestamp("cooldown_end");
               var7 = cooldownEnd != null && cooldownEnd.after(new Timestamp(System.currentTimeMillis()));
               break label49;
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

         return false;
      }

      if (stmt != null) {
         stmt.close();
      }

      return var7;
   }

   private boolean hasCooldown(String sender, String target) {
      try {
         Connection conn = this.databaseManager.getConnection();

         boolean var4;
         try {
            var4 = this.hasCooldown(conn, sender, target);
         } catch (Throwable var7) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (conn != null) {
            conn.close();
         }

         return var4;
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка при проверке задержки: " + var8.getMessage());
         return false;
      }
   }

   public Report getReportById(int reportId) {
      try {
         Connection conn = this.databaseManager.getConnection();

         Report var5;
         label79: {
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reports WHERE id = ?");

               label74: {
                  try {
                     stmt.setInt(1, reportId);
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        break label74;
                     }

                     var5 = new Report(
                        rs.getInt("id"),
                        rs.getString("sender"),
                        rs.getString("target"),
                        rs.getTimestamp("date"),
                        rs.getString("reason"),
                        rs.getString("type"),
                        rs.getString("admin"),
                        rs.getString("mode")
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

            return null;
         }

         if (conn != null) {
            conn.close();
         }

         return var5;
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка при получении репорта: " + var10.getMessage());
         return null;
      }
   }

   public int getReportsCountForPlayer(String target) {
      try {
         Connection conn = this.databaseManager.getConnection();

         int var5;
         label79: {
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM reports WHERE target = ? AND type = 'EXPECT'");

               label74: {
                  try {
                     stmt.setString(1, target);
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        break label74;
                     }

                     var5 = rs.getInt("total");
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

            return 0;
         }

         if (conn != null) {
            conn.close();
         }

         return var5;
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка при получении количества репортов: " + var10.getMessage());
         return 0;
      }
   }

   public boolean hasActiveReport(Player player) {
      try {
         Connection conn = this.databaseManager.getConnection();

         boolean var5;
         try {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM reports WHERE admin = ? AND type = 'EXPECT' LIMIT 1");

            try {
               stmt.setString(1, player.getName());
               ResultSet rs = stmt.executeQuery();
               var5 = rs.next() && rs.getInt("total") > 0;
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
      } catch (SQLException var10) {
         return false;
      }
   }

   public List<Integer> getActiveReports(Player player) {
      List<Integer> activeReports = new ArrayList<>();

      try {
         Connection conn = this.databaseManager.getConnection();

         try {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM reports WHERE admin = ? AND type = 'EXPECT'");

            try {
               stmt.setString(1, player.getName());
               ResultSet rs = stmt.executeQuery();

               while (rs.next()) {
                  activeReports.add(rs.getInt("id"));
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
      } catch (SQLException var11) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fВнутрення ошибка SQL."));
      }

      return activeReports;
   }

   public void takeAllPlayerReports(Player player, String target) {
      player.closeInventory();
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fБерем дело..."));
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            Connection conn = this.databaseManager.getConnection();

            try {
               String updateQuery = "UPDATE reports SET admin = ? WHERE target = ? AND type = 'EXPECT' AND admin IS NULL";
               PreparedStatement updateStmt = conn.prepareStatement(updateQuery);

               try {
                  updateStmt.setString(1, player.getName());
                  updateStmt.setString(2, target);
                  int updatedRows = updateStmt.executeUpdate();
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     if (updatedRows > 0) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fВзято репортов на игрока &x&3&1&D&E&2&A" + target));
                        this.openReportsMenu(player, 1);
                     } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fНе удалось взять репорты."));
                        this.openReportsMenu(player, 1);
                     }
                  });
               } catch (Throwable var10) {
                  if (updateStmt != null) {
                     try {
                        updateStmt.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (updateStmt != null) {
                  updateStmt.close();
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
            this.plugin.getLogger().severe("Ошибка при взятии всех репортов: " + var12.getMessage());
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fОшибка при взятии репортов."));
               this.openReportsMenu(player, 1);
            });
         }
      });
   }

   public void resolveAllActiveReports(Player player, boolean accepted) {
      player.closeInventory();
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &f" + (accepted ? "Принимаем" : "Отклоняем") + " дело..."));
      this.plugin
         .getServer()
         .getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               try {
                  Connection conn = this.databaseManager.getConnection();

                  label132: {
                     try {
                        List<Report> activeReports = new ArrayList<>();
                        String selectQuery = "SELECT * FROM reports WHERE admin = ? AND type = 'EXPECT'";
                        PreparedStatement selectStmt = conn.prepareStatement(selectQuery);

                        try {
                           selectStmt.setString(1, player.getName());
                           ResultSet rs = selectStmt.executeQuery();

                           while (rs.next()) {
                              activeReports.add(
                                 new Report(
                                    rs.getInt("id"),
                                    rs.getString("sender"),
                                    rs.getString("target"),
                                    rs.getTimestamp("date"),
                                    rs.getString("reason"),
                                    rs.getString("type"),
                                    rs.getString("admin"),
                                    rs.getString("mode")
                                 )
                              );
                           }
                        } catch (Throwable var15) {
                           if (selectStmt != null) {
                              try {
                                 selectStmt.close();
                              } catch (Throwable var14) {
                                 var15.addSuppressed(var14);
                              }
                           }

                           throw var15;
                        }

                        if (selectStmt != null) {
                           selectStmt.close();
                        }

                        if (activeReports.isEmpty()) {
                           this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &fУ вас нет активных дел."));
                              this.openReportsMenu(player, 1);
                           });
                           break label132;
                        }

                        Map<String, Integer> targetReportsCount = new HashMap<>();

                        for (Report report : activeReports) {
                           targetReportsCount.put(report.target, targetReportsCount.getOrDefault(report.target, 0) + 1);
                        }

                        String updateQuery = "UPDATE reports SET type = ? WHERE admin = ? AND type = 'EXPECT'";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);

                        try {
                           updateStmt.setString(1, accepted ? "ACCEPTED" : "REJECTED");
                           updateStmt.setString(2, player.getName());
                           int updatedRows = updateStmt.executeUpdate();

                           for (Report report : activeReports) {
                              this.reputationManager.updateReputation(report, accepted, 1);
                              if (!report.sender.equalsIgnoreCase("System")) {
                                 this.plugin
                                    .getServer()
                                    .getScheduler()
                                    .runTask(
                                       this.plugin,
                                       () -> {
                                          if (accepted) {
                                             SyncAPI.addAction(
                                                "player",
                                                report.sender,
                                                "message",
                                                "&x&0&E&C&E&F&4 ▶ &fВаша жалоба на игрока &x&3&1&D&E&2&A"
                                                   + report.target
                                                   + " &fбыла успешно &aпринята. &fВаша &x&3&1&D&E&2&Aрепутация увеличена &fи теперь ваши &x&3&1&D&E&2&Aжалобы &fбудут рассматривать &x&3&1&D&E&2&Aчаще."
                                             );
                                             SyncAPI.addAction("player", report.sender, "sound", "ENTITY_PLAYER_LEVELUP 2 1");
                                          } else {
                                             SyncAPI.addAction(
                                                "player",
                                                report.sender,
                                                "message",
                                                "&x&0&E&C&E&F&4 ▶ &fВаша жалоба на игрока &x&3&1&D&E&2&A"
                                                   + report.target
                                                   + " &fбыла &cотклонена. &fЕсли вы продолжите отправлять &x&3&1&D&E&2&Aложные жалобы &fна игроков, то ваша &x&3&1&D&E&2&Aвозможность отправки жалоб &fбудет &x&3&1&D&E&2&Aзаблокирована."
                                             );
                                             SyncAPI.addAction("player", report.sender, "sound", "ENTITY_PLAYER_LEVELUP 2 1");
                                          }
                                       }
                                    );
                              }
                           }

                           this.plugin
                              .getServer()
                              .getScheduler()
                              .runTask(
                                 this.plugin,
                                 () -> {
                                    player.sendMessage(
                                       ChatColor.translateAlternateColorCodes(
                                          '&', "&x&0&E&C&E&F&4 ▶ &fРепорт успешно &x&3&1&D&E&2&A" + (accepted ? "принят" : "отклонен") + "."
                                       )
                                    );
                                    this.openReportsMenu(player, 1);
                                 }
                              );
                        } catch (Throwable var16) {
                           if (updateStmt != null) {
                              try {
                                 updateStmt.close();
                              } catch (Throwable var13) {
                                 var16.addSuppressed(var13);
                              }
                           }

                           throw var16;
                        }

                        if (updateStmt != null) {
                           updateStmt.close();
                        }
                     } catch (Throwable var17) {
                        if (conn != null) {
                           try {
                              conn.close();
                           } catch (Throwable var12) {
                              var17.addSuppressed(var12);
                           }
                        }

                        throw var17;
                     }

                     if (conn != null) {
                        conn.close();
                     }

                     return;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var18) {
                  this.plugin.getLogger().severe("Ошибка при обработке всех репортов: " + var18.getMessage());
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                     player.sendMessage(ChatColor.RED + "Ошибка при обработке репортов.");
                     this.openReportsMenu(player, 1);
                  });
               }
            }
         );
   }

   public ReportGUI getReportGUI() {
      return this.reportGUI;
   }
}
