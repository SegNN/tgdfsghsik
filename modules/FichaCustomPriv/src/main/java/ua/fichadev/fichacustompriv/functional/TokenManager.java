package ua.fichadev.fichacustompriv.functional;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.functional.database.DataBaseManager;

public class TokenManager {
   private FichaCustomPriv plugin;
   private DataBaseManager databaseManager;
   private HashMap<UUID, DataBaseManager.PlayerTokens> privilegeTokens;
   private HashMap<UUID, Integer> titulTokens;
   private HashMap<UUID, String> lastUsedTokenType;

   public TokenManager(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.databaseManager = plugin.getDatabaseManager();
      this.privilegeTokens = new HashMap<>();
      this.titulTokens = new HashMap<>();
      this.lastUsedTokenType = new HashMap<>();
   }

   public void loadPlayerTokens(Player player) {
      UUID playerUUID = player.getUniqueId();
      DataBaseManager.PlayerTokens privilegePlayerTokens = this.databaseManager.loadPlayerTokens(playerUUID, "privileges");
      int titulPlayerTokens = this.databaseManager.loadTitulTokens(playerUUID);
      this.privilegeTokens.put(playerUUID, privilegePlayerTokens);
      this.titulTokens.put(playerUUID, titulPlayerTokens);
   }

   public void savePlayerTokens(Player player) {
      UUID playerUUID = player.getUniqueId();
      DataBaseManager.PlayerTokens privilegePlayerTokens = this.privilegeTokens.get(playerUUID);
      Integer titulPlayerTokens = this.titulTokens.get(playerUUID);
      if (privilegePlayerTokens != null) {
         this.databaseManager
            .savePlayerTokens(
               playerUUID, privilegePlayerTokens.getTokens30d(), privilegePlayerTokens.getTokens90d(), privilegePlayerTokens.getTokensInfinity(), "privileges"
            );
      }

      if (titulPlayerTokens != null) {
         this.databaseManager.saveTitulTokens(playerUUID, titulPlayerTokens);
      }
   }

   public void saveAllTokens() {
      for (UUID playerUUID : this.privilegeTokens.keySet()) {
         DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(playerUUID);
         this.databaseManager
            .savePlayerTokens(playerUUID, playerTokens.getTokens30d(), playerTokens.getTokens90d(), playerTokens.getTokensInfinity(), "privileges");
      }

      for (UUID playerUUID : this.titulTokens.keySet()) {
         Integer tokens = this.titulTokens.get(playerUUID);
         this.databaseManager.saveTitulTokens(playerUUID, tokens);
      }
   }

   public int getPrivilegeTokens30(Player player) {
      DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(player.getUniqueId());
      return playerTokens != null ? playerTokens.getTokens30d() : 0;
   }

   public int getPrivilegeTokens90(Player player) {
      DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(player.getUniqueId());
      return playerTokens != null ? playerTokens.getTokens90d() : 0;
   }

   public int getPrivilegeTokensInfinity(Player player) {
      DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(player.getUniqueId());
      return playerTokens != null ? playerTokens.getTokensInfinity() : 0;
   }

   public int getTitulTokens(Player player) {
      Integer tokens = this.titulTokens.get(player.getUniqueId());
      return tokens != null ? tokens : 0;
   }

   public boolean hasPrivilegeTokens(Player player) {
      DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(player.getUniqueId());
      return playerTokens != null && (playerTokens.getTokens30d() > 0 || playerTokens.getTokens90d() > 0 || playerTokens.getTokensInfinity() > 0);
   }

   public boolean hasTitulTokens(Player player) {
      Integer tokens = this.titulTokens.get(player.getUniqueId());
      return tokens != null && tokens > 0;
   }

   public boolean consumePrivilegeToken(Player player) {
      UUID playerUUID = player.getUniqueId();
      DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(playerUUID);
      if (playerTokens == null) {
         return false;
      } else {
         boolean consumed = false;
         if (playerTokens.getTokens30d() > 0) {
            playerTokens.setTokens30d(playerTokens.getTokens30d() - 1);
            this.lastUsedTokenType.put(playerUUID, "30d");
            consumed = true;
         } else if (playerTokens.getTokens90d() > 0) {
            playerTokens.setTokens90d(playerTokens.getTokens90d() - 1);
            this.lastUsedTokenType.put(playerUUID, "90d");
            consumed = true;
         } else if (playerTokens.getTokensInfinity() > 0) {
            playerTokens.setTokensInfinity(playerTokens.getTokensInfinity() - 1);
            this.lastUsedTokenType.put(playerUUID, "infinity");
            consumed = true;
         }

         if (consumed) {
            this.savePlayerTokens(player);
         }

         return consumed;
      }
   }

