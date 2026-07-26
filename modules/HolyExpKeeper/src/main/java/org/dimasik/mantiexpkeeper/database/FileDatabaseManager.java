package org.dimasik.mantiexpkeeper.database;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileDatabaseManager implements DatabaseManager {
   private final File dataFolder;
   private final Logger logger;
   private final String fileName;
   private File dbFile;
   private YamlConfiguration dbConfig;
   private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> cache = new ConcurrentHashMap<>();
   private final Object lock = new Object();
   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");

   public FileDatabaseManager(File dataFolder, YamlConfiguration config, Logger logger) {
      this.dataFolder = dataFolder;
      this.logger = logger;
      this.fileName = config.getString("database.file", "expkeeper.yml");
   }

   @Override
   public void init() throws Exception {
      this.dbFile = new File(this.dataFolder, this.fileName);
      if (!this.dbFile.exists()) {
         this.dbFile.createNewFile();
      }

      this.dbConfig = YamlConfiguration.loadConfiguration(this.dbFile);
      this.loadIntoCache();
      this.logger.info("Файловая база данных успешно инициализирована: " + this.dbFile.getAbsolutePath());
   }

   private void loadIntoCache() {
      for (String player : this.dbConfig.getKeys(false)) {
         ConcurrentHashMap<String, Long> playerAbilities = new ConcurrentHashMap<>();

         for (String ability : this.dbConfig.getConfigurationSection(player).getKeys(false)) {
            long expiry = this.dbConfig.getLong(player + "." + ability);
            playerAbilities.put(ability, expiry);
         }

         this.cache.put(player.toLowerCase(), playerAbilities);
      }
   }

   private void saveCache() {
      synchronized (this.lock) {
         this.dbConfig = new YamlConfiguration();

         for (String player : this.cache.keySet()) {
            ConcurrentHashMap<String, Long> abilities = this.cache.get(player);
            if (abilities != null && !abilities.isEmpty()) {
               for (String ability : abilities.keySet()) {
                  Long expiry = abilities.get(ability);
                  if (expiry != null) {
                     this.dbConfig.set(player + "." + ability, expiry);
                  }
               }
            }
         }

         try {
            this.dbConfig.save(this.dbFile);
         } catch (IOException var9) {
            this.logger.severe("Ошибка при сохранении базы данных: " + var9.getMessage());
         }
      }
   }

   @Override
   public void close() {
      this.saveCache();
   }

   @Override
   public CompletableFuture<Void> cleanExpiredAbilities(Executor executor) {
      return CompletableFuture.runAsync(() -> {
         long now = System.currentTimeMillis();
         boolean changed = false;

         for (String player : this.cache.keySet()) {
            ConcurrentHashMap<String, Long> abilities = this.cache.get(player);
            if (abilities != null) {
               changed |= abilities.entrySet().removeIf(entry -> entry.getValue() != -1L && entry.getValue() < now);
            }
         }

         if (changed) {
            this.saveCache();
         }
      }, executor);
   }

   @Override
   public CompletableFuture<Void> addTime(Executor executor, String player, String ability, long expiry) {
      return CompletableFuture.runAsync(() -> {
         String playerKey = player.toLowerCase();
         ConcurrentHashMap<String, Long> abilities = this.cache.computeIfAbsent(playerKey, k -> new ConcurrentHashMap<>());
         abilities.put(ability, expiry);
         this.saveCache();
      }, executor);
   }

   @Override
   public CompletableFuture<Void> addInf(Executor executor, String player, String ability) {
      return CompletableFuture.runAsync(() -> {
         String playerKey = player.toLowerCase();
         ConcurrentHashMap<String, Long> abilities = this.cache.computeIfAbsent(playerKey, k -> new ConcurrentHashMap<>());
         abilities.put(ability, -1L);
         this.saveCache();
      }, executor);
   }

   @Override
   public CompletableFuture<Void> removePlayer(Executor executor, String player, String ability) {
      return CompletableFuture.runAsync(() -> {
         String playerKey = player.toLowerCase();
         ConcurrentHashMap<String, Long> abilities = this.cache.get(playerKey);
         if (abilities != null) {
            abilities.remove(ability);
            if (abilities.isEmpty()) {
               this.cache.remove(playerKey);
            }

            this.saveCache();
         }
      }, executor);
   }

   @Override
   public CompletableFuture<Boolean> hasAbility(Executor executor, String player, String ability) {
      return CompletableFuture.supplyAsync(() -> {
         ConcurrentHashMap<String, Long> abilities = this.cache.get(player.toLowerCase());
         if (abilities == null) {
            return false;
         } else {
            Long expiry = abilities.get(ability);
            return expiry == null ? false : expiry == -1L || expiry > System.currentTimeMillis();
         }
      }, executor);
   }

   @Override
   public CompletableFuture<String> getAbilityTime(Executor executor, String player, String ability) {
      return CompletableFuture.supplyAsync(() -> {
         ConcurrentHashMap<String, Long> abilities = this.cache.get(player.toLowerCase());
         if (abilities == null) {
            return null;
         } else {
            Long expiry = abilities.get(ability);
            if (expiry == null) {
               return null;
            } else {
               return expiry == -1L ? "**.**.****  **:**:**" : DATE_FORMAT.format(new Date(expiry));
            }
         }
      }, executor);
   }
}
