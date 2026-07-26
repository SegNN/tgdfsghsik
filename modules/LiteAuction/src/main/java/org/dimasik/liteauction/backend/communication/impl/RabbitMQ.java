package org.dimasik.liteauction.backend.communication.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;

public class RabbitMQ extends AbstractCommunication {
   private Connection connection;
   private Channel channel;
   private final String host;
   private final int port;
   private final String username;
   private final String password;
   private final String virtualHost;
   private DefaultConsumer consumer;

   public RabbitMQ(String host, int port, String username, String password, String virtualHost, String channel) {
      super(channel);
      this.host = host;
      this.port = port;
      this.username = username;
      this.password = password;
      this.virtualHost = virtualHost;
      this.connect();
      this.subscribeToChannel();
   }

   @Override
   public void connect() {
      try {
         ConnectionFactory factory = new ConnectionFactory();
         factory.setHost(this.host);
         factory.setPort(this.port);
         factory.setUsername(this.username);
         factory.setPassword(this.password);
         factory.setVirtualHost(this.virtualHost);
         factory.setConnectionTimeout(2000);
         factory.setRequestedHeartbeat(30);
         this.connection = factory.newConnection();
         this.channel = this.connection.createChannel();
         this.channel.exchangeDeclare(this.channel + "_msg", "fanout", true);
         this.channel.exchangeDeclare(this.channel + "_hover", "fanout", true);
         this.channel.exchangeDeclare(this.channel + "_sound", "fanout", true);
         this.channel.exchangeDeclare(this.channel + "_update", "fanout", true);
      } catch (TimeoutException | IOException var2) {
         throw new RuntimeException("Failed to connect to RabbitMQ", var2);
      }
   }

   public void subscribeToChannel() {
      try {
         String queueNameMsg = this.channel.queueDeclare().getQueue();
         String queueNameHover = this.channel.queueDeclare().getQueue();
         String queueNameSound = this.channel.queueDeclare().getQueue();
         String queueNameUpdate = this.channel.queueDeclare().getQueue();
         this.channel.queueBind(queueNameMsg, this.channel + "_msg", "");
         this.channel.queueBind(queueNameHover, this.channel + "_hover", "");
         this.channel.queueBind(queueNameSound, this.channel + "_sound", "");
         this.channel.queueBind(queueNameUpdate, this.channel + "_update", "");
         this.consumer = new DefaultConsumer(this.channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
               String message = new String(body);
               String routingKey = envelope.getExchange();
               String channelName = routingKey.replace(RabbitMQ.access$001(RabbitMQ.this) + "_", "");
               RabbitMQ.super.onMessage(channelName, message);
            }
         };
         this.channel.basicConsume(queueNameMsg, true, this.consumer);
         this.channel.basicConsume(queueNameHover, true, this.consumer);
         this.channel.basicConsume(queueNameSound, true, this.consumer);
         this.channel.basicConsume(queueNameUpdate, true, this.consumer);
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   @Override
   public void publishMessage(String channelType, String message) {
      try {
         this.channel.basicPublish(this.channel + "_" + channelType, "", null, message.getBytes());
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public void close() {
      try {
         if (this.channel != null && this.channel.isOpen()) {
            this.channel.close();
         }

         if (this.connection != null && this.connection.isOpen()) {
            this.connection.close();
         }
      } catch (TimeoutException | IOException var2) {
      }
   }
}
