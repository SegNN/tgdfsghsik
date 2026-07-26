package ru.kotikstasika.eventship.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.kotikstasika.eventship.EventShip;
import ru.kotikstasika.eventship.enums.EventRarity;
import ru.kotikstasika.eventship.enums.KeyType;
import ru.kotikstasika.eventship.items.KeyItem;

public class EventShipCommand implements CommandExecutor, TabCompleter {
   private final EventShip plugin;

   public EventShipCommand(EventShip plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("eventship.admin")) {
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
            case "tp":
               this.handleTp(sender);
               break;
            case "give":
               this.handleGive(sender, args);
               break;
            case "update":
               this.handleUpdate(sender, args);
               break;
            case "help":
               this.sendHelp(sender);
               break;
            default:
               this.sendMessage(sender, "&c&l▶ &fНеизвестная команда! Используйте &6/ship help&f для справки.");
         }

         return true;
      }
   }

   private void handleSpawn(CommandSender sender, String[] args) {
      if (this.plugin.getEventManager().isActive()) {
         this.sendMessage(sender, "&c&l▶ &fИвент уже активен!");
      } else if (this.plugin.getEventManager().isSearchingLocation()) {
         this.sendMessage(sender, "&e&l▶ &fПоиск локации уже идет, подождите...");
      } else {
         EventRarity rarity = null;
         if (args.length > 1) {
            try {
               rarity = EventRarity.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException var5) {
               this.sendMessage(sender, "&c&l▶ &fНеверная редкость! Доступные: &6DEFAULT, PROSPEROUS, LUXURIOUS");
               return;
            }
         }

         if (this.plugin.getEventManager().spawnEventInWater(rarity)) {
            String rarityText = rarity != null ? rarity.getDisplayName() : "случайная";
            this.sendMessage(sender, "&a&l▶ &fИвент корабля успешно создан с редкостью: &6" + rarityText);
         } else {
            this.sendMessage(sender, "&c&l▶ &fНе удалось найти подходящую водную локацию!");
         }
      }
   }

   private void handleGive(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.sendMessage(sender, "&c&l▶ &fИспользование: &6/ship give <игрок> [тип_отмычки]");
      } else {
         String playerName = args[1];
         Player target = this.plugin.getServer().getPlayer(playerName);
         if (target == null) {
            this.sendMessage(sender, "&c&l▶ &fИгрок &6" + playerName + "&f не найден!");
         } else {
            KeyType keyType = KeyType.DEFAULT;
            if (args.length > 2) {
               try {
                  keyType = KeyType.valueOf(args[2].toUpperCase());
               } catch (IllegalArgumentException var7) {
                  this.sendMessage(sender, "&c&l▶ &fНеверный тип отмычки! Доступные: &6DEFAULT, RARE, UNIQUE, SECRET");
                  return;
               }
            }

            if (this.plugin.getEventManager().isActive()) {
               this.plugin.getEventManager().getCaptain().removeKey();
            }

            target.getInventory().addItem(new ItemStack[]{KeyItem.createKey(keyType, this.plugin)});
            this.sendMessage(sender, "&a&l▶ &fВы успешно выдали отмычку типа &6" + keyType.name() + "&f игроку &6" + target.getName() + "&f!");
            this.sendMessage(target, "&a&l▶ &fВы получили отмычку типа &6" + keyType.name() + "&f!");
         }
      }
   }

   private void handleUpdate(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.sendMessage(sender, "&c&l▶ &fИспользование: &6/ship update <отмычку|бочки>");
      } else if (!this.plugin.getEventManager().isActive()) {
         this.sendMessage(sender, "&c&l▶ &fИвент не активен!");
      } else {
         String updateType = args[1].toLowerCase();
         switch (updateType) {
            case "отмычку":
               if (this.plugin.getEventManager().getCaptain() != null) {
                  this.plugin.getEventManager().getCaptain().removeKey();
                  this.plugin.getEventManager().getCaptain().spawnKey();
               }

               this.sendMessage(sender, "&a&l▶ &fОтмычка у капитана успешно обновлена!");
               break;
            case "бочки":
               this.plugin.getEventManager().getBarrelManager().removeAllBarrels();
               this.sendMessage(sender, "&a&l▶ &fВсе бочки успешно закрыты!");
               break;
            default:
               this.sendMessage(sender, "&c&l▶ &fНеверный тип обновления! Доступные: &6отмычку, бочки");
         }
      }
   }

   private void handleRemove(CommandSender sender) {
      if (!this.plugin.getEventManager().isActive()) {
         this.sendMessage(sender, "&c&l▶ &fИвент не активен!");
      } else {
         this.plugin.getEventManager().removeEvent();
         this.sendMessage(sender, "&a&l▶ &fИвент корабля удален!");
      }
   }

   private void handleStatus(CommandSender sender) {
      if (this.plugin.getEventManager().isActive()) {
         Location location = this.plugin.getEventManager().getShipLocation();
         EventRarity rarity = this.plugin.getEventManager().getRarity();
         this.sendMessage(sender, "&a&l▶ &fСтатус: &6Запущено");
         if (location != null) {
            this.sendMessage(sender, "&fКоординаты: &6" + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
         }

         if (rarity != null) {
            this.sendMessage(sender, "&fРедкость: &6" + rarity.getDisplayName());
         }
      } else if (this.plugin.getEventManager().isSearchingLocation()) {
         this.sendMessage(sender, "&e&l▶ &fСтатус: &6Поиск локации...");
      } else {
         this.sendMessage(sender, "&c&l▶ &fСтатус: &6Остановлено");
      }
   }

   private void handleTp(CommandSender sender) {
      if (!(sender instanceof Player)) {
         this.sendMessage(sender, "&c&l▶ &fЭта команда только для игроков!");
      } else {
         Player player = (Player)sender;
         if (this.plugin.getEventManager().isActive() && this.plugin.getEventManager().getShipLocation() != null) {
            Location tpLocation = this.plugin.getEventManager().getShipLocation().clone().add(0.5, 2.0, 0.5);
            tpLocation.setYaw(player.getLocation().getYaw());
            tpLocation.setPitch(player.getLocation().getPitch());
            player.teleport(tpLocation);
            this.sendMessage(player, "&a&l▶ &fВы телепортированы к кораблю!");
         } else {
            this.sendMessage(player, "&c&l▶ &fИвент не активен!");
         }
      }
   }

   private void sendHelp(CommandSender sender) {
      this.sendMessage(sender, "&6&l▶ &fКоманды EventShip:");
      this.sendMessage(sender, "&f  &6/ship spawn [редкость]&f - Создать ивент корабля в воде");
      this.sendMessage(sender, "&f  &6/ship remove&f - Удалить ивент корабля");
      this.sendMessage(sender, "&f  &6/ship status&f - Статус ивента");
      this.sendMessage(sender, "&f  &6/ship tp&f - Телепорт к кораблю");
      this.sendMessage(sender, "&f  &6/ship give <игрок> [тип]&f - Выдать отмычку игроку");
      this.sendMessage(sender, "&f  &6/ship update <отмычку|бочки>&f - Обновить компоненты");
      this.sendMessage(sender, "&f  &6/ship help&f - Показать эту справку");
   }

   private void sendMessage(CommandSender sender, String message) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!sender.hasPermission("eventship.admin")) {
         return new ArrayList<>();
      } else if (args.length == 1) {
         List<String> completions = Arrays.asList("spawn", "remove", "status", "tp", "give", "update", "help");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else {
         if (args.length == 2) {
            String completions = args[0].toLowerCase();
            switch (completions) {
               case "spawn":
                  return Arrays.stream(EventRarity.values())
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
                  List<String> updateTypes = Arrays.asList("отмычку", "бочки");
                  return updateTypes.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
         }

         return (List<String>)(args.length == 3 && args[0].equalsIgnoreCase("give")
            ? Arrays.stream(KeyType.values()).map(Enum::name).filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList())
            : new ArrayList<>());
      }
   }
}
