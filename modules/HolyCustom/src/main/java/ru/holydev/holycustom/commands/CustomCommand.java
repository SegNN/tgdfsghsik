package ru.holydev.holycustom.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.holydev.holycustom.menus.MainMenu;

public class CustomCommand implements CommandExecutor {
   private final MainMenu menu;

   public CustomCommand(MainMenu menu) {
      this.menu = menu;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&F&F&B&0&0&0▶ &fКоманду могут использовать только игроки."));
         return true;
      } else {
         this.menu.open(player);
         return true;
      }
   }
}
