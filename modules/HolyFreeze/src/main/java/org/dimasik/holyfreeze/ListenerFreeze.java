package org.dimasik.holyfreeze;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;

public class ListenerFreeze implements Listener {
   public HolyFreeze plugin;

   public ListenerFreeze(HolyFreeze plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void on(PlayerMoveEvent event) {
      if ((event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
         && HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on1(PlayerMoveEvent event) {
      if ((event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
         && this.plugin.statusFreezeHashMap.containsKey(event.getPlayer().getUniqueId())
         && this.plugin.statusFreezeHashMap.get(event.getPlayer().getUniqueId()) == StatusFreeze.AFK) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && HolyFreeze.freezeHashMap.containsKey(event.getEntity().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerInteractEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(InventoryClickEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getWhoClicked().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerTeleportEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerDropItemEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerAttemptPickupItemEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerCommandPreprocessEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())
         && !event.getMessage().startsWith("/m ")
         && !event.getMessage().startsWith("/msg ")
         && !event.getMessage().startsWith("/r ")
         && !event.getMessage().startsWith("/reply ")
         && !event.getMessage().startsWith("/message ")) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerJumpEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player && HolyFreeze.freezeHashMap.containsKey(event.getDamager().getUniqueId())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void on(PlayerPreLoginEvent event) {
      try {
         String server = HolyFreeze.database.getServerByPlayer(event.getName()).get();
         if (server != null && !server.equalsIgnoreCase(HolyFreeze.serverName)) {
            event.disallow(Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', HolyFreeze.kickMsg.replace("%server%", server)));
         }
      } catch (Exception var3) {
      }
   }

   @EventHandler
   public void on(PlayerJoinEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         HolyFreeze.freezeHashMap.get(event.getPlayer().getUniqueId()).updatePlayer();
      }
   }

   @EventHandler
   public void on(PlayerKickEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         HolyFreeze.freezeHashMap.get(event.getPlayer().getUniqueId()).stopKick(false);
      }
   }

   @EventHandler
   public void on(PlayerQuitEvent event) {
      if (HolyFreeze.freezeHashMap.containsKey(event.getPlayer().getUniqueId())) {
         HolyFreeze.freezeHashMap.get(event.getPlayer().getUniqueId()).stopKick(true);
      }
   }
}
