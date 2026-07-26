package org.dimasik.shame.modules.impl;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.dimasik.shame.modules.Module;

public class DamageModule extends Module {
   public static final HashMap<Player, Double> editedDamages = new HashMap<>();

   public DamageModule() {
      super.registerListener();
   }

   @EventHandler
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && editedDamages.get((Player)event.getEntity()) != null) {
         event.setDamage(event.getDamage() * editedDamages.get((Player)event.getEntity()));
      }
   }
}
