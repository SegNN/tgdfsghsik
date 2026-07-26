package ru.extas7y.saveitems.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class MyDatabase {
   private Connection connection;
   private final JavaPlugin plugin;
   private final String type;
   private final String host;
   private final String database;
   private final String user;
   private final String password;

   public MyDatabase(JavaPlugin plugin, String type, String host, String database, String user, String password) {
      this.plugin = plugin;
      this.type = type;
      this.host = host;
      this.database = database;
      this.user = user;
      this.password = password;
      this.connect();
   }

   private void connect() {
      try {
         if (this.type.equalsIgnoreCase("MySQL")) {
            String url = "jdbc:mysql://" + this.host + "/" + this.database + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            this.connection = DriverManager.getConnection(url, this.user, this.password);
            Bukkit.getLogger().info("§aПодключено к MySQL!");
         } else {
            File folder = this.plugin.getDataFolder();
            if (!folder.exists()) {
               folder.mkdirs();
            }

            File file = new File(folder, "data.db");
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            Bukkit.getLogger().info("§aПодключено к SQLite!");
         }

         this.createTable();
      } catch (SQLException var4) {
         Bukkit.getLogger().severe("§cОшибка подключения к базе: " + var4.getMessage());
         var4.printStackTrace();
      }
   }

   private void checkConnection() {
      try {
         if (this.connection == null || this.connection.isClosed()) {
            this.connect();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   private void createTable() {
      this.checkConnection();
      String sql;
      if (this.type.equalsIgnoreCase("MySQL")) {
         sql = "CREATE TABLE IF NOT EXISTS saved_items (id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(255) UNIQUE NOT NULL,item_data LONGBLOB NOT NULL,price DOUBLE NOT NULL)";
      } else {
         sql = "CREATE TABLE IF NOT EXISTS saved_items (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL,item_data BLOB NOT NULL,price REAL NOT NULL)";
      }

      try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
         ps.execute();
      } catch (SQLException var7) {
         var7.printStackTrace();
      }
   }

   public void addItem(String name, ItemStack item, double price) {
      this.checkConnection();
      String sql = "INSERT INTO saved_items (name, item_data, price) VALUES (?, ?, ?)";

      try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
         ps.setString(1, name);
         ps.setBytes(2, this.itemStackToBytes(item));
         ps.setDouble(3, price);
         ps.executeUpdate();
      } catch (SQLException var11) {
         var11.printStackTrace();
      }
   }

   public void removeItem(String name) {
      this.checkConnection();
      String sql = "DELETE FROM saved_items WHERE name = ?";

      try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
         ps.setString(1, name);
         ps.executeUpdate();
      } catch (SQLException var8) {
         var8.printStackTrace();
      }
   }

   public String listItems() {
      this.checkConnection();
      StringBuilder sb = new StringBuilder();
      String sql = "SELECT name, price FROM saved_items";

      try (
         PreparedStatement ps = this.connection.prepareStatement(sql);
         ResultSet rs = ps.executeQuery();
      ) {
         while (rs.next()) {
            sb.append(rs.getString("name")).append(" - ").append(rs.getDouble("price")).append("\n");
         }
      } catch (SQLException var11) {
         var11.printStackTrace();
      }

      return sb.toString();
   }

   public ItemStack getItem(String name) {
      this.checkConnection();
      String sql = "SELECT item_data FROM saved_items WHERE name = ?";

      try {
         ItemStack var12;
         try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
               if (!rs.next()) {
                  return null;
               }

               var12 = this.itemStackFromBytes(rs.getBytes("item_data"));
            }
         }

         return var12;
      } catch (SQLException var11) {
         var11.printStackTrace();
         return null;
      }
   }

   public double getPrice(String name) {
      this.checkConnection();
      String sql = "SELECT price FROM saved_items WHERE name = ?";

      try {
         double var12;
         try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
               if (!rs.next()) {
                  return -1.0;
               }

               var12 = rs.getDouble("price");
            }
         }

         return var12;
      } catch (SQLException var11) {
         var11.printStackTrace();
         return -1.0;
      }
   }

   public void closeConnection() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   private byte[] itemStackToBytes(ItemStack item) {
      try {
         byte[] var4;
         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);

            try {
               oos.writeObject(item);
               var4 = baos.toByteArray();
            } catch (Throwable var8) {
               try {
                  oos.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            oos.close();
         }

         return var4;
      } catch (IOException var10) {
         var10.printStackTrace();
         return null;
      }
   }

   private ItemStack itemStackFromBytes(byte[] data) {
      try {
         ItemStack var4;
         try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);

            try {
               var4 = (ItemStack)ois.readObject();
            } catch (Throwable var8) {
               try {
                  ois.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            ois.close();
         }

         return var4;
      } catch (ClassNotFoundException | IOException var10) {
         var10.printStackTrace();
         return null;
      }
   }
}
