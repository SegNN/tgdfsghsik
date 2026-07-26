package ru.extas7y.saveitems.command.args;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.ISubCommand;

public class Add implements ISubCommand {
   public Add() {
   }

   @Override
   public String Argument() {
      return "add";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         if (args.length < 3) {
            return true;
         } else {
            String itemName = args[1];

            double price;
            try {
               price = Double.parseDouble(args[2]);
            } catch (NumberFormatException var8) {
               player.sendMessage("Цена должна быть числом!");
               return true;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null && !itemInHand.getType().isAir()) {
               SaveItems.getInstance().getDbHandler().addItem(itemName, itemInHand, price);
               player.sendMessage("Предмет добавлен в базу данных.");
               return true;
            } else {
               player.sendMessage("Вы должны держать предмет в руке.");
               return true;
            }
         }
      } else {
         return false;
      }
   }
}
