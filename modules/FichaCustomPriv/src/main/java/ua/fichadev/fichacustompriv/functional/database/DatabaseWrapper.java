package ua.fichadev.fichacustompriv.functional.database;

import java.util.UUID;
import ua.fichadev.fichacustompriv.FichaCustomPriv;

public class DatabaseWrapper {
   private DataBaseManager database;

   public DatabaseWrapper(FichaCustomPriv plugin) {
      String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
      if (dbType.equals("mysql")) {
         this.database = new MySQLManager(plugin);
      } else {
         this.database = new SQLiteManager(plugin);
      }

      this.database.connect();
   }

   public DataBaseManager getDatabase() {
      return this.database;
   }

   public void closeConnection() {
      this.database.disconnect();
   }

   public void savePlayerTokens(UUID playerUUID, int tokens30d, int tokens90d, int tokensInfinity, String tokenCategory) {
      this.database.savePlayerTokens(playerUUID, tokens30d, tokens90d, tokensInfinity, tokenCategory);
   }

   public DataBaseManager.PlayerTokens loadPlayerTokens(UUID playerUUID, String tokenCategory) {
      return this.database.loadPlayerTokens(playerUUID, tokenCategory);
   }

   public int loadTitulTokens(UUID playerUUID) {
      return this.database.loadTitulTokens(playerUUID);
   }

   public void saveTitulTokens(UUID playerUUID, int tokens) {
      this.database.saveTitulTokens(playerUUID, tokens);
   }
}
