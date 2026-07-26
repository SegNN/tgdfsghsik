package org.nezxenka.litestaffchat.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nezxenka.litestaffchat.LiteStaffChat;

public class PlayerListener implements Listener {
   private final LiteStaffChat plugin;

   public PlayerListener(LiteStaffChat plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (player.hasPermission("litestaffchat.see")) {
         this.plugin.getStaffChatEnabled().add(player.getUniqueId());
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.plugin.getStaffChatEnabled().remove(event.getPlayer().getUniqueId());
   }
}
