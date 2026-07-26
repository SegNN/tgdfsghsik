package org.nezxenka.litestaffchat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.litestaffchat.command.StaffChatCommand;
import org.nezxenka.litestaffchat.command.StaffChatTabCompleter;
import org.nezxenka.litestaffchat.command.StaffChatToggleCommand;
import org.nezxenka.litestaffchat.config.ConfigManager;
import org.nezxenka.litestaffchat.config.DatabaseConfig;
import org.nezxenka.litestaffchat.listener.PlayerListener;
import org.nezxenka.litestaffchat.redis.RedisManager;
import org.nezxenka.litestaffchat.redis.RedisSubscriber;
import org.nezxenka.litestaffchat.service.ChatService;

public final class LiteStaffChat extends JavaPlugin {
   private static LiteStaffChat instance;
   private ConfigManager configManager;
   private DatabaseConfig databaseConfig;
   private RedisManager redisManager;
   private RedisSubscriber redisSubscriber;
   private ChatService chatService;
   private final Set<UUID> staffChatEnabled = ConcurrentHashMap.newKeySet();
   private boolean placeholderAPIAvailable;

   public LiteStaffChat() {
   }

   public void onEnable() {
      long startTime = System.currentTimeMillis();
      instance = this;
      this.saveDefaultConfig();
      this.databaseConfig = new DatabaseConfig(this);
      this.configManager = new ConfigManager(this);
      this.placeholderAPIAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
      this.chatService = new ChatService(this);
      if (this.setupRedis()) {
         this.redisSubscriber = new RedisSubscriber(this, this.redisManager, this.chatService);
         this.redisSubscriber.start();
      }

      this.registerCommands();
      this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

      for (Player player : Bukkit.getOnlinePlayers()) {
         if (player.hasPermission("litestaffchat.see")) {
            this.staffChatEnabled.add(player.getUniqueId());
         }
      }

      long loadTime = System.currentTimeMillis() - startTime;
      this.getLogger().info("LiteStaffChat enabled in " + loadTime + "ms");
   }

   public void onDisable() {
      if (this.redisSubscriber != null) {
         this.redisSubscriber.shutdown();
      }

      if (this.redisManager != null) {
         this.redisManager.shutdown();
      }

      this.staffChatEnabled.clear();
   }

   private boolean setupRedis() {
      if (!this.databaseConfig.isRedisEnabled()) {
         this.getLogger().info("Redis is disabled in config, running in single-server mode");
         return false;
      } else {
         try {
            this.redisManager = new RedisManager(this, this.databaseConfig);
            return true;
         } catch (Exception var2) {
            this.getLogger().severe("Failed to connect to Redis: " + var2.getMessage());
            return false;
         }
      }
   }

   private void registerCommands() {
      StaffChatCommand staffChatCommand = new StaffChatCommand(this);
      StaffChatTabCompleter tabCompleter = new StaffChatTabCompleter();
      this.getCommand("sc").setExecutor(staffChatCommand);
      this.getCommand("sc").setTabCompleter(tabCompleter);
      this.getCommand("sct").setExecutor(new StaffChatToggleCommand(this));
   }

   public static LiteStaffChat getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public DatabaseConfig getDatabaseConfig() {
      return this.databaseConfig;
   }

   public RedisManager getRedisManager() {
      return this.redisManager;
   }

   public ChatService getChatService() {
      return this.chatService;
   }

   public Set<UUID> getStaffChatEnabled() {
      return this.staffChatEnabled;
   }

   public boolean isStaffChatEnabled(Player player) {
      return this.staffChatEnabled.contains(player.getUniqueId());
   }

   public boolean isPlaceholderAPIAvailable() {
      return this.placeholderAPIAvailable;
   }
}
