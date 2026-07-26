package ua.holydev.holycustompriv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.holydev.holycustompriv.HolyCustomPriv;

public class TitulMainCMD implements CommandExecutor {
   private HolyCustomPriv plugin;

   public TitulMainCMD(HolyCustomPriv plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage("Эта команда только для игроков!");
         return true;
      } else {
         this.plugin.getTitulMenuManager().openMainMenu(player);
         return true;
      }
   }
}
