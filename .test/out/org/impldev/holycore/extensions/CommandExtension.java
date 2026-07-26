package org.impldev.holycore.extensions;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandExtension implements Listener {
   public CommandExtension() {
   }

   @EventHandler
   public void on(PlayerCommandPreprocessEvent event) {
      if (!event.isCancelled()) {
         Player sender = event.getPlayer();
         String message = event.getMessage();
         if (message.toLowerCase().startsWith("/tpa ") || message.toLowerCase().startsWith("/call ")) {
            String[] args = message.split(" ");
            if (args.length >= 2) {
               String targetName = args[1];
               Player target = Bukkit.getPlayer(targetName);
               if (target != null) {
                  boolean isInPvPMode = this.checkPvPMode(target);
                  if (isInPvPMode) {
                     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&0&0&0&0▶ &fИгрок находится в &x&F&F&0&0&0&0режиме PvP!"));
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   private boolean checkPvPMode(Player player) {
      return PlaceholderAPI.setPlaceholders(player, "%antirelog_isinpvp%").equalsIgnoreCase("yes");
   }
}
