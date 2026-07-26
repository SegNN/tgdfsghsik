package ua.fichamine.fichartp.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.fichamine.fichartp.FichaRTP;
import ua.fichamine.fichartp.utils.ColorUtils;

public class RtpCommand implements CommandExecutor, TabCompleter {
   private final FichaRTP plugin;
   private final List<String> subCommands = Arrays.asList("default", "long", "player", "base");

   public RtpCommand(FichaRTP plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("это консоль");
         return true;
      } else {
         Player player = (Player)sender;
         if (!player.hasPermission("rtp.use")) {
            player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNoPermissionMessage()));
            return true;
         } else if (!this.isWorldAllowed(player)) {
            player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getWorldNotAllowedMessage()));
            return true;
         } else {
            String type = "default";
            if (args.length > 0) {
               String arg = args[0].toLowerCase();
               if (this.subCommands.contains(arg)) {
                  type = arg;
               }
            }

            return this.executeRtp(player, type);
         }
      }
   }

   private boolean executeRtp(Player player, String type) {
      switch (type) {
         case "default":
            return this.executeDefaultRtp(player);
         case "long":
            return this.executeLongRtp(player);
         case "player":
            return this.executePlayerRtp(player);
         case "base":
            return this.executeBaseRtp(player);
         default:
            return this.executeDefaultRtp(player);
      }
   }

   private boolean executeDefaultRtp(Player player) {
      if (!this.plugin.getConfigManager().isDefaultEnabled()) {
         player.sendMessage(ColorUtils.colorize("&cDefault RTP is disabled."));
         return true;
      } else if (!player.hasPermission("rtp.bypass.cooldown") && this.plugin.getCooldownManager().hasCooldown(player, "default")) {
         long remaining = this.plugin.getCooldownManager().getRemainingCooldown(player, "default");
         String message = this.plugin.getConfigManager().getCooldownDefaultMessage().replace("%time%", String.valueOf(remaining));
         player.sendMessage(ColorUtils.colorize(message));
         return true;
      } else {
         this.plugin.getTeleportManager().teleportDefault(player);
         return true;
      }
   }

   private boolean executeLongRtp(Player player) {
      if (!this.plugin.getConfigManager().isLongEnabled()) {
         player.sendMessage(ColorUtils.colorize("&cLong RTP is disabled."));
         return true;
      } else if (!player.hasPermission("rtp.long")) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNoPermissionMessage()));
         return true;
      } else if (!player.hasPermission("rtp.bypass.cooldown") && this.plugin.getCooldownManager().hasCooldown(player, "long")) {
         long remaining = this.plugin.getCooldownManager().getRemainingCooldown(player, "long");
         String message = this.plugin.getConfigManager().getCooldownCustomMessage().replace("%time%", String.valueOf(remaining));
         player.sendMessage(ColorUtils.colorize(message));
         return true;
      } else {
         this.plugin.getTeleportManager().teleportLong(player);
         return true;
      }
   }

   private boolean executePlayerRtp(Player player) {
      if (!this.plugin.getConfigManager().isPlayerEnabled()) {
         player.sendMessage(ColorUtils.colorize("&cPlayer RTP is disabled."));
         return true;
      } else if (!player.hasPermission("rtp.player")) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNoPermissionMessage()));
         return true;
      } else if (!player.hasPermission("rtp.bypass.cooldown") && this.plugin.getCooldownManager().hasCooldown(player, "player")) {
         long remaining = this.plugin.getCooldownManager().getRemainingCooldown(player, "player");
         String message = this.plugin.getConfigManager().getCooldownCustomMessage().replace("%time%", String.valueOf(remaining));
         player.sendMessage(ColorUtils.colorize(message));
         return true;
      } else {
         this.plugin.getTeleportManager().teleportToPlayer(player);
         return true;
      }
   }

   private boolean executeBaseRtp(Player player) {
      if (!this.plugin.getConfigManager().isBaseEnabled()) {
         player.sendMessage(ColorUtils.colorize("&cBase RTP is disabled."));
         return true;
      } else if (!player.hasPermission("rtp.base")) {
         player.sendMessage(ColorUtils.colorize(this.plugin.getConfigManager().getNoPermissionMessage()));
         return true;
      } else if (!player.hasPermission("rtp.bypass.cooldown") && this.plugin.getCooldownManager().hasCooldown(player, "base")) {
         long remaining = this.plugin.getCooldownManager().getRemainingCooldown(player, "base");
         String message = this.plugin.getConfigManager().getCooldownCustomMessage().replace("%time%", String.valueOf(remaining));
         player.sendMessage(ColorUtils.colorize(message));
         return true;
      } else {
         this.plugin.getTeleportManager().teleportToBase(player);
         return true;
      }
   }

   private boolean isWorldAllowed(Player player) {
      List<String> allowedWorlds = this.plugin.getConfigManager().getAllowedWorlds();
      return allowedWorlds.contains(player.getWorld().getName());
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!(sender instanceof Player)) {
         return new ArrayList<>();
      } else {
         Player player = (Player)sender;
         List<String> completions = new ArrayList<>();
         if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("default".startsWith(partial)) {
               completions.add("default");
            }

            if ("long".startsWith(partial) && player.hasPermission("rtp.long")) {
               completions.add("long");
            }

            if ("player".startsWith(partial) && player.hasPermission("rtp.player")) {
               completions.add("player");
            }

            if ("base".startsWith(partial) && player.hasPermission("rtp.base")) {
               completions.add("base");
            }
         }

         return completions;
      }
   }
}
