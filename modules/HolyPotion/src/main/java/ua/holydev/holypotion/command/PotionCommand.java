package ua.holydev.holypotion.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.holydev.holypotion.HolyPotion;

public class PotionCommand implements CommandExecutor, TabCompleter {
   private final HolyPotion plugin;

   public PotionCommand(HolyPotion plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(ChatColor.RED + "Используйте: /holypotion save [тип] или /holypotion give [ник] [тип] (количество)");
         return true;
      } else {
         String sub = args[0].toLowerCase();
         if (sub.equals("save")) {
            return this.handleSave(sender, args);
         } else if (sub.equals("give")) {
            return this.handleGive(sender, args);
         } else {
            sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда!");
            return true;
         }
      }
   }

   private boolean handleSave(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
         return true;
      } else {
         String type = args[1].toLowerCase();
         Player player = (Player)sender;
         ItemStack item = player.getInventory().getItemInMainHand();
         if (item.getType() != Material.POTION) {
            sender.sendMessage(ChatColor.RED + "Вы должны держать зелье в руке!");
            return true;
         } else {
            this.plugin.getPotionManager().savePotion(type, item);
            sender.sendMessage(ChatColor.GREEN + "Зелье типа " + type + " успешно сохранено!");
            return true;
         }
      }
   }

   private boolean handleGive(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(ChatColor.RED + "Используйте: /holypotion give [ник] [тип] (количество)");
         return true;
      } else {
         Player target = this.plugin.getServer().getPlayer(args[1]);
         if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
         } else {
            String type = args[2].toLowerCase();
            int amount = 1;
            if (args.length > 3) {
               try {
                  amount = Integer.parseInt(args[3]);
               } catch (NumberFormatException var7) {
                  sender.sendMessage(ChatColor.RED + "Неверное количество!");
                  return true;
               }
            }

            ItemStack potion = this.plugin.getPotionManager().getPotion(type);
            if (potion == null) {
               sender.sendMessage(ChatColor.RED + "Зелье типа " + type + " не найдено!");
               return true;
            } else {
               potion.setAmount(amount);
               target.getInventory().addItem(new ItemStack[]{potion});
               sender.sendMessage(ChatColor.GREEN + "Выдали " + amount + " зелий типа " + type + " игроку " + target.getName());
               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         return this.filter(Arrays.asList("save", "give"), args[0]);
      } else {
         String sub = args[0].toLowerCase();
         if (sub.equals("save") && args.length == 2) {
            return this.filter(this.plugin.getPotionManager().getPotionTypes(), args[1]);
         } else {
            if (sub.equals("give")) {
               if (args.length == 2) {
                  return this.filter(this.getOnlinePlayerNames(), args[1]);
               }

               if (args.length == 3) {
                  return this.filter(this.plugin.getPotionManager().getPotionTypes(), args[2]);
               }

               if (args.length == 4) {
                  return this.filter(Arrays.asList("1", "16", "32", "64"), args[3]);
               }
            }

            return new ArrayList<>();
         }
      }
   }

   private List<String> getOnlinePlayerNames() {
      return Bukkit.getOnlinePlayers().stream().<String>map(OfflinePlayer::getName).collect(Collectors.toList());
   }

   private List<String> filter(List<String> options, String input) {
      String lower = input.toLowerCase();
      return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
   }
}
