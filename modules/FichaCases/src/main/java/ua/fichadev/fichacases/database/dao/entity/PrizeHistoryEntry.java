package ua.fichadev.fichacases.database.dao.entity;

import java.beans.ConstructorProperties;

public record PrizeHistoryEntry(String opener, String caseName, String luckName, String prizeName, String prizeMaterial, long timestamp, String serverName) {
   @ConstructorProperties({"opener", "case_name", "luck_name", "prize_name", "prize_material", "timestamp", "server_name"})
   public PrizeHistoryEntry(String opener, String caseName, String luckName, String prizeName, String prizeMaterial, long timestamp, String serverName) {
      this.opener = opener;
      this.caseName = caseName;
      this.luckName = luckName;
      this.prizeName = prizeName;
      this.prizeMaterial = prizeMaterial;
      this.timestamp = timestamp;
      this.serverName = serverName;
   }
}
