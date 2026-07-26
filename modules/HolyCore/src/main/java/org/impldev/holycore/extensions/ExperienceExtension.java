package org.impldev.holycore.extensions;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.impldev.holycore.HolyCore;

public class ExperienceExtension implements Listener {
   private final HolyCore plugin;

   public ExperienceExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      event.setKeepLevel(true);
      event.setDroppedExp(0);
   }

   @EventHandler
   public void onExp(PlayerPickupExperienceEvent event) {
      Player player = event.getPlayer();
      if (player.hasPermission("experience.infinity")) {
         event.getExperienceOrb().setExperience(event.getExperienceOrb().getExperience() * 2);
      } else if (player.hasPermission("experience.eternity")) {
         event.getExperienceOrb().setExperience(event.getExperienceOrb().getExperience() * 2);
      } else if (player.hasPermission("experience.stinger")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.8));
      } else if (player.hasPermission("experience.dragon")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.6));
      } else if (player.hasPermission("experience.kraken")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.4));
      } else if (player.hasPermission("experience.wither")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.2));
      } else if (player.hasPermission("experience.ghast")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.2));
      } else if (player.hasPermission("experience.mustang")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.1));
      } else if (player.hasPermission("experience.griefer")) {
         event.getExperienceOrb().setExperience((int)(event.getExperienceOrb().getExperience() * 1.1));
      }
   }
}
