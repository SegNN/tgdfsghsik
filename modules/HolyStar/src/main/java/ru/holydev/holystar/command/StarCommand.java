package ru.holydev.holystar.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.holydev.holystar.star.StarManager;
import ru.holydev.holystar.utils.HexUtil;

public class StarCommand implements CommandExecutor, TabCompleter {
   private final StarManager starManager;
   private static final List<String> SUBCOMMANDS = List.of("get", "add", "remove");

   public StarCommand(StarManager starManager) {
      this.starManager = starManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(HexUtil.translate("&#FF5555Использование: /star <get|add|remove> <игрок> [количество]"));
         return true;
      } else {
         String action = args[0].toLowerCase();
         if (!sender.hasPermission("holystar.admin")) {
            sender.sendMessage(HexUtil.translate("&#FF5555Нет прав!"));
            return true;
         } else if (action.equals("get")) {
            if (args.length < 2) {
               sender.sendMessage(HexUtil.translate("&#FF5555Использование: /star get <игрок>"));
               return true;
            } else {
               String targetName = args[1];
               OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
               int stars = this.starManager.get(target.getName());
               sender.sendMessage(HexUtil.translate("&#55FF55У " + targetName + " - " + stars + " звёзд"));
               return true;
            }
         } else if (!action.equals("add") && !action.equals("remove")) {
            sender.sendMessage(HexUtil.translate("&#FF5555Неизвестная команда. Используйте: /star <get|add|remove>"));
            return true;
         } else if (args.length < 3) {
            sender.sendMessage(HexUtil.translate("&#FF5555Использование: /star " + action + " <игрок> <количество>"));
            return true;
         } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage(HexUtil.translate("&#FF5555Игрок не найден или не в сети"));
               return true;
            } else {
               try {
                  int amount = Integer.parseInt(args[2]);
                  if (amount <= 0) {
                     sender.sendMessage(HexUtil.translate("&#FF5555Число должно быть положительным"));
                     return true;
                  }

                  if (action.equals("add")) {
                     this.starManager.add(target, amount);
                     sender.sendMessage(HexUtil.translate("&#55FF55Вы добавили " + amount + " звёзд игроку " + target.getName()));
                  } else {
                     int current = this.starManager.get(target);
                     if (current >= amount) {
                        this.starManager.remove(target, amount);
                        sender.sendMessage(HexUtil.translate("&#55FF55Вы забрали " + amount + " звёзд у игрока " + target.getName()));
                     } else {
                        sender.sendMessage(HexUtil.translate("&#FF5555У игрока недостаточно звёзд"));
                     }
                  }
               } catch (NumberFormatException var9) {
                  sender.sendMessage(HexUtil.translate("&#FF5555Неверное количество"));
               }

               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("holystar.admin")) {
         return completions;
      } else if (args.length == 1) {
         return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else if (args.length == 2) {
         return Bukkit.getOnlinePlayers()
            .stream()
            .<String>map(OfflinePlayer::getName)
            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
            .collect(Collectors.toList());
      } else if (args.length != 3 || !args[0].equalsIgnoreCase("add") && !args[0].equalsIgnoreCase("remove")) {
         return completions;
      } else {
         completions.add("1");
         completions.add("5");
         completions.add("10");
         completions.add("50");
         completions.add("100");
         return completions.stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
      }
   }
}
