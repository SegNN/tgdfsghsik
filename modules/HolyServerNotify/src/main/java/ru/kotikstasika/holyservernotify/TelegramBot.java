package ru.kotikstasika.holyservernotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TelegramBot {
   private final String token;
   private final long ownerId;
   private final List<Long> authorizedChats = new CopyOnWriteArrayList<>();
   private final HolyServerNotify plugin;
   private int lastUpdateId = 0;

   public TelegramBot(String token, long ownerId, long ownerChatId, HolyServerNotify plugin) {
      this.token = token;
      this.ownerId = ownerId;
      this.plugin = plugin;
      this.authorizedChats.add(ownerChatId);
   }

   public void startPolling() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         while (this.plugin.isEnabled()) {
            try {
               this.poll();
               Thread.sleep(2000L);
            } catch (Exception var2) {
            }
         }
      });
   }

   private void poll() throws Exception {
      String urlString = "https://api.telegram.org/bot" + this.token + "/getUpdates?offset=" + (this.lastUpdateId + 1);
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
         StringBuilder response = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            response.append(line);
         }

         JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
         if (json.has("result")) {
            for (JsonElement element : json.getAsJsonArray("result")) {
               JsonObject update = element.getAsJsonObject();
               this.lastUpdateId = update.get("update_id").getAsInt();
               if (update.has("message")) {
                  JsonObject message = update.getAsJsonObject("message");
                  if (message.has("text") && message.has("chat")) {
                     String text = message.get("text").getAsString();
                     long chatId = message.getAsJsonObject("chat").get("id").getAsLong();
                     long fromId = message.getAsJsonObject("from").get("id").getAsLong();
                     if (fromId == this.ownerId) {
                        if (text.equalsIgnoreCase("/start")) {
                           if (!this.authorizedChats.contains(chatId)) {
                              this.authorizedChats.add(chatId);
                              this.sendMessage(
                                 chatId,
                                 "✅ Сервер авторизован: "
                                    + this.plugin.getServerIp()
                                    + ":"
                                    + this.plugin.getServerPort()
                                    + " | "
                                    + Bukkit.getOnlinePlayers().size()
                                    + " игроков"
                              );
                           }
                        } else if (this.authorizedChats.contains(chatId)) {
                           if (text.equalsIgnoreCase("/status") || text.equalsIgnoreCase("/list")) {
                              this.sendMessage(
                                 chatId,
                                 "\ud83d\udcca Инстанс: "
                                    + this.plugin.getServerIp()
                                    + ":"
                                    + this.plugin.getServerPort()
                                    + "\nОнлайн: "
                                    + Bukkit.getOnlinePlayers().size()
                                    + "/"
                                    + Bukkit.getMaxPlayers()
                              );
                           } else if (text.equalsIgnoreCase("/stats")) {
                              this.updateGlobalStats();
                              this.sendMessage(chatId, "\ud83d\udd04 Статистика обновляется... Проверьте закрепленное сообщение.");
                           } else if (text.equalsIgnoreCase("/ping")) {
                              this.sendMessage(chatId, "\ud83c\udfd3 Понг от " + this.plugin.getServerIp() + ":" + this.plugin.getServerPort());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void sendMessage(long chatId, String text) {
      this.sendMessage(chatId, text, false);
   }

   public void sendMessage(long chatId, String text, boolean pin) {
      Runnable task = () -> {
         try {
            String urlString = "https://api.telegram.org/bot" + this.token + "/sendMessage?chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
               StringBuilder response = new StringBuilder();

               String line;
               while ((line = reader.readLine()) != null) {
                  response.append(line);
               }

               if (pin) {
                  JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                  if (json.has("result")) {
                     int messageId = json.getAsJsonObject("result").get("message_id").getAsInt();
                     this.pinMessage(chatId, messageId);
                  }
               }
            }
         } catch (Exception var24) {
         }
      };
      if (this.plugin.isEnabled()) {
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
      } else {
         new Thread(task).start();
      }
   }

   private void pinMessage(long chatId, int messageId) {
      try {
         String urlString = "https://api.telegram.org/bot" + this.token + "/pinChatMessage?chat_id=" + chatId + "&message_id=" + messageId;
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         conn.getInputStream().close();
      } catch (Exception var7) {
      }
   }

   public void updateGlobalStats() {
      for (Long chatId : this.authorizedChats) {
         Runnable task = () -> {
            try {
               String urlString = "https://api.telegram.org/bot" + this.token + "/getChat?chat_id=" + chatId;
               URL url = new URL(urlString);
               HttpURLConnection conn = (HttpURLConnection)url.openConnection();
               conn.setRequestMethod("GET");

               try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                  StringBuilder response = new StringBuilder();

                  String line;
                  while ((line = reader.readLine()) != null) {
                     response.append(line);
                  }

                  JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                  if (json.has("result") && json.getAsJsonObject("result").has("pinned_message")) {
                     JsonObject pinned = json.getAsJsonObject("result").getAsJsonObject("pinned_message");
                     String currentText = pinned.get("text").getAsString();
                     int messageId = pinned.get("message_id").getAsInt();
                     if (currentText.startsWith("\ud83d\udcca Глобальная статистика")) {
                        String newText = this.rebuildStats(currentText);
                        if (!newText.equals(currentText)) {
                           this.editMessage(chatId, messageId, newText);
                        }

                        return;
                     }
                  }

                  String initialText = "\ud83d\udcca Глобальная статистика\n\nВсего запущенно сборок: 1\nСейчас запущено: 1\nВсего онлайн: "
                     + Bukkit.getOnlinePlayers().size()
                     + "\n\nСписок:\n- "
                     + this.plugin.getServerIp()
                     + ":"
                     + this.plugin.getServerPort()
                     + " ("
                     + Bukkit.getOnlinePlayers().size()
                     + ")";
                  this.sendMessage(chatId, initialText, true);
               }
            } catch (Exception var26) {
            }
         };
         if (this.plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
         } else {
            new Thread(task).start();
         }
      }
   }

   private String rebuildStats(String oldText) {
      if (oldText != null && !oldText.isEmpty()) {
         try {
            String[] lines = oldText.split("\n");
            int totalLaunched = 0;

            for (String line : lines) {
               if (line.startsWith("Всего запущенно сборок: ")) {
                  try {
                     totalLaunched = Integer.parseInt(line.replace("Всего запущенно сборок: ", "").trim());
                  } catch (Exception var19) {
                  }
               }
            }

            String myEntry = this.plugin.getServerIp() + ":" + this.plugin.getServerPort();
            List<String> servers = new ArrayList<>();
            boolean found = false;
            int totalOnline = 0;
            int activeBuilds = 0;

            for (String linex : lines) {
               if (linex.startsWith("- ")) {
                  String serverInfo = linex.substring(2);
                  if (serverInfo.contains(" ")) {
                     String ipPort = serverInfo.split(" ")[0];
                     if (ipPort.equals(myEntry)) {
                        found = true;
                        servers.add("- " + myEntry + " (" + Bukkit.getOnlinePlayers().size() + ")");
                        totalOnline += Bukkit.getOnlinePlayers().size();
                        activeBuilds++;
                     } else {
                        servers.add(linex);

                        try {
                           int start = serverInfo.indexOf("(") + 1;
                           int end = serverInfo.indexOf(")");
                           if (start > 0 && end > start) {
                              String onlineStr = serverInfo.substring(start, end);
                              totalOnline += Integer.parseInt(onlineStr);
                              activeBuilds++;
                           }
                        } catch (Exception var18) {
                        }
                     }
                  }
               }
            }

            if (!found) {
               servers.add("- " + myEntry + " (" + Bukkit.getOnlinePlayers().size() + ")");
               totalOnline += Bukkit.getOnlinePlayers().size();
               activeBuilds++;
               totalLaunched++;
            }

            StringBuilder sb = new StringBuilder("\ud83d\udcca Глобальная статистика\n\n");
            sb.append("Всего запущенно сборок: ").append(totalLaunched).append("\n");
            sb.append("Сейчас запущено: ").append(activeBuilds).append("\n");
            sb.append("Всего онлайн: ").append(totalOnline).append("\n\nСписок:\n");

            for (String s : servers) {
               sb.append(s).append("\n");
            }

            return sb.toString().trim();
         } catch (Exception var20) {
            return oldText;
         }
      } else {
         return "";
      }
   }

   private void editMessage(long chatId, int messageId, String text) {
      try {
         String urlString = "https://api.telegram.org/bot"
            + this.token
            + "/editMessageText?chat_id="
            + chatId
            + "&message_id="
            + messageId
            + "&text="
            + URLEncoder.encode(text, "UTF-8");
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         conn.getInputStream().close();
      } catch (Exception var8) {
      }
   }

   public void broadcast(String text) {
      for (Long chatId : this.authorizedChats) {
         this.sendMessage(chatId, text);
      }
   }
}
