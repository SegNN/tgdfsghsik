package org.impldev.holycore.extensions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.impldev.holycore.HolyCore;
import org.jetbrains.annotations.NotNull;

public class AfkExtension implements Listener, CommandExecutor {
   private final HolyCore plugin;
   private static final String AFK_META_KEY = "isAFK";

   public AfkExtension(HolyCore plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Ле ну ты ухуел уже");
         return true;
      } else {
         Player player = (Player)sender;
         if (player.getWorld() == Bukkit.getWorld("spawn")) {
            if (this.isAFK(player)) {
               this.stopAFK(player);
            } else {
               this.startAFK(player);
            }
         } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4▶ &fАФК возможно активировать только находясь на спавне."));
         }

         return true;
      }
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld() == Bukkit.getWorld("spawn")
         && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
         && this.isAFK(player)) {
         this.stopAFK(player);
      }
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      if (player.getWorld() == Bukkit.getWorld("spawn") && this.isAFK(player)) {
         this.stopAFK(player);
      }
   }

   private void startAFK(Player player) {
      player.setMetadata("isAFK", new FixedMetadataValue(this.plugin, true));
      player.setCollidable(false);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4▶ &fАФК включен. Коллизия отключена. Пошевелитесь, чтобы выйти из AFK."));
      player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&c&lAFK"), ChatColor.translateAlternateColorCodes('&', ""), 0, 100000000, 1);
   }

   private void stopAFK(Player player) {
      player.setMetadata("isAFK", new FixedMetadataValue(this.plugin, false));
      player.setCollidable(true);
      player.sendTitle(ChatColor.translateAlternateColorCodes('&', " "), ChatColor.translateAlternateColorCodes('&', " "), 0, 1, 0);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b▶ &fАФК выключен."));
   }

   private boolean isAFK(Player player) {
      for (MetadataValue value : player.getMetadata("isAFK")) {
         if (value.getOwningPlugin() == this.plugin) {
            return value.asBoolean();
         }
      }

      return false;
   }
}
