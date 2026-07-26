package ua.fichadev.fichaexpbottle.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.fichadev.fichaexpbottle.FichaExpBottle;
import ua.fichadev.fichaexpbottle.utils.ColorUtils;

public class ExpBottleCommand implements CommandExecutor, TabCompleter {
   public ExpBottleCommand() {
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage(ColorUtils.colorize("&cИспользование: /expbottle give <ник> <тип> <количество>"));
         return true;
      } else if (!args[0].equalsIgnoreCase("give")) {
         return false;
      } else if (!sender.hasPermission("fichaexpbottle.give")) {
         sender.sendMessage(ColorUtils.colorize("&cУ вас нет прав!"));
         return true;
      } else if (args.length < 4) {
         sender.sendMessage(ColorUtils.colorize("&cИспользование: /expbottle give <ник> <тип> <количество>"));
         return true;
      } else {
         Player target = Bukkit.getPlayer(args[1]);
         if (target == null) {
            sender.sendMessage(ColorUtils.colorize("&cИгрок не найден!"));
            return true;
         } else {
            String bottleType = args[2];
            ConfigurationSection bottleConfig = FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles." + bottleType);
            if (bottleConfig == null) {
               sender.sendMessage(
                  ColorUtils.colorize(
                     "&cТип бутылька не найден в конфиге! Доступные типы: "
                        + String.join(", ", FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles").getKeys(false))
                  )
               );
               return true;
            } else {
               int amount;
               try {
                  amount = Integer.parseInt(args[3]);
               } catch (NumberFormatException var18) {
                  sender.sendMessage(ColorUtils.colorize("&cНеверное количество!"));
                  return true;
               }

               if (amount <= 0) {
                  sender.sendMessage(ColorUtils.colorize("&cКоличество должно быть больше 0!"));
                  return true;
               } else {
                  int expValue = bottleConfig.getInt("value");
                  String bottleName = ColorUtils.colorize(bottleConfig.getString("name"));
                  List<String> bottleLore = bottleConfig.getStringList("lore");
                  ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
                  ItemMeta meta = bottle.getItemMeta();
                  meta.setDisplayName(bottleName);
                  meta.setCustomModelData(expValue);
                  List<String> coloredLore = new ArrayList<>();

                  for (String line : bottleLore) {
                     coloredLore.add(ColorUtils.colorize(line));
                  }

                  meta.setLore(coloredLore);
                  bottle.setItemMeta(meta);
                  int remaining = amount;

                  while (remaining > 0) {
                     int stackSize = Math.min(remaining, 64);
                     ItemStack stack = bottle.clone();
                     stack.setAmount(stackSize);
                     target.getInventory().addItem(new ItemStack[]{stack});
                     remaining -= stackSize;
                  }

                  return true;
               }
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("give");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
         return Bukkit.getOnlinePlayers()
            .stream()
            .<String>map(OfflinePlayer::getName)
            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
            .collect(Collectors.toList());
      } else {
         if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            ConfigurationSection bottles = FichaExpBottle.getInstance().getPluginConfig().getConfigurationSection("bottles");
            if (bottles != null) {
               return bottles.getKeys(false).stream().filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
            }
         }

         if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("16");
            completions.add("32");
            completions.add("64");
            return completions;
         } else {
            return completions;
         }
      }
   }
}
