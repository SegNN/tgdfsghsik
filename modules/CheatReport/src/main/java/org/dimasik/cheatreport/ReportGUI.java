package org.dimasik.cheatreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ReportGUI implements Listener {
   private final ReportManager reportManager;
   private final DatabaseManager databaseManager;
   private final CheatReport plugin;
   private final ReputationManager reputationManager;
   private final BatchDataHelper batchHelper;

   public ReportGUI(
      ReportManager reportManager, DatabaseManager databaseManager, CheatReport plugin, ReputationManager reputationManager, BatchDataHelper batchHelper
   ) {
      this.reportManager = reportManager;
      this.databaseManager = databaseManager;
      this.plugin = plugin;
      this.reputationManager = reputationManager;
      this.batchHelper = batchHelper;
   }

   private void loadReportsDataAsync(final Player player, final int page, final Inventory inventory) {
      (new BukkitRunnable() {
            public void run() {
               try {
                  Connection conn = ReportGUI.this.databaseManager.getConnection();

                  try {
                     int itemsPerPage = 45;
                     final int totalUniquePlayers = ReportGUI.this.getTotalUniquePlayersWithReports(conn);
                     int totalPages = ReportGUI.this.getTotalPages(conn, itemsPerPage);
                     if (totalPages == 0) {
                        totalPages = 1;
                     }

                     int correctedPage = Math.max(1, Math.min(page, totalPages));
                     SortType sortType = ReportGUI.this.reputationManager.getSortType(player);
                     List<Pair<String, Integer>> groupedReports = ReportGUI.this.getPaginatedGroupedReports(conn, correctedPage, itemsPerPage, sortType);
                     Set<String> allPlayers = new HashSet<>();
                     List<Report> firstReports = new ArrayList<>();

                     for (Pair<String, Integer> entry : groupedReports) {
                        String target = entry.getFirst();
                        Report firstReport = ReportGUI.this.getFirstReportForPlayer(conn, target);
                        if (firstReport != null) {
                           firstReports.add(firstReport);
                           allPlayers.add(firstReport.target);
                           allPlayers.add(firstReport.sender);
                        }
                     }

                     Map<String, BatchDataHelper.PlayerReputationData> reputationData = ReportGUI.this.batchHelper.getBatchReputation(allPlayers);
                     Map<String, String> onlineStatuses = ReportGUI.this.batchHelper.getBatchOnlineStatus(allPlayers);
                     Map<String, Integer> reportsCount = ReportGUI.this.batchHelper.getBatchReportsCount(allPlayers);
                     final List<ItemStack> reportItems = new ArrayList<>();
                     boolean onlyOnline = ReportGUI.this.plugin.onlinePlayerManager.isOnlyOnline(player);

                     for (Report report : firstReports) {
                        if (!onlyOnline || !"Оффлайн".equals(onlineStatuses.get(report.target))) {
                           int count = reportsCount.getOrDefault(report.target, 0);
                           ItemStack item = ReportGUI.this.createGroupedReportItemWithCache(report, count, player, reputationData, onlineStatuses);
                           reportItems.add(item);
                        }
                     }

                     final int finalCorrectedPage = correctedPage;
                     final int finalTotalPages = totalPages;
                     (new BukkitRunnable() {
                           public void run() {
                              String newTitle = ChatColor.translateAlternateColorCodes(
                                 '&', "&0Список жалоб (" + totalUniquePlayers + " шт.) [" + finalCorrectedPage + "/" + finalTotalPages + "]"
                              );
                              Inventory newInventory = Bukkit.createInventory((InventoryHolder)null, 54, newTitle);

                              for (int i = 45; i < 54; i++) {
                                 newInventory.setItem(i, inventory.getItem(i));
                              }

                              for (int i = 0; i < 45; i++) {
                                 newInventory.setItem(i, null);
                              }

                              for (ItemStack item : reportItems) {
                                 newInventory.addItem(new ItemStack[]{item});
                              }

                              player.openInventory(newInventory);
                           }
                        })
                        .runTask(ReportGUI.this.plugin);
                  } catch (Throwable var20) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var19) {
                           var20.addSuppressed(var19);
                        }
                     }

                     throw var20;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var21) {
                  ReportGUI.this.plugin.getLogger().severe("Ошибка при загрузке репортов: " + var21.getMessage());
               }
            }
         })
         .runTaskAsynchronously(this.plugin);
   }

   public void openReportsMenu(Player player, int page) {
      String title = ChatColor.translateAlternateColorCodes('&', "&0Список жалоб (загрузка...)");
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54, title);
      ItemStack loadingItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta loadingMeta = loadingItem.getItemMeta();
      loadingMeta.setDisplayName(ChatColor.GRAY + "Загрузка...");
      loadingItem.setItemMeta(loadingMeta);

      for (int i = 0; i < 45; i++) {
         inventory.setItem(i, loadingItem);
      }

      ItemStack blueGlass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta blueMeta = blueGlass.getItemMeta();
      blueMeta.setDisplayName(" ");
      blueGlass.setItemMeta(blueMeta);

      for (int i = 45; i < 54; i++) {
         inventory.setItem(i, blueGlass);
      }

      this.addNavigationButtons(inventory, page);
      this.addEnderPearlButton(inventory, player);
      this.addSortButton(inventory, player);
      this.addUpdateButton(inventory);
      this.addActiveReportSlot(inventory, player);
      player.openInventory(inventory);
      this.loadReportsDataAsync(player, page, inventory);
   }

   public void openPlayerReportsMenu(Player player, String target, int page) {
      String title = ChatColor.translateAlternateColorCodes('&', "Репорты на " + target + " (загрузка...)");
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 54, title);
      ItemStack loadingItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta loadingMeta = loadingItem.getItemMeta();
      loadingMeta.setDisplayName(ChatColor.GRAY + "Загрузка...");
      loadingItem.setItemMeta(loadingMeta);

      for (int i = 0; i < 45; i++) {
         inventory.setItem(i, loadingItem);
      }

      this.addActiveReportSlot(inventory, player);
      ItemStack blueGlass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta blueMeta = blueGlass.getItemMeta();
      blueMeta.setDisplayName(" ");
      blueGlass.setItemMeta(blueMeta);

      for (int i = 47; i < 54; i++) {
         inventory.setItem(i, blueGlass);
      }

      this.addNavigationButtons(inventory, page);
      ItemStack backButton = new ItemStack(Material.TIPPED_ARROW);
      ItemMeta backMeta = backButton.getItemMeta();
      backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&f&f&7&3&0&0[⟲] Вернуться.."));
      PotionMeta potionMeta = (PotionMeta)backMeta;
      potionMeta.setColor(Color.ORANGE);
      backMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
      backButton.setItemMeta(backMeta);
      inventory.setItem(49, backButton);
      player.openInventory(inventory);
      this.loadPlayerReportsDataAsync(player, target, page, inventory);
   }

   private void loadPlayerReportsDataAsync(final Player player, final String target, final int page, final Inventory inventory) {
      (new BukkitRunnable() {
            public void run() {
               try {
                  Connection conn = ReportGUI.this.databaseManager.getConnection();

                  try {
                     int itemsPerPage = 45;
                     int reportsCount = 0;
                     PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) AS total FROM reports WHERE target = ? AND type = 'EXPECT' AND admin IS NULL"
                     );

                     try {
                        stmt.setString(1, target);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                           reportsCount = rs.getInt("total");
                        }
                     } catch (Throwable var15) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var14) {
                              var15.addSuppressed(var14);
                           }
                        }

                        throw var15;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }

                     final List<Report> reports = new ArrayList<>();
                     PreparedStatement stmtx = conn.prepareStatement(
                        "SELECT * FROM reports WHERE target = ? AND type = 'EXPECT' AND admin IS NULL ORDER BY date DESC LIMIT ? OFFSET ?"
                     );

                     try {
                        stmtx.setString(1, target);
                        stmtx.setInt(2, itemsPerPage);
                        stmtx.setInt(3, (page - 1) * itemsPerPage);
                        ResultSet rs = stmtx.executeQuery();

                        while (rs.next()) {
                           reports.add(
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
                     } catch (Throwable var16) {
                        if (stmtx != null) {
                           try {
                              stmtx.close();
                           } catch (Throwable var13) {
                              var16.addSuppressed(var13);
                           }
                        }

                        throw var16;
                     }

                     if (stmtx != null) {
                        stmtx.close();
                     }

                     Set<String> allPlayers = new HashSet<>();

                     for (Report report : reports) {
                        allPlayers.add(report.sender);
                        allPlayers.add(report.target);
                     }

                     Map<String, BatchDataHelper.PlayerReputationData> reputationData = ReportGUI.this.batchHelper.getBatchReputation(allPlayers);
                     Map<String, String> onlineStatuses = ReportGUI.this.batchHelper.getBatchOnlineStatus(allPlayers);
                     final int finalTotalPages = (int)Math.ceil((double)reportsCount / itemsPerPage);
                     final int finalReportsCount = reportsCount;
                     final Map<String, BatchDataHelper.PlayerReputationData> finalReputationData = reputationData;
                     final Map<String, String> finalOnlineStatuses = onlineStatuses;
                     (new BukkitRunnable() {
                           public void run() {
                              String newTitle = ChatColor.translateAlternateColorCodes(
                                 '&', "Репорты на " + target + " (" + finalReportsCount + " шт.) [" + page + "/" + finalTotalPages + "]"
                              );
                              Inventory newInventory = Bukkit.createInventory((InventoryHolder)null, 54, newTitle);

                              for (int i = 45; i < 54; i++) {
                                 newInventory.setItem(i, inventory.getItem(i));
                              }

                              for (Report report : reports) {
                                 ItemStack item = ReportGUI.this.createReportItemWithCache(
                                    report, player, finalReputationData, finalOnlineStatuses, finalReportsCount
                                 );
                                 newInventory.addItem(new ItemStack[]{item});
                              }

                              player.openInventory(newInventory);
                           }
                        })
                        .runTask(ReportGUI.this.plugin);
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
               } catch (SQLException var18) {
                  ReportGUI.this.plugin.getLogger().severe("Ошибка при загрузке репортов игрока: " + var18.getMessage());
               }
            }
         })
         .runTaskAsynchronously(this.plugin);
   }

   private ItemStack createGroupedReportItemWithCache(
      Report report, int reportCount, Player viewer, Map<String, BatchDataHelper.PlayerReputationData> reputationData, Map<String, String> onlineStatuses
   ) {
      ItemStack item = new ItemStack(Material.ZOMBIE_HEAD, reportCount);
      if (report.sender.equals(ChatColor.translateAlternateColorCodes('&', "System"))) {
         item = new ItemStack(Material.SKELETON_SKULL, reportCount);
      }

      ItemMeta meta = item.getItemMeta();
      String displayName = this.replacePlaceholdersWithCache(
         this.plugin.getConfig().getString("gui.reports_format.displayName"), report, viewer, reportCount, reputationData, onlineStatuses, reportCount
      );
      List<String> lore = this.plugin
         .getConfig()
         .getStringList("gui.reports_format.lore")
         .stream()
         .map(line -> this.replacePlaceholdersWithCache(line, report, viewer, reportCount, reputationData, onlineStatuses, reportCount))
         .collect(Collectors.toList());
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
      meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
      meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "reportId"), PersistentDataType.INTEGER, report.id);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createReportItemWithCache(
      Report report, Player viewer, Map<String, BatchDataHelper.PlayerReputationData> reputationData, Map<String, String> onlineStatuses, int totalReportsCount
   ) {
      ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
      if (report.sender.equals(ChatColor.translateAlternateColorCodes('&', "System"))) {
         item = new ItemStack(Material.SKELETON_SKULL, 1);
      }

      ItemMeta meta = item.getItemMeta();
      String displayName = this.replacePlaceholdersWithCache(
         this.plugin.getConfig().getString("gui.reports_format.displayName"), report, viewer, null, reputationData, onlineStatuses, totalReportsCount
      );
      List<String> lore = this.plugin
         .getConfig()
         .getStringList("gui.reports_format.lore")
         .stream()
         .map(line -> this.replacePlaceholdersWithCache(line, report, viewer, null, reputationData, onlineStatuses, totalReportsCount))
         .collect(Collectors.toList());
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
      meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
      meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "reportId"), PersistentDataType.INTEGER, report.id);
      item.setItemMeta(meta);
      return item;
   }

   private String replacePlaceholdersWithCache(
      String text,
      Report report,
      Player viewer,
      Integer count,
      Map<String, BatchDataHelper.PlayerReputationData> reputationData,
      Map<String, String> onlineStatuses,
      int reportsCount
   ) {
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
         String formattedDate = sdf.format(report.getDate());
         String reason = report.getReason();
         if (count != null && count >= 2) {
            count = count - 1;
            reason = reason + ChatColor.translateAlternateColorCodes('&', " &7&o(и ещё " + count + " записей)");
         }

         BatchDataHelper.PlayerReputationData senderData = reputationData.getOrDefault(report.sender, new BatchDataHelper.PlayerReputationData(0, 0, 0, 0));
         BatchDataHelper.PlayerReputationData targetData = reputationData.getOrDefault(report.target, new BatchDataHelper.PlayerReputationData(0, 0, 0, 0));
         String onlineStatus = onlineStatuses.getOrDefault(report.target, "Оффлайн");
         return text.replace("{sender}", report.getSender())
            .replace("{target}", report.getTarget())
            .replace("{date}", formattedDate)
            .replace("{reason}", reason)
            .replace("{mode}", report.getMode())
            .replace("{online}", onlineStatus)
            .replace("{sender_statsSuccess}", String.valueOf(senderData.successReports))
            .replace("{sender_statsFailure}", String.valueOf(senderData.failureReports))
            .replace("{target_statsSuccess}", String.valueOf(targetData.successReports))
            .replace("{target_statsFailure}", String.valueOf(targetData.failureReports))
            .replace("{amount}", String.valueOf(reportsCount))
            .replace("{sender_statsSuccessCheck}", String.valueOf(senderData.successChecks))
            .replace("{sender_statsFailureCheck}", String.valueOf(senderData.failureChecks))
            .replace("{target_statsSuccessCheck}", String.valueOf(targetData.successChecks))
            .replace("{target_statsFailureCheck}", String.valueOf(targetData.failureChecks));
      } catch (Exception var14) {
         return text;
      }
   }

   private ItemStack createGroupedReportItem(Report report, int reportCount, Player viewer) {
      ItemStack item = new ItemStack(Material.ZOMBIE_HEAD, reportCount);
      if (report.sender.equals(ChatColor.translateAlternateColorCodes('&', "System"))) {
         item = new ItemStack(Material.SKELETON_SKULL, reportCount);
      }

      ItemMeta meta = item.getItemMeta();
      String displayName = this.replacePlaceholders(this.plugin.getConfig().getString("gui.reports_format.displayName"), report, viewer, reportCount);
      List<String> lore = this.plugin
         .getConfig()
         .getStringList("gui.reports_format.lore")
         .stream()
         .map(line -> this.replacePlaceholders(line, report, viewer, reportCount))
         .collect(Collectors.toList());
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
      meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
      meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "reportId"), PersistentDataType.INTEGER, report.id);
      item.setItemMeta(meta);
      return item;
   }

   private String replacePlaceholders(String text, Report report, Player viewer, Integer count) {
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
         String formattedDate = sdf.format(report.getDate());
         String reason = report.getReason();
         if (count != null && count >= 2) {
            count = count - 1;
            reason = reason + ChatColor.translateAlternateColorCodes('&', " &7&o(и ещё " + count + " записей)");
         }

         int cnt = this.getReportsCountForPlayer(report.getTarget());
         return text.replace("{sender}", report.getSender())
            .replace("{target}", report.getTarget())
            .replace("{date}", formattedDate)
            .replace("{reason}", reason)
            .replace("{mode}", report.getMode())
            .replace("{online}", this.getPlayerOnlineStatus(report.getTarget()))
            .replace("{sender_statsSuccess}", String.valueOf(this.reputationManager.getSuccessReports(report.getSender())))
            .replace("{sender_statsFailure}", String.valueOf(this.reputationManager.getFailureReports(report.getSender())))
            .replace("{target_statsSuccess}", String.valueOf(this.reputationManager.getSuccessReports(report.getTarget())))
            .replace("{target_statsFailure}", String.valueOf(this.reputationManager.getFailureReports(report.getTarget())))
            .replace("{amount}", String.valueOf(cnt))
            .replace("{sender_statsSuccessCheck}", String.valueOf(this.reputationManager.getSuccessChecks(report.getSender())))
            .replace("{sender_statsFailureCheck}", String.valueOf(this.reputationManager.getFailureChecks(report.getSender())))
            .replace("{target_statsSuccessCheck}", String.valueOf(this.reputationManager.getSuccessChecks(report.getTarget())))
            .replace("{target_statsFailureCheck}", String.valueOf(this.reputationManager.getFailureChecks(report.getTarget())));
      } catch (SQLException var9) {
         return text;
      }
   }

   private int getTotalUniquePlayersWithReports(Connection conn) throws SQLException {
      String query = "SELECT COUNT(DISTINCT target) AS total FROM reports WHERE type = 'EXPECT' AND admin IS NULL";
      PreparedStatement stmt = conn.prepareStatement(query);

      int var5;
      label67: {
         try {
            ResultSet rs = stmt.executeQuery();

            label69: {
               try {
                  if (!rs.next()) {
                     break label69;
                  }

                  var5 = rs.getInt("total");
               } catch (Throwable var9) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (rs != null) {
                  rs.close();
               }
               break label67;
            }

            if (rs != null) {
               rs.close();
            }
         } catch (Throwable var10) {
            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (stmt != null) {
            stmt.close();
         }

         return 0;
      }

      if (stmt != null) {
         stmt.close();
      }

      return var5;
   }

   private List<Pair<String, Integer>> getPaginatedGroupedReports(Connection conn, int page, int itemsPerPage, SortType sortType) throws SQLException {
      List<Pair<String, Integer>> groupedReports = new ArrayList<>();
      String query = "SELECT target, COUNT(*) AS report_count, MAX(date) AS max_date FROM reports WHERE type = 'EXPECT' AND admin IS NULL GROUP BY target ORDER BY "
         + sortType.getSqlOrder()
         + " LIMIT ? OFFSET ?";
      PreparedStatement stmt = conn.prepareStatement(query);

      try {
         stmt.setInt(1, itemsPerPage);
         stmt.setInt(2, (page - 1) * itemsPerPage);
         ResultSet rs = stmt.executeQuery();

         while (rs.next()) {
            String target = rs.getString("target");
            int reportCount = rs.getInt("report_count");
            groupedReports.add(new Pair<>(target, reportCount));
         }
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

      return groupedReports;
   }

   private Report getFirstReportForPlayer(Connection conn, String target) throws SQLException {
      String query = "SELECT * FROM reports WHERE target = ? AND type = 'EXPECT' AND admin IS NULL LIMIT 1";
      PreparedStatement stmt = conn.prepareStatement(query);

      Report var6;
      label40: {
         try {
            stmt.setString(1, target);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
               var6 = new Report(
                  rs.getInt("id"),
                  rs.getString("sender"),
                  rs.getString("target"),
                  rs.getTimestamp("date"),
                  rs.getString("reason"),
                  rs.getString("type"),
                  rs.getString("admin"),
                  rs.getString("mode")
               );
               break label40;
            }
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

         return null;
      }

      if (stmt != null) {
         stmt.close();
      }

      return var6;
   }

   private int getTotalPages(Connection conn, int itemsPerPage) throws SQLException {
      int totalUniquePlayers = this.getTotalUniquePlayersWithReports(conn);
      return totalUniquePlayers == 0 ? 1 : (int)Math.ceil((double)totalUniquePlayers / itemsPerPage);
   }

   public int getReportsCountForPlayer(String target) throws SQLException {
      Connection conn = this.databaseManager.getConnection();

      int var5;
      label67: {
         try {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM reports WHERE target = ? AND type = 'EXPECT'");

            label69: {
               try {
                  stmt.setString(1, target);
                  ResultSet rs = stmt.executeQuery();
                  if (!rs.next()) {
                     break label69;
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
               break label67;
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
   }

   private String getPlayerOnlineStatus(String playerName) {
      try {
         Connection conn = this.databaseManager.getConnection();

         String var6;
         label86: {
            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT server FROM online_players WHERE nickname = ?");

               label79: {
                  try {
                     stmt.setString(1, playerName);
                     ResultSet rs = stmt.executeQuery();
                     if (!rs.next()) {
                        break label79;
                     }

                     String serverName = rs.getString("server");
                     var6 = serverName != null ? "Онлайн | " + serverName : "Оффлайн";
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
                  break label86;
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

            return "Оффлайн";
         }

         if (conn != null) {
            conn.close();
         }

         return var6;
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка при проверке онлайн-статуса игрока: " + var11.getMessage());
         return "Оффлайн";
      }
   }

   private void addUpdateButton(Inventory inventory) {
      ItemStack updateButton = new ItemStack(Material.NETHER_STAR);
      ItemMeta meta = updateButton.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&E&C&E&F&4❐ Обновить ❐"));
      updateButton.setItemMeta(meta);
      inventory.setItem(49, updateButton);
   }

   private void addEnderPearlButton(Inventory inventory, Player player) {
      ItemStack enderPearl = new ItemStack(Material.ENDER_PEARL);
      ItemMeta meta = enderPearl.getItemMeta();
      boolean onlyOnline = this.plugin.onlinePlayerManager.isOnlyOnline(player);
      meta.setDisplayName(
         ChatColor.translateAlternateColorCodes(
            '&', "&x&0&E&C&E&F&4❐ Только игроки онлайн: " + (onlyOnline ? "&x&0&5&F&B&0&0Включено" : "&x&F&F&2&2&2&2Выключено") + "&x&0&E&C&E&F&4 ❐"
         )
      );
      if (onlyOnline) {
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         meta.addEnchant(Enchantment.LUCK, 1, true);
      }

      enderPearl.setItemMeta(meta);
      inventory.setItem(53, enderPearl);
   }

   private void addSortButton(Inventory inventory, Player player) {
      ItemStack sortButton = new ItemStack(Material.HOPPER);
      ItemMeta meta = sortButton.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&0&E&C&E&F&4❐ Сортировка ❐"));
      SortType currentSort = this.reputationManager.getSortType(player);
      List<String> lore = new ArrayList<>();
      lore.add("");

      for (SortType sortType : SortType.values()) {
         String displayName = sortType.getDisplayName();
         if (sortType == currentSort) {
            lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4&n▍&r &x&3&1&D&E&2&A" + displayName));
         } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4&n▍&r &f" + displayName));
         }
      }

      lore.add("");
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &x&3&1&D&E&2&A[ЛКМ] &fЛистать вниз"));
      lore.add(ChatColor.translateAlternateColorCodes('&', " &x&0&E&C&E&F&4▶ &x&3&1&D&E&2&A[ПКМ] &fЛистать вверх"));
      meta.setLore(lore);
      sortButton.setItemMeta(meta);
      inventory.setItem(52, sortButton);
   }

   private void addNavigationButtons(Inventory inventory, int page) {
      ItemStack nextPage = new ItemStack(Material.ARROW);
      ItemMeta nextPageMeta = nextPage.getItemMeta();
      nextPageMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("gui.buttons.nextPage.displayName", "&fСледующая страница &6▶"))
      );
      nextPage.setItemMeta(nextPageMeta);
      inventory.setItem(47, nextPage);
      ItemStack prevPage = new ItemStack(Material.ARROW);
      ItemMeta prevPageMeta = prevPage.getItemMeta();
      prevPageMeta.setDisplayName(
         ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("gui.buttons.prevPage.displayName", "&6◀ &fПредыдущая страница"))
      );
      prevPage.setItemMeta(prevPageMeta);
      inventory.setItem(45, prevPage);
   }

   private void addActiveReportSlot(Inventory inventory, Player player) {
      ItemStack loadingItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta loadingMeta = loadingItem.getItemMeta();
      loadingMeta.setDisplayName(ChatColor.GRAY + "Загрузка активных дел...");
      loadingItem.setItemMeta(loadingMeta);
      inventory.setItem(46, loadingItem);
      this.plugin
         .getServer()
         .getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               try {
                  Connection conn = this.databaseManager.getConnection();

                  label82: {
                     try {
                        String query = "SELECT r.id, r.sender, r.target, r.reason, r.mode, r.date, (SELECT COUNT(*) FROM reports WHERE target = r.target AND type = 'EXPECT') as report_count FROM reports r WHERE r.admin = ? AND r.type = 'EXPECT' LIMIT 1";
                        PreparedStatement stmt = conn.prepareStatement(query);

                        label76: {
                           try {
                              stmt.setString(1, player.getName());
                              ResultSet rs = stmt.executeQuery();
                              if (!rs.next()) {
                                 break label76;
                              }

                              Report report = new Report(
                                 rs.getInt("id"),
                                 rs.getString("sender"),
                                 rs.getString("target"),
                                 rs.getTimestamp("date"),
                                 rs.getString("reason"),
                                 "EXPECT",
                                 player.getName(),
                                 rs.getString("mode")
                              );
                              int reportCount = rs.getInt("report_count");
                              Set<String> players = new HashSet<>();
                              players.add(report.sender);
                              players.add(report.target);
                              Map<String, BatchDataHelper.PlayerReputationData> reputationData = this.batchHelper.getBatchReputation(players);
                              Map<String, String> onlineStatuses = this.batchHelper.getBatchOnlineStatus(players);
                              this.plugin
                                 .getServer()
                                 .getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       ItemStack activeReportItem = new ItemStack(Material.ZOMBIE_HEAD, reportCount);
                                       if (report.sender.equals(ChatColor.translateAlternateColorCodes('&', "System"))) {
                                          activeReportItem = new ItemStack(Material.SKELETON_SKULL, reportCount);
                                       }

                                       ItemMeta meta = activeReportItem.getItemMeta();
                                       meta.setDisplayName(
                                          ChatColor.translateAlternateColorCodes(
                                             '&',
                                             this.plugin
                                                .getConfig()
                                                .getString("gui.buttons.activeReport.displayName", "&#0ECEF4❐ Дело игрока {target} ❐")
                                                .replace("{target}", report.target)
                                          )
                                       );
                                       List<String> lore = new ArrayList<>();

                                       for (String line : this.plugin.getConfig().getStringList("gui.buttons.activeReport.lore")) {
                                          lore.add(
                                             this.replacePlaceholdersWithCache(line, report, player, reportCount, reputationData, onlineStatuses, reportCount)
                                          );
                                       }

                                       meta.setLore(lore.stream().map(linex -> ChatColor.translateAlternateColorCodes('&', linex)).collect(Collectors.toList()));
                                       activeReportItem.setItemMeta(meta);
                                       inventory.setItem(46, activeReportItem);
                                    }
                                 );
                           } catch (Throwable var14) {
                              if (stmt != null) {
                                 try {
                                    stmt.close();
                                 } catch (Throwable var13) {
                                    var14.addSuppressed(var13);
                                 }
                              }

                              throw var14;
                           }

                           if (stmt != null) {
                              stmt.close();
                           }
                           break label82;
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
                                 ItemStack noActiveReportItem = new ItemStack(Material.BARRIER);
                                 ItemMeta meta = noActiveReportItem.getItemMeta();
                                 meta.setDisplayName(
                                    ChatColor.translateAlternateColorCodes(
                                       '&', this.plugin.getConfig().getString("gui.buttons.noActiveReport.displayName", "&#D52123У вас нет активных дел")
                                    )
                                 );
                                 noActiveReportItem.setItemMeta(meta);
                                 inventory.setItem(46, noActiveReportItem);
                              }
                           );
                     } catch (Throwable var15) {
                        if (conn != null) {
                           try {
                              conn.close();
                           } catch (Throwable var12) {
                              var15.addSuppressed(var12);
                           }
                        }

                        throw var15;
                     }

                     if (conn != null) {
                        conn.close();
                     }

                     return;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var16) {
                  this.plugin.getLogger().severe("Ошибка при загрузке активного репорта: " + var16.getMessage());
               }
            }
         );
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.plugin.onlinePlayerManager.addPlayer(event.getPlayer());
   }

   @EventHandler
   public void onPlayerLeave(PlayerQuitEvent event) {
      this.plugin.onlinePlayerManager.removePlayer(event.getPlayer());
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      String title = event.getView().getTitle();
      if (title.startsWith(ChatColor.translateAlternateColorCodes('&', "&0Список жалоб")) || title.startsWith("Репорты на")) {
         event.setCancelled(true);
         Player player = (Player)event.getWhoClicked();
         ItemStack clickedItem = event.getCurrentItem();
         if (clickedItem == null) {
            return;
         }

         if (clickedItem.getType() == Material.ARROW) {
            this.handleNavigationClick(event);
         } else if (event.getSlot() == 46) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
            this.handleActiveReportClick(event);
         } else if (event.getSlot() == 49 && clickedItem.getType() == Material.NETHER_STAR) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0F, 1.0F);
            int currentPage = this.extractCurrentPage(title);
            this.openReportsMenu(player, currentPage);
         } else if (event.getSlot() == 52 && clickedItem.getType() == Material.HOPPER) {
            this.handleSortButtonClick(event, player, title);
         } else if (event.getSlot() == 49 && clickedItem.getType() == Material.TIPPED_ARROW) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            this.openReportsMenu(player, 1);
         } else if (event.getSlot() == 53 && clickedItem.getType() == Material.ENDER_PEARL) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
            boolean onlyOnline = this.plugin.onlinePlayerManager.isOnlyOnline(player);
            this.plugin.onlinePlayerManager.toggleOnlyOnline(player, !onlyOnline);
            int currentPage = this.extractCurrentPage(title);
            this.openReportsMenu(player, currentPage);
         } else if (clickedItem.getType() == Material.ZOMBIE_HEAD || clickedItem.getType() == Material.SKELETON_SKULL) {
            Integer reportId = this.getReportIdFromItem(clickedItem);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (reportId != null) {
               Report report = this.reportManager.getReportById(reportId);
               if (report != null) {
                  if (event.isRightClick()) {
                     this.openPlayerReportsMenu(player, report.target, 1);
                  } else if (event.isLeftClick()) {
                     if (this.reportManager.hasActiveReport(player)) {
                        player.sendMessage(
                           ChatColor.translateAlternateColorCodes(
                              '&', " &x&0&E&C&E&F&4▶ &fУ вас уже есть &x&3&1&D&E&2&Aактивное дело. &fЗавершите его сначала."
                           )
                        );
                        return;
                     }

                     if (report.admin != null && !report.admin.isEmpty()) {
                        player.sendMessage(
                           ChatColor.translateAlternateColorCodes(
                              '&', " &x&0&E&C&E&F&4▶ &fРепорты на этого игрока &x&3&1&D&E&2&Aуже взяты &fдругим модератором."
                           )
                        );
                        return;
                     }

                     this.reportManager.takeAllPlayerReports(player, report.target);
                  }
               }
            }
         }
      }
   }

   private void handleSortButtonClick(InventoryClickEvent event, Player player, String title) {
      SortType currentSort = this.reputationManager.getSortType(player);
      player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
      SortType newSort;
      if (event.isLeftClick()) {
         newSort = this.getNextSortType(currentSort);
      } else {
         if (!event.isRightClick()) {
            return;
         }

         newSort = this.getPreviousSortType(currentSort);
      }

      this.reputationManager.setSortType(player, newSort);
      int currentPage = this.extractCurrentPage(title);
      this.openReportsMenu(player, currentPage);
   }

   private void handleNavigationClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
      String title = event.getView().getTitle();
      int currentPage = this.extractCurrentPage(title);
      if (displayName.equals(
         ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("gui.buttons.nextPage.displayName", "&fСледующая страница &6▶"))
      )) {
         int newPage = currentPage + 1;
         this.openReportsMenu(player, newPage);
      } else if (displayName.equals(
         ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("gui.buttons.prevPage.displayName", "&6◀ &fПредыдущая страница"))
      )) {
         int newPage = currentPage - 1;
         if (newPage < 1) {
            return;
         }

         this.openReportsMenu(player, newPage);
      }
   }

   private void handleActiveReportClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (this.reportManager.hasActiveReport(player)) {
         List<Integer> activeReports = this.reportManager.getActiveReports(player);
         if (!activeReports.isEmpty()) {
            if (event.isLeftClick()) {
               this.reportManager.resolveAllActiveReports(player, true);
            } else if (event.isRightClick()) {
               this.reportManager.resolveAllActiveReports(player, false);
            }
         }
      }
   }

   private int extractCurrentPage(String title) {
      try {
         if (title.contains("[") && title.contains("/")) {
            String pagePart = title.split("\\[")[1].split("/")[0];
            return Integer.parseInt(pagePart);
         }
      } catch (Exception var3) {
      }

      return 1;
   }

   private SortType getNextSortType(SortType current) {
      SortType[] values = SortType.values();
      int nextIndex = (current.ordinal() + 1) % values.length;
      return values[nextIndex];
   }

   private SortType getPreviousSortType(SortType current) {
      SortType[] values = SortType.values();
      int prevIndex = (current.ordinal() - 1 + values.length) % values.length;
      return values[prevIndex];
   }

   private Integer getReportIdFromItem(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            return (Integer)meta.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "reportId"), PersistentDataType.INTEGER);
         }
      }

      return null;
   }
}
