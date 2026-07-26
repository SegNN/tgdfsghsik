package ru.kotikstasika.holyliteend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.kotikstasika.holyliteend.api.Hologram;
import ru.kotikstasika.holyliteend.api.PlaceHolderAPI;
import ru.kotikstasika.holyliteend.command.EggCommand;
import ru.kotikstasika.holyliteend.command.HolyLiteEndCommand;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.end.listeners.EggListener;
import ru.kotikstasika.holyliteend.end.listeners.EndListener;
import ru.kotikstasika.holyliteend.end.managers.Dragon;
import ru.kotikstasika.holyliteend.end.managers.EndEvent;
import ru.kotikstasika.holyliteend.end.managers.bossbar.EggBossBar;
import ru.kotikstasika.holyliteend.end.managers.bossbar.EndBossBar;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;
import ua.fichatime.fichaspawneroved.FichaSpawneroved;

public class HolyLiteEnd extends JavaPlugin {
   private static HolyLiteEnd instance;
   private HikariDataSource dataSource;
   private DatabaseManager databaseManager;
   private EndEvent endEventManager;
   private Hologram hologramManager;
   private EggBossBar eggBossBarManager;
   private EndBossBar endBossBarManager;
   private Dragon dragonManager;
   private Location endSpawnLocation;
   private FichaSpawneroved fichaSpawneroved;

