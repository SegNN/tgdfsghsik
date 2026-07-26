package ru.extas7y.saveitems.command.args;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.ISubCommand;

public class List implements ISubCommand {
   public List() {
   }

   @Override
   public String Argument() {
      return "list";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         String list = SaveItems.getInstance().getDbHandler().listItems();
         player.sendMessage("Список предметов:\n" + list);
         return true;
      } else {
         return false;
      }
   }
}
