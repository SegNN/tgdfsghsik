package org.dimasik.holyfreeze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandFreeze implements CommandExecutor, TabExecutor {
   public final HolyFreeze plugin;

   public CommandFreeze(HolyFreeze plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!command.getName().equalsIgnoreCase("freezing") && !command.getName().equalsIgnoreCase("freeze")) {
         if (command.getName().equalsIgnoreCase("afkstaff")) {
            if (sender instanceof Player) {
               Player player = (Player)sender;
               if (this.plugin.statusFreezeHashMap.get(player.getUniqueId()) != null
                  && this.plugin.statusFreezeHashMap.get(player.getUniqueId()) == StatusFreeze.AFK) {
                  this.plugin.statusFreezeHashMap.remove(player.getUniqueId());
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[Staff] Афк выключен!"));
                  player.clearTitle();
               } else {
                  this.plugin.statusFreezeHashMap.put(player.getUniqueId(), StatusFreeze.AFK);
                  this.plugin.updateStatus(player);
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[Staff] Афк включен!"));
               }
            } else {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9▶ &fКоманду может использовать только &6игрок."));
            }
         } else if (command.getName().equalsIgnoreCase("prova")) {
            if (sender instanceof Player) {
               Player player = (Player)sender;
               if (this.plugin.statusFreezeHashMap.get(player.getUniqueId()) != null
                  && this.plugin.statusFreezeHashMap.get(player.getUniqueId()) != StatusFreeze.NONE) {
                  this.plugin.statusFreezeHashMap.remove(player.getUniqueId());
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[Staff] Вы закончили проверку!"));
                  player.clearTitle();
               } else {
                  this.plugin.statusFreezeHashMap.put(player.getUniqueId(), StatusFreeze.CHECKING);
                  this.plugin.updateStatus(player);
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[Staff] Вы начали проверку!"));
               }
            } else {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&E&D&2&4&2&9▶ &fКоманду может использовать только &6игрок."));
            }
         }
      } else if (sender instanceof Player) {
         Player player = (Player)sender;
         if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&l▶ &fИспользование: &6/" + command.getName() + " [игрок]"));
            return true;
         }

         boolean teleport = true;
         if (args.length > 1 && args[1].equalsIgnoreCase("-s")) {
            teleport = false;
         }

         OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
         if (target == player) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Вы не можете заморозить себя."));
            return true;
         }

         if (HolyFreeze.freezeHashMap.containsKey(target.getUniqueId())) {
            if (Bukkit.getPlayer(HolyFreeze.freezeHashMap.get(target.getUniqueId()).checkerName) != null
               && !HolyFreeze.freezeHashMap.get(target.getUniqueId()).checkerName.equalsIgnoreCase(sender.getName())) {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Вы не замораживали данного игрока!"));
            } else {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Игрок разморожен."));
               HolyFreeze.freezeHashMap.remove(target.getUniqueId()).stop();
            }
         } else {
            if (target.getPlayer() == null) {
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Игрок не заморожен."));
               return true;
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Игрок заморожен."));
            Freeze freeze = new Freeze(this.plugin, player, target.getPlayer(), teleport);
            HolyFreeze.freezeHashMap.put(target.getUniqueId(), freeze);
         }
      } else {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&l▶ &fКоманду может использовать только &6игрок&f."));
      }

      return true;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (!command.getName().equalsIgnoreCase("freeze") && !command.getName().equalsIgnoreCase("freezing")) {
         return Collections.emptyList();
      } else {
         List<String> tabCompleter = new ArrayList<>();
         if (args.length <= 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               tabCompleter.add(player.getName());
            }
         }

         List<String> startWith = new ArrayList<>();

         for (int i = 0; i < tabCompleter.size(); i++) {
            String s = tabCompleter.get(i);
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
               startWith.add(s);
            }
         }

         return startWith;
      }
   }
}
