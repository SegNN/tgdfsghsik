package ua.holydev.holycustompriv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.holydev.holycustompriv.HolyCustomPriv;
import ua.holydev.holycustompriv.functional.menu.TitulAdmin;
import ua.holydev.holycustompriv.utils.HexUtil;

public class TitulAdminCMD implements CommandExecutor {
   private HolyCustomPriv plugin;

   public TitulAdminCMD(HolyCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(HexUtil.color(this.plugin.getMessage("only-players")));
         return true;
      } else if (!player.hasPermission("HolyCustomPriv.admin")) {
         player.sendMessage(HexUtil.color(this.plugin.getMessage("no-permission")));
         return true;
      } else {
         new TitulAdmin(this.plugin, 1).open(player);
         return true;
      }
   }
}
