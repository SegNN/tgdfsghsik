package org.impldev.holycore.commands;

import java.util.HashMap;
import java.util.Map;
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

public class Bc implements CommandExecutor {
   private final JavaPlugin plugin;
   private final Map<UUID, Long> cooldowns = new HashMap<>();

   public Bc(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         FileConfiguration config = this.plugin.getConfig();
         if (!config.getBoolean("settings.bc.enable", true)) {
            return true;
         } else if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&0&0&A&E&F&F▶ &fДля отправки объявления: &x&0&0&A&E&F&F/bc [сообщение]"));
            return true;
         } else {
            int cooldownSeconds = config.getInt("settings.bc.cooldown", 1800);
            if (!player.hasPermission("holycore.bc.bypass") && this.cooldowns.containsKey(player.getUniqueId())) {
               long elapsed = (System.currentTimeMillis() - this.cooldowns.get(player.getUniqueId())) / 1000L;
               long remaining = cooldownSeconds - elapsed;
               if (remaining > 0L) {
                  long minutes = remaining / 60L;
                  long seconds = remaining % 60L;
                  String timeStr;
                  if (minutes > 0L) {
                     timeStr = minutes + " мин. " + seconds + " сек.";
                  } else {
                     timeStr = seconds + " сек.";
                  }

                  player.sendMessage(
                     ChatColor.translateAlternateColorCodes('&', "&x&F&F&6&7&0&0▶ &fПодождите &x&F&F&6&7&0&0" + timeStr + " &fперед следующим объявлением.")
                  );
                  return true;
               }
            }

            this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            String msg = String.join(" ", args);

            for (String template : config.getStringList("settings.bc.message")) {
               String message = template.replace("{player}", player.getName()).replace("{msg}", msg).replace("{color}", "");
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
            }

            return true;
         }
      }
   }
}
