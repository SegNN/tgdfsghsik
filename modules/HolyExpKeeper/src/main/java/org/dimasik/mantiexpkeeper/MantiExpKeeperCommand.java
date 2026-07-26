package org.dimasik.mantiexpkeeper;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MantiExpKeeperCommand implements CommandExecutor {
   private MantiExpKeeper plugin;

   public MantiExpKeeperCommand(MantiExpKeeper plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("holyexpkeeper")) {
         if (args.length == 0) {
            sender.sendMessage("Использование: /holyexpkeeper [add/remove/get/check/give/getexp]");
            return true;
         }

         String mode = args[0].toLowerCase();
         switch (mode) {
            case "getexp":
               Player player = (Player)sender;
               long playerExperience = this.plugin.getPlayerExp(player);
               sender.sendMessage(String.valueOf(playerExperience));
               break;
            case "give":
               if (args.length < 3) {
                  sender.sendMessage("Использование: /holyexpkeeper give [никнейм] [название] (количество)");
                  return true;
               }

               Player givetarget = Bukkit.getPlayer(args[1]);
               if (givetarget == null) {
                  sender.sendMessage("Игрок не найден");
                  return true;
               }

               String item = args[2];
               int count = 1;
               if (args.length > 3) {
                  try {
                     count = Integer.parseInt(args[3]);
                  } catch (NumberFormatException var24) {
                     sender.sendMessage("Количество должно быть числом");
                     return true;
                  }
               }

               String var27 = item.toLowerCase();
               switch (var27) {
                  case "охотник":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getHunter(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "снеговик":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getSnowman(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "иллюминатор":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getIlluminator(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "эндермен":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getEnderman(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "антифантом":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getAntifantom(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "телекинез":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getTelekines(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "гравитация":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getGravity(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "вампиризм":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getVampire(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "справедливость":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getJustice(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "артефакт":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getArtefact(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  case "фулл_артефакт":
                     this.plugin.addItemInventory(givetarget.getInventory(), this.plugin.mantiExpItemUtil.getFullArtefact(), givetarget.getLocation());
                     sender.sendMessage("Предмет выдан");
                     return true;
                  default:
                     sender.sendMessage(
                        "Предмет не найден. Предметы: охотник, снеговик, иллюминатор, эндермен, антифантом, телекинез, гравитация, вампиризм, справедливость, артефакт, фулл_артефакт"
                     );
                     return true;
               }
            case "add":
               if (args.length < 4) {
                  sender.sendMessage("Использование: /holyexpkeeper add [никнейм] [название] [длительность в часах]");
                  return true;
               }

               String target = args[1];
               String name = args[2];

               try {
                  int duration = Integer.parseInt(args[3]);
                  this.plugin.addTime(target, name, duration);
                  sender.sendMessage("Способность добавлена");
               } catch (NumberFormatException var25) {
                  this.plugin.addInf(target, name);
                  sender.sendMessage("Способность добавлена навсегда");
               }
               break;
            case "remove":
               if (args.length < 3) {
                  sender.sendMessage("Использование: /holyexpkeeper remove [никнейм] [название]");
                  return true;
               }

               String target1 = args[1];
               String name1 = args[2];
               this.plugin.removePlayer(target1, name1);
               sender.sendMessage("Способность удалена");
               break;
            case "get":
               if (args.length < 3) {
                  sender.sendMessage("Использование: /holyexpkeeper get [никнейм] [название]");
                  return true;
               }

               String target2 = args[1];
               String name2 = args[2];
               CompletableFuture<String> result = this.plugin.getAbilityTime(target2, name2);
               sender.sendMessage("Функция вернула: " + result);
               break;
            case "check":
               if (args.length < 3) {
                  sender.sendMessage("Использование: /holyexpkeeper check [никнейм] [название]");
                  return true;
               }

               String target3 = args[1];
               String name3 = args[2];
               CompletableFuture<Boolean> result2 = this.plugin.hasAbility(target3, name3);
               sender.sendMessage("Функция вернула: " + result2);
               break;
            default:
               sender.sendMessage("Использование: /holyexpkeeper [add/remove/get/check/give/getexp]");
         }
      } else if (command.getName().equalsIgnoreCase("expkeeper")) {
         new MantiExpAbilities(this.plugin, (Player)sender);
         return true;
      }

      return true;
   }
}
