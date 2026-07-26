package ru.kotikstasika.eventpolana.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.kotikstasika.eventpolana.EventPolana;
import ru.kotikstasika.eventpolana.model.PolanaRarity;
import ru.kotikstasika.eventpolana.model.PolanaState;

public class PolanaCommand implements CommandExecutor, TabCompleter {
   private final EventPolana plugin;

   public PolanaCommand(EventPolana plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("eventpolana.admin")) {
         this.sendMessage(sender, "&c&l▶ &fУ вас нет прав для использования этой команды!");
         return true;
      } else if (args.length == 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "spawn":
               this.handleSpawn(sender, args);
               break;
            case "remove":
               this.handleRemove(sender);
               break;
            case "status":
               this.handleStatus(sender);
               break;
            case "give":
               this.handleGive(sender, args);
               break;
            case "update":
               this.handleUpdate(sender, args);
               break;
            case "tp":
               this.handleTp(sender);
               break;
            case "help":
               this.sendHelp(sender);
               break;
            default:
               this.sendMessage(sender, "&c&l▶ &fНеизвестная команда! Используйте &6/polana help&f для справки.");
         }

         return true;
      }
   }

   private void handleSpawn(CommandSender sender, String[] args) {
      if (this.plugin.getState() == PolanaState.SPAWNED) {
         this.sendMessage(sender, "&c&l▶ &fСобытие уже запущено!");
      } else if (this.plugin.isSearchingLocation()) {
         this.sendMessage(sender, "&c&l▶ &fПоиск локации уже идет, подождите...");
      } else {
         PolanaRarity rarity;
         if (args.length > 1) {
            try {
               rarity = PolanaRarity.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException var5) {
               this.sendMessage(sender, "&c&l▶ &fНеверная редкость! Доступные: &6DEFAULT, EPIC, LEGENDARY");
               return;
            }
         } else {
            PolanaRarity[] values = PolanaRarity.values();
            rarity = values[ThreadLocalRandom.current().nextInt(values.length)];
         }

         this.plugin.spawnPolana(rarity);
         this.sendMessage(sender, "&a&l▶ &fВы запустили событие &6Цветочная поляна&f с редкостью: &6" + rarity.name());
      }
   }

   private void handleRemove(CommandSender sender) {
      if (this.plugin.getState() != PolanaState.SPAWNED) {
         this.sendMessage(sender, "&c&l▶ &fСобытие не запущено!");
      } else {
         this.plugin.removePolana();
         this.sendMessage(sender, "&a&l▶ &fСобытие &6Цветочная поляна&f успешно остановлено!");
      }
   }

   private void handleStatus(CommandSender sender) {
      PolanaState state = this.plugin.getState();
      String statusMessage;
      switch (state) {
         case SPAWNED:
            statusMessage = "&a&l▶ &fСтатус: &6Запущено";
            if (this.plugin.getPolanaLocation() != null) {
               statusMessage = statusMessage
                  + "&f | Координаты: &6"
                  + this.plugin.getPolanaLocation().getBlockX()
                  + " "
                  + this.plugin.getPolanaLocation().getBlockY()
                  + " "
                  + this.plugin.getPolanaLocation().getBlockZ();
            }

            if (this.plugin.getRarity() != null) {
               statusMessage = statusMessage + "&f | Редкость: &6" + this.plugin.getRarity().name();
            }
            break;
         case NOT_SPAWNED:
            if (this.plugin.isSearchingLocation()) {
               statusMessage = "&e&l▶ &fСтатус: &6Поиск локации...";
            } else {
               statusMessage = "&c&l▶ &fСтатус: &6Остановлено";
            }
            break;
         default:
            statusMessage = "&7&l▶ &fСтатус: &6Неизвестно";
      }

      this.sendMessage(sender, statusMessage);
   }

   private void handleGive(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.sendMessage(sender, "&c&l▶ &fИспользование: &6/polana give <игрок>");
      } else {
         String playerName = args[1];
         Player target = this.plugin.getServer().getPlayer(playerName);
         if (target == null) {
            this.sendMessage(sender, "&b&l▶ &fИгрок &6" + playerName + "&f не найден!");
         } else {
            this.plugin.giveShears(sender, playerName);
            this.sendMessage(sender, "&a&l▶ &fВы успешно выдали ножницы игроку &6" + target.getName() + "&f!");
         }
      }
   }

   private void handleUpdate(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.sendMessage(sender, "&c&l▶ &fИспользование: &6/polana update <shears|flowers>");
      } else {
         String updateType = args[1].toLowerCase();
         switch (updateType) {
            case "shears":
               if (this.plugin.getState() == PolanaState.SPAWNED) {
                  this.plugin.getPolanaManager().setShearsTaken(false);
                  this.sendMessage(sender, "&a&l▶ &fНожницы успешно обновлены!");
               } else {
                  this.sendMessage(sender, "&c&l▶ &fСобытие не запущено!");
               }
               break;
            case "flowers":
               if (this.plugin.getState() == PolanaState.SPAWNED) {
                  this.plugin.getPolanaManager().restartFlowerSpawning();
                  this.sendMessage(sender, "&a&l▶ &fСпавн цветов успешно обновлен!");
               } else {
                  this.sendMessage(sender, "&c&l▶ &fСобытие не запущено!");
               }
               break;
            default:
               this.sendMessage(sender, "&c&l▶ &fНеверный тип обновления! Доступные: &6shears, flowers");
         }
      }
   }

   private void handleTp(CommandSender sender) {
      if (!(sender instanceof Player)) {
         this.sendMessage(sender, "&c&l▶ &fЭта команда только для игроков!");
      } else {
         Player player = (Player)sender;
         if (this.plugin.getState() == PolanaState.SPAWNED && this.plugin.getPolanaLocation() != null) {
            float yaw = player.getLocation().getYaw();
            float pitch = player.getLocation().getPitch();
            Location tp = this.plugin.getPolanaLocation().clone().add(0.5, 2.0, 0.5);
            tp.setYaw(yaw);
            tp.setPitch(pitch);
            player.teleport(tp);
            this.sendMessage(player, "&a&l▶ &fВы телепортированы к &6Цветочной поляне&f!");
         } else {
            this.sendMessage(player, "&c&l▶ &fСобытие не запущено!");
         }
      }
   }

   private void sendHelp(CommandSender sender) {
      this.sendMessage(sender, "&6&l▶ &fКоманды EventPolana:");
      this.sendMessage(sender, "&f  &6/polana spawn [редкость]&f - Запустить событие (без аргумента — случайная редкость)");
      this.sendMessage(sender, "&f  &6/polana remove&f - Остановить событие");
      this.sendMessage(sender, "&f  &6/polana status&f - Статус события");
      this.sendMessage(sender, "&f  &6/polana tp&f - Телепорт к событию");
      this.sendMessage(sender, "&f  &6/polana give <игрок>&f - Выдать ножницы");
      this.sendMessage(sender, "&f  &6/polana update <shears|flowers>&f - Обновить компоненты");
      this.sendMessage(sender, "&f  &6/polana help&f - Показать эту справку");
   }

   private void sendMessage(CommandSender sender, String message) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("eventpolana.admin")) {
         return new ArrayList<>();
      } else if (args.length == 1) {
         List<String> completions = Arrays.asList("spawn", "remove", "status", "tp", "give", "update", "help");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String completions = args[0].toLowerCase();
            switch (completions) {
               case "spawn":
                  return Arrays.stream(PolanaRarity.values())
                     .map(Enum::name)
                     .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
               case "give":
                  return this.plugin
                     .getServer()
                     .getOnlinePlayers()
                     .stream()
                     .<String>map(OfflinePlayer::getName)
                     .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                     .collect(Collectors.toList());
               case "update":
                  List<String> updateTypes = Arrays.asList("shears", "flowers");
                  return updateTypes.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
         }

         return new ArrayList<>();
      }
   }
}
