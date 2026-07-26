package ua.fichamine.fichartp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ua.fichamine.fichartp.FichaRTP;
import ua.fichamine.fichartp.utils.ActionBarUtils;

public class PlayerMoveListener implements Listener {
   private final FichaRTP plugin;

   public PlayerMoveListener(FichaRTP plugin) {
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
