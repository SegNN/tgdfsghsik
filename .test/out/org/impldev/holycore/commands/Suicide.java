package org.impldev.holycore.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Suicide implements CommandExecutor {
   private final JavaPlugin plugin;
   private final HashMap<UUID, Long> pendingSuicide = new HashMap<>();

   public Suicide(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         FileConfiguration config = this.plugin.getConfig();
         if (!config.getBoolean("settings.suicide.enable", true)) {
            player.sendMessage("Команда /suicide отключена.");
            return true;
         } else {
            int cooldown = config.getInt("settings.suicide.cooldown", 10);
            UUID uuid = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (this.pendingSuicide.containsKey(uuid)) {
               long previousTime = this.pendingSuicide.get(uuid);
               if (currentTime - previousTime <= cooldown * 1000L) {
                  player.setHealth(0.0);

                  for (String msg : config.getStringList("settings.suicide.message-died")) {
                     Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%player_suicide%", player.getName())));
                  }

                  this.pendingSuicide.remove(uuid);
                  return true;
               }

               this.pendingSuicide.remove(uuid);
            }

            this.pendingSuicide.put(uuid, currentTime);
            List<String> warningMessages = config.getStringList("settings.suicide.message");
            if (warningMessages.isEmpty()) {
               player.sendMessage("Вы уверены, что хотите это сделать? Повторите команду для подтверждения.");
            } else {
               for (String msg : warningMessages) {
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
               }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.pendingSuicide.remove(uuid), 20L * cooldown);
            return true;
         }
      }
   }
}
