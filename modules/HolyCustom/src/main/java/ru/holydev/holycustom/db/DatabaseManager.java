package ru.holydev.holycustom.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import ru.holydev.holycustom.HolyCustom;
import ru.holydev.holycustom.manager.ConfigManager;

public class DatabaseManager {
   private final HolyCustom plugin;
   private final ConfigManager configManager;
   private static Connection connection;
   private String host;
   private String database;
   private static String username;
   private static String password;
   private static String url;
   private static final Executor databaseExecutor = Executors.newFixedThreadPool(4);

   public DatabaseManager(HolyCustom plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
      this.loadDatabase();
      this.initializeDatabase();
   }

   private void initializeDatabase() {
      connectToDatabase();
      this.createTableIfNotExists();
   }

   private void loadDatabase() {
      this.host = this.configManager.getString("database.host", " ");
      this.database = this.configManager.getString("database.name", "custom");
      username = this.configManager.getString("database.username", "root");
      password = this.configManager.getString("database.password", " ");
      url = "jdbc:mysql://" + this.host + "/" + this.database + "?useSSL=false&autoReconnect=true";
   }

   private static void connectToDatabase() {
      try {
         Class.forName("com.mysql.jdbc.Driver");
         connection = DriverManager.getConnection(url, username, password);
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }

   private void createTableIfNotExists() {
      String sql = "CREATE TABLE IF NOT EXISTS players (name VARCHAR(16) PRIMARY KEY, data TEXT);";

      try (PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.execute();
      } catch (SQLException var7) {
         var7.printStackTrace();
      }
   }

   private static void checkConnection() {
      try {
         if (connection == null || connection.isClosed()) {
            connectToDatabase();
         }
      } catch (SQLException var1) {
         var1.printStackTrace();
      }
   }

   public static CompletableFuture<Void> setPlayerAsync(String name, String base64) {
      return CompletableFuture.runAsync(() -> {
         checkConnection();
         String sql = "REPLACE INTO players (name, data) VALUES (?, ?)";

         try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, base64);
            statement.executeUpdate();
         } catch (SQLException var8) {
            var8.printStackTrace();
         }
      }, databaseExecutor);
   }

   public static CompletableFuture<String> getPlayerAsync(String name) {
      return CompletableFuture.supplyAsync(() -> {
         checkConnection();
         String sql = "SELECT data FROM players WHERE name = ?";

         try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getString("data") : null;
         } catch (SQLException var7) {
            var7.printStackTrace();
            return null;
         }
      }, databaseExecutor);
   }

   public static String toBase64(HashMap<String, String> map) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(map);
         oos.close();
         return Base64.getEncoder().encodeToString(baos.toByteArray());
      } catch (IOException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static HashMap<String, String> fromBase64(String base64) {
      if (base64 != null && !base64.isEmpty()) {
         try {
            byte[] data = Base64.getDecoder().decode(base64);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            HashMap<String, String> map = (HashMap<String, String>)ois.readObject();
            ois.close();
            return map;
         } catch (ClassNotFoundException | IOException var4) {
            var4.printStackTrace();
            return new HashMap<>();
         }
      } else {
         return new HashMap<>();
      }
   }
}
