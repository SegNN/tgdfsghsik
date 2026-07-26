package ua.fichadev.fichacustompriv.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class TokenAdminCMD implements CommandExecutor, TabCompleter {
   private FichaCustomPriv plugin;

   public TokenAdminCMD(FichaCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("FichaCustomPriv.admin")) {
         sender.sendMessage(HexUtil.color(this.plugin.getMessage("no-permission")));
         return true;
      } else if (args.length < 4) {
         sender.sendMessage(HexUtil.color("&#1573FB▶ &fИспользование: /fichacustompriv give/take ник priv/titul [30d/90d/infinity] [количество]"));
         sender.sendMessage(HexUtil.color("&#1573FB▶ &fДля титулов тип жетона не указывается"));
         return true;
      } else {
         String action = args[0].toLowerCase();
         String playerName = args[1];
         String tokenCategory = args[2].toLowerCase();
         Player target = Bukkit.getPlayer(playerName);
         if (target == null) {
            sender.sendMessage(HexUtil.color("&#1573FB▶ &fДанный игрок не найден"));
            return true;
         } else if (!tokenCategory.equals("priv") && !tokenCategory.equals("titul")) {
            sender.sendMessage(HexUtil.color("&#1573FB▶ &fНеверная категория жетонов. Используйте: priv или titul"));
            return true;
         } else {
            String tokenType = "";
            String actualCategory = tokenCategory.equals("priv") ? "privileges" : "tituls";
            int amount;
            if (tokenCategory.equals("priv")) {
               if (args.length < 5) {
                  sender.sendMessage(HexUtil.color("&#1573FB▶ &fДля привилегий укажите тип: /fichacustompriv give/take ник priv 30d/90d/infinity [количество]"));
                  return true;
               }

               tokenType = args[3];
               amount = args.length > 4 ? Integer.parseInt(args[4]) : 1;
               if (!tokenType.equals("30d") && !tokenType.equals("90d") && !tokenType.equals("infinity")) {
                  sender.sendMessage(HexUtil.color("&#1573FB▶ &fНеверный тип жетона. Используйте: 30d, 90d или infinity"));
                  return true;
               }
            } else {
               amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;
               tokenType = "tokens";
            }

            String categoryName = tokenCategory.equals("priv") ? "привилегий" : "титулов";
            switch (action) {
               case "give":
                  this.plugin.getTokenManager().giveTokens(target, tokenType, amount, actualCategory);
                  if (tokenCategory.equals("priv")) {
                     sender.sendMessage(
                        HexUtil.color(
                           "&#1573FB▶ &fВыдано &e" + amount + " &fжетонов типа &e" + tokenType + " &fдля &e" + categoryName + " &fигроку &e" + playerName
                        )
                     );
                  } else {
                     sender.sendMessage(HexUtil.color("&#1573FB▶ &fВыдано &e" + amount + " &fжетонов для &e" + categoryName + " &fигроку &e" + playerName));
                  }
                  break;
               case "take":
                  this.plugin.getTokenManager().takeTokens(target, tokenType, amount, actualCategory);
                  if (tokenCategory.equals("priv")) {
                     sender.sendMessage(
                        HexUtil.color(
                           "&#1573FB▶ &fИзъято &e" + amount + " &fжетонов типа &e" + tokenType + " &fдля &e" + categoryName + " &fу игрока &e" + playerName
                        )
                     );
                  } else {
                     sender.sendMessage(HexUtil.color("&#1573FB▶ &fИзъято &e" + amount + " &fжетонов для &e" + categoryName + " &fу игрока &e" + playerName));
                  }
                  break;
               default:
                  sender.sendMessage(HexUtil.color("&#1573FB▶ &fИспользование: /fichacustompriv give/take ник priv/titul [30d/90d/infinity] [количество]"));
            }

            return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("FichaCustomPriv.admin")) {
         return completions;
      } else {
         if (args.length == 1) {
            completions.add("give");
            completions.add("take");
         } else {
            if (args.length == 2) {
               return Bukkit.getOnlinePlayers().stream().<String>map(OfflinePlayer::getName).collect(Collectors.toList());
            }

            if (args.length == 3) {
               completions.add("priv");
               completions.add("titul");
            } else if (args.length == 4) {
               String category = args[2].toLowerCase();
               if (category.equals("priv")) {
                  completions.add("30d");
                  completions.add("90d");
                  completions.add("infinity");
               } else if (category.equals("titul")) {
                  completions.add("1");
                  completions.add("5");
                  completions.add("10");
               }
            } else if (args.length == 5) {
               String category = args[2].toLowerCase();
               if (category.equals("priv")) {
                  completions.add("1");
                  completions.add("5");
                  completions.add("10");
               }
            }
         }

         String input = args[args.length - 1].toLowerCase();
         return completions.stream().filter(s -> s.toLowerCase().startsWith(input)).collect(Collectors.toList());
      }
   }
}
