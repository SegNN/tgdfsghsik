package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class Redis extends AbstractCommunication {
   private JedisPool jedisPool;
   private JedisPubSub pubSub;
   private final String host;
   private final int port;
   private final String password;

   public Redis(String host, int port, String password, String channel) {
      super(channel);
      this.host = host;
      this.port = port;
      this.password = password;
      this.connect();
      this.subscribeToChannel();
   }

   @Override
   public void connect() {
      try {
         JedisPoolConfig poolConfig = new JedisPoolConfig();
         poolConfig.setMaxTotal(10);
         poolConfig.setMaxIdle(5);
         poolConfig.setMinIdle(1);
         poolConfig.setTestOnBorrow(true);
         poolConfig.setTestOnReturn(true);
         poolConfig.setTestWhileIdle(true);
         this.jedisPool = new JedisPool(poolConfig, this.host, this.port, 2000, this.password);
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   public void subscribeToChannel() {
      this.pubSub = new JedisPubSub() {
         @Override
         public void onMessage(String channel, String message) {
            Redis.super.onMessage(channel, message);
         }
      };
      new Thread(() -> {
         try {
            try (Jedis jedis = this.jedisPool.getResource()) {
               jedis.subscribe(this.pubSub, this.channel + "_msg", this.channel + "_hover", this.channel + "_sound", this.channel + "_update");
            }
         } catch (Exception var6) {
            throw new RuntimeException(var6);
         }
      }, "Auction redis thread").start();
   }

   @Override
   public void publishMessage(String channel, String message) {
      try {
         try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.publish(this.channel + "_" + channel, message);
         }
      } catch (Exception var8) {
         throw new RuntimeException(var8);
      }
   }

   @Override
   public void close() {
      if (this.pubSub != null && this.pubSub.isSubscribed()) {
         this.pubSub.unsubscribe();
      }

      if (this.jedisPool != null) {
         this.jedisPool.close();
      }
   }
}
