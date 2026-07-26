package ru.kotikstasika.holyliteend.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.holyliteend.HolyLiteEnd;
import ru.kotikstasika.holyliteend.end.EggItem;
import ru.kotikstasika.holyliteend.end.managers.EndEvent;
import ru.kotikstasika.holyliteend.utils.ColorUtils;
import ru.kotikstasika.holyliteend.utils.DatabaseManager;

public class HolyLiteEndCommand implements CommandExecutor, TabCompleter {
   private final HolyLiteEnd plugin;

   public HolyLiteEndCommand(HolyLiteEnd plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("holyliteend.admin")) {
         return true;
      } else if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String subCommand = args[0].toLowerCase();
         switch (subCommand) {
            case "give":
               this.handleGive(sender, args);
               break;
            case "start":
               this.handleStart(sender);
               break;
            case "stop":
               this.handleStop(sender);
               break;
            case "settime":
               if (args.length < 3) {
                  sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fИспользование: &6/holyliteend settime <день_недели> <часы> <минуты>"));
                  return true;
               }

               this.handleSetTime(sender, args);
               break;
            case "setspawn":
               this.handleSetSpawn(sender);
               break;
            case "help":
               this.sendHelp(sender);
               break;
            default:
               this.sendHelp(sender);
         }

         return true;
      }
   }

   private void handleGive(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fИспользование: &6/holyliteend give <игрок> <цена>"));
      } else {
         String playerName = args[1];
         Player target = this.plugin.getServer().getPlayer(playerName);
         if (target == null) {
            sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fИгрок &6" + playerName + "&f не найден!"));
         } else {
            try {
               int price = Integer.parseInt(args[2]);
               ItemStack egg = EggItem.createDragonEgg(price);
               target.getInventory().addItem(new ItemStack[]{egg});
               sender.sendMessage(
                  ColorUtils.translateColors("&a&l▶ &fВы выдали яйцо дракона игроку &6" + target.getName() + "&f со стоимостью &6" + price + "&f!")
               );
            } catch (NumberFormatException var7) {
               sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fНеверная цена! Укажите число."));
            }
         }
      }
   }

   private void handleStart(CommandSender sender) {
      this.plugin.getEndEventManager();
      if (EndEvent.isEventActive()) {
         sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fСобытие 'Захват Энда' уже активно!"));
      } else {
         this.plugin.getEndEventManager().startEvent();
      }
   }

   private void handleStop(CommandSender sender) {
      this.plugin.getEndEventManager();
      if (!EndEvent.isEventActive()) {
         sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fСобытие 'Захват Энда' не активно!"));
      } else {
         this.plugin.getEndEventManager().endEvent();
      }
   }

   private void handleSetTime(CommandSender sender, String[] args) {
      try {
         int dayOfWeek = Integer.parseInt(args[1]);
         int hours = Integer.parseInt(args[2]);
         int minutes = Integer.parseInt(args[3]);
         if (dayOfWeek < 1 || dayOfWeek > 7) {
            sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fДень недели должен быть от 1 до 7 (1=понедельник, 7=воскресенье)"));
            return;
         }

         if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fНеверное время! Часы: 0-23, минуты: 0-59"));
            return;
         }

         DatabaseManager.EndSettings settings = this.plugin.getDatabaseManager().getEndSettings();
         settings.setDayOfWeek(dayOfWeek);
         settings.setStartHour(hours);
         settings.setStartMinute(minutes);
         this.plugin.getDatabaseManager().updateEndSettings(settings);
         sender.sendMessage(
            ColorUtils.translateColors("&a&l▶ &fВремя события установлено: &6день " + dayOfWeek + ", " + hours + ":" + String.format("%02d", minutes))
         );
      } catch (NumberFormatException var7) {
         sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fНеверный формат! Используйте числа."));
      }
   }

   private void handleSetSpawn(CommandSender sender) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ColorUtils.translateColors("&x&f&b&0&0&0&0&l▶ &fЭта команда только для игроков!"));
      } else {
         Player player = (Player)sender;
         this.plugin.setEndSpawnLocation(player.getLocation());
         sender.sendMessage(ColorUtils.translateColors("&a&l▶ &fЛокация спавна в Энде установлена!"));
      }
   }

   private void sendHelp(CommandSender sender) {
      sender.sendMessage(ColorUtils.translateColors("&6&l▶ &fКоманды HolyLiteEnd:"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend give <игрок> <цена>&f - Выдать яйцо дракона игроку"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend start&f - Открыть событие 'Захват Энда'"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend stop&f - Закрыть событие 'Захват Энда'"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend settime <день> <ч> <м>&f - Установить время события"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend setspawn&f - Установить точку спавна в энде"));
      sender.sendMessage(ColorUtils.translateColors("&f  &6/holyliteend help&f - Показать эту справку"));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("holyliteend.admin")) {
         return new ArrayList<>();
      } else if (args.length == 1) {
         List<String> completions = Arrays.asList("give", "start", "stop", "settime", "setspawn", "help");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
               case "give":
                  return this.plugin
                     .getServer()
                     .getOnlinePlayers()
                     .stream()
                     .<String>map(OfflinePlayer::getName)
                     .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
               case "settime":
                  return Arrays.asList("1", "2", "3", "4", "5", "6", "7")
                     .stream()
                     .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
            }
         }

         return new ArrayList<>();
      }
   }
}
