package ru.extas7y.saveitems.command;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.args.Add;
import ru.extas7y.saveitems.command.args.Buy;
import ru.extas7y.saveitems.command.args.Give;
import ru.extas7y.saveitems.command.args.List;
import ru.extas7y.saveitems.command.args.Remove;
import ru.extas7y.saveitems.database.MyDatabase;
import ru.extas7y.saveitems.object.SaveItemsObject;

public class CommandHandler implements CommandExecutor {
   private final Map<String, ISubCommand> commands = new HashMap<>();
   private final SaveItems plugin;
   private final MyDatabase dbHandler;
   private final SaveItemsObject savedItemsInventory;

   public CommandHandler(SaveItems plugin, MyDatabase dbHandler, SaveItemsObject savedItemsInventory) {
      this.plugin = plugin;
      this.dbHandler = dbHandler;
      this.savedItemsInventory = savedItemsInventory;
      this.registerCommand(new Add());
      this.registerCommand(new Remove());
      this.registerCommand(new List());
      this.registerCommand(new Give());
      this.registerCommand(new Buy(plugin, dbHandler, savedItemsInventory));
   }

   private void registerCommand(ISubCommand command) {
      this.commands.put(command.Argument().toLowerCase(), command);
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      if (args.length == 0) {
         sender.sendMessage("Используйте подкоманду: add, remove, list, give, buy");
         return true;
      } else {
         ISubCommand subCommand = this.commands.get(args[0].toLowerCase());
         if (subCommand == null) {
            sender.sendMessage("Неизвестная подкоманда. Доступные: add, remove, list, give, buy");
            return true;
         } else {
            return subCommand.execute(sender, args);
         }
      }
   }
}
