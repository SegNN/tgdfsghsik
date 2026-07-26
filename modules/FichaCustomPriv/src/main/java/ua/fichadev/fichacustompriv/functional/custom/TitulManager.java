package ua.fichadev.fichacustompriv.functional.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class TitulManager {
   private FichaCustomPriv plugin;
   private Map<UUID, String> playerTitles;
   private List<Map<String, String>> pendingTitles;
   private List<Map<String, String>> approvedTitles;
   private int nextId;

   public TitulManager(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.playerTitles = new HashMap<>();
      this.pendingTitles = new ArrayList<>();
      this.approvedTitles = new ArrayList<>();
      this.nextId = 1;
      this.loadTitlesFromDatabase();
   }

   private void loadTitlesFromDatabase() {
      try {
         List<Map<String, String>> pending = this.plugin.getDatabaseManager().loadPendingTitles();
         if (pending != null) {
            this.pendingTitles.addAll(pending);

            for (Map<String, String> title : pending) {
               int id = Integer.parseInt(title.get("id"));
               if (id >= this.nextId) {
                  this.nextId = id + 1;
               }
            }
         }

         List<Map<String, String>> approved = this.plugin.getDatabaseManager().loadApprovedTitles();
         if (approved != null) {
            this.approvedTitles.addAll(approved);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public boolean createPendingTitle(String playerName, String playerUUID, String title) {
      try {
         if (title != null && !title.trim().isEmpty()) {
            Map<String, String> titleData = new HashMap<>();
            titleData.put("id", String.valueOf(this.nextId));
            titleData.put("player_name", playerName);
            titleData.put("player_uuid", playerUUID);
            titleData.put("title", title.trim());
            titleData.put("created_time", String.valueOf(System.currentTimeMillis()));
            titleData.put("status", "pending");
            this.pendingTitles.add(titleData);
            this.plugin.getDatabaseManager().saveTitle(titleData);
            this.nextId++;
            return true;
         } else {
            return false;
         }
      } catch (Exception var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public List<Map<String, String>> getPendingTitles() {
      return new ArrayList<>(this.pendingTitles);
   }

   public List<Map<String, String>> getApprovedTitles() {
      return new ArrayList<>(this.approvedTitles);
   }

   public boolean hasActiveTitle(String playerUUID) {
      for (Map<String, String> title : this.approvedTitles) {
         if (playerUUID.equals(title.get("player_uuid"))) {
            return true;
         }
      }

      return false;
   }

   public boolean approveTitle(int titleId, String approvedBy) {
      try {
         Map<String, String> titleToApprove = null;

         for (Map<String, String> title : this.pendingTitles) {
            if (Integer.parseInt(title.get("id")) == titleId) {
               titleToApprove = title;
               break;
            }
         }

         if (titleToApprove == null) {
            return false;
         } else {
            String playerName = titleToApprove.get("player_name");
            String titleText = titleToApprove.get("title");
            if (titleText != null && !titleText.trim().isEmpty()) {
               Map<String, String> approvedTitle = new HashMap<>(titleToApprove);
               approvedTitle.put("approved_by", approvedBy);
               approvedTitle.put("approved_time", String.valueOf(System.currentTimeMillis()));
               approvedTitle.put("status", "approved");
               this.approvedTitles.add(approvedTitle);
               this.pendingTitles.remove(titleToApprove);
               this.plugin.getDatabaseManager().updateTitleStatus(titleId, "approved", approvedBy);
               this.giveLuckPermsTitle(playerName, titleText);
               return true;
            } else {
               return false;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return false;
      }
   }

   public boolean rejectTitle(int titleId) {
      try {
         Map<String, String> titleToReject = null;

         for (Map<String, String> title : this.pendingTitles) {
            if (Integer.parseInt(title.get("id")) == titleId) {
               titleToReject = title;
               break;
            }
         }

         if (titleToReject != null) {
            String playerName = titleToReject.get("player_name");
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
               this.plugin.getTokenManager().giveTokens(player, "tokens", 1, "tituls");
            }

            this.pendingTitles.remove(titleToReject);
            this.plugin.getDatabaseManager().deleteTitle(titleId);
            return true;
         } else {
            return false;
         }
      } catch (Exception var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public int getPendingTitlesCount() {
      return this.pendingTitles.size();
   }

   public int getApprovedTitlesCount() {
      return this.approvedTitles.size();
   }

   public void clearPendingTitles() {
      this.pendingTitles.clear();
      this.plugin.getDatabaseManager().clearPendingTitles();
   }

   public void giveLuckPermsTitle(String playerName, String title) {
      try {
         if (title == null || title.trim().isEmpty()) {
            return;
         }

         String formattedTitle = HexUtil.color(title.trim());
         String escapedTitle = formattedTitle.replace("\"", "\\\"");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta setsuffix 0 \"" + escapedTitle + "\"");
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public void removeTitle(String playerName) {
      try {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta removesuffix 0");
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public String getPlayerTitle(Player player) {
      return this.playerTitles.get(player.getUniqueId());
   }

   public void setPlayerTitle(Player player, String title) {
      if (title != null && !title.trim().isEmpty()) {
         this.playerTitles.put(player.getUniqueId(), title.trim());
      }
   }

   public void clearPlayerTitle(Player player) {
      this.playerTitles.remove(player.getUniqueId());
   }
}
