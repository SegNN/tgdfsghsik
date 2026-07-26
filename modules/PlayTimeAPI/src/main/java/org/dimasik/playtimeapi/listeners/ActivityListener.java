package org.dimasik.playtimeapi.listeners;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.dimasik.playtimeapi.Main;

public class ActivityListener implements Listener {
   private final Main plugin;

   public ActivityListener(Main plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      this.updateActivity(event.getWhoClicked().getUniqueId());
   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerChat(PlayerChatEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
      this.updateActivity(event.getPlayer().getUniqueId());
   }

   private void updateActivity(UUID uuid) {
      this.plugin.lastActivity.put(uuid, System.currentTimeMillis());
   }
}
