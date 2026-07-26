package org.dimasik.playtimeapi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.playtimeapi.commands.PlayTimeCommand;
import org.dimasik.playtimeapi.database.DatabaseManager;
import org.dimasik.playtimeapi.listeners.ActivityListener;
import org.dimasik.playtimeapi.models.PlayerData;

public final class Main extends JavaPlugin implements Listener {
   private DatabaseManager databaseManager;
   private String serverName;
   private final Map<UUID, PlayerData> activePlayers = new ConcurrentHashMap<>();
   public final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
   private static Main instance;

   public Main() {
   }

   public static Main getInstance() {
      return instance;
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.serverName = this.getConfig().getString("server.name", "unknown");
      this.databaseManager = new DatabaseManager(this.getConfig(), this.getDataFolder());
      this.getCommand("playtime").setExecutor(new PlayTimeCommand(this));

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.loadPlayerData(player.getUniqueId());
      }

      Bukkit.getPluginManager().registerEvents(this, this);
      Bukkit.getPluginManager().registerEvents(new ActivityListener(this), this);
      this.startActivityCheckTask();
      this.startAutoSaveTask();
   }

   public void onDisable() {
      this.saveAllPlayerData();
      this.databaseManager.closeConnection();
   }

   private void loadPlayerData(UUID uuid) {
      this.databaseManager.getPlayerDataAsync(uuid).thenAccept(playerData -> {
         if (playerData == null) {
            playerData = new PlayerData(uuid);
         }

         playerData.setCurrentServer(this.serverName);
         playerData.setLastLogin(System.currentTimeMillis());
         playerData.setLastActivity(System.currentTimeMillis());
         this.activePlayers.put(uuid, playerData);
         this.lastActivity.put(uuid, System.currentTimeMillis());
         this.databaseManager.savePlayerDataAsync(playerData);
      });
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.loadPlayerData(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.handlePlayerQuit(event.getPlayer());
   }

   private void handlePlayerQuit(Player player) {
      UUID uuid = player.getUniqueId();
      PlayerData playerData = this.activePlayers.remove(uuid);
      if (playerData != null) {
         playerData.setCurrentServer("Оффлайн");
         this.databaseManager.savePlayerDataAsync(playerData);
      }

      this.lastActivity.remove(uuid);
   }

   private void startActivityCheckTask() {
      Bukkit.getScheduler().runTaskTimer(this, () -> {
         long currentTime = System.currentTimeMillis();

         for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!player.isDead() && player.isOnline()) {
               PlayerData playerData = this.activePlayers.get(uuid);
               if (playerData != null) {
                  long lastActive = this.lastActivity.getOrDefault(uuid, currentTime);
                  playerData.addPlayTime(1000L);
                  if (currentTime - lastActive <= 5000L) {
                     playerData.addActiveTime(1000L);
                  }
               }
            }
         }
      }, 20L, 20L);
   }

   private void startAutoSaveTask() {
      Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveAllPlayerData, 100L, 100L);
   }

   private void saveAllPlayerData() {
      for (PlayerData playerData : this.activePlayers.values()) {
         this.databaseManager.savePlayerDataAsync(playerData);
      }
   }

   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   public PlayerData getPlayerData(UUID uuid) {
      PlayerData data = this.activePlayers.get(uuid);
      return data != null ? data : this.databaseManager.getPlayerDataAsync(uuid).join();
   }

   public long getLastActivity(UUID uuid) {
      return this.lastActivity.getOrDefault(uuid, System.currentTimeMillis());
   }
}
