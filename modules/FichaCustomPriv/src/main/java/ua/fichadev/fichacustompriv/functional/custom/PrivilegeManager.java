package ua.fichadev.fichacustompriv.functional.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class PrivilegeManager {
   private FichaCustomPriv plugin;
   private List<Map<String, String>> pendingPrivileges;
   private List<Map<String, String>> approvedPrivileges;
   private int nextId;

   public PrivilegeManager(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.pendingPrivileges = new ArrayList<>();
      this.approvedPrivileges = new ArrayList<>();
      this.nextId = 1;
      this.loadPrivilegesFromDatabase();
   }

   private void loadPrivilegesFromDatabase() {
      try {
         List<Map<String, String>> pending = this.plugin.getDatabaseManager().loadPendingPrivileges();
         if (pending != null) {
            this.pendingPrivileges.addAll(pending);

            for (Map<String, String> priv : pending) {
               int id = Integer.parseInt(priv.get("id"));
               if (id >= this.nextId) {
                  this.nextId = id + 1;
               }
            }
         }

         List<Map<String, String>> approved = this.plugin.getDatabaseManager().loadApprovedPrivileges();
         if (approved != null) {
            this.approvedPrivileges.addAll(approved);

            for (Map<String, String> privx : approved) {
               int id = Integer.parseInt(privx.get("id"));
               if (id >= this.nextId) {
                  this.nextId = id + 1;
               }
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   public boolean createPendingPrivilege(String playerName, String playerUUID, String privilegeText, String privilegeColor, int durationDays) {
      try {
         if (privilegeText != null && !privilegeText.trim().isEmpty()) {
            Map<String, String> privilege = new HashMap<>();
            privilege.put("id", String.valueOf(this.nextId));
            privilege.put("player_name", playerName);
            privilege.put("player_uuid", playerUUID);
            privilege.put("privilege_text", privilegeText.trim());
            privilege.put("privilege_color", privilegeColor != null ? privilegeColor : "&f");
            privilege.put("duration_days", String.valueOf(durationDays));
            privilege.put("created_time", new Date().toString());
            privilege.put("status", "pending");
            this.pendingPrivileges.add(privilege);
            this.plugin.getDatabaseManager().savePrivilege(privilege);
            this.nextId++;
            return true;
         } else {
            return false;
         }
      } catch (Exception var71) {
         var71.printStackTrace();
         return false;
      }
   }

   public List<Map<String, String>> getPendingPrivileges() {
      List<Map<String, String>> result = new ArrayList<>(this.pendingPrivileges);
      Collections.reverse(result);
      return result;
   }

   public boolean hasActivePrivilege(String playerUUID) {
      for (Map privilege : this.approvedPrivileges) {
         if (playerUUID.equals(privilege.get("player_uuid"))) {
            return true;
         }
      }

      return false;
   }

   public boolean approvePrivilege(int privilegeId, String approvedBy) {
      try {
         Map<String, String> privilegeToApprove = null;

         for (Map<String, String> privilege : this.pendingPrivileges) {
            if (Integer.parseInt(privilege.get("id")) == privilegeId) {
               privilegeToApprove = privilege;
               break;
            }
         }

         if (privilegeToApprove == null) {
            return false;
         } else {
            String playerName = privilegeToApprove.get("player_name");
            String privilegeText = privilegeToApprove.get("privilege_text");
            String privilegeColor = privilegeToApprove.get("privilege_color");
            int durationDays = Integer.parseInt(privilegeToApprove.get("duration_days"));
            if (privilegeText != null && !privilegeText.trim().isEmpty()) {
               Map<String, String> approvedPrivilege = new HashMap<>(privilegeToApprove);
               approvedPrivilege.put("approved_by", approvedBy);
               approvedPrivilege.put("approved_time", new Date().toString());
               approvedPrivilege.put("status", "approved");
               this.approvedPrivileges.add(approvedPrivilege);
               this.pendingPrivileges.remove(privilegeToApprove);
               this.plugin.getDatabaseManager().updatePrivilegeStatus(privilegeId, "approved", approvedBy);
               this.giveLuckPermsPrivilege(playerName, privilegeText, privilegeColor, durationDays);
               return true;
            } else {
               return false;
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
         return false;
      }
   }

   public boolean rejectPrivilege(int privilegeId, String rejectedBy) {
      try {
         Map<String, String> privilegeToReject = null;

         for (Map<String, String> privilege : this.pendingPrivileges) {
            if (Integer.parseInt(privilege.get("id")) == privilegeId) {
               privilegeToReject = privilege;
               break;
            }
         }

         if (privilegeToReject != null) {
            String playerUUID = privilegeToReject.get("player_uuid");
            String playerName = privilegeToReject.get("player_name");
            int durationDays = Integer.parseInt(privilegeToReject.get("duration_days"));
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
               String tokenType;
               if (durationDays == 30) {
                  tokenType = "30d";
               } else if (durationDays == 90) {
                  tokenType = "90d";
               } else {
                  tokenType = "infinity";
               }

               this.plugin.getTokenManager().giveTokens(player, tokenType, 1, "privileges");
            }

            this.pendingPrivileges.remove(privilegeToReject);
            this.plugin.getDatabaseManager().deletePrivilege(privilegeId);
            return true;
         } else {
            return false;
         }
      } catch (Exception var10) {
         var10.printStackTrace();
         return false;
      }
   }

   public int getApprovedPrivilegesCount() {
      return this.approvedPrivileges.size();
   }

   public void clearPendingPrivileges() {
      this.pendingPrivileges.clear();
      this.plugin.getDatabaseManager().clearPendingPrivileges();
   }

   private void giveLuckPermsPrivilege(String playerName, String privilegeText, String privilegeColor, int durationDays) {
      try {
         if (privilegeText == null || privilegeText.trim().isEmpty()) {
            return;
         }

         String formattedPrefix = HexUtil.color(privilegeColor + privilegeText + " ");
         String escapedPrefix = formattedPrefix.replace("\"", "\\\"");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta removeprefix 100");
         if (durationDays > 0) {
            String duration = durationDays + "d";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta settempprefix 100 \"" + escapedPrefix + "\" " + duration);
         } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta setprefix 100 \"" + escapedPrefix + "\"");
         }

         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " parent add infinity");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set group.infinity true");
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   public int getPendingPrivilegesCount() {
      return this.pendingPrivileges.size();
   }

   public List<Map<String, String>> getApprovedPrivileges() {
      return new ArrayList<>(this.approvedPrivileges);
   }
}
