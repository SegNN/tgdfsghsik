package org.dimasik.liteexchange.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteexchange.database.venom.Coins;
import org.dimasik.liteexchange.database.venom.Logs;
import org.dimasik.liteexchange.database.venom.Lots;

public class DBCore {
   private HikariDataSource dataSource;
   private Coins coins;
   private Lots lots;
   private Logs logs;
   private JavaPlugin plugin;
   public String tablePrefix;

   public DBCore(JavaPlugin plugin, String host, int port, String database, String username, String password) {
      this.plugin = plugin;

      try {
         HikariConfig config = new HikariConfig();
         config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
         config.setUsername(username);
         config.setPassword(password);
         config.addDataSourceProperty("autoReconnect", "true");
         config.addDataSourceProperty("cachePrepStmts", "true");
         config.addDataSourceProperty("prepStmtsCacheSize", "250");
         config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         this.dataSource = new HikariDataSource(config);
         if (!this.testConnection()) {
            plugin.getLogger().severe("Не удалось подключиться к базе данных. Плагин будет отключен.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
         }

         this.createTables();
         this.initManagers();
      } catch (Exception var81) {
         plugin.getLogger().severe("Ошибка при инициализации подключения к базе данных: " + var81.getMessage());
         plugin.getServer().getPluginManager().disablePlugin(plugin);
      }
   }

   private boolean testConnection() {
      try {
         boolean var2;
         try (Connection connection = this.dataSource.getConnection()) {
            var2 = connection.isValid(2);
         }

         return var2;
      } catch (SQLException var7) {
         this.plugin.getLogger().severe("Проверка подключения к БД не пройдена: " + var7.getMessage());
         return false;
      }
   }

   private void createTables() {
      try {
         this.tablePrefix = this.plugin.getConfig().getString("database.table-prefix", "coins_");

         try (Connection connection = this.dataSource.getConnection()) {
            connection.createStatement()
               .execute("CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "coins (player_name VARCHAR(16) PRIMARY KEY, value INT NOT NULL)");
            connection.createStatement()
               .execute(
                  "CREATE TABLE IF NOT EXISTS "
                     + this.tablePrefix
                     + "lots (id INT AUTO_INCREMENT PRIMARY KEY, player_name VARCHAR(16) NOT NULL, coins INT NOT NULL, rate INT NOT NULL, createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
               );
            connection.createStatement()
               .execute(
                  "CREATE TABLE IF NOT EXISTS "
                     + this.tablePrefix
                     + "logs (id INT AUTO_INCREMENT PRIMARY KEY, player_name VARCHAR(16) NOT NULL, target VARCHAR(16) NOT NULL, time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, action ENUM('GIVE','TAKE','SET','PAY') NOT NULL, coins INT NOT NULL, rate INT NOT NULL)"
               );
         }
      } catch (SQLException var61) {
         this.plugin.getLogger().severe("Ошибка при создании таблиц: " + var61.getMessage());
      }
   }

   private void initManagers() {
      this.tablePrefix = this.plugin.getConfig().getString("database.table-prefix", "coins_");
      this.coins = new Coins(this.dataSource, this.tablePrefix);
      this.lots = new Lots(this.dataSource, this.tablePrefix);
      this.logs = new Logs(this.dataSource, this.tablePrefix);
   }

   public Connection getConnection() throws SQLException {
      return this.dataSource.getConnection();
   }

   public Coins getCoins() {
      return this.coins;
   }

   public Lots getLots() {
      return this.lots;
   }

   public Logs getLogs() {
      return this.logs;
   }

   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }
}
