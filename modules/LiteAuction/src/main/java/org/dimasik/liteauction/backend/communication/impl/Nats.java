package org.dimasik.liteauction.backend.communication.impl;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Options.Builder;
import java.io.IOException;
import java.time.Duration;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;

public class Nats extends AbstractCommunication {
   private Connection connection;
   private final String[] servers;
   private final String username;
   private final String password;
   private Dispatcher dispatcher;

   public Nats(String[] servers, String username, String password, String channel) {
      super(channel);
      this.servers = servers;
      this.username = username;
      this.password = password;
      this.connect();
      this.subscribeToChannel();
   }

   @Override
   public void connect() {
      try {
         Builder optionsBuilder = new Builder()
            .servers(this.servers)
            .connectionTimeout(Duration.ofSeconds(2L))
            .pingInterval(Duration.ofSeconds(30L))
            .reconnectWait(Duration.ofSeconds(1L))
            .maxReconnects(10);
         if (this.username != null && this.password != null) {
            optionsBuilder.userInfo(this.username, this.password);
         }

         this.connection = io.nats.client.Nats.connect(optionsBuilder.build());
      } catch (InterruptedException | IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   public void subscribeToChannel() {
      this.dispatcher = this.connection.createDispatcher(msg -> {
         String subject = msg.getSubject();
         String channelType = this.extractChannelType(subject);
         String message = new String(msg.getData());
         access$001(this, channelType, message);
      });
      this.dispatcher.subscribe(this.channel + "_msg.*");
      this.dispatcher.subscribe(this.channel + "_hover.*");
      this.dispatcher.subscribe(this.channel + "_sound.*");
      this.dispatcher.subscribe(this.channel + "_update.*");
   }

   private String extractChannelType(String subject) {
      int underscoreIndex = subject.lastIndexOf(95);
      return underscoreIndex != -1 && underscoreIndex < subject.length() - 1 ? subject.substring(underscoreIndex + 1) : subject;
   }

   @Override
   public void publishMessage(String channelType, String message) {
      try {
         String subject = this.channel + "_" + channelType;
         this.connection.publish(subject, message.getBytes());
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public void close() {
      try {
         if (this.dispatcher != null) {
            this.dispatcher.unsubscribe(this.channel + "_msg.*");
            this.dispatcher.unsubscribe(this.channel + "_hover.*");
            this.dispatcher.unsubscribe(this.channel + "_sound.*");
            this.dispatcher.unsubscribe(this.channel + "_update.*");
         }

         if (this.connection != null) {
            this.connection.close();
         }
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }
   }
}
