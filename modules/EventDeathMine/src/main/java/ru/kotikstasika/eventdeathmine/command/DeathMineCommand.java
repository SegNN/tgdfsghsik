package ru.kotikstasika.eventdeathmine.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.kotikstasika.eventdeathmine.manager.DeathMineManager;
import ru.kotikstasika.eventdeathmine.model.DeathMineState;
import ru.kotikstasika.eventdeathmine.util.ColorUtil;

public class DeathMineCommand implements CommandExecutor, TabCompleter {
   private final DeathMineManager manager;

   public DeathMineCommand(DeathMineManager manager) {
      this.manager = manager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(ColorUtil.colorize(this.prefix("commands")));
         this.sendUsage(sender);
         return true;
      } else {
         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "spawn":
               if (sender instanceof Player && !sender.hasPermission("deathmine.spawn")) {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.no_permission")));
               } else {
                  this.manager.spawnMine(sender);
               }
               break;
            case "remove":
               if (sender instanceof Player && !sender.hasPermission("deathmine.remove")) {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.no_permission")));
               } else {
                  this.manager.removeMine(sender);
               }
               break;
            case "settimer":
               if (sender instanceof Player && !sender.hasPermission("deathmine.settimer")) {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.no_permission")));
               } else {
                  if (this.manager.getState() == DeathMineState.NOT_SPAWNED) {
                     sender.sendMessage(ColorUtil.colorize(this.cfg("messages.not_started")));
                     return true;
                  }

                  if (args.length < 3) {
                     sender.sendMessage(ChatColor.RED + "Использование: /deathmine settimer <минуты> <секунды>");
                     return true;
                  }

                  try {
                     int minutes = Integer.parseInt(args[1]);
                     int seconds = Integer.parseInt(args[2]);
                     if (minutes > 20) {
                        sender.sendMessage(ColorUtil.colorize(this.cfg("messages.timer_minutes_max")));
                        return true;
                     }

                     if (seconds > 60) {
                        sender.sendMessage(ColorUtil.colorize(this.cfg("messages.timer_seconds_max")));
                        return true;
                     }

                     if (minutes == 20) {
                        seconds = 0;
                     }

                     this.manager.setTimerSeconds(minutes * 60 + seconds, sender);
                  } catch (NumberFormatException var9) {
                     sender.sendMessage(ChatColor.RED + "Некорректное время!");
                  }
               }
               break;
            case "tp":
               if (sender instanceof Player) {
                  this.manager.teleportToMine((Player)sender);
               } else {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.only_players")));
               }
               break;
            case "status":
               DeathMineState st = this.manager.getState();
               if (st == DeathMineState.NOT_SPAWNED) {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.status_stopped")));
               } else if (st == DeathMineState.SPAWNED) {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.status_running")));
               } else {
                  sender.sendMessage(ColorUtil.colorize(this.cfg("messages.status_unknown")));
               }
               break;
            default:
               this.sendUsage(sender);
         }

         return true;
      }
   }

   private void sendUsage(CommandSender sender) {
      sender.sendMessage(ColorUtil.colorize("&f &6&l▶ &fКоманды EventDeathMine:"));
      sender.sendMessage(ColorUtil.colorize("&f  &6/deathmine spawn &f- создать шахту"));
      sender.sendMessage(ColorUtil.colorize("&f  &6/deathmine remove &f- удалить шахту"));
      sender.sendMessage(ColorUtil.colorize("&f  &6/deathmine settimer <минуты> <секунды> &f- установить таймер"));
      sender.sendMessage(ColorUtil.colorize("&f  &6/deathmine tp &f- телепорт к шахте"));
      sender.sendMessage(ColorUtil.colorize("&f  &6/deathmine status &f- статус события"));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("eventdeathmine.admin")) {
         return new ArrayList<>();
      } else if (args.length == 1) {
         List<String> completions = Arrays.asList("spawn", "remove", "settimer", "tp", "status");
         String prefix = args[0] == null ? "" : args[0].toLowerCase();
         return completions.stream().filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String secondSuggestions = args[0].toLowerCase();
            switch (secondSuggestions) {
               case "spawn":
               case "remove":
               case "tp":
               case "status":
                  return Collections.emptyList();
               case "settimer":
                  List<String> minuteSuggestions = Arrays.asList("0", "1", "2", "3", "5", "10", "15", "20");
                  String mPrefix = args[1] == null ? "" : args[1].toLowerCase();
                  return minuteSuggestions.stream().filter(s -> s.toLowerCase().startsWith(mPrefix)).collect(Collectors.toList());
            }
         }

         if (args.length == 3 && "settimer".equalsIgnoreCase(args[0])) {
            List<String> secondSuggestions = Arrays.asList("0", "5", "10", "15", "20", "30", "45", "60");
            String sPrefix = args[2] == null ? "" : args[2].toLowerCase();
            return secondSuggestions.stream().filter(s -> s.toLowerCase().startsWith(sPrefix)).collect(Collectors.toList());
         } else {
            return new ArrayList<>();
         }
      }
   }

   private String cfg(String path) {
      return this.manager.getPlugin().getConfig().getString(path, "");
   }

   private String prefix(String kind) {
      return this.cfg("messages.prefix." + kind);
   }
}
