package org.dimasik.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Main {
   private static final String CONFIG_FILE = "server.properties";
   private static final String DEFAULT_PORT = "8080";
   private static final String DEFAULT_PASSWORD = "admin123";
   private static int serverPort;
   private static String password;
   private static WebSocketServer server;
   private static final Set<WebSocket> authenticatedConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());

   public Main() {
   }

   public static void main(String[] args) {
      loadConfig();
      System.out.println("   [   зᴀпуᴄᴋ ᴄᴇᴘʙᴇᴘᴀ   ]   ");
      startServer();
   }

   private static void loadConfig() {
      Properties props = new Properties();
      File configFile = new File("server.properties");

      try {
         if (!configFile.exists()) {
            createDefaultConfig(configFile);
            System.out.println("   |   ᴄᴏздᴀʜ ȹᴀйл ᴋᴏʜфигуᴘᴀции");
         }

         try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
         }

         serverPort = Integer.parseInt(props.getProperty("server-port", "8080"));
         password = props.getProperty("password", "admin123");
         System.out.println("   [   ᴋᴏʜфигуᴘᴀция зᴀгᴘужᴇʜᴀ   ]   ");
         System.out.println("   |   пᴏᴘт: " + serverPort);
         System.out.println("   |   пᴀᴘᴏль: " + (password.isEmpty() ? "ʜᴇ зᴀдᴀʜ" : "зᴀдᴀʜ"));
      } catch (IOException var7) {
         System.err.println("   |   ᴏшибᴋᴀ чтᴇʜия ᴋᴏʜȹигуᴘᴀции: " + var7.getMessage());
         System.exit(1);
      } catch (NumberFormatException var8) {
         System.err.println("   |   ʜᴇʙᴇᴘʜый пᴏᴘт ʙ ᴋᴏʜфигуᴘᴀции");
         System.exit(1);
      }
   }

   private static void createDefaultConfig(File configFile) throws IOException {
      try (FileOutputStream fos = new FileOutputStream(configFile)) {
         Properties defaultProps = new Properties();
         defaultProps.setProperty("server-port", "8080");
         defaultProps.setProperty("password", "admin123");
         defaultProps.store(fos, "WebSocket server properties");
      }
   }

   private static void startServer() {
      server = new WebSocketServer(new InetSocketAddress(serverPort)) {
         @Override
         public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("   |   ʜᴏʙᴏᴇ пᴏдᴋлючᴇʜиᴇ: " + conn.getRemoteSocketAddress());
         }

         @Override
         public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("   |   ᴏтᴋлючᴇʜиᴇ: " + conn.getRemoteSocketAddress());
            Main.authenticatedConnections.remove(conn);
         }

         @Override
         public void onMessage(WebSocket conn, String message) {
            this.handleMessage(conn, message);
         }

         @Override
         public void onMessage(WebSocket conn, ByteBuffer message) {
            String textMessage = StandardCharsets.UTF_8.decode(message).toString();
            this.handleMessage(conn, textMessage);
         }

         private void handleMessage(WebSocket conn, String message) {
            try {
               String[] parts = message.split("\\|", 3);
               if (parts.length < 3) {
                  conn.send("ERROR Неверный формат сообщения");
                  return;
               }

               String command = parts[0];
               String channel = parts[1];
               String content = parts[2];
               if ("AUTH".equals(command)) {
                  this.handleAuthentication(conn, content);
               } else if ("MESSAGE".equals(command)) {
                  this.handleChannelMessage(conn, channel, content);
               } else {
                  conn.send("ERROR Неизвестная команда: " + command);
               }
            } catch (Exception var7) {
               conn.send("ERROR Ошибка обработки сообщения: " + var7.getMessage());
            }
         }

         private void handleAuthentication(WebSocket conn, String receivedPassword) {
            if (Main.password.equals(receivedPassword)) {
               Main.authenticatedConnections.add(conn);
               conn.send("AUTH SUCCESS");
               System.out.println("   |   ᴋлиᴇʜт ᴀутᴇʜтифициᴘᴏʙᴀʜ: " + conn.getRemoteSocketAddress());
            } else {
               conn.send("AUTH FAILED");
               System.out.println("   |   ʜᴇудᴀчʜᴀя пᴏпытᴋᴀ ᴀутᴇʜтифиᴋᴀции: " + conn.getRemoteSocketAddress());
            }
         }

         private void handleChannelMessage(WebSocket conn, String channel, String message) {
            if (!Main.authenticatedConnections.contains(conn)) {
               conn.send("ERROR Требуется аутентификация");
            } else {
               String broadcastMessage = "MESSAGE " + channel + " " + message;

               for (WebSocket client : Main.authenticatedConnections) {
                  if (client.isOpen()) {
                     client.send(broadcastMessage);
                  }
               }

               System.out.println("   |   ᴄᴏᴏбщᴇʜиᴇ ʙ ᴋᴀʜᴀлᴇ '" + channel + "': " + message);
            }
         }

         @Override
         public void onError(WebSocket conn, Exception ex) {
            if (conn != null) {
               System.err.println("   |   ᴏшибᴋᴀ для ᴋлиᴇʜтᴀ " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
            } else {
               System.err.println("   |   ᴏшибᴋᴀ ᴄᴇᴘʙᴇᴘᴀ: " + ex.getMessage());
            }
         }

         @Override
         public void onStart() {
            System.out.println("   |   ᴄᴇᴘʙᴇᴘ зᴀпущᴇʜ ʜᴀ пᴏᴘту " + Main.serverPort);
         }
      };
      server.start();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         try {
            server.stop();
            System.out.println("   |   ᴄᴇᴘʙᴇᴘ ᴏᴄтᴀʜᴏʙлᴇʜ");
         } catch (InterruptedException var1) {
            Thread.currentThread().interrupt();
         }
      }));
   }
}
