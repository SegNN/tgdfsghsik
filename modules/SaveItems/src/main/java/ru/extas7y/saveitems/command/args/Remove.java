package ru.extas7y.saveitems.command.args;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.ISubCommand;

public class Remove implements ISubCommand {
   public Remove() {
   }

   @Override
   public String Argument() {
      return "remove";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         if (args.length < 2) {
            return true;
         } else {
            String itemName = args[1];
            SaveItems.getInstance().getDbHandler().removeItem(itemName);
            player.sendMessage("Предмет удален из базы данных.");
            return true;
         }
      } else {
         return false;
      }
   }
}
