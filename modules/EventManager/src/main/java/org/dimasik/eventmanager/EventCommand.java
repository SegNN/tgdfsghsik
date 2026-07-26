package org.dimasik.eventmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventCommand implements CommandExecutor {
   public final EventManager plugin;

   public EventCommand(EventManager plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if ((!sender.hasPermission("eventmanager.admin") || args.length == 0) && sender instanceof Player) {
         this.plugin.sendEventMessage((Player)sender);
      } else if (args.length == 0) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&B&C&3&B▶ &fИспользование: &x&F&2&9&F&1&5/event settimer [новое время]"));
      } else if (args.length < 2) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&B&C&3&B▶ &fИспользование: &x&F&2&9&F&1&5/event settimer [новое время]"));
      } else {
         try {
            int time = Integer.parseInt(args[1]);
            this.plugin.setTimer(time);
            this.plugin.getConfig().set("timer", time);
            this.plugin.saveConfig();
            sender.sendMessage(
               ChatColor.translateAlternateColorCodes('&', "&x&F&F&B&C&3&B▶ &fВремя до следующего голосования задано на &x&F&2&9&F&1&5" + time + " сек.")
            );
         } catch (NumberFormatException var6) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&B&C&3&B▶ &fИспользование: &x&F&2&9&F&1&5/event settimer [новое время]"));
         }
      }

      return true;
   }
}
