package ru.kotikstasika.kotiktotemrune.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.kotikstasika.kotiktotemrune.item.Item;

public class CommandGive implements CommandExecutor {
   Item itemrune = new Item();

   public CommandGive() {
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
      if (!sender.hasPermission("kotiktotemrune.admin")) {
         sender.sendMessage("бесправный еблан");
         return true;
      } else if (args.length == 0) {
         sender.sendMessage("/kotiktotemrune give <name> <rune> <amount>");
         return true;
      } else if (args.length < 4) {
         sender.sendMessage("/kotiktotemrune give <name> <rune> <amount>");
         return true;
      } else {
         if (args[0].equalsIgnoreCase("give")) {
            Player playergive = Bukkit.getPlayer(args[1]);
            if (playergive == null) {
               sender.sendMessage("Player is null");
               return true;
            }

            try {
               String runeType = args[2].toLowerCase();
               int amount = Integer.parseInt(args[3]);
               ItemStack rune = null;
               String runeName = "";
               if (!runeType.equals("bessmertie") && !runeType.equals("immortality")) {
                  if (!runeType.equals("vosstanovlenie") && !runeType.equals("recovery")) {
                     sender.sendMessage("неизвестная руна. доступные: bessmertie, vosstanovlenie");
                     return true;
                  }

                  rune = this.itemrune.giveRecoveryRune(playergive, amount);
                  runeName = "Восстановление";
               } else {
                  rune = this.itemrune.giveImmortalityRune(playergive, amount);
                  runeName = "Бессмертие";
               }

               playergive.getInventory().addItem(new ItemStack[]{rune});
               sender.sendMessage("Вы успешно выдали " + amount + " рун «" + runeName + "» для " + playergive.getName());
            } catch (NumberFormatException var10) {
               sender.sendMessage("ты чо ебанафт? тебе сказанно кол-во а ты написал какуе то хуету");
            }
         }

         return true;
      }
   }
}
