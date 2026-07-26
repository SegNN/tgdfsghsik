package org.nezxenka.litestaffchat.redis;

import org.bukkit.Bukkit;
import org.nezxenka.litestaffchat.LiteStaffChat;
import org.nezxenka.litestaffchat.service.ChatService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber extends JedisPubSub {
   private final LiteStaffChat plugin;
   private final RedisManager redisManager;
   private final ChatService chatService;
   private volatile boolean running;

   public RedisSubscriber(LiteStaffChat plugin, RedisManager redisManager, ChatService chatService) {
      this.plugin = plugin;
      this.redisManager = redisManager;
      this.chatService = chatService;
   }

   public void start() {
      this.running = true;
      Thread subscriberThread = new Thread(() -> {
         while (this.running && !Thread.currentThread().isInterrupted()) {
            try (Jedis jedis = this.redisManager.getResource()) {
               jedis.subscribe(this, "StaffChat");
            } catch (Exception var16) {
               if (this.running) {
                  this.plugin.getLogger().warning("Redis subscriber disconnected, reconnecting...");

                  try {
                     Thread.sleep(5000L);
                  } catch (InterruptedException var15) {
                     Thread.currentThread().interrupt();
                     break;
                  }
               }
            }
         }
      }, "Redis-Subscriber");
      subscriberThread.setDaemon(true);
      subscriberThread.start();
   }

   @Override
   public void onMessage(String channel, String message) {
      String[] parts = message.split("\\|\\|", 3);
      if (parts.length >= 3) {
         Bukkit.getScheduler().runTask(this.plugin, () -> this.chatService.broadcastMessage(parts[0], parts[1], parts[2]));
      }
   }

   @Override
   public void onSubscribe(String channel, int subscribedChannels) {
      this.plugin.getLogger().info("Subscribed to Redis channel: " + channel);
   }

   public void shutdown() {
      this.running = false;
      if (this.isSubscribed()) {
         this.unsubscribe();
      }
   }
}
