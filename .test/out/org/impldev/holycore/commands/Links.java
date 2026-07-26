package org.impldev.holycore.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.impldev.holycore.HolyCore;
import org.jetbrains.annotations.NotNull;

public class Links implements CommandExecutor {
   private final HolyCore plugin;

   public Links(HolyCore plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         List<String> messages = this.plugin.getConfig().getStringList("settings.links.message");
         if (messages.isEmpty()) {
            return true;
         } else {
            for (String msg : messages) {
               player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }

            return true;
         }
      }
   }
}
