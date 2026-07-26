package ua.holymine.holyrtp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ua.holymine.holyrtp.HolyRTP;
import ua.holymine.holyrtp.utils.ActionBarUtils;

public class PlayerMoveListener implements Listener {
   private final HolyRTP plugin;

   public PlayerMoveListener(HolyRTP plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (this.plugin.getTeleportManager().isTeleporting(player)) {
         if (this.plugin.getTeleportManager().hasPlayerMoved(player)) {
            this.plugin.getTeleportManager().cancelTeleport(player);
            ActionBarUtils.clearActionBar(player);
         }
      }
   }
}
