package org.impldev.holycore.extensions;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.impldev.holycore.HolyCore;

public class SpawnExtension implements Listener {
   private final HolyCore plugin;

   public SpawnExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onSpawnInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK
         && event.getClickedBlock() != null
         && event.getClickedBlock().getWorld().getName().equalsIgnoreCase("spawn")) {
         Material material = event.getClickedBlock().getType();
         if (material == Material.ENCHANTING_TABLE) {
            event.getPlayer().openEnchanting(event.getClickedBlock().getLocation(), true);
         } else if (material == Material.ANVIL) {
            event.getPlayer().openWorkbench(event.getClickedBlock().getLocation(), true);
         } else if (material == Material.SMITHING_TABLE) {
            event.getPlayer().openWorkbench(event.getClickedBlock().getLocation(), true);
         }
      }
   }
}
