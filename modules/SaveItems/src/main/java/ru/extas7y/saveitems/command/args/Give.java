package ru.extas7y.saveitems.command.args;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.extas7y.saveitems.SaveItems;
import ru.extas7y.saveitems.command.ISubCommand;

public class Give implements ISubCommand {
   public Give() {
   }

   @Override
   public String Argument() {
      return "give";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 4) {
         return true;
      } else {
         String targetName = args[1];
         String itemName = args[2];

         int amount;
         try {
            amount = Integer.parseInt(args[3]);
         } catch (NumberFormatException var8) {
            sender.sendMessage("Количество должно быть числом!");
            return true;
         }

         Player target = Bukkit.getPlayer(targetName);
         if (target == null) {
            sender.sendMessage("Игрок не найден.");
            return true;
         } else {
            ItemStack item = SaveItems.getInstance().getDbHandler().getItem(itemName);
            if (item == null) {
               sender.sendMessage("Предмет не найден.");
               return true;
            } else {
               item.setAmount(amount);
               SaveItems.getInstance().addItemInventory(target.getInventory(), item, target.getLocation());
               target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
               return true;
            }
         }
      }
   }
}
