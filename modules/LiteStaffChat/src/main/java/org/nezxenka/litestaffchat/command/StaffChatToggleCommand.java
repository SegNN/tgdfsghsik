package org.nezxenka.litestaffchat.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.litestaffchat.LiteStaffChat;

public class StaffChatToggleCommand implements CommandExecutor {
   private final LiteStaffChat plugin;

   public StaffChatToggleCommand(LiteStaffChat plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfigManager().getMessage("onlyplayer")));
         return true;
      } else {
         Player player = (Player)sender;
         if (this.plugin.getStaffChatEnabled().remove(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfigManager().getMessage("toggle.off")));
         } else {
            this.plugin.getStaffChatEnabled().add(player.getUniqueId());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfigManager().getMessage("toggle.on")));
         }

         return true;
      }
   }
}