   public HolyLiteEnd() {
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.fichaSpawneroved = (FichaSpawneroved)Bukkit.getPluginManager().getPlugin("FichaSpawneroved");
      this.initializeDatabase();
      this.databaseManager = new DatabaseManager(this);
      this.endEventManager = new EndEvent(this);
      this.hologramManager = new Hologram(this);
      this.eggBossBarManager = new EggBossBar(this);
      this.endBossBarManager = new EndBossBar(this);
      this.dragonManager = new Dragon(this);
      EggItem.initialize(this);
      this.registerCommands();
      this.registerListeners();
      this.databaseManager.loadAllEggsOnReload();
      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         try {
            PlaceHolderAPI expansion = new PlaceHolderAPI(this);
            expansion.register();
         } catch (Exception var2) {
            this.getLogger().warning("Не удалось зарегистрировать PlaceholderAPI расширение: " + var2.getMessage());
         }
      }
   }

   public void onDisable() {
      if (this.endEventManager != null) {
         this.endEventManager.shutdownEvent();
         this.endEventManager.shutdown();
      }

      if (this.hologramManager != null) {
         this.hologramManager.shutdown();
      }

      if (this.eggBossBarManager != null) {
         this.eggBossBarManager.shutdown();
      }

      if (this.endBossBarManager != null) {
         this.endBossBarManager.stopEndBossBar();
      }

      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   private void initializeDatabase() {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(this.getConfig().getString("database.url", "jdbc:mysql://localhost:3306/holyliteend"));
      config.setUsername(this.getConfig().getString("database.username", "root"));
      config.setPassword(this.getConfig().getString("database.password", ""));
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(5);
      config.setConnectionTimeout(30000L);
      config.setIdleTimeout(600000L);
      config.setMaxLifetime(1800000L);
      config.setKeepaliveTime(60000L);
      config.setValidationTimeout(5000L);
      config.addDataSourceProperty("socketTimeout", "15000");
      config.addDataSourceProperty("connectTimeout", "10000");
      config.addDataSourceProperty("tcpKeepAlive", "true");
      this.dataSource = new HikariDataSource(config);

      try {
         Connection connection = this.dataSource.getConnection();

         try {
            this.createTables(connection);
         } catch (Throwable var6) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException var7) {
         this.getLogger().severe("Ошибка при инициализации базы данных: " + var7.getMessage());
      }
   }

   private void createTables(Connection connection) throws SQLException {
      String createEggsTable = "CREATE TABLE IF NOT EXISTS dragon_eggs (id INT AUTO_INCREMENT PRIMARY KEY,x INT NOT NULL,y INT NOT NULL,z INT NOT NULL,world VARCHAR(64) NOT NULL,server VARCHAR(64) NOT NULL DEFAULT '',base_value INT NOT NULL DEFAULT 250,current_value INT NOT NULL DEFAULT 250,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,last_increase_time BIGINT NOT NULL DEFAULT 0)";
      String createEndSettingsTable = "CREATE TABLE IF NOT EXISTS end_settings (id INT PRIMARY KEY,start_hour INT NOT NULL,start_minute INT NOT NULL,day_of_week INT NOT NULL,duration INT NOT NULL,timezone VARCHAR(64) NOT NULL DEFAULT 'Europe/Moscow',updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
      connection.createStatement().execute(createEggsTable);
      connection.createStatement().execute(createEndSettingsTable);

      try {
         String insertDefaultSettings = "INSERT INTO end_settings (id, start_hour, start_minute, day_of_week, duration, timezone) SELECT 1, 16, 0, 1, 90, 'Europe/Moscow' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM end_settings WHERE id = 1)";
         connection.createStatement().execute(insertDefaultSettings);
      } catch (SQLException var9) {
      }

      try {
         String alterAddServer = "ALTER TABLE dragon_eggs ADD COLUMN IF NOT EXISTS server VARCHAR(64) NOT NULL DEFAULT ''";
         connection.createStatement().execute(alterAddServer);

         try {
            String alterRemovePlayer = "ALTER TABLE dragon_eggs DROP COLUMN player_name";
            connection.createStatement().execute(alterRemovePlayer);
         } catch (SQLException var7) {
         }
      } catch (SQLException var8) {
      }

      try {
         connection.createStatement().execute("DROP TABLE IF EXISTS end_events");
      } catch (SQLException var6) {
      }
   }

   private void registerCommands() {
      HolyLiteEndCommand mainCommand = new HolyLiteEndCommand(this);
      EggCommand eggCommand = new EggCommand(this);
      super.getCommand("holyliteend").setExecutor(mainCommand);
      super.getCommand("holyliteend").setTabCompleter(mainCommand);
      super.getCommand("egg").setExecutor(eggCommand);
      super.getCommand("egg").setTabCompleter(eggCommand);
   }

   private void registerListeners() {
      super.getServer().getPluginManager().registerEvents(new EndListener(this), this);
      super.getServer().getPluginManager().registerEvents(new EggListener(this), this);
   }

   public void setEndSpawnLocation(Location location) {
      this.endSpawnLocation = location;
      this.getConfig().set("end-spawn.world", location.getWorld().getName());
      this.getConfig().set("end-spawn.x", location.getX());
      this.getConfig().set("end-spawn.y", location.getY());
      this.getConfig().set("end-spawn.z", location.getZ());
      this.getConfig().set("end-spawn.yaw", location.getYaw());
      this.getConfig().set("end-spawn.pitch", location.getPitch());
      this.saveConfig();
   }

   public Location getEndSpawnLocation() {
      if (this.endSpawnLocation == null && this.getConfig().contains("end-spawn")) {
         String worldName = this.getConfig().getString("end-spawn.world", "world_the_end");
         double x = this.getConfig().getDouble("end-spawn.x", 0.0);
         double y = this.getConfig().getDouble("end-spawn.y", 64.0);
         double z = this.getConfig().getDouble("end-spawn.z", 0.0);
         float yaw = (float)this.getConfig().getDouble("end-spawn.yaw", 0.0);
         float pitch = (float)this.getConfig().getDouble("end-spawn.pitch", 0.0);
         this.endSpawnLocation = new Location(this.getServer().getWorld(worldName), x, y, z, yaw, pitch);
      }

      return this.endSpawnLocation;
   }

   @Generated
   public static HolyLiteEnd getInstance() {
      return instance;
   }

   @Generated
   public HikariDataSource getDataSource() {
      return this.dataSource;
   }

   @Generated
   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   @Generated
   public EndEvent getEndEventManager() {
      return this.endEventManager;
   }

   @Generated
   public Hologram getHologramManager() {
      return this.hologramManager;
   }

   @Generated
   public EggBossBar getEggBossBarManager() {
      return this.eggBossBarManager;
   }

   @Generated
   public EndBossBar getEndBossBarManager() {
      return this.endBossBarManager;
   }

   @Generated
   public Dragon getDragonManager() {
      return this.dragonManager;
   }

   @Generated
   public FichaSpawneroved getFichaSpawneroved() {
      return this.fichaSpawneroved;
   }
}
