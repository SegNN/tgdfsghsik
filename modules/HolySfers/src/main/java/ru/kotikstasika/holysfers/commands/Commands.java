package ru.kotikstasika.holysfers.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kotikstasika.holysfers.HolySfers;
import ru.kotikstasika.holysfers.sferomant.menu.Menu;
import ru.kotikstasika.holysfers.utils.Data;
import ru.kotikstasika.holysfers.utils.Utils;

public class Commands implements TabExecutor {
   private final Utils utils = HolySfers.getInstance().getUtils();
   private final HolySfers plugin = HolySfers.getInstance();
   private final Data data = new Data();
   private final Menu menu = new Menu();

   public Commands() {
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("sferomant")) {
         if (sender instanceof Player) {
            this.menu.openMenu((Player)sender);
         } else {
            sender.sendMessage("нахуй пошел не игрок");
         }
      }

      if (command.getName().equalsIgnoreCase("holysfers")) {
         if (!sender.hasPermission("holysfers.admin")) {
            sender.sendMessage("ты лох без прав");
            return true;
         }

         if (args.length == 0) {
            sender.sendMessage("Использование: /holysfers [add/type/tag/head/name/save/delete/give]");
            return true;
         }

         String var5 = args[0].toLowerCase();
         switch (var5) {
            case "open":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               this.menu.openMenu((Player)sender);
            case "add":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               if (args.length < 3) {
                  sender.sendMessage("Использование: /holysfers add <тип_эффекта> <уровень>");
                  return true;
               }

               Player player = (Player)sender;
               if (player.getItemInHand().getType() != Material.PLAYER_HEAD && player.getItemInHand().getType() != Material.TOTEM_OF_UNDYING) {
                  sender.sendMessage("Вы должны держать в руках голову игрока / тотем бессмертия.");
                  return true;
               }

               String var24 = args[1].toLowerCase();
               Utils.EffectType effectType;
               switch (var24) {
                  case "damage":
                     effectType = Utils.EffectType.DAMAGE;
                     break;
                  case "armor":
                     effectType = Utils.EffectType.ARMOR;
                     break;
                  case "speed":
                     effectType = Utils.EffectType.SPEED;
                     break;
                  case "health":
                     effectType = Utils.EffectType.HEALTH;
                     break;
                  case "attack_speed":
                     effectType = Utils.EffectType.ATTACK_SPEED;
                     break;
                  case "haste":
                     effectType = Utils.EffectType.HASTE;
                     break;
                  default:
                     sender.sendMessage("Неверный тип эффекта. Доступные типы: DAMAGE, ARMOR, SPEED, HEALTH, ATTACK_SPEED, HASTE.");
                     return true;
               }

               try {
                  int level = Integer.parseInt(args[2]);
                  this.utils.addToSphere(player, effectType, level);
                  sender.sendMessage("Эффект успешно добавлен!");
               } catch (NumberFormatException var23) {
                  sender.sendMessage("Неверный уровень. Уровень должен быть числом.");
               }
               break;
            case "type":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers type <тип_сферы>");
                  return true;
               }

               Player player1 = (Player)sender;
               if (player1.getItemInHand().getType() != Material.PLAYER_HEAD && player1.getItemInHand().getType() != Material.TOTEM_OF_UNDYING) {
                  sender.sendMessage("Вы должны держать в руках голову игрока / тотем бессмертия.");
                  return true;
               }

               String var25 = args[1].toLowerCase();
               Utils.SphereType sphereType;
               switch (var25) {
                  case "default":
                     sphereType = Utils.SphereType.DEFAULT;
                     break;
                  case "epic":
                     sphereType = Utils.SphereType.EPIC;
                     break;
                  case "legendary":
                     sphereType = Utils.SphereType.LEGENDARY;
                     break;
                  case "mythic":
                     sphereType = Utils.SphereType.MYTHIC;
                     break;
                  case "custom":
                     sphereType = Utils.SphereType.CUSTOM;
                     break;
                  default:
                     sender.sendMessage("Неверный тип сферы. Доступные типы: DEFAULT, EPIC, LEGENDARY, MYTHIC, CUSTOM.");
                     return true;
               }

               Player player2 = (Player)sender;
               this.utils.setSphereType(player2, sphereType, player2.getItemInHand().getType() == Material.PLAYER_HEAD);
               sender.sendMessage("Тип сферы успешно изменен!");
               break;
            case "tag":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               Player player3 = (Player)sender;
               if (player3.getItemInHand().getType() != Material.PLAYER_HEAD && player3.getItemInHand().getType() != Material.TOTEM_OF_UNDYING) {
                  sender.sendMessage("Вы должны держать в руках голову игрока / тотем бессмертия.");
                  return true;
               }

               this.utils.addLore(player3);
               sender.sendMessage("Тег успешно добавлен!");
               break;
            case "head":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers head <base64>");
                  return true;
               }

