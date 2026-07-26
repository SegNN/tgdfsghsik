package ua.holydev.holycases.configuration;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ua.holydev.holycases.database.ConnectionInfo;

public class DatabaseConfig {
   private File rawConfig;
   private YamlConfiguration config;

   public DatabaseConfig(Plugin plugin) {
      this.rawConfig = new File(plugin.getDataFolder(), "DatabaseConfig.yml");
      if (!this.rawConfig.exists()) {
         try {
            this.rawConfig.getParentFile().mkdirs();
            this.rawConfig.createNewFile();
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      }

      this.config = YamlConfiguration.loadConfiguration(this.rawConfig);
      this.checkDefault();
      this.save();
   }

   private void save() {
      try {
         this.config.save(this.rawConfig);
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   private void setIfNotExists(String path, Object o) {
      if (!this.config.contains(path)) {
         this.config.set(path, o);
      }
   }

   private void checkDefault() {
      this.setIfNotExists("mysql.host", "localhost");
      this.setIfNotExists("mysql.port", 3306);
      this.setIfNotExists("mysql.database", "db");
      this.setIfNotExists("mysql.user", "root");
      this.setIfNotExists("mysql.password", "root");
      this.setIfNotExists("mysql.group-concat-length", 1000000);
   }

   public ConnectionInfo getConnectionInfo() {
      String host = this.config.getString("mysql.host");
      int port = this.config.getInt("mysql.port");
      String database = this.config.getString("mysql.database");
      String user = this.config.getString("mysql.user");
      String password = this.config.getString("mysql.password");
      int groupConcatLength = this.config.getInt("mysql.group-concat-length");
      return new ConnectionInfo(host, port, database, user, password, groupConcatLength);
   }
}
