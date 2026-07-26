package ua.holydev.holyeventsun.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ua.holydev.holyeventsun.HolyEventSun;
import ua.holydev.holyeventsun.model.Phase;
import ua.holydev.holyeventsun.util.ColorUtils;

public class PlayerMoveListener implements Listener {
   private final HolyEventSun plugin;
   private final Set<UUID> insideZone = new HashSet<>();

   public PlayerMoveListener(HolyEventSun plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getTo() != null) {
         if (event.getFrom().getBlockX() != event.getTo().getBlockX()
            || event.getFrom().getBlockY() != event.getTo().getBlockY()
            || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            this.checkZone(event.getPlayer(), event.getTo());
         }
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (event.getTo() != null) {
         this.checkZone(event.getPlayer(), event.getTo());
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.checkZone(player, player.getLocation()), 5L);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.insideZone.remove(event.getPlayer().getUniqueId());
   }

   private void checkZone(Player player, Location to) {
      Location coreLoc = this.plugin.getCfg().getCoreLocation();
      if (coreLoc != null && coreLoc.getWorld() != null) {
         if (to.getWorld() != null && to.getWorld().equals(coreLoc.getWorld())) {
            int radius = this.plugin.getCfg().getRadius();
            int radiusSq = radius * radius;
            boolean isInside = to.distanceSquared(coreLoc) <= radiusSq;
            UUID uuid = player.getUniqueId();
            if (isInside && !this.insideZone.contains(uuid)) {
               this.insideZone.add(uuid);
               Phase phase = this.plugin.getPhaseManager().getCurrentPhase();
               String message = this.plugin.getCfg().getEnterMessage().replace("%radius%", String.valueOf(radius)).replace("%reward%", phase.getRewardName());
               player.sendMessage(ColorUtils.colorize(message));
            } else if (!isInside && this.insideZone.contains(uuid)) {
               this.insideZone.remove(uuid);
            }
         } else {
            this.insideZone.remove(player.getUniqueId());
         }
      }
   }
}
