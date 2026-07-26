package ua.fichadev.fichacustompriv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.fichadev.fichacustompriv.FichaCustomPriv;

public class PrivilegeMainCMD implements CommandExecutor {
   private FichaCustomPriv plugin;

   public PrivilegeMainCMD(FichaCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("Эта команда только для игроков!");
         return true;
      } else {
         this.plugin.getPrivilegeMenuManager().openMainMenu(player);
         return true;
      }
   }
}
