package org.dimasik.liteauction.backend.storage.databases.impl;

import com.zaxxer.hikari.HikariConfig;
import org.dimasik.liteauction.backend.storage.databases.AbstractDatabase;

public class Mysql extends AbstractDatabase {
   public Mysql(String host, String username, String password, String database) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
      config.setUsername(username);
      config.setPassword(password);
      config.addDataSourceProperty("autoReconnect", "true");
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtsCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      super.initialize(config);
   }
}
