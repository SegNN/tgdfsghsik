package ru.nezxenka.holykits.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.nezxenka.holykits.HolyKits;
import ru.nezxenka.holykits.managers.Kit;
import ru.nezxenka.holykits.managers.KitManager;

public class KitAdminCommand implements CommandExecutor, TabCompleter {
   private final HolyKits plugin;

   public KitAdminCommand(HolyKits plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("holykits.admin")) {
         sender.sendMessage("§cУ вас нет прав на использование этой команды!");
         return true;
      } else if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         KitManager kitManager = this.plugin.getKitManager();
         String subCommand = args[0].toLowerCase();
         switch (subCommand) {
            case "setinv":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("§cЭта команда только для игроков!");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("§cИспользование: /kitadmin setinv <название набора>");
                  return true;
               }

               String kitName = args[1].toLowerCase();
               if (kitManager.createKit(kitName, (Player)sender)) {
                  sender.sendMessage("§aНабор " + kitName + " успешно создан из вашего инвентаря!");
               } else {
                  sender.sendMessage("§aНабор с таким именем уже существует, инвентарь обновлен!");
               }
               break;
            case "remove":
               if (args.length < 2) {
                  sender.sendMessage("§cИспользование: /kitadmin remove <название набора>");
                  return true;
               }

               String kitName1 = args[1].toLowerCase();
               if (kitManager.deleteKit(kitName1)) {
                  sender.sendMessage("§aНабор " + kitName1 + " успешно удален!");
               } else {
                  sender.sendMessage("§cНабор с таким именем не существует!");
               }
               break;
            case "settime":
               if (args.length < 3) {
                  sender.sendMessage("§cИспользование: /kitadmin settime <название набора> <время в секундах>");
                  return true;
               }

               try {
                  long cooldown = Long.parseLong(args[2]);
                  kitManager.setKitCooldown(args[1].toLowerCase(), cooldown);
                  sender.sendMessage("§aКулдаун для набора " + args[1] + " установлен на " + cooldown + " секунд.");
               } catch (NumberFormatException var19) {
                  sender.sendMessage("§cВремя должно быть числом!");
               }
               break;
            case "setpriority":
               if (args.length < 3) {
                  sender.sendMessage("§cИспользование: /kitadmin setpriority <название набора> <приоритет>");
                  return true;
               }

               try {
                  int priority = Integer.parseInt(args[2]);
                  kitManager.setKitPriority(args[1].toLowerCase(), priority);
                  sender.sendMessage("§aПриоритет для набора " + args[1] + " установлен на " + priority + ".");
               } catch (NumberFormatException var18) {
                  sender.sendMessage("§cПриоритет должен быть числом!");
               }
               break;
            case "clearcooldown":
               if (args.length < 3) {
                  sender.sendMessage("§cИспользование: /kitadmin clearcooldown <игрок> <название набора>");
                  return true;
               }

               Player target = this.plugin.getServer().getPlayer(args[1]);
               if (target == null) {
                  sender.sendMessage("§cИгрок не найден или не в сети!");
                  return true;
               }

               kitManager.clearCooldown(target, args[2].toLowerCase());
               sender.sendMessage("§aКулдаун для игрока " + target.getName() + " на набор " + args[2] + " был сброшен.");
               break;
            case "setenabled":
               if (args.length < 2) {
                  sender.sendMessage("§cИспользование: /kitadmin setenabled <true/false> [время в секундах]");
                  return true;
               }

               boolean enabled;
               try {
                  enabled = Boolean.parseBoolean(args[1]);
               } catch (Exception var17) {
                  sender.sendMessage("§cУкажите true или false для включения/выключения наборов");
                  return true;
               }

               if (args.length >= 3 && args[1].equalsIgnoreCase("false")) {
                  try {
                     if (this.plugin.getDatabase().setKitToggleTime(args[2])) {
                        sender.sendMessage("§aУспешно");
                     } else {
                        sender.sendMessage("§cОшибка");
                     }
                  } catch (NumberFormatException var16) {
                     sender.sendMessage("§cВремя должно быть числом!");
                     return true;
                  }
               } else {
                  this.plugin.getDatabase().toggleKits(!enabled);
                  sender.sendMessage("§aНаборы теперь " + (enabled ? "включены" : "выключены"));
               }
               break;
            case "give":
               if (args.length < 3) {
                  sender.sendMessage("§cИспользование: /kitadmin give <игрок> <название набора>");
                  return true;
               }

               Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
               if (targetPlayer == null) {
                  sender.sendMessage("§cИгрок не найден или не в сети!");
                  return true;
               }

               String giveKitName = args[2].toLowerCase();
               Kit giveKit = kitManager.getKit(giveKitName);
               if (giveKit == null) {
                  sender.sendMessage("§cНабор с таким именем не существует!");
                  return true;
               }

               giveKit.applyToInventory(targetPlayer.getInventory());
               targetPlayer.updateInventory();
               sender.sendMessage("§aВы выдали набор §b" + giveKitName + " §aигроку §b" + targetPlayer.getName());
               targetPlayer.sendMessage("§aВам выдали набор §b" + giveKitName);
               break;
            default:
               this.sendHelp(sender);
         }

         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("holykits.admin")) {
         return completions;
      } else if (args.length == 1) {
         List<String> subcommands = Arrays.asList("setinv", "remove", "settime", "setpriority", "clearcooldown", "setenabled", "give");
         return subcommands.stream().filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
               case "remove":
               case "settime":
               case "setpriority":
                  return this.plugin
                     .getKitManager()
                     .getAllKits()
                     .stream()
                     .map(Kit::getName)
                     .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
               case "clearcooldown":
               case "give":
                  return Bukkit.getOnlinePlayers()
                     .stream()
                     .<String>map(OfflinePlayer::getName)
                     .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
               case "setenabled":
                  return Arrays.asList("true", "false").stream().filter(val -> val.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
         }

         if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("clearcooldown") || subCommand.equals("give")) {
               return this.plugin
                  .getKitManager()
                  .getAllKits()
                  .stream()
                  .map(Kit::getName)
                  .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                  .collect(Collectors.toList());
            }

            if (subCommand.equals("setenabled") && args[1].equalsIgnoreCase("false")) {
               return Arrays.asList("1h", "2h", "3h", "12h", "24h", "1d", "2d", "3d", "-1")
                  .stream()
                  .filter(val -> val.startsWith(args[2].toLowerCase()))
                  .collect(Collectors.toList());
            }
         }

         return completions;
      }
   }

   private void sendHelp(CommandSender sender) {
      sender.sendMessage("§6===== HolyKits Admin Help =====");
      sender.sendMessage("§a/kitadmin setinv <название> - создать набор из инвентаря");
      sender.sendMessage("§a/kitadmin settime <название> <секунды> - установить кулдаун");
      sender.sendMessage("§a/kitadmin setpriority <название> <приоритет> - установить приоритет");
      sender.sendMessage("§a/kitadmin clearcooldown <игрок> <название> - сбросить кулдаун");
      sender.sendMessage("§a/kitadmin setenabled <true/false> [время] - вкл/выкл наборы");
      sender.sendMessage("§a/kitadmin give <игрок> <название> - выдать набор игроку");
   }
}
