package org.nezxenka.litestaffchat.config;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nezxenka.litestaffchat.LiteStaffChat;

public class DatabaseConfig {
   private final LiteStaffChat plugin;
   private final File file;
   private FileConfiguration config;
   private String serverName;
   private String serverMode;
   private boolean redisEnabled;
   private String host;
   private int port;
   private String password;

   public DatabaseConfig(LiteStaffChat plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "database.yml");
      this.loadConfig();
   }

   public void loadConfig() {
      if (!this.file.exists()) {
         this.plugin.saveResource("database.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(this.file);
      this.serverName = this.config.getString("server.name", "lite-1");
      this.serverMode = this.config.getString("server.mode", "lite").toLowerCase();
      this.redisEnabled = this.config.getBoolean("redis.enabled", false);
      this.host = this.config.getString("redis.host", "localhost");
      this.port = this.config.getInt("redis.port", 6379);
      this.password = this.config.getString("redis.password", "");
   }

   public void reloadConfig() {
      this.config = YamlConfiguration.loadConfiguration(this.file);
      this.loadConfig();
   }

   public boolean isRedisEnabled() {
      return this.redisEnabled;
   }

   public String getServerName() {
      return this.serverName;
   }

   public String getServerMode() {
      return this.serverMode;
   }

   public String getHost() {
      return this.host;
   }

   public int getPort() {
      return this.port;
   }

   public String getPassword() {
      return this.password;
   }
}
