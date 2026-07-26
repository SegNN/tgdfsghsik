package org.dimasik.liteauction.backend.communication.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocket extends AbstractCommunication {
   private WebSocketClient webSocketClient;
   private final String host;
   private final int port;
   private final String password;
   private boolean authenticated = false;

   public WebSocket(String host, int port, String password, String channel) {
      super(channel);
      this.host = host;
      this.port = port;
      this.password = password;
      this.connect();
   }

   @Override
   public void connect() {
      try {
         URI serverUri = new URI("ws://" + this.host + ":" + this.port);
         this.webSocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
               WebSocket.this.authenticate();
            }

            @Override
            public void onMessage(String message) {
               WebSocket.this.handleIncomingMessage(message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
               String message = StandardCharsets.UTF_8.decode(bytes).toString();
               WebSocket.this.handleIncomingMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
               WebSocket.this.authenticated = false;
            }

            @Override
            public void onError(Exception ex) {
               throw new RuntimeException(ex);
            }
         };
         this.webSocketClient.connect();
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   private void authenticate() {
      if (this.password != null && !this.password.isEmpty()) {
         String authMessage = "AUTH|" + this.channel + "|" + this.password;
         this.webSocketClient.send(authMessage);
      }
   }

   private void handleIncomingMessage(String message) {
      try {
         String[] parts = message.split(" ", 3);
         if (parts.length < 2) {
            System.err.println("Invalid message format: " + message);
         } else {
            String command = parts[0];
            String channel = parts[1];
            String content = parts.length > 2 ? parts[2] : "";
            if ("AUTH".equals(command)) {
               if ("SUCCESS".equals(content)) {
                  this.authenticated = true;
               } else {
                  this.authenticated = false;
               }
            } else if ("MESSAGE".equals(command)) {
               access$001(this, channel, content);
            } else if ("ERROR".equals(command)) {
               throw new RuntimeException("Ошибка в WebSocket: " + content);
            }
         }
      } catch (Exception var6) {
         throw new RuntimeException(var6);
      }
   }

   @Override
   public void publishMessage(String channelType, String message) {
      if (this.webSocketClient != null && this.webSocketClient.isOpen()) {
         String fullMessage = "MESSAGE|" + this.channel + "_" + channelType + "|" + message;
         this.webSocketClient.send(fullMessage);
      } else {
         throw new RuntimeException("DISCONNECTED");
      }
   }

   @Override
   public void close() {
      if (this.webSocketClient != null) {
         this.webSocketClient.close();
      }
   }
}