   public boolean consumeTitulToken(Player player) {
      UUID playerUUID = player.getUniqueId();
      Integer tokens = this.titulTokens.get(playerUUID);
      if (tokens != null && tokens > 0) {
         this.titulTokens.put(playerUUID, tokens - 1);
         this.savePlayerTokens(player);
         return true;
      } else {
         return false;
      }
   }

   public void refundLastPrivilegeToken(Player player) {
      UUID playerUUID = player.getUniqueId();
      String lastType = this.lastUsedTokenType.get(playerUUID);
      if (lastType != null) {
         DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(playerUUID);
         if (playerTokens != null) {
            switch (lastType) {
               case "30d":
                  playerTokens.setTokens30d(playerTokens.getTokens30d() + 1);
                  break;
               case "90d":
                  playerTokens.setTokens90d(playerTokens.getTokens90d() + 1);
                  break;
               case "infinity":
                  playerTokens.setTokensInfinity(playerTokens.getTokensInfinity() + 1);
            }

            this.lastUsedTokenType.remove(playerUUID);
            this.savePlayerTokens(player);
         }
      }
   }

   public void refundTitulToken(Player player) {
      UUID playerUUID = player.getUniqueId();
      Integer tokens = this.titulTokens.get(playerUUID);
      if (tokens != null) {
         this.titulTokens.put(playerUUID, tokens + 1);
         this.savePlayerTokens(player);
      }
   }

   public int getDurationDays(Player player) {
      String lastType = this.lastUsedTokenType.get(player.getUniqueId());
      if (lastType == null) {
         return 30;
      } else {
         switch (lastType) {
            case "30d":
               return 30;
            case "90d":
               return 90;
            case "infinity":
               return 0;
            default:
               return 30;
         }
      }
   }

   public void removePlayerData(Player player) {
      UUID playerUUID = player.getUniqueId();
      this.privilegeTokens.remove(playerUUID);
      this.titulTokens.remove(playerUUID);
      this.lastUsedTokenType.remove(playerUUID);
   }

   public void giveTokens(Player target, String tokenType, int amount, String tokenCategory) {
      if (target != null && amount > 0) {
         UUID playerUUID = target.getUniqueId();
         if (tokenCategory.equals("tituls")) {
            Integer currentTokens = this.titulTokens.get(playerUUID);
            if (currentTokens == null) {
               currentTokens = 0;
            }

            this.titulTokens.put(playerUUID, currentTokens + amount);
         } else {
            DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(playerUUID);
            if (playerTokens == null) {
               playerTokens = new DataBaseManager.PlayerTokens(0, 0, 0);
               this.privilegeTokens.put(playerUUID, playerTokens);
            }

            String var7 = tokenType.toLowerCase();
            switch (var7) {
               case "30d":
                  playerTokens.setTokens30d(playerTokens.getTokens30d() + amount);
                  break;
               case "90d":
                  playerTokens.setTokens90d(playerTokens.getTokens90d() + amount);
                  break;
               case "infinity":
                  playerTokens.setTokensInfinity(playerTokens.getTokensInfinity() + amount);
                  break;
               default:
                  return;
            }
         }

         this.savePlayerTokens(target);
      }
   }

   public void takeTokens(Player target, String tokenType, int amount, String tokenCategory) {
      if (target != null && amount > 0) {
         UUID playerUUID = target.getUniqueId();
         if (tokenCategory.equals("tituls")) {
            Integer currentTokens = this.titulTokens.get(playerUUID);
            if (currentTokens != null) {
               int newAmount = Math.max(0, currentTokens - amount);
               this.titulTokens.put(playerUUID, newAmount);
            }
         } else {
            DataBaseManager.PlayerTokens playerTokens = this.privilegeTokens.get(playerUUID);
            if (playerTokens != null) {
               String var13 = tokenType.toLowerCase();
               switch (var13) {
                  case "30d":
                     int current30d = playerTokens.getTokens30d();
                     playerTokens.setTokens30d(Math.max(0, current30d - amount));
                     break;
                  case "90d":
                     int current90d = playerTokens.getTokens90d();
                     playerTokens.setTokens90d(Math.max(0, current90d - amount));
                     break;
                  case "infinity":
                     int currentInfinity = playerTokens.getTokensInfinity();
                     playerTokens.setTokensInfinity(Math.max(0, currentInfinity - amount));
                     break;
                  default:
                     return;
               }
            }
         }

         this.savePlayerTokens(target);
      }
   }
}
