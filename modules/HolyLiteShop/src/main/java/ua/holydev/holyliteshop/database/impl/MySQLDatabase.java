package ua.holydev.holyliteshop.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import ua.holydev.holyliteshop.HolyLiteShop;
import ua.holydev.holyliteshop.database.DatabaseManager;

public class MySQLDatabase implements DatabaseManager {
   private final HikariDataSource dataSource;

   public MySQLDatabase(HolyLiteShop plugin) {
      FileConfiguration cfg = plugin.getConfig();
      String host = cfg.getString("database.mysql.host", "");
      int port = cfg.getInt("database.mysql.port", 3306);
      String database = cfg.getString("database.mysql.database", "HolyLiteShop");
      String username = cfg.getString("database.mysql.username", "");
      String password = cfg.getString("database.mysql.password", "");
      int poolSize = cfg.getInt("database.mysql.pool-size", 10);
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
      config.setUsername(username);
      config.setPassword(password);
      config.setDriverClassName("com.mysql.jdbc.Driver");
      config.setMaximumPoolSize(poolSize);
      config.setPoolName("HolyLiteShop-MySQL");
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      this.dataSource = new HikariDataSource(config);
      this.createTables();
   }

   private void createTables() {
      try (Connection conn = this.dataSource.getConnection()) {
         conn.createStatement()
            .executeUpdate(
               "CREATE TABLE IF NOT EXISTS warehouse (id INTEGER PRIMARY KEY AUTO_INCREMENT, player_uuid VARCHAR(36) NOT NULL, item_data LONGTEXT NOT NULL)"
            );
         conn.createStatement()
            .executeUpdate(
               "CREATE TABLE IF NOT EXISTS purchase_stats (player_uuid VARCHAR(36) NOT NULL, item_id VARCHAR(128) NOT NULL, amount INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (player_uuid, item_id))"
            );
         conn.createStatement()
            .executeUpdate(
               "CREATE TABLE IF NOT EXISTS player_shop (player_uuid VARCHAR(36) NOT NULL PRIMARY KEY, current_items LONGTEXT, discounts LONGTEXT, last_refresh BIGINT NOT NULL DEFAULT 0)"
            );
      } catch (SQLException var14) {
         var14.printStackTrace();
      }
   }

   private Connection getConnection() throws SQLException {
      return this.dataSource.getConnection();
   }

   @Override
   public void savePlayerShop(UUID playerId, List<String> currentItems, Map<String, Double> discounts, long lastRefresh) {
      String itemsStr = String.join(";", currentItems);
      StringBuilder discountsStr = new StringBuilder();

      for (Entry<String, Double> entry : discounts.entrySet()) {
         if (discountsStr.length() > 0) {
            discountsStr.append(";");
         }

         discountsStr.append(entry.getKey()).append("=").append(entry.getValue());
      }

      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO player_shop (player_uuid, current_items, discounts, last_refresh) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE current_items=?, discounts=?, last_refresh=?"
         );
      ) {
         ps.setString(1, playerId.toString());
         ps.setString(2, itemsStr);
         ps.setString(3, discountsStr.toString());
         ps.setLong(4, lastRefresh);
         ps.setString(5, itemsStr);
         ps.setString(6, discountsStr.toString());
         ps.setLong(7, lastRefresh);
         ps.executeUpdate();
      } catch (SQLException var40) {
         var40.printStackTrace();
      }
   }

   @Override
   public void loadPlayerShop(UUID playerId, List<String> currentItems, Map<String, Double> discounts, long[] lastRefresh) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT current_items, discounts, last_refresh FROM player_shop WHERE player_uuid=?");
      ) {
         ps.setString(1, playerId.toString());
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            String itemsStr = rs.getString("current_items");
            if (itemsStr != null && !itemsStr.isEmpty()) {
               for (String s : itemsStr.split(";")) {
                  if (!s.isEmpty()) {
                     currentItems.add(s);
                  }
               }
            }

            String discountsRaw = rs.getString("discounts");
            if (discountsRaw != null && !discountsRaw.isEmpty()) {
               for (String pair : discountsRaw.split(";")) {
                  String[] parts = pair.split("=");
                  if (parts.length == 2) {
                     discounts.put(parts[0], Double.parseDouble(parts[1]));
                  }
               }
            }

            lastRefresh[0] = rs.getLong("last_refresh");
         }
      } catch (SQLException var44) {
         var44.printStackTrace();
      }
   }

   @Override
   public void addWarehouseItem(UUID playerId, ItemStack item) {
      String data = this.itemToBase64(item);
      if (data != null) {
         try (
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO warehouse (player_uuid, item_data) VALUES (?, ?)");
         ) {
            ps.setString(1, playerId.toString());
            ps.setString(2, data);
            ps.executeUpdate();
         } catch (SQLException var36) {
            var36.printStackTrace();
         }
      }
   }

   @Override
   public List<ItemStack> getWarehouseItems(UUID playerId) {
      List<ItemStack> items = new ArrayList<>();

      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT id, item_data FROM warehouse WHERE player_uuid=? ORDER BY id ASC");
      ) {
         ps.setString(1, playerId.toString());
         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            ItemStack item = this.itemFromBase64(rs.getString("item_data"));
            if (item != null) {
               items.add(item);
            }
         }
      } catch (SQLException var36) {
         var36.printStackTrace();
      }

      return items;
   }

   @Override
   public ItemStack removeWarehouseItem(UUID playerId, int index) {
      List<Integer> ids = new ArrayList<>();
      List<String> datas = new ArrayList<>();

      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT id, item_data FROM warehouse WHERE player_uuid=? ORDER BY id ASC");
      ) {
         ps.setString(1, playerId.toString());
         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            ids.add(rs.getInt("id"));
            datas.add(rs.getString("item_data"));
         }
      } catch (SQLException var97) {
         var97.printStackTrace();
         return null;
      }

      if (index >= 0 && index < ids.size()) {
         int rowId = ids.get(index);
         ItemStack item = this.itemFromBase64(datas.get(index));

         try (
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM warehouse WHERE id=?");
         ) {
            ps.setInt(1, rowId);
            ps.executeUpdate();
            return item;
         } catch (SQLException var92) {
            var92.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public void addPurchaseStat(UUID playerId, String itemId, int amount) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO purchase_stats (player_uuid, item_id, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + ?"
         );
      ) {
         ps.setString(1, playerId.toString());
         ps.setString(2, itemId);
         ps.setInt(3, amount);
         ps.setInt(4, amount);
         ps.executeUpdate();
      } catch (SQLException var36) {
         var36.printStackTrace();
      }
   }

   @Override
   public Map<String, Integer> getPlayerPurchaseStats(UUID playerId) {
      Map<String, Integer> stats = new HashMap<>();

      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT item_id, amount FROM purchase_stats WHERE player_uuid=?");
      ) {
         ps.setString(1, playerId.toString());
         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            stats.put(rs.getString("item_id"), rs.getInt("amount"));
         }
      } catch (SQLException var35) {
         var35.printStackTrace();
      }

      return stats;
   }

   @Override
   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   private String itemToBase64(ItemStack item) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeObject(item);
         dataOutput.close();
         return Base64.getEncoder().encodeToString(outputStream.toByteArray());
      } catch (IOException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   private ItemStack itemFromBase64(String data) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
         BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
         ItemStack item = (ItemStack)dataInput.readObject();
         dataInput.close();
         return item;
      } catch (ClassNotFoundException | IOException var5) {
         var5.printStackTrace();
         return null;
      }
   }
}
