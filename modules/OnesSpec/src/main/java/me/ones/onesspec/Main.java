package me.ones.onesspec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Listener {
   private Connection connection;
   private HashMap<UUID, Location> originalLocations = new HashMap<>();
   private HashMap<UUID, GameMode> originalGameModes = new HashMap<>();

   public Main() {
   }

   private void sendColorMessage(@NotNull CommandSender player, String message) {
      if (message != null) {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
      }
   }

   public void onEnable() {
      if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         this.getLogger().severe("PlaceholderAPI не найден! Плагин будет отключен.");
         Bukkit.getPluginManager().disablePlugin(this);
      } else if (!Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
         this.getLogger().severe("Essentials не найден! Плагин будет отключен.");
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         this.saveDefaultConfig();
         this.setupDatabase();
         this.getServer().getPluginManager().registerEvents(this, this);
         Objects.requireNonNull(this.getCommand("spec")).setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("onesspec.use")) {
               this.sendColorMessage(sender, this.getConfig().getString("messages.no-permission"));
               return true;
            } else if (!(sender instanceof Player)) {
               this.sendColorMessage(sender, this.getConfig().getString("messages.only-players"));
               return true;
            } else if (args.length != 1) {
               this.sendColorMessage(sender, this.getConfig().getString("messages.usage"));
               return true;
            } else {
               Player moderator = (Player)sender;
               Player target = Bukkit.getPlayer(args[0]);
               if (target == null) {
                  this.sendColorMessage(moderator, this.getConfig().getString("messages.player-not-found"));
                  return true;
               } else if (moderator.equals(target)) {
                  this.sendColorMessage(moderator, this.getConfig().getString("messages.cant-spec-yourself"));
                  return true;
               } else {
                  this.startSpectating(moderator, target);
                  return true;
               }
            }
         });
         Objects.requireNonNull(this.getCommand("unspec")).setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
               return true;
            } else {
               Player player = (Player)sender;
               if (this.originalLocations.containsKey(player.getUniqueId())) {
                  this.stopSpectating(player);
                  this.sendColorMessage(player, this.getConfig().getString("messages.spec-stopped"));
               }

               return true;
            }
         });
      }
   }

   public void onDisable() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   private void setupDatabase() {
      CompletableFuture.runAsync(
         () -> {
            try {
               Class.forName("com.mysql.jdbc.Driver");
               this.connection = DriverManager.getConnection(
                  "jdbc:mysql://"
                     + this.getConfig().getString("database.host")
                     + ":"
                     + this.getConfig().getInt("database.port")
                     + "/"
                     + this.getConfig().getString("database.name"),
                  this.getConfig().getString("database.user"),
                  this.getConfig().getString("database.password")
               );
               Statement statement = this.connection.createStatement();
               statement.executeUpdate(
                  "CREATE TABLE IF NOT EXISTS lite_spec (id INT AUTO_INCREMENT PRIMARY KEY,moderator_uuid VARCHAR(36) NOT NULL,moderator_name VARCHAR(16) NOT NULL,target_uuid VARCHAR(36) NOT NULL,target_name VARCHAR(16) NOT NULL,start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,end_time TIMESTAMP NULL,start_location TEXT NOT NULL,end_location TEXT NULL)"
               );
            } catch (Exception var2) {
               var2.printStackTrace();
            }
         }
      );
   }

   private void startSpectating(Player moderator, Player target) {
      this.originalLocations.put(moderator.getUniqueId(), moderator.getLocation());
      this.originalGameModes.put(moderator.getUniqueId(), moderator.getGameMode());
      moderator.setGameMode(GameMode.SPECTATOR);
      moderator.teleport(target);
      if (!this.isVanished(moderator)) {
         this.setVanish(moderator, true);
      }

      String startMessage = this.getConfig().getString("messages.spec-start").replace("%target%", target.getName());
      this.sendColorMessage(moderator, startMessage);
      CompletableFuture.runAsync(
         () -> {
            try {
               PreparedStatement ps = this.connection
                  .prepareStatement("INSERT INTO lite_spec (moderator_uuid, moderator_name, target_uuid, target_name, start_location) VALUES (?, ?, ?, ?, ?)");
               ps.setString(1, moderator.getUniqueId().toString());
               ps.setString(2, moderator.getName());
               ps.setString(3, target.getUniqueId().toString());
               ps.setString(4, target.getName());
               ps.setString(5, this.locationToString(moderator.getLocation()));
               ps.executeUpdate();
            } catch (SQLException var4) {
               var4.printStackTrace();
            }
         }
      );
   }

   private void stopSpectating(Player moderator) {
      Location originalLocation = this.originalLocations.get(moderator.getUniqueId());
      GameMode originalGameMode = this.originalGameModes.get(moderator.getUniqueId());
      if (originalLocation != null) {
         moderator.teleport(originalLocation);
         this.originalLocations.remove(moderator.getUniqueId());
      }

      if (originalGameMode != null) {
         moderator.setGameMode(originalGameMode);
         this.originalGameModes.remove(moderator.getUniqueId());
      }

      this.setVanish(moderator, false);
      CompletableFuture.runAsync(
         () -> {
            try {
               PreparedStatement ps = this.connection
                  .prepareStatement(
                     "UPDATE lite_spec SET end_time = CURRENT_TIMESTAMP, end_location = ? WHERE moderator_uuid = ? AND end_time IS NULL ORDER BY start_time DESC LIMIT 1"
                  );
               ps.setString(1, this.locationToString(moderator.getLocation()));
               ps.setString(2, moderator.getUniqueId().toString());
               ps.executeUpdate();
            } catch (SQLException var3x) {
               var3x.printStackTrace();
            }
         }
      );
   }

   private boolean isVanished(Player player) {
      if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         String vanished = PlaceholderAPI.setPlaceholders(player, "%essentials_vanished%");
         return vanished.equalsIgnoreCase("yes");
      } else {
         return false;
      }
   }

   private void setVanish(Player player, boolean enable) {
      if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
         String command = "vanish " + (enable ? "on" : "off") + " " + player.getName();
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
      }
   }

   private String locationToString(Location location) {
      return location.getWorld().getName()
         + ","
         + location.getX()
         + ","
         + location.getY()
         + ","
         + location.getZ()
         + ","
         + location.getYaw()
         + ","
         + location.getPitch();
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (this.originalLocations.containsKey(player.getUniqueId())) {
         Player target = Bukkit.getPlayer(
            String.valueOf(event.getTo().getWorld().getPlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null))
         );
      }
   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (this.originalLocations.containsKey(player.getUniqueId())) {
         String command = event.getMessage().split(" ")[0].toLowerCase();

         for (String blocked : this.getConfig().getStringList("blocked-commands")) {
            if (command.equalsIgnoreCase(blocked)) {
               this.sendColorMessage(player, this.getConfig().getString("messages.command-blocked"));
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (this.originalLocations.containsKey(player.getUniqueId())) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).getType().toString().contains("CHEST")) {
            this.sendColorMessage(player, this.getConfig().getString("messages.chest-blocked"));
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (this.originalLocations.containsKey(player.getUniqueId())) {
         this.stopSpectating(player);
      }
   }

   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      if (this.originalLocations.containsKey(player.getUniqueId())) {
         CompletableFuture.runAsync(
            () -> {
               try {
                  PreparedStatement ps = this.connection
                     .prepareStatement(
                        "INSERT INTO lite_spec_chat (spec_id, message) VALUES ((SELECT id FROM lite_spec WHERE moderator_uuid = ? ORDER BY start_time DESC LIMIT 1), ?)"
                     );
                  ps.setString(1, player.getUniqueId().toString());
                  ps.setString(2, event.getMessage());
                  ps.executeUpdate();
               } catch (SQLException var4) {
                  var4.printStackTrace();
               }
            }
         );
      }
   }
}
