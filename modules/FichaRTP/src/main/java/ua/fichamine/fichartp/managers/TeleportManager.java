package ua.fichamine.fichartp.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichamine.fichartp.FichaRTP;
import ua.fichamine.fichartp.utils.ActionBarUtils;
import ua.fichamine.fichartp.utils.ColorUtils;
import ua.fichamine.fichartp.utils.LocationUtils;

public class TeleportManager {
   private final FichaRTP plugin;
   private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
   private final Map<UUID, Location> lastLocations = new HashMap<>();

   public TeleportManager(FichaRTP plugin) {
      this.plugin = plugin;
   }

   public void teleportDefault(Player player) {
      this.teleportWithDelay(player, "default", this.plugin.getConfigManager().getDefaultTeleportDelay());
   }

   public void teleportLong(Player player) {
      World targetWorld = Bukkit.getWorld(this.plugin.getConfigManager().getTargetWorld());
      if (targetWorld != null) {
         int minRadius = this.plugin.getConfigManager().getLongMinRadius();
         int maxRadius = this.plugin.getConfigManager().getLongMaxRadius();
         Location location = LocationUtils.getRandomLocation(targetWorld, minRadius, maxRadius);
         this.teleportPlayer(player, location);
         this.plugin.getCooldownManager().setCooldown(player, "long", this.plugin.getConfigManager().getLongCooldown());
      }
   }

   public void teleportToPlayer(Player player) {
      List<Player> validPlayers = this.getValidPlayersForTeleport(player);
      if (validPlayers.size() < this.plugin.getConfigManager().getMinPlayersOnline()) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNotEnoughPlayersMessage()));
      } else if (validPlayers.isEmpty()) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNotEnoughPlayersMessage()));
      } else {
         Player targetPlayer = validPlayers.get((int)(Math.random() * validPlayers.size()));
         Location targetLocation = targetPlayer.getLocation();
         Location safeLocation = LocationUtils.getRandomLocationAroundPoint(targetLocation, 10, 50);
         this.teleportPlayer(player, safeLocation);
         this.plugin.getCooldownManager().setCooldown(player, "player", this.plugin.getConfigManager().getPlayerCooldown());
      }
   }

   public void teleportToBase(Player player) {
      Location baseLocation = this.plugin.getRegionManager().getRandomRegionLocation(player);
      if (baseLocation == null) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNoBasesFoundMessage()));
      } else {
         this.teleportPlayer(player, baseLocation);
         this.plugin.getCooldownManager().setCooldown(player, "base", this.plugin.getConfigManager().getBaseCooldown());
      }
   }

   private void teleportWithDelay(final Player player, final String type, final int delay) {
      final UUID playerId = player.getUniqueId();
      if (this.shouldBypassDelay(player)) {
         this.performDefaultTeleport(player, type);
      } else {
         this.cancelTeleport(player);
         this.lastLocations.put(playerId, player.getLocation().clone());
         BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = delay;

            public void run() {
               if (!player.isOnline()) {
                  this.cancel();
                  TeleportManager.this.cleanup(playerId);
               } else if (this.timeLeft <= 0) {
                  TeleportManager.this.performDefaultTeleport(player, type);
                  TeleportManager.this.cleanup(playerId);
                  this.cancel();
               } else {
                  String message = TeleportManager.this.plugin.getConfigManager().getTeleportingMessage().replace("%time%", String.valueOf(this.timeLeft));
                  ActionBarUtils.sendActionBar(player, message);
                  this.timeLeft--;
               }
            }
         };
         this.teleportTasks.put(playerId, task);
         task.runTaskTimer(this.plugin, 0L, 20L);
      }
   }

   private void performDefaultTeleport(Player player, String type) {
      World targetWorld = Bukkit.getWorld(this.plugin.getConfigManager().getTargetWorld());
      if (targetWorld != null) {
         int minRadius = this.plugin.getConfigManager().getDefaultMinRadius();
         int maxRadius = this.plugin.getConfigManager().getDefaultMaxRadius();
         Location location = LocationUtils.getRandomLocation(targetWorld, minRadius, maxRadius);
         this.teleportPlayer(player, location);
         int cooldown = type.equals("default") ? this.plugin.getConfigManager().getDefaultCooldown() : this.plugin.getConfigManager().getDefaultCooldown();
         this.plugin.getCooldownManager().setCooldown(player, type, cooldown);
      }
   }

   private void teleportPlayer(Player player, Location location) {
      player.teleport(location);
      ActionBarUtils.clearActionBar(player);
      String message = this.plugin
         .getConfigManager()
         .getTeleportSuccessMessage()
         .replace("%x%", String.valueOf((int)location.getX()))
         .replace("%y%", String.valueOf((int)location.getY()))
         .replace("%z%", String.valueOf((int)location.getZ()));
      player.sendMessage(ColorUtils.colorize(message));
   }

   public void cancelTeleport(Player player) {
      UUID playerId = player.getUniqueId();
      if (this.teleportTasks.containsKey(playerId)) {
         this.teleportTasks.get(playerId).cancel();
         this.cleanup(playerId);
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getTeleportCancelledMessage()));
      }
   }

   public boolean isTeleporting(Player player) {
      return this.teleportTasks.containsKey(player.getUniqueId());
   }

   public boolean hasPlayerMoved(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.lastLocations.containsKey(playerId)) {
         return false;
      } else {
         Location lastLoc = this.lastLocations.get(playerId);
         Location currentLoc = player.getLocation();
         return lastLoc.distanceSquared(currentLoc) > 0.01;
      }
   }

   private boolean shouldBypassDelay(Player player) {
      if (player.hasPermission("rtp.bypass.delay")) {
         return true;
      } else if (this.plugin.getConfigManager().isBypassDelayInSpawn()) {
         String spawnWorld = this.plugin.getConfigManager().getSpawnWorld();
         return player.getWorld().getName().equals(spawnWorld);
      } else {
         return false;
      }
   }

   private List<Player> getValidPlayersForTeleport(Player excludePlayer) {
      List<Player> validPlayers = new ArrayList<>();
      List<String> excludedWorlds = this.plugin.getConfigManager().getPlayerExcludedWorlds();

      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         if (!onlinePlayer.equals(excludePlayer)
            && !excludedWorlds.contains(onlinePlayer.getWorld().getName())
            && (!this.plugin.getConfigManager().isIgnoreVanishedPlayers() || !onlinePlayer.hasMetadata("vanished") && onlinePlayer.canSee(excludePlayer))
            && (!this.plugin.getConfigManager().isIgnoreInvisiblePlayers() || !onlinePlayer.hasPotionEffect(PotionEffectType.INVISIBILITY))) {
            validPlayers.add(onlinePlayer);
         }
      }

      return validPlayers;
   }

   private void cleanup(UUID playerId) {
      this.teleportTasks.remove(playerId);
      this.lastLocations.remove(playerId);
   }
}
