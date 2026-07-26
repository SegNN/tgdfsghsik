package org.nezxenka.litestaffchat.redis;

import org.nezxenka.litestaffchat.LiteStaffChat;
import org.nezxenka.litestaffchat.config.DatabaseConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
   private final LiteStaffChat plugin;
   private final JedisPool jedisPool;

   public RedisManager(LiteStaffChat plugin, DatabaseConfig databaseConfig) {
      this.plugin = plugin;
      this.jedisPool = this.createPool(databaseConfig);
   }

   private JedisPool createPool(DatabaseConfig config) {
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(10);
      poolConfig.setMaxIdle(5);
      poolConfig.setMinIdle(1);
      poolConfig.setMaxWaitMillis(2000L);
      poolConfig.setTestOnBorrow(true);
      poolConfig.setTestOnReturn(true);
      String password = config.getPassword();
      int timeout = 2000;
      return password != null && !password.isEmpty()
         ? new JedisPool(poolConfig, config.getHost(), config.getPort(), timeout, password)
         : new JedisPool(poolConfig, config.getHost(), config.getPort(), timeout);
   }

   public Jedis getResource() {
      return this.jedisPool.getResource();
   }

   public void publish(String channel, String message) {
      try (Jedis jedis = this.getResource()) {
         jedis.publish(channel, message);
      } catch (Exception var16) {
         this.plugin.getLogger().severe("Redis publish error: " + var16.getMessage());
      }
   }

   public void subscribe(Jedis jedis, RedisSubscriber subscriber, String... channels) {
      jedis.subscribe(subscriber, channels);
   }

   public void shutdown() {
      if (this.jedisPool != null && !this.jedisPool.isClosed()) {
         this.jedisPool.close();
      }
   }

   public boolean isConnected() {
      try (Jedis jedis = this.getResource()) {
         return "PONG".equals(jedis.ping());
      } catch (Exception var15) {
         return false;
      }
   }
}
