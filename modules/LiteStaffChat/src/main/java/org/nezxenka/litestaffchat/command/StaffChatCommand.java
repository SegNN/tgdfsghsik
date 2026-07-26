package org.nezxenka.litestaffchat.command;

import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.litestaffchat.LiteStaffChat;

public class StaffChatCommand implements CommandExecutor {
   private final LiteStaffChat plugin;

   public StaffChatCommand(LiteStaffChat plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfigManager().getMessage("onlyplayer")));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length == 0) {
            this.sendMessage(player, this.plugin.getConfigManager().getMessage("0args").replace("{команда}", label));
            return true;
         } else if (!this.plugin.isStaffChatEnabled(player)) {
            this.sendMessage(player, this.plugin.getConfigManager().getMessage("senddisabled"));
            return true;
         } else {
            String mode = "GLOBAL";
            int messageStartIndex = 0;
            if (args.length > 1 && args[0].matches("(?i)LOCAL|GLOBAL|LITE|CLASSIC|CURRENT")) {
               mode = args[0].toUpperCase();
               messageStartIndex = 1;
            }

            if (mode.equals("CURRENT")) {
               mode = this.plugin.getDatabaseConfig().getServerMode().toUpperCase();
            }

            String message = String.join(" ", Arrays.copyOfRange(args, messageStartIndex, args.length));
            if (message.isEmpty()) {
               this.sendMessage(player, this.plugin.getConfigManager().getMessage("0args").replace("{команда}", label));
               return true;
            } else {
               this.plugin.getChatService().sendStaffChat(player, message, mode);
               return true;
            }
         }
      }
   }

   private void sendMessage(Player player, String message) {
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
   }
}
