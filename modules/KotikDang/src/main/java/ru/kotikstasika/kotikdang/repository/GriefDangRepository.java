package ru.kotikstasika.kotikdang.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.kotikstasika.kotikdang.model.GriefDangData;

public class GriefDangRepository {
   public GriefDangRepository() {
   }

   public void initSchema(Connection connection) throws SQLException {
      try (Statement stmt = connection.createStatement()) {
         stmt.execute(
            "CREATE TABLE IF NOT EXISTS grief_dangs (id TEXT PRIMARY KEY,world TEXT NOT NULL,x REAL NOT NULL,y REAL NOT NULL,z REAL NOT NULL,anchor_world TEXT,anchor_x REAL,anchor_y REAL,anchor_z REAL,unlocked BOOLEAN NOT NULL,anchor_destroyed BOOLEAN NOT NULL,level INTEGER NOT NULL,loot_level INTEGER NOT NULL,name TEXT NOT NULL,backup_path TEXT,region_radius INTEGER DEFAULT 35)"
         );
         stmt.execute(
            "CREATE TABLE IF NOT EXISTS grief_chests (dang_id TEXT NOT NULL,location TEXT NOT NULL,opened BOOLEAN NOT NULL,PRIMARY KEY (dang_id, location),FOREIGN KEY (dang_id) REFERENCES grief_dangs(id) ON DELETE CASCADE)"
         );
         stmt.execute(
            "CREATE TABLE IF NOT EXISTS compass_dangs (id TEXT PRIMARY KEY,world TEXT NOT NULL,x REAL NOT NULL,y REAL NOT NULL,z REAL NOT NULL,name TEXT NOT NULL,level INTEGER NOT NULL,FOREIGN KEY (id) REFERENCES grief_dangs(id) ON DELETE CASCADE)"
         );
      }
   }

   public void saveDang(Connection connection, GriefDangData dangData) throws SQLException {
      this.saveDangToDatabase(connection, dangData);
      this.deleteChestsForDang(connection, dangData.getId());
      this.insertChestsForDang(connection, dangData);
   }

   private void saveDangToDatabase(Connection connection, GriefDangData dangData) throws SQLException {
      String sql = "INSERT OR REPLACE INTO grief_dangs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, dangData.getId());
         stmt.setString(2, dangData.getLocation().getWorld().getName());
         stmt.setDouble(3, dangData.getLocation().getX());
         stmt.setDouble(4, dangData.getLocation().getY());
         stmt.setDouble(5, dangData.getLocation().getZ());
         if (dangData.getAnchor() != null) {
            stmt.setString(6, dangData.getAnchor().getWorld().getName());
            stmt.setDouble(7, dangData.getAnchor().getX());
            stmt.setDouble(8, dangData.getAnchor().getY());
            stmt.setDouble(9, dangData.getAnchor().getZ());
         } else {
            stmt.setNull(6, 12);
            stmt.setNull(7, 8);
            stmt.setNull(8, 8);
            stmt.setNull(9, 8);
         }

         stmt.setBoolean(10, dangData.isUnlocked());
         stmt.setBoolean(11, dangData.isAnchorDestroyed());
         stmt.setInt(12, dangData.getLevel());
         stmt.setInt(13, dangData.getLootLevel());
         stmt.setString(14, dangData.getName());
         if (dangData.getBackupPath() != null) {
            stmt.setString(15, dangData.getBackupPath());
         } else {
            stmt.setNull(15, 12);
         }

         stmt.setInt(16, dangData.getRegionRadius());
         stmt.executeUpdate();
      }
   }

   private void deleteChestsForDang(Connection connection, String dangId) throws SQLException {
      String sql = "DELETE FROM grief_chests WHERE dang_id = ?";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, dangId);
         stmt.executeUpdate();
      }
   }

   private void insertChestsForDang(Connection connection, GriefDangData dangData) throws SQLException {
      String sql = "INSERT INTO grief_chests VALUES (?, ?, ?)";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         for (Entry<String, Boolean> entry : dangData.getChests().entrySet()) {
            stmt.setString(1, dangData.getId());
            stmt.setString(2, entry.getKey());
            stmt.setBoolean(3, entry.getValue());
            stmt.addBatch();
         }

         stmt.executeBatch();
      }
   }

   public void deleteDang(Connection connection, String id) throws SQLException {
      String sql = "DELETE FROM grief_dangs WHERE id = ?";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, id);
         stmt.executeUpdate();
      }
   }

   public List<String> loadAllIds(Connection connection) throws SQLException {
      List<String> ids = new ArrayList<>();
      String sql = "SELECT id FROM grief_dangs";

      try (
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql);
      ) {
         while (rs.next()) {
            ids.add(rs.getString("id"));
         }
      }

      return ids;
   }

   public GriefDangData loadDang(Connection connection, String id) throws SQLException {
      String sql = "SELECT * FROM grief_dangs WHERE id = ?";

      GriefDangData var10;
      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, id);

         try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
               return null;
            }

            World world = Bukkit.getWorld(rs.getString("world"));
            if (world == null) {
               return null;
            }

            Location location = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"));
            Location anchor = this.loadAnchorLocation(rs);
            Map<String, Boolean> chests = this.loadChestsForDang(connection, id);
            var10 = new GriefDangData(
               id,
               location,
               anchor,
               rs.getBoolean("unlocked"),
               rs.getBoolean("anchor_destroyed"),
               rs.getInt("level"),
               rs.getInt("loot_level"),
               rs.getString("name"),
               chests,
               rs.getString("backup_path"),
               rs.getInt("region_radius")
            );
         }
      }

      return var10;
   }

   private Location loadAnchorLocation(ResultSet rs) throws SQLException {
      String anchorWorld = rs.getString("anchor_world");
      if (anchorWorld == null) {
         return null;
      } else {
         World world = Bukkit.getWorld(anchorWorld);
         return world == null ? null : new Location(world, rs.getDouble("anchor_x"), rs.getDouble("anchor_y"), rs.getDouble("anchor_z"));
      }
   }

   private Map<String, Boolean> loadChestsForDang(Connection connection, String dangId) throws SQLException {
      Map<String, Boolean> chests = new HashMap<>();
      String sql = "SELECT * FROM grief_chests WHERE dang_id = ?";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, dangId);

         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               chests.put(rs.getString("location"), rs.getBoolean("opened"));
            }
         }
      }

      return chests;
   }
}
