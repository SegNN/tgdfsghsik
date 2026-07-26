package org.impldev.holycore.extensions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.impldev.holycore.HolyCore;
import org.impldev.holycore.database.DatabaseManager;

public class JoinExtension implements Listener {
   private final HolyCore plugin;
   private final DatabaseManager databaseManager;
   private final Set<UUID> playersWithFirstJoinReward = new HashSet<>();

   public JoinExtension(HolyCore plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.databaseManager = databaseManager;
   }

   @EventHandler
   public void join(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      this.sendAlwaysJoinMessage(player);
      if (!player.hasPermission("holycore.disable_join_message")) {
         final List<String> messages = this.plugin.getConfig().getStringList("settings.join.message");
         (new BukkitRunnable() {
            public void run() {
               if (player != null && player.isOnline()) {
                  for (String msg : messages) {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                  }
               }
            }
         }).runTaskLater(this.plugin, 100L);
      }

      this.handleFirstJoinRewards(player);
   }

   private void sendAlwaysJoinMessage(Player player) {
      List<String> alwaysMessages = this.plugin.getConfig().getStringList("alwaysjoinmessage");
      if (alwaysMessages != null && !alwaysMessages.isEmpty()) {
         for (String msg : alwaysMessages) {
            if (msg != null && !msg.trim().isEmpty()) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
         }
      }
   }

   private void handleFirstJoinRewards(Player player) {
      this.databaseManager.hasPlayerReceivedFirstJoinRewardAsync(player.getUniqueId().toString()).thenAcceptAsync(hasReward -> (new BukkitRunnable() {
         public void run() {
            if (!hasReward && !JoinExtension.this.playersWithFirstJoinReward.contains(player.getUniqueId())) {
               JoinExtension.this.executeFirstJoinCommands(player);
               JoinExtension.this.playersWithFirstJoinReward.add(player.getUniqueId());
               JoinExtension.this.databaseManager.saveFirstJoinRewardAsync(player.getUniqueId().toString(), player.getName());
            }
         }
      }).runTask(this.plugin));
   }

   private void executeFirstJoinCommands(Player player) {
      List<String> firstJoinCommands = this.plugin.getConfig().getStringList("firstjoincommands");
      if (firstJoinCommands != null && !firstJoinCommands.isEmpty()) {
         for (String command : firstJoinCommands) {
            if (command != null && !command.trim().isEmpty()) {
               String processedCommand = command.replace("%player%", player.getName());
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
         }
      }
   }
}
