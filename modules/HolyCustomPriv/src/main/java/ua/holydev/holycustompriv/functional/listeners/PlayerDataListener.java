package ua.holydev.holycustompriv.functional.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.holydev.holycustompriv.HolyCustomPriv;

public class PlayerDataListener implements Listener {
   private HolyCustomPriv plugin;

   public PlayerDataListener(HolyCustomPriv plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.plugin.getTokenManager().loadPlayerTokens(player);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.plugin.getTokenManager().savePlayerTokens(player);
      this.plugin.getTokenManager().removePlayerData(player);
   }
}
