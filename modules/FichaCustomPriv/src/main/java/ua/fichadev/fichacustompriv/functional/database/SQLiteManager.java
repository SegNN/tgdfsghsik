package ua.fichadev.fichacustompriv.functional.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ua.fichadev.fichacustompriv.FichaCustomPriv;

public class SQLiteManager extends DataBaseManager {
   private Connection connection;
   private String fileName;

   public SQLiteManager(FichaCustomPriv plugin) {
      super(plugin);
      this.fileName = plugin.getConfig().getString("database.sqlite.filename", "custompriv.db");
   }

   @Override
   public void connect() {
      try {
         String url = "jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/" + this.fileName;
         this.connection = DriverManager.getConnection(url);
         this.createTables();
         this.plugin.getLogger().info("SQLite подключена");
      } catch (SQLException var2) {
         this.plugin.getLogger().severe("Ошибка подключения к SQLite: " + var2.getMessage());
      }
   }

   @Override
   public void disconnect() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException var2) {
         this.plugin.getLogger().severe("Ошибка отключения от SQLite: " + var2.getMessage());
      }
   }

   @Override
   public void createTables() {
      String privilegeTokensTable = "CREATE TABLE IF NOT EXISTS privilege_tokens (id INTEGER PRIMARY KEY AUTOINCREMENT,player_uuid VARCHAR(36) NOT NULL UNIQUE,tokens_30d INTEGER DEFAULT 0,tokens_90d INTEGER DEFAULT 0,tokens_infinity INTEGER DEFAULT 0,last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
      String titulTokensTable = "CREATE TABLE IF NOT EXISTS titul_tokens (id INTEGER PRIMARY KEY AUTOINCREMENT,player_uuid VARCHAR(36) NOT NULL UNIQUE,tokens INTEGER DEFAULT 0,last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
      String privilegesTable = "CREATE TABLE IF NOT EXISTS privileges (id INTEGER PRIMARY KEY AUTOINCREMENT,player_name VARCHAR(32) NOT NULL,player_uuid VARCHAR(36) NOT NULL,privilege_text TEXT NOT NULL,privilege_color VARCHAR(20) DEFAULT '&f',duration_days INTEGER DEFAULT 30,created_time TEXT NOT NULL,status VARCHAR(20) DEFAULT 'pending',approved_by VARCHAR(32),approved_time TEXT);";
      String titlesTable = "CREATE TABLE IF NOT EXISTS titles (id INTEGER PRIMARY KEY AUTOINCREMENT,player_name VARCHAR(32) NOT NULL,player_uuid VARCHAR(36) NOT NULL,title TEXT NOT NULL,created_time TEXT NOT NULL,status VARCHAR(20) DEFAULT 'pending',approved_by VARCHAR(32),approved_time TEXT);";

      try (Statement stmt = this.connection.createStatement()) {
         stmt.execute(privilegeTokensTable);
         stmt.execute(titulTokensTable);
         stmt.execute(privilegesTable);
         stmt.execute(titlesTable);
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка создания таблиц SQLite: " + var10.getMessage());
      }
   }

   @Override
   public void savePlayerTokens(UUID playerUUID, int tokens30d, int tokens90d, int tokensInfinity, String tokenCategory) {
      String tableName = tokenCategory.equals("tituls") ? "titul_tokens" : "privilege_tokens";
      String query = "INSERT OR REPLACE INTO " + tableName + " (player_uuid, tokens_30d, tokens_90d, tokens_infinity) VALUES (?, ?, ?, ?)";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, playerUUID.toString());
         stmt.setInt(2, tokens30d);
         stmt.setInt(3, tokens90d);
         stmt.setInt(4, tokensInfinity);
         stmt.executeUpdate();
      } catch (SQLException var13) {
         this.plugin.getLogger().severe("Ошибка сохранения токенов SQLite для " + playerUUID + ": " + var13.getMessage());
      }
   }

   @Override
   public DataBaseManager.PlayerTokens loadPlayerTokens(UUID playerUUID, String tokenCategory) {
      String tableName = tokenCategory.equals("tituls") ? "titul_tokens" : "privilege_tokens";
      String query = "SELECT tokens_30d, tokens_90d, tokens_infinity FROM " + tableName + " WHERE player_uuid = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, playerUUID.toString());
         ResultSet rs = stmt.executeQuery();
         return rs.next()
            ? new DataBaseManager.PlayerTokens(rs.getInt("tokens_30d"), rs.getInt("tokens_90d"), rs.getInt("tokens_infinity"))
            : new DataBaseManager.PlayerTokens(0, 0, 0);
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка загрузки токенов SQLite для " + playerUUID + ": " + var10.getMessage());
         return new DataBaseManager.PlayerTokens(0, 0, 0);
      }
   }

   @Override
   public int loadTitulTokens(UUID playerUUID) {
      String query = "SELECT tokens FROM titul_tokens WHERE player_uuid = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, playerUUID.toString());
         ResultSet rs = stmt.executeQuery();
         return rs.next() ? rs.getInt("tokens") : 0;
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка загрузки титульных токенов SQLite для " + playerUUID + ": " + var8.getMessage());
         return 0;
      }
   }

   @Override
   public void saveTitulTokens(UUID playerUUID, int tokens) {
      String query = "INSERT OR REPLACE INTO titul_tokens (player_uuid, tokens) VALUES (?, ?)";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, playerUUID.toString());
         stmt.setInt(2, tokens);
         stmt.executeUpdate();
      } catch (SQLException var9) {
         this.plugin.getLogger().severe("Ошибка сохранения титульных токенов SQLite для " + playerUUID + ": " + var9.getMessage());
      }
   }

   @Override
   public void savePrivilege(Map<String, String> privilege) {
      String query = "INSERT INTO privileges (id, player_name, player_uuid, privilege_text, privilege_color, duration_days, created_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setInt(1, Integer.parseInt(privilege.get("id")));
         stmt.setString(2, privilege.get("player_name"));
         stmt.setString(3, privilege.get("player_uuid"));
         stmt.setString(4, privilege.get("privilege_text"));
         stmt.setString(5, privilege.get("privilege_color"));
         stmt.setInt(6, Integer.parseInt(privilege.get("duration_days")));
         stmt.setString(7, privilege.get("created_time"));
         stmt.setString(8, privilege.get("status"));
         stmt.executeUpdate();
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка сохранения привилегии: " + var8.getMessage());
      }
   }

   @Override
   public void saveTitle(Map<String, String> title) {
      String query = "INSERT INTO titles (id, player_name, player_uuid, title, created_time, status) VALUES (?, ?, ?, ?, ?, ?)";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setInt(1, Integer.parseInt(title.get("id")));
         stmt.setString(2, title.get("player_name"));
         stmt.setString(3, title.get("player_uuid"));
         stmt.setString(4, title.get("title"));
         stmt.setString(5, title.get("created_time"));
         stmt.setString(6, title.get("status"));
         stmt.executeUpdate();
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка сохранения титула: " + var8.getMessage());
      }
   }

   @Override
   public List<Map<String, String>> loadPendingPrivileges() {
      List<Map<String, String>> privileges = new ArrayList<>();
      String query = "SELECT * FROM privileges WHERE status = 'pending' ORDER BY id DESC";

      try (
         PreparedStatement stmt = this.connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
      ) {
         while (rs.next()) {
            Map<String, String> privilege = new HashMap<>();
            privilege.put("id", String.valueOf(rs.getInt("id")));
            privilege.put("player_name", rs.getString("player_name"));
            privilege.put("player_uuid", rs.getString("player_uuid"));
            privilege.put("privilege_text", rs.getString("privilege_text"));
            privilege.put("privilege_color", rs.getString("privilege_color"));
            privilege.put("duration_days", String.valueOf(rs.getInt("duration_days")));
            privilege.put("created_time", rs.getString("created_time"));
            privilege.put("status", rs.getString("status"));
            privileges.add(privilege);
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка загрузки ожидающих привилегий: " + var11.getMessage());
      }

      return privileges;
   }

   @Override
   public List<Map<String, String>> loadApprovedPrivileges() {
      List<Map<String, String>> privileges = new ArrayList<>();
      String query = "SELECT * FROM privileges WHERE status = 'approved' ORDER BY id DESC";

      try (
         PreparedStatement stmt = this.connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
      ) {
         while (rs.next()) {
            Map<String, String> privilege = new HashMap<>();
            privilege.put("id", String.valueOf(rs.getInt("id")));
            privilege.put("player_name", rs.getString("player_name"));
            privilege.put("player_uuid", rs.getString("player_uuid"));
            privilege.put("privilege_text", rs.getString("privilege_text"));
            privilege.put("privilege_color", rs.getString("privilege_color"));
            privilege.put("duration_days", String.valueOf(rs.getInt("duration_days")));
            privilege.put("created_time", rs.getString("created_time"));
            privilege.put("status", rs.getString("status"));
            privilege.put("approved_by", rs.getString("approved_by"));
            privilege.put("approved_time", rs.getString("approved_time"));
            privileges.add(privilege);
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка загрузки одобренных привилегий: " + var11.getMessage());
      }

      return privileges;
   }

   @Override
   public List<Map<String, String>> loadPendingTitles() {
      List<Map<String, String>> titles = new ArrayList<>();
      String query = "SELECT * FROM titles WHERE status = 'pending' ORDER BY id DESC";

      try (
         PreparedStatement stmt = this.connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
      ) {
         while (rs.next()) {
            Map<String, String> title = new HashMap<>();
            title.put("id", String.valueOf(rs.getInt("id")));
            title.put("player_name", rs.getString("player_name"));
            title.put("player_uuid", rs.getString("player_uuid"));
            title.put("title", rs.getString("title"));
            title.put("created_time", rs.getString("created_time"));
            title.put("status", rs.getString("status"));
            titles.add(title);
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка загрузки ожидающих титулов: " + var11.getMessage());
      }

      return titles;
   }

   @Override
   public List<Map<String, String>> loadApprovedTitles() {
      List<Map<String, String>> titles = new ArrayList<>();
      String query = "SELECT * FROM titles WHERE status = 'approved' ORDER BY id DESC";

      try (
         PreparedStatement stmt = this.connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
      ) {
         while (rs.next()) {
            Map<String, String> title = new HashMap<>();
            title.put("id", String.valueOf(rs.getInt("id")));
            title.put("player_name", rs.getString("player_name"));
            title.put("player_uuid", rs.getString("player_uuid"));
            title.put("title", rs.getString("title"));
            title.put("created_time", rs.getString("created_time"));
            title.put("status", rs.getString("status"));
            title.put("approved_by", rs.getString("approved_by"));
            title.put("approved_time", rs.getString("approved_time"));
            titles.add(title);
         }
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Ошибка загрузки одобренных титулов: " + var11.getMessage());
      }

      return titles;
   }

   @Override
   public void updatePrivilegeStatus(int id, String status, String approvedBy) {
      String query = "UPDATE privileges SET status = ?, approved_by = ?, approved_time = ? WHERE id = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, status);
         stmt.setString(2, approvedBy);
         stmt.setString(3, new Date().toString());
         stmt.setInt(4, id);
         stmt.executeUpdate();
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка обновления статуса привилегии: " + var10.getMessage());
      }
   }

   @Override
   public void updateTitleStatus(int id, String status, String approvedBy) {
      String query = "UPDATE titles SET status = ?, approved_by = ?, approved_time = ? WHERE id = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setString(1, status);
         stmt.setString(2, approvedBy);
         stmt.setString(3, String.valueOf(System.currentTimeMillis()));
         stmt.setInt(4, id);
         stmt.executeUpdate();
      } catch (SQLException var10) {
         this.plugin.getLogger().severe("Ошибка обновления статуса титула: " + var10.getMessage());
      }
   }

   @Override
   public void deletePrivilege(int id) {
      String query = "DELETE FROM privileges WHERE id = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setInt(1, id);
         stmt.executeUpdate();
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка удаления привилегии: " + var8.getMessage());
      }
   }

   @Override
   public void deleteTitle(int id) {
      String query = "DELETE FROM titles WHERE id = ?";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.setInt(1, id);
         stmt.executeUpdate();
      } catch (SQLException var8) {
         this.plugin.getLogger().severe("Ошибка удаления титула: " + var8.getMessage());
      }
   }

   @Override
   public void clearPendingPrivileges() {
      String query = "DELETE FROM privileges WHERE status = 'pending'";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.executeUpdate();
      } catch (SQLException var7) {
         this.plugin.getLogger().severe("Ошибка очистки ожидающих привилегий: " + var7.getMessage());
      }
   }

   @Override
   public void clearPendingTitles() {
      String query = "DELETE FROM titles WHERE status = 'pending'";

      try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
         stmt.executeUpdate();
      } catch (SQLException var7) {
         this.plugin.getLogger().severe("Ошибка очистки ожидающих титулов: " + var7.getMessage());
      }
   }
}
