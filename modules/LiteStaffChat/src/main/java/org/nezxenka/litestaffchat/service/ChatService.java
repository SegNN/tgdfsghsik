package org.nezxenka.litestaffchat.service;

import java.util.Collection;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.nezxenka.litestaffchat.LiteStaffChat;
import org.nezxenka.litestaffchat.config.ConfigManager;

public class ChatService {
   private final LiteStaffChat plugin;

   public ChatService(LiteStaffChat plugin) {
      this.plugin = plugin;
   }

   public void sendStaffChat(Player player, String message, String mode) {
      String formattedMessage = this.formatMessage(player, message, ConfigManager.ChatMode.valueOf(mode));
      if (this.plugin.getRedisManager() != null) {
         String fullMessage = this.plugin.getDatabaseConfig().getServerName() + "||" + mode + "||" + formattedMessage;
         this.plugin.getRedisManager().publish("StaffChat", fullMessage);
      } else {
         this.broadcastMessage(this.plugin.getDatabaseConfig().getServerName(), mode, formattedMessage);
      }
   }

   public void broadcastMessage(String targetServer, String targetMode, String message) {
      Collection<? extends Player> players = Bukkit.getOnlinePlayers();
      String serverName = this.plugin.getDatabaseConfig().getServerName();
      String serverMode = this.plugin.getDatabaseConfig().getServerMode();

      for (Player player : players) {
         if (player.hasPermission("litestaffchat.see")
            && this.plugin.isStaffChatEnabled(player)
            && this.shouldReceive(targetServer, targetMode, serverName, serverMode)) {
            player.sendMessage(message);
         }
      }
   }

   private boolean shouldReceive(String targetServer, String targetMode, String serverName, String serverMode) {
      switch (targetMode) {
         case "LOCAL":
            return targetServer.equals(serverName);
         case "LITE":
            return serverMode.equals("lite");
         case "CLASSIC":
            return serverMode.equals("classic");
         default:
            return true;
      }
   }

   private String formatMessage(Player player, String message, ConfigManager.ChatMode mode) {
      String format = this.plugin.getConfigManager().getChatFormat(mode);
      String formatted = format.replace("{сервер}", this.plugin.getDatabaseConfig().getServerName()).replace("{сообщение}", message);
      if (this.plugin.isPlaceholderAPIAvailable()) {
         formatted = PlaceholderAPI.setPlaceholders(player, formatted);
      }

      return ChatColor.translateAlternateColorCodes('&', formatted);
   }
}
