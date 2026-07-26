package ua.holydev.holycustompriv.functional.database;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ua.holydev.holycustompriv.HolyCustomPriv;

public abstract class DataBaseManager {
   protected HolyCustomPriv plugin;

   public DataBaseManager(HolyCustomPriv plugin) {
      this.plugin = plugin;
   }

   public abstract void connect();

   public abstract void disconnect();

   public abstract void createTables();

   public abstract void savePlayerTokens(UUID var1, int var2, int var3, int var4, String var5);

   public abstract DataBaseManager.PlayerTokens loadPlayerTokens(UUID var1, String var2);

   public abstract int loadTitulTokens(UUID var1);

   public abstract void saveTitulTokens(UUID var1, int var2);

   public abstract void savePrivilege(Map<String, String> var1);

   public abstract void saveTitle(Map<String, String> var1);

   public abstract List<Map<String, String>> loadPendingPrivileges();

   public abstract List<Map<String, String>> loadApprovedPrivileges();

   public abstract List<Map<String, String>> loadPendingTitles();

   public abstract List<Map<String, String>> loadApprovedTitles();

   public abstract void updatePrivilegeStatus(int var1, String var2, String var3);

   public abstract void updateTitleStatus(int var1, String var2, String var3);

   public abstract void deletePrivilege(int var1);

   public abstract void deleteTitle(int var1);

   public abstract void clearPendingPrivileges();

   public abstract void clearPendingTitles();

   public static class PlayerTokens {
      private int tokens30d;
      private int tokens90d;
      private int tokensInfinity;

      public PlayerTokens(int tokens30d, int tokens90d, int tokensInfinity) {
         this.tokens30d = tokens30d;
         this.tokens90d = tokens90d;
         this.tokensInfinity = tokensInfinity;
      }

      public int getTokens30d() {
         return this.tokens30d;
      }

      public int getTokens90d() {
         return this.tokens90d;
      }

      public int getTokensInfinity() {
         return this.tokensInfinity;
      }

      public void setTokens30d(int tokens30d) {
         this.tokens30d = tokens30d;
      }

      public void setTokens90d(int tokens90d) {
         this.tokens90d = tokens90d;
      }

      public void setTokensInfinity(int tokensInfinity) {
         this.tokensInfinity = tokensInfinity;
      }
   }
}
