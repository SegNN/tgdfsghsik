package ua.fichamine.fichaautomine.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import ua.fichamine.fichaautomine.FichaAutoMine;
import ua.fichamine.fichaautomine.mines.EndNetherMine;
import ua.fichamine.fichaautomine.mines.OverworldMine;

public class MineCommand implements TabExecutor {
   private final FichaAutoMine plugin;
   private static final List<String> MAIN_COMMANDS = Arrays.asList("update", "timer");
   private static final List<String> UPDATE_TYPES = Arrays.asList("overworld", "end", "nether");
   private static final List<String> RARITIES = Arrays.asList("default", "epic", "legendary");
   private static final List<String> TIMER_TYPES = Arrays.asList("world", "endnether");

   public MineCommand(FichaAutoMine plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length == 0) {
         return false;
      } else {
         String subCommand = args[0].toLowerCase();
         switch (subCommand) {
            case "update":
               return this.handleUpdate(sender, args);
            case "timer":
               return this.handleTimer(sender, args);
            default:
               return false;
         }
      }
   }

   private boolean handleUpdate(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage("§cИспользование: /fichaautomine update <overworld|end|nether> <default|epic|legendary>");
         return false;
      } else {
         String mineType = args[1].toLowerCase();
         String rarity = args[2].toLowerCase();
         switch (mineType) {
            case "overworld":
               OverworldMine overworldMine = this.plugin.getOverworldMine();
               overworldMine.forceUpdate(rarity, false);
               overworldMine.resetTimer();
               sender.sendMessage("§aШахта верхнего мира обновлена!");
               return true;
            case "end":
            case "nether":
               EndNetherMine endNetherMine = this.plugin.getEndNetherMine();
               endNetherMine.forceUpdate(mineType, rarity, false);
               endNetherMine.resetTimer();
               sender.sendMessage("§aШахта " + mineType + " обновлена!");
               return true;
            default:
               sender.sendMessage("§cНеизвестный тип шахты");
               return false;
         }
      }
   }

   private boolean handleTimer(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage("§cИспользование: /fichaautomine timer <world|endnether> <время в секундах>");
         return false;
      } else {
         try {
            int time = Integer.parseInt(args[2]);
            if (time <= 0) {
               throw new NumberFormatException();
            } else {
               String type = args[1].toLowerCase();
               switch (type) {
                  case "world":
                     this.plugin.getOverworldMine().setNextUpdateTimer(time);
                     sender.sendMessage("§aТаймер шахты верхнего мира установлен на " + time + " секунд");
                     return true;
                  case "endnether":
                     this.plugin.getEndNetherMine().setNextUpdateTimer(time);
                     sender.sendMessage("§aТаймер шахты других миров установлен на " + time + " секунд");
                     return true;
                  default:
                     sender.sendMessage("§cНеизвестный тип шахты. Используйте: world или endnether");
                     return false;
               }
            }
         } catch (NumberFormatException var7) {
            sender.sendMessage("§cВремя должно быть положительным числом!");
            return false;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         return (List<String>)StringUtil.copyPartialMatches(args[0], MAIN_COMMANDS, new ArrayList());
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("update")) {
               return (List<String>)StringUtil.copyPartialMatches(args[1], UPDATE_TYPES, new ArrayList());
            }

            if (args[0].equalsIgnoreCase("timer")) {
               return (List<String>)StringUtil.copyPartialMatches(args[1], TIMER_TYPES, new ArrayList());
            }
         } else if (args.length == 3 && args[0].equalsIgnoreCase("update")) {
            return (List<String>)StringUtil.copyPartialMatches(args[2], RARITIES, new ArrayList());
         }

         return completions;
      }
   }
}
