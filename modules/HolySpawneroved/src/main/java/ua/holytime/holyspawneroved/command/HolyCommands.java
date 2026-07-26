package ua.holytime.holyspawneroved.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.holytime.holyspawneroved.HolySpawneroved;
import ua.holytime.holyspawneroved.gui.Menu;
import ua.holytime.holyspawneroved.item.EggType;
import ua.holytime.holyspawneroved.item.Item;

public class HolyCommands implements TabExecutor {
   private final HolySpawneroved plugin;
   private final Item item;

   public HolyCommands(HolySpawneroved plugin) {
      this.plugin = plugin;
      this.item = new Item(plugin);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!command.getName().equalsIgnoreCase("eggkeeper")) {
         return false;
      } else if (args.length == 0) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return false;
         } else {
            ((Player)sender).openInventory(new Menu(this.plugin).getInventory());
            return true;
         }
      } else if (!args[0].equalsIgnoreCase("give")) {
         if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("eggkeeper.admin")) {
               sender.sendMessage("§cУ вас нет прав на использование этой команды!");
               return false;
            } else {
               this.plugin.reloadPluginConfig();
               sender.sendMessage("§aКонфигурация перезагружена!");
               return true;
            }
         } else {
            sender.sendMessage("§cНеизвестная подкоманда. Используйте §f/eggkeeper");
            return false;
         }
      } else if (!sender.hasPermission("eggkeeper.admin")) {
         sender.sendMessage("§cУ вас нет прав на использование этой команды!");
         return false;
      } else if (args.length < 3) {
         sender.sendMessage("§6Использование: §f/eggkeeper give <ник> <яйцо> [количество]");
         return false;
      } else {
         Player target = Bukkit.getPlayer(args[1]);
         if (target == null) {
            sender.sendMessage("§cИгрок не найден или не в сети!");
            return false;
         } else {
            EggType eggType = EggType.fromCommandName(args[2]);
            if (eggType == null) {
               String availableTypes = Arrays.stream(EggType.values()).map(EggType::getCommandName).collect(Collectors.joining("§c, §e"));
               sender.sendMessage("§cНеизвестный тип яйца. Доступные варианты: §e" + availableTypes);
               return false;
            } else {
               ItemStack itemStack = this.item.createEgg(eggType);
               int amount = 1;
               if (args.length >= 4) {
                  try {
                     amount = Integer.parseInt(args[3]);
                     if (amount < 1 || amount > 64) {
                        sender.sendMessage("§cКоличество должно быть от 1 до 64!");
                        return false;
                     }
                  } catch (NumberFormatException var10) {
                     sender.sendMessage("§cНеверное количество! Используйте число от 1 до 64.");
                     return false;
                  }
               }

               itemStack.setAmount(amount);
               target.getInventory().addItem(new ItemStack[]{itemStack});
               sender.sendMessage("§aВыдано §e" + amount + " §a" + eggType.getCommandName() + " игроку §e" + target.getName());
               if (!sender.equals(target)) {
                  target.sendMessage("§aВы получили §e" + amount + " §a" + eggType.getCommandName());
               }

               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (command.getName().equalsIgnoreCase("eggkeeper") && sender.hasPermission("eggkeeper.admin")) {
         List<String> completions = new ArrayList<>();
         if (args.length == 1) {
            completions.add("give");
            completions.add("reload");
            return this.filterCompletions(completions, args[0]);
         } else {
            if (args[0].equalsIgnoreCase("give")) {
               switch (args.length) {
                  case 2:
                     Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                     break;
                  case 3:
                     for (EggType type : EggType.values()) {
                        completions.add(type.getCommandName());
                     }
                     break;
                  case 4:
                     for (int i = 1; i <= 64; i *= 2) {
                        completions.add(String.valueOf(i));
                     }
               }
            }

            return this.filterCompletions(completions, args[args.length - 1]);
         }
      } else {
         return Collections.emptyList();
      }
   }

   private List<String> filterCompletions(List<String> completions, String input) {
      return completions.stream().filter(s -> s.toLowerCase().startsWith(input.toLowerCase())).collect(Collectors.toList());
   }
}
