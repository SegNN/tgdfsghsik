package ua.fichadev.fichacustompriv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.functional.menu.TitulAdmin;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class TitulAdminCMD implements CommandExecutor {
   private FichaCustomPriv plugin;

   public TitulAdminCMD(FichaCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(HexUtil.color(this.plugin.getMessage("only-players")));
         return true;
      } else if (!player.hasPermission("FichaCustomPriv.admin")) {
         player.sendMessage(HexUtil.color(this.plugin.getMessage("no-permission")));
         return true;
      } else {
         new TitulAdmin(this.plugin, 1).open(player);
         return true;
      }
   }
}
