package ru.extas7y.saveitems.command.args;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.ISubCommand;
import ru.extas7y.saveitems.database.MyDatabase;
import ru.extas7y.saveitems.object.SaveItemsObject;

public class Buy implements ISubCommand {
   private final SaveItems plugin;
   private final MyDatabase dbHandler;
   private final SaveItemsObject savedItemsInventory;

   public Buy(SaveItems plugin, MyDatabase dbHandler, SaveItemsObject savedItemsInventory) {
      this.plugin = plugin;
      this.dbHandler = dbHandler;
      this.savedItemsInventory = savedItemsInventory;
   }

   @Override
   public String Argument() {
      return "buy";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 4) {
         return true;
      } else {
         String targetPlayerName = args[1];
         String itemName = args[2];

         int amount;
         try {
            amount = Integer.parseInt(args[3]);
         } catch (NumberFormatException var12) {
            return true;
         }

         StringBuilder commandBuilder = new StringBuilder();

         for (int i = 4; i < args.length; i++) {
            commandBuilder.append(args[i]).append(" ");
         }

         String cmd = commandBuilder.toString().trim();
         Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
         if (targetPlayer == null) {
            return true;
         } else {
            ItemStack item = this.dbHandler.getItem(itemName);
            if (item == null) {
               return true;
            } else {
               item.setAmount(amount);
               double price = this.dbHandler.getPrice(itemName) * amount;
               this.savedItemsInventory.display(targetPlayer, item, itemName, price, cmd);
               return true;
            }
         }
      }
   }
}
