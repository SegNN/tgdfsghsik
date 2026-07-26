package org.impldev.holycore.extensions;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.impldev.holycore.HolyCore;

public class CraftingExtension implements Listener {
   private final HolyCore plugin;

   public CraftingExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onShield(CraftItemEvent event) {
      if (event.getInventory().getResult() != null && event.getInventory().getResult().getType() == Material.SHIELD) {
         event.setCancelled(true);
      }
   }
}
