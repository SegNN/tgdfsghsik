package org.impldev.holycore.extensions;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.impldev.holycore.utils.Particles;
import org.impldev.holycore.utils.UtilParticles;

public class PlayerExtension implements Listener {
   public PlayerExtension() {
   }

   @EventHandler
   public void on(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      Location loc = player.getLocation();
      loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.getX(), loc.getY() + 1.0, loc.getZ(), 50, 0.2, 0.7, 0.2, 0.02);
   }

   @EventHandler
   public void on(PlayerKickEvent event) {
      Player player = event.getPlayer();
      Location loc = player.getLocation();

      for (Location location : getCircle(loc.clone().add(0.0, 0.2, 0.0), 0.125, 180)) {
         Vector direction = location.toVector().subtract(loc.clone().add(0.0, 0.2, 0.0).toVector()).normalize();
         UtilParticles.display(Particles.EXPLOSION_NORMAL, direction, loc, 1.0F);
      }
   }

   public static List<Location> getCircle(Location location, double radius, int points) {
      List<Location> locations = new ArrayList<>();
      double increment = (Math.PI * 2) / points;

      for (int i = 0; i < points; i++) {
         double angle = i * increment;
         double x = location.getX() + Math.cos(angle) * radius;
         double z = location.getZ() + Math.sin(angle) * radius;
         locations.add(new Location(location.getWorld(), x, location.getY(), z));
      }

      return locations;
   }
}
