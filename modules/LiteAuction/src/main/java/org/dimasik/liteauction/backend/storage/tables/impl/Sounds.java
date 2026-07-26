package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;

public class Sounds extends AbstractTable {
   public Sounds(HikariDataSource dataSource) {
      super(dataSource);
   }

   @Override
   public CompletableFuture<Void> createTable() {
      return CompletableFuture.runAsync(() -> {
         try {
            try (
               Connection connection = this.dataSource.getConnection();
               Statement statement = connection.createStatement();
            ) {
               String sql = "CREATE TABLE IF NOT EXISTS sounds (player VARCHAR(16) PRIMARY KEY, toggle BOOLEAN NOT NULL DEFAULT TRUE)";
               statement.execute(sql);
            }
         } catch (SQLException var9) {
            throw new RuntimeException(var9);
         }
      });
   }

   public CompletableFuture<Boolean> getSoundToggle(String player) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Boolean var5;
            try (
               Connection connection = this.dataSource.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT toggle FROM sounds WHERE player = ?");
            ) {
               statement.setString(1, player);

               try (ResultSet rs = statement.executeQuery()) {
                  if (rs.next()) {
                     return rs.getBoolean("toggle");
                  }

                  var5 = true;
               }
            }

            return var5;
         } catch (SQLException var13) {
            throw new RuntimeException(var13);
         }
      });
   }

   public CompletableFuture<Void> setSoundToggle(String player, boolean toggle) {
      return CompletableFuture.runAsync(
         () -> {
            try {
               try (
                  Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO sounds (player, toggle) VALUES (?, ?) ON DUPLICATE KEY UPDATE toggle = ?"
                  );
               ) {
                  statement.setString(1, player);
                  statement.setBoolean(2, toggle);
                  statement.setBoolean(3, toggle);
                  statement.executeUpdate();
               }
            } catch (SQLException var11) {
               throw new RuntimeException(var11);
            }
         }
      );
   }
}
