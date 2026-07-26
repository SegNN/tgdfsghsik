package ua.fichadev.fichacustompriv.cmd;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.functional.menu.PrivilegeAdminMenu;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class PrivilegeAdminCMD implements CommandExecutor, TabCompleter {
   private FichaCustomPriv plugin;

   public PrivilegeAdminCMD(FichaCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(HexUtil.color(this.plugin.getMessage("only-players")));
         return true;
      } else if (!player.hasPermission("FichaCustomPriv.admin")) {
         player.sendMessage(HexUtil.color(this.plugin.getMessage("no-permission")));
         return true;
      } else if (args.length == 0) {
         new PrivilegeAdminMenu(this.plugin, 1).open(player);
         return true;
      } else {
         String var6 = args[0].toLowerCase();
         switch (var6) {
            case "reload":
               this.plugin.reloadConfig();
               player.sendMessage(HexUtil.color("&#1573FB▶ &fКонфиг перезагружен"));
               break;
            case "stats":
               this.showStats(player);
               break;
            default:
               player.sendMessage(HexUtil.color("&#1573FB▶ &f/privadmin [reload/stats]"));
         }

         return true;
      }
   }

   private void showStats(Player player) {
      int pendingCount = this.plugin.getPrivilegeManager().getPendingPrivilegesCount();
      int approvedCount = this.plugin.getPrivilegeManager().getApprovedPrivilegesCount();
      player.sendMessage(HexUtil.color("&#1573FB▌ &fСтатистика"));
      player.sendMessage(HexUtil.color("&#1573FB▌ &7Ожидающих заявок: &f" + pendingCount));
      player.sendMessage(HexUtil.color("&#1573FB▌ &7Одобренных заявок: &f" + approvedCount));
      player.sendMessage(HexUtil.color("&#1573FB▌ &7Используйте &f/privadmin &7для управления"));
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1 && sender.hasPermission("FichaCustomPriv.admin")) {
         completions.add("reload");
         completions.add("stats");
      }

      return completions;
   }
}
