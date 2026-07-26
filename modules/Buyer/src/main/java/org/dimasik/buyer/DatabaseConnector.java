package org.dimasik.buyer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseConnector {
   private String url;
   private String user;
   private String password;
   private Connection connection;

   public DatabaseConnector(Buyer plugin) {
      String host = plugin.getConfig().getString("MySQL.host");
      String database = plugin.getConfig().getString("MySQL.database");
      this.user = plugin.getConfig().getString("MySQL.user");
      this.password = plugin.getConfig().getString("MySQL.password");
      this.url = "jdbc:mysql://" + host + "/" + database + "?autoReconnect=true";

      try {
         this.connection = DriverManager.getConnection(this.url, this.user, this.password);
         this.createTables();
      } catch (SQLException var5) {
         var5.printStackTrace();
      }
   }

   private void createTables() {
      try {
         Statement statement = this.connection.createStatement();
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS koefficent (player VARCHAR(36) NOT NULL PRIMARY KEY, points DOUBLE NOT NULL DEFAULT 0.0, koefficent DOUBLE NOT NULL DEFAULT 1.0) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
         );
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS prices (name VARCHAR(100) NOT NULL PRIMARY KEY, displayName VARCHAR(255) NOT NULL, points DOUBLE NOT NULL DEFAULT 0.0, price DOUBLE NOT NULL DEFAULT 0.0, category VARCHAR(50) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
         );
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS playerdata (player VARCHAR(36) NOT NULL, material VARCHAR(100) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (player, material)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
         );
         statement.executeUpdate(
            "INSERT IGNORE INTO prices (name, displayName, points, price, category) VALUES ('DIAMOND', 'Алмаз', 10.0, 100.0, 'mine'), ('EMERALD', 'Изумруд', 15.0, 150.0, 'mine'), ('GOLD_INGOT', 'Золотой слиток', 5.0, 50.0, 'mine'), ('IRON_INGOT', 'Железный слиток', 2.5, 25.0, 'mine'), ('COAL', 'Уголь', 0.5, 5.0, 'mine'), ('REDSTONE', 'Красная пыль', 1.0, 10.0, 'mine'), ('LAPIS_LAZULI', 'Лазурит', 2.0, 20.0, 'mine'), ('ANCIENT_DEBRIS', 'Древние обломки', 50.0, 500.0, 'mine'), ('NETHERITE_SCRAP', 'Незеритовый скрап', 40.0, 400.0, 'mine'), ('BONE', 'Кость', 0.5, 5.0, 'mob'), ('ROTTEN_FLESH', 'Гнилая плоть', 0.3, 3.0, 'mob'), ('SPIDER_EYE', 'Паучий глаз', 1.0, 10.0, 'mob'), ('STRING', 'Нить', 0.5, 5.0, 'mob'), ('GUNPOWDER', 'Порох', 2.0, 20.0, 'mob'), ('ENDER_PEARL', 'Жемчуг Края', 5.0, 50.0, 'mob'), ('BLAZE_ROD', 'Огненный стержень', 10.0, 100.0, 'mob'), ('SLIME_BALL', 'Слизь', 3.0, 30.0, 'mob'), ('OAK_LOG', 'Дубовое бревно', 0.5, 5.0, 'tree'), ('SPRUCE_LOG', 'Еловое бревно', 0.5, 5.0, 'tree'), ('BIRCH_LOG', 'Берёзовое бревно', 0.5, 5.0, 'tree'), ('JUNGLE_LOG', 'Тропическое бревно', 0.5, 5.0, 'tree'), ('ACACIA_LOG', 'Акациевое бревно', 0.5, 5.0, 'tree'), ('DARK_OAK_LOG', 'Бревно тёмного дуба', 0.5, 5.0, 'tree'), ('CRIMSON_STEM', 'Адский стебель', 1.0, 10.0, 'tree'), ('WARPED_STEM', 'Искажённый стебель', 1.0, 10.0, 'tree'), ('WHEAT', 'Пшеница', 0.3, 3.0, 'fermer'), ('CARROT', 'Морковь', 0.3, 3.0, 'fermer'), ('POTATO', 'Картофель', 0.3, 3.0, 'fermer'), ('BEETROOT', 'Свёкла', 0.3, 3.0, 'fermer'), ('MELON_SLICE', 'Ломтик арбуза', 0.2, 2.0, 'fermer'), ('PUMPKIN', 'Тыква', 0.5, 5.0, 'fermer'), ('SUGAR_CANE', 'Сахарный тростник', 0.5, 5.0, 'fermer'), ('CACTUS', 'Кактус', 0.5, 5.0, 'fermer'), ('BAMBOO', 'Бамбук', 0.2, 2.0, 'fermer'), ('SWEET_BERRIES', 'Сладкие ягоды', 0.5, 5.0, 'fermer')"
         );
         statement.close();
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   public Connection getConnection() {
      return this.connection;
   }

   public CompletableFuture<Void> closeConnection() {
      return CompletableFuture.runAsync(() -> {
         if (this.connection != null) {
            try {
               this.connection.close();
            } catch (SQLException var2) {
               var2.printStackTrace();
            }
         }
      });
   }
}