               Player player4 = (Player)sender;
               if (player4.getItemInHand().getType() != Material.PLAYER_HEAD && player4.getItemInHand().getType() != Material.TOTEM_OF_UNDYING) {
                  sender.sendMessage("Вы должны держать в руках голову игрока / тотем бессмертия.");
                  return true;
               }

               player4.setItemInHand(this.utils.getSkull(player4.getItemInHand(), args[1]));
               sender.sendMessage("Скин успешно изменен!");
               break;
            case "name":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers name <имя>");
                  return true;
               }

               Player player5 = (Player)sender;
               if (player5.getItemInHand().getType() != Material.PLAYER_HEAD && player5.getItemInHand().getType() != Material.TOTEM_OF_UNDYING) {
                  sender.sendMessage("Вы должны держать в руках голову игрока / тотем бессмертия.");
                  return true;
               }

               String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
               this.utils.setName(player5, name);
               sender.sendMessage("Тег успешно добавлен!");
               break;
            case "save":
               if (!(sender instanceof Player)) {
                  sender.sendMessage("ты не игрок, иди отсюда нахуй");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers save <имя>");
                  return true;
               }

               Player player6 = (Player)sender;
               ItemStack itemInHand = player6.getInventory().getItemInMainHand();
               if (itemInHand == null || itemInHand.getType().isAir()) {
                  sender.sendMessage("Вы должны держать предмет в руке.");
                  return true;
               }

               this.data.saveItem(args[1], itemInHand);
               sender.sendMessage("Предмет сохранен как " + args[1]);
               break;
            case "delete":
               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers delete <имя>");
                  return true;
               }

               this.data.deleteItem(args[1]);
               sender.sendMessage("Предмет " + args[1] + " удален.");
               break;
            case "give":
               if (args.length < 2) {
                  sender.sendMessage("Использование: /holysfers give <никнейм> <имя> [количество]");
                  return true;
               }

               String targetName = args[1];
               String itemName = args[2];
               int amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;
               Player target = this.plugin.getServer().getPlayer(targetName);
               if (target == null) {
                  sender.sendMessage("Игрок " + targetName + " не найден.");
                  return true;
               }

               ItemStack item = this.data.getItem(itemName);
               if (item == null) {
                  sender.sendMessage("Предмет " + itemName + " не найден.");
                  return true;
               }

               item.setAmount(amount);
               target.getInventory().addItem(new ItemStack[]{item});
               sender.sendMessage("Выдано " + amount + " предметов " + itemName + " игроку " + targetName);
               break;
            default:
               sender.sendMessage("Использование: /holysfers [add/type/tag/head/name/save/delete/give]");
         }
      }

      return true;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("holysfers.admin")) {
         return completions;
      } else {
         if (command.getName().equalsIgnoreCase("holysfers")) {
            if (args.length == 1) {
               for (String cmd : Arrays.asList("add", "type", "tag", "head", "name", "save", "delete", "give")) {
                  if (cmd.startsWith(args[0].toLowerCase())) {
                     completions.add(cmd);
                  }
               }
            } else if (args.length == 2) {
               String var12 = args[0].toLowerCase();
               switch (var12) {
                  case "add":
                     for (String typex : Arrays.asList("damage", "armor", "speed", "health", "attack_speed", "haste")) {
                        if (typex.startsWith(args[1].toLowerCase())) {
                           completions.add(typex);
                        }
                     }
                     break;
                  case "type":
                     for (String type : Arrays.asList("default", "epic", "legendary", "mythic", "custom")) {
                        if (type.startsWith(args[1].toLowerCase())) {
                           completions.add(type);
                        }
                     }
                     break;
                  case "give":
                     for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                        completions.add(player.getName());
                     }
                     break;
                  case "delete":
                     completions.addAll(this.data.getSavedItemNames());
               }
            } else if (args.length == 3) {
               String var13 = args[0].toLowerCase();
               byte var15 = -1;
               switch (var13.hashCode()) {
                  case 3173137:
                     if (var13.equals("give")) {
                        var15 = 0;
                     }
                  default:
                     switch (var15) {
                        case 0:
                           completions.addAll(this.data.getSavedItemNames());
                     }
               }
            }
         } else if (command.getName().equalsIgnoreCase("sferomant")) {
            return completions;
         }

         return completions;
      }
   }
}
